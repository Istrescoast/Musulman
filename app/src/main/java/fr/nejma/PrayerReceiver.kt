package fr.nejma

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlin.math.abs

/** Reçoit l'alarme d'une prière, et les événements du téléphone qui obligent à reprogrammer. */
class PrayerReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Scheduler.ACTION) {
            val key = intent.getStringExtra(Scheduler.EXTRA_KEY)
            val time = intent.getLongExtra(Scheduler.EXTRA_TIME, 0)
            val s = AppSettings.load(ctx)
            // Pas de rappel en retard de plus de 30 minutes (téléphone éteint, par exemple).
            if (key != null && key in s.enabled && abs(System.currentTimeMillis() - time) < 30 * 60_000) {
                notify(ctx, key, s.city)
            }
        }
        Scheduler.reschedule(ctx)
    }

    private fun notify(ctx: Context, key: String, city: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Heures de prière", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Rappel à l'heure de chaque prière"
            }
        )
        val open = PendingIntent.getActivity(
            ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val name = PrayerTimes.NAMES[key] ?: key
        val n = android.app.Notification.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_nejma)
            .setContentTitle("C'est l'heure de $name")
            .setContentText(if (city.isNotBlank()) "Prière de $name · $city" else "Prière de $name")
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(android.app.Notification.CATEGORY_REMINDER)
            .build()
        nm.notify(PrayerTimes.KEYS.indexOf(key) + 1, n)
    }

    private companion object {
        const val CHANNEL = "prieres"
    }
}
