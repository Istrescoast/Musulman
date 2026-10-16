package fr.nejma

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.util.ULocale
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Widget de l'écran d'accueil : prochaine prière et date hégirienne. */
class NextPrayerWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, manager: AppWidgetManager, ids: IntArray) {
        draw(ctx, manager, ids)
    }

    companion object {
        fun refresh(ctx: Context) {
            val manager = AppWidgetManager.getInstance(ctx)
            val ids = manager.getAppWidgetIds(ComponentName(ctx, NextPrayerWidget::class.java))
            if (ids.isNotEmpty()) draw(ctx, manager, ids)
        }

        private fun draw(ctx: Context, manager: AppWidgetManager, ids: IntArray) {
            val s = AppSettings.load(ctx)
            val next = PrayerTimes.next(System.currentTimeMillis(), s.lat, s.lng)
            val hijri = runCatching {
                DateFormat.getDateInstance(DateFormat.LONG, ULocale("fr_FR@calendar=islamic-umalqura")).format(Date())
            }.getOrDefault("")
            val open = PendingIntent.getActivity(
                ctx, 1, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
            )
            val views = RemoteViews(ctx.packageName, R.layout.widget_next_prayer).apply {
                setTextViewText(R.id.w_label, if (s.city.isNotBlank()) "Prochaine prière · ${s.city}" else "Prochaine prière")
                setTextViewText(R.id.w_name, next?.let { PrayerTimes.NAMES[it.key] } ?: "–")
                setTextViewText(R.id.w_time, next?.let { SimpleDateFormat("HH:mm", Locale.FRANCE).format(Date(it.time)) } ?: "")
                setTextViewText(R.id.w_hijri, hijri)
                setOnClickPendingIntent(R.id.w_root, open)
            }
            manager.updateAppWidget(ids, views)
        }
    }
}
