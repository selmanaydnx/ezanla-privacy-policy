package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.calc.PrayerCalculationEngine
import com.example.data.AppPreferencesRepository
import com.example.data.CityDatabase
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import java.util.Calendar
import java.util.Locale

class SmallPrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_REFRESH) {
            updateAllWidgets(context)
        }
    }

    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.ACTION_SMALL_WIDGET_REFRESH"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, SmallPrayerWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_small_prayer_times)

            val prefsRepo = AppPreferencesRepository(context)
            val savedCityId = prefsRepo.getSavedCityId("34")
            val city = CityDatabase.cities.find { it.id == savedCityId } ?: CityDatabase.defaultCity
            val precaution = prefsRepo.getPrecautionMinutes()

            // Resolve theme, motif, and visual styling
            val currentAppTheme = prefsRepo.getSavedTheme()
            val widgetThemeOption = prefsRepo.getWidgetTheme()
            val widgetMotifOption = prefsRepo.getWidgetMotif()
            val opacity = prefsRepo.getWidgetBackgroundOpacity()
            val showWatermark = prefsRepo.isWidgetMotifWatermarkEnabled()

            val effectiveTheme = WidgetThemeRenderer.resolveWidgetTheme(widgetThemeOption, currentAppTheme)
            val effectiveMotif = WidgetThemeRenderer.resolveWidgetMotif(widgetMotifOption, effectiveTheme)
            val colors = WidgetThemeRenderer.getWidgetColors(effectiveTheme)

            // Dynamic background bitmap
            try {
                val bgBitmap = WidgetThemeRenderer.generateWidgetBackgroundBitmap(
                    width = 400,
                    height = 360,
                    theme = effectiveTheme,
                    motif = effectiveMotif,
                    showMotif = showWatermark,
                    opacityPercent = opacity
                )
                views.setImageViewBitmap(R.id.widget_small_bg_image, bgBitmap)
            } catch (_: Exception) {}

            // Dynamic badge for Next prayer card
            try {
                val nextSolid = applyAlphaToColor(colors.accent, 0.16f)
                val nextStroke = applyAlphaToColor(colors.accent, 0.85f)
                val nextBgBitmap = WidgetThemeRenderer.generateCardBadgeBitmap(
                    width = 340,
                    height = 140,
                    solidColor = nextSolid,
                    strokeColor = nextStroke,
                    cornerRadius = 18f
                )
                views.setImageViewBitmap(R.id.widget_small_next_bg, nextBgBitmap)
            } catch (_: Exception) {}

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)

            // Calculate raw times
            val rawTimes = PrayerCalculationEngine.calculateTimesForDate(year, month, day, city)
            val times = if (precaution == 0) {
                rawTimes
            } else {
                rawTimes.map { item ->
                    val adjustedMillis = item.timestampMillis + (precaution * 60 * 1000L)
                    val c = Calendar.getInstance().apply { timeInMillis = adjustedMillis }
                    val formatted = String.format(Locale.getDefault(), "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
                    item.copy(timeFormatted = formatted, timestampMillis = adjustedMillis)
                }
            }

            // Calculate current and next prayer
            var currentItem: PrayerTimeItem? = null
            var nextItem: PrayerTimeItem? = null

            for (i in times.indices) {
                if (now >= times[i].timestampMillis) {
                    currentItem = times[i]
                } else {
                    if (nextItem == null) {
                        nextItem = times[i]
                    }
                }
            }

            // Target millis for countdown
            var nextTargetMillis: Long = 0L
            val nextNameTimeFormatted: String
            val currentInfoFormatted: String

            if (currentItem != null) {
                currentInfoFormatted = "Şu an: ${currentItem.type.titleTr} (${currentItem.timeFormatted})"
            } else {
                val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                val yTimes = PrayerCalculationEngine.calculateTimesForDate(
                    yesterdayCal.get(Calendar.YEAR),
                    yesterdayCal.get(Calendar.MONTH) + 1,
                    yesterdayCal.get(Calendar.DAY_OF_MONTH),
                    city
                )
                val yatsi = yTimes.find { it.type == PrayerType.YATSI }?.timeFormatted ?: "--:--"
                currentInfoFormatted = "Şu an: Yatsı ($yatsi)"
            }

            if (nextItem != null) {
                nextTargetMillis = nextItem.timestampMillis
                nextNameTimeFormatted = "${nextItem.type.titleTr.uppercase(Locale("tr", "TR"))} ${nextItem.timeFormatted}"
            } else {
                val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                val tTimes = PrayerCalculationEngine.calculateTimesForDate(
                    tomorrowCal.get(Calendar.YEAR),
                    tomorrowCal.get(Calendar.MONTH) + 1,
                    tomorrowCal.get(Calendar.DAY_OF_MONTH),
                    city
                )
                val tomorrowFajr = tTimes.find { it.type == PrayerType.IMSAK }
                if (tomorrowFajr != null) {
                    nextTargetMillis = tomorrowFajr.timestampMillis + (precaution * 60 * 1000L)
                    nextNameTimeFormatted = "İMSAK ${tomorrowFajr.timeFormatted}"
                } else {
                    nextNameTimeFormatted = "İMSAK"
                }
            }

            // Countdown calculation
            val diffMillis = nextTargetMillis - now
            val countdownText = if (diffMillis > 0) {
                val hours = diffMillis / (1000 * 60 * 60)
                val minutes = (diffMillis / (1000 * 60)) % 60
                if (hours > 0) {
                    String.format(Locale.getDefault(), "%02d sa %02d dk kaldı", hours, minutes)
                } else {
                    String.format(Locale.getDefault(), "%d dk kaldı", minutes)
                }
            } else {
                "Vakit Girdi"
            }

            // Motif badge in header/bottom
            val motifBadgeText = when (effectiveMotif) {
                com.example.model.ThemeMotif.SELJUK_STAR -> "✦ Selçuklu"
                com.example.model.ThemeMotif.KISVE_GOLD -> "✦ Kâbe"
                com.example.model.ThemeMotif.OTTOMAN_TULIP -> "✦ Lâle"
                com.example.model.ThemeMotif.RAVZA_DOME -> "✦ Ravza"
                com.example.model.ThemeMotif.ALHAMBRA_ARCH -> "✦ Elhamra"
                com.example.model.ThemeMotif.ARABESQUE_SUN -> "✦ Şemse"
                com.example.model.ThemeMotif.MADINAH_ROSE -> "✦ Medine"
                com.example.model.ThemeMotif.QUDS_OCTAGON -> "✦ Kudüs"
                com.example.model.ThemeMotif.TEZHIP_MARBLE -> "✦ Tezhip"
                com.example.model.ThemeMotif.TOPKAPI_RUMI -> "✦ Rûmî"
            }

            // Bind values to RemoteViews
            views.setTextViewText(R.id.widget_small_city_name, city.name)
            views.setTextColor(R.id.widget_small_city_name, colors.textPrimary)

            views.setTextViewText(R.id.widget_small_next_label, "SIRADAKİ VAKİT")
            views.setTextColor(R.id.widget_small_next_label, colors.accent)

            views.setTextViewText(R.id.widget_small_next_name_time, nextNameTimeFormatted)
            views.setTextColor(R.id.widget_small_next_name_time, colors.textPrimary)

            views.setTextViewText(R.id.widget_small_countdown_text, countdownText)
            views.setTextColor(R.id.widget_small_countdown_text, colors.accent)

            views.setTextViewText(R.id.widget_small_current_info, currentInfoFormatted)
            views.setTextColor(R.id.widget_small_current_info, colors.textSecondary)

            views.setTextViewText(R.id.widget_small_motif_badge, motifBadgeText)
            views.setTextColor(R.id.widget_small_motif_badge, colors.primary)

            // Click Intent for whole widget: Open MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingLaunch = PendingIntent.getActivity(
                context,
                200,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_small_root, pendingLaunch)

            // Click Intent for Refresh button
            val refreshIntent = Intent(context, SmallPrayerWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            val pendingRefresh = PendingIntent.getBroadcast(
                context,
                201,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_small_button_refresh, pendingRefresh)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun applyAlphaToColor(color: Int, alphaFactor: Float): Int {
            val a = (255 * alphaFactor.coerceIn(0f, 1f)).toInt()
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            return Color.argb(a, r, g, b)
        }
    }
}
