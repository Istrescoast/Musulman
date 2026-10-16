package fr.nejma

import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.JavascriptInterface
import java.util.Locale

/** Fonctions du téléphone appelées par l'interface (window.Android en JavaScript). */
class Bridge(private val activity: MainActivity) {

    /** Position et rappels choisis dans l'interface ; reprogramme l'alarme de la prochaine prière. */
    @JavascriptInterface
    fun setSettings(json: String) {
        AppSettings.save(activity, json)
        Scheduler.reschedule(activity)
    }

    @JavascriptInterface
    fun canExactAlarms(): Boolean = Scheduler.canExact(activity)

    /** Ouvre l'écran Android qui autorise les alarmes à l'heure exacte. */
    @JavascriptInterface
    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        activity.runOnUiThread {
            runCatching {
                activity.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + activity.packageName))
                )
            }
        }
    }

    /** Nom de la ville pour une position ; vide si le téléphone ne peut pas le trouver (hors ligne). */
    @JavascriptInterface
    fun cityName(lat: Double, lng: Double): String = runCatching {
        @Suppress("DEPRECATION")
        val a = Geocoder(activity, Locale.FRANCE).getFromLocation(lat, lng, 1)?.firstOrNull()
        a?.locality ?: a?.subAdminArea ?: ""
    }.getOrDefault("")
}
