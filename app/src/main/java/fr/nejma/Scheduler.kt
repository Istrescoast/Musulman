package fr.nejma

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Programme une seule alarme : la prochaine prière. Quand elle sonne, [PrayerReceiver] affiche
 * le rappel (si la prière est activée), met le widget à jour et programme la suivante.
 */
object Scheduler {
    const val ACTION = "fr.nejma.PRAYER"
    const val EXTRA_KEY = "key"
    const val EXTRA_TIME = "time"

    fun canExact(ctx: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ctx.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun reschedule(ctx: Context) {
        val s = AppSettings.load(ctx)
        val am = ctx.getSystemService(AlarmManager::class.java)
        // Une petite marge évite de reprogrammer la prière qui vient juste de sonner.
        val next = PrayerTimes.next(System.currentTimeMillis() + 30_000, s.lat, s.lng)
        NextPrayerWidget.refresh(ctx)
        if (next == null) return
        val intent = Intent(ctx, PrayerReceiver::class.java)
            .setAction(ACTION)
            .putExtra(EXTRA_KEY, next.key)
            .putExtra(EXTRA_TIME, next.time)
        val pi = PendingIntent.getBroadcast(
            ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (canExact(ctx)) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.time, pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.time, pi)
    }
}
