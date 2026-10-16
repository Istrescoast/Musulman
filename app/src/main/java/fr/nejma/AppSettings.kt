package fr.nejma

import android.content.Context
import org.json.JSONObject

/** Réglages envoyés par l'interface : position et prières pour lesquelles un rappel est voulu. */
data class AppSettings(val lat: Double, val lng: Double, val city: String, val enabled: Set<String>) {
    companion object {
        private const val PREFS = "nejma"
        private const val KEY = "settings"

        // Avant le premier lancement de l'interface : Paris, toutes les prières.
        private val DEFAULT = AppSettings(48.8566, 2.3522, "Paris", PrayerTimes.KEYS.toSet())

        fun load(ctx: Context): AppSettings {
            val json = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return DEFAULT
            return runCatching {
                val o = JSONObject(json)
                val notif = o.optJSONObject("notif")
                AppSettings(
                    o.getDouble("lat"),
                    o.getDouble("lng"),
                    o.optString("city", ""),
                    PrayerTimes.KEYS.filter { notif?.optBoolean(it, true) ?: true }.toSet()
                )
            }.getOrDefault(DEFAULT)
        }

        fun save(ctx: Context, json: String) {
            ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, json).apply()
        }
    }
}
