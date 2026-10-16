package fr.nejma

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

/**
 * Horaires de prière, même calcul que l'interface (web/app.html) :
 * méthode UOIF (Fajr et Isha à 12°), Asr selon l'école shafi'ite (ombre = 1).
 */
object PrayerTimes {
    val KEYS = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    val NAMES = mapOf("fajr" to "Fajr", "dhuhr" to "Dhuhr", "asr" to "Asr", "maghrib" to "Maghrib", "isha" to "Isha")

    data class Next(val key: String, val time: Long)

    private fun rad(d: Double) = d * PI / 180
    private fun deg(r: Double) = r * 180 / PI
    private fun fixA(a: Double) = ((a % 360) + 360) % 360
    private fun fixH(h: Double) = ((h % 24) + 24) % 24

    /** Déclinaison du soleil et équation du temps. */
    private fun sun(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixA(357.529 + 0.98560028 * d)
        val q = fixA(280.459 + 0.98564736 * d)
        val l = fixA(q + 1.915 * sin(rad(g)) + 0.020 * sin(rad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = deg(atan2(cos(rad(e)) * sin(rad(l)), cos(rad(l)))) / 15
        return deg(asin(sin(rad(e)) * sin(rad(l)))) to (q / 15 - fixH(ra))
    }

    /** Heures des 5 prières (en millisecondes) pour le jour de [day], dans le fuseau du téléphone. */
    fun forDay(day: Calendar, lat: Double, lng: Double): Map<String, Long> {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH))
        }
        val jd = utc.timeInMillis / 86400000.0 + 2440587.5 - lng / 360
        val midnight = (day.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tz = day.timeZone.getOffset(midnight + 12 * 3600000L) / 3600000.0

        fun noon(t: Double) = fixH(12 - sun(jd + t).second)
        fun angleTime(a: Double, t: Double, ccw: Boolean): Double {
            val decl = sun(jd + t).first
            val x = deg(acos((-sin(rad(a)) - sin(rad(decl)) * sin(rad(lat))) / (cos(rad(decl)) * cos(rad(lat))))) / 15
            return noon(t) + if (ccw) -x else x
        }
        fun asr(t: Double): Double {
            val decl = sun(jd + t).first
            return angleTime(-deg(atan(1 / (1 + tan(rad(abs(lat - decl)))))), t, false)
        }

        val raw = mapOf(
            "fajr" to angleTime(12.0, 5 / 24.0, true),
            "dhuhr" to noon(12 / 24.0) + 1 / 60.0,
            "asr" to asr(13 / 24.0),
            "maghrib" to angleTime(0.833, 18 / 24.0, false),
            "isha" to angleTime(12.0, 18 / 24.0, false)
        )
        // Aux latitudes extrêmes certaines heures n'existent pas (NaN) : elles sont ignorées.
        return raw.filterValues { !it.isNaN() }
            .mapValues { (_, h) -> midnight + ((h + tz - lng / 15) * 60).roundToLong() * 60000L }
    }

    /** Prochaine prière après [now], sur aujourd'hui et les deux jours suivants. */
    fun next(now: Long, lat: Double, lng: Double): Next? {
        val day = Calendar.getInstance().apply { timeInMillis = now }
        repeat(3) {
            val times = forDay(day, lat, lng)
            KEYS.mapNotNull { k -> times[k]?.let { Next(k, it) } }
                .filter { it.time > now }
                .minByOrNull { it.time }
                ?.let { return it }
            day.add(Calendar.DAY_OF_MONTH, 1)
        }
        return null
    }
}
