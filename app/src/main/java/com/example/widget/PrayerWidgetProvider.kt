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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PrayerWidgetProvider : AppWidgetProvider() {

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
        const val ACTION_WIDGET_REFRESH = "com.example.ACTION_WIDGET_REFRESH"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, PrayerWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }

            // Also update small/compact widgets
            try {
                SmallPrayerWidgetProvider.updateAllWidgets(context)
            } catch (_: Exception) {}
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)

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

            // Dynamic background bitmap with gradient, stroke, and geometric motif
            try {
                val bgBitmap = WidgetThemeRenderer.generateWidgetBackgroundBitmap(
                    width = 760,
                    height = 340,
                    theme = effectiveTheme,
                    motif = effectiveMotif,
                    showMotif = showWatermark,
                    opacityPercent = opacity
                )
                views.setImageViewBitmap(R.id.widget_bg_image, bgBitmap)
            } catch (_: Exception) {}

            // Dynamic badges for Current and Next prayer cards
            try {
                // Current card: subtle primary tint background
                val currentSolid = applyAlphaToColor(colors.primary, 0.16f)
                val currentStroke = applyAlphaToColor(colors.primary, 0.85f)
                val currentBgBitmap = WidgetThemeRenderer.generateCardBadgeBitmap(
                    width = 340,
                    height = 150,
                    solidColor = currentSolid,
                    strokeColor = currentStroke,
                    cornerRadius = 22f
                )
                views.setImageViewBitmap(R.id.widget_current_bg, currentBgBitmap)

                // Next card: subtle accent tint background
                val nextSolid = applyAlphaToColor(colors.accent, 0.16f)
                val nextStroke = applyAlphaToColor(colors.accent, 0.85f)
                val nextBgBitmap = WidgetThemeRenderer.generateCardBadgeBitmap(
                    width = 340,
                    height = 150,
                    solidColor = nextSolid,
                    strokeColor = nextStroke,
                    cornerRadius = 22f
                )
                views.setImageViewBitmap(R.id.widget_next_bg, nextBgBitmap)

                // Timeline bar: subtle surface background
                val barSolid = applyAlphaToColor(colors.surface, 0.70f)
                val barStroke = applyAlphaToColor(colors.primary, 0.20f)
                val barBgBitmap = WidgetThemeRenderer.generateCardBadgeBitmap(
                    width = 700,
                    height = 70,
                    solidColor = barSolid,
                    strokeColor = barStroke,
                    cornerRadius = 14f
                )
                views.setImageViewBitmap(R.id.widget_times_bar_bg, barBgBitmap)
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
            val nextNameFormatted: String
            val currentNameFormatted: String
            val currentTimeFormatted: String

            if (currentItem != null) {
                currentNameFormatted = currentItem.type.titleTr.uppercase(Locale("tr", "TR"))
                currentTimeFormatted = currentItem.timeFormatted
            } else {
                currentNameFormatted = "YATSI (DÜN)"
                val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                val yTimes = PrayerCalculationEngine.calculateTimesForDate(
                    yesterdayCal.get(Calendar.YEAR),
                    yesterdayCal.get(Calendar.MONTH) + 1,
                    yesterdayCal.get(Calendar.DAY_OF_MONTH),
                    city
                )
                currentTimeFormatted = yTimes.find { it.type == PrayerType.YATSI }?.timeFormatted ?: "--:--"
            }

            if (nextItem != null) {
                nextTargetMillis = nextItem.timestampMillis
                nextNameFormatted = "${nextItem.type.titleTr.uppercase(Locale("tr", "TR"))} (${nextItem.timeFormatted})"
            } else {
                // Next is tomorrow's Fajr (İmsak)
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
                    nextNameFormatted = "İMSAK (${tomorrowFajr.timeFormatted})"
                } else {
                    nextNameFormatted = "İMSAK"
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

            // Date formatting
            val hijri = PrayerCalculationEngine.calculateHijriDate(year, month, day)
            val sdf = SimpleDateFormat("d MMMM", Locale("tr", "TR"))
            val gregorianShort = sdf.format(cal.time)
            val dateText = "$gregorianShort • ${hijri.format()}"

            // Bind values to RemoteViews with dynamic colors
            views.setTextViewText(R.id.widget_city_name, city.name)
            views.setTextColor(R.id.widget_city_name, colors.textPrimary)

            views.setTextViewText(R.id.widget_date_info, dateText)
            views.setTextColor(R.id.widget_date_info, colors.textSecondary)

            // Motif badge in header
            val motifBadgeText = when (effectiveMotif) {
                com.example.model.ThemeMotif.SELJUK_STAR -> "✦ Selçuklu Yıldızı"
                com.example.model.ThemeMotif.KISVE_GOLD -> "✦ Kâbe Bordürü"
                com.example.model.ThemeMotif.OTTOMAN_TULIP -> "✦ Saray Lâlesi"
                com.example.model.ThemeMotif.RAVZA_DOME -> "✦ Kubbe-i Hadrâ"
                com.example.model.ThemeMotif.ALHAMBRA_ARCH -> "✦ Elhamra Kemeri"
                com.example.model.ThemeMotif.ARABESQUE_SUN -> "✦ Güneş Şemse"
                com.example.model.ThemeMotif.MADINAH_ROSE -> "✦ Medine Gülü"
                com.example.model.ThemeMotif.QUDS_OCTAGON -> "✦ Kudüs Sekizgeni"
                com.example.model.ThemeMotif.TEZHIP_MARBLE -> "✦ Mermer Tezhip"
                com.example.model.ThemeMotif.TOPKAPI_RUMI -> "✦ Çintemani Rûmî"
            }
            views.setTextViewText(R.id.widget_motif_badge, motifBadgeText)
            views.setTextColor(R.id.widget_motif_badge, colors.primary)

            // Current prayer card texts
            views.setTextViewText(R.id.widget_current_label, "ŞU ANKİ VAKİT")
            views.setTextColor(R.id.widget_current_label, colors.primary)

            views.setTextViewText(R.id.widget_current_name, currentNameFormatted)
            views.setTextColor(R.id.widget_current_name, colors.textPrimary)

            views.setTextViewText(R.id.widget_current_time, currentTimeFormatted)
            views.setTextColor(R.id.widget_current_time, colors.textSecondary)

            // Next prayer card texts
            views.setTextViewText(R.id.widget_next_label, "SIRADAKİ VAKİT")
            views.setTextColor(R.id.widget_next_label, colors.accent)

            views.setTextViewText(R.id.widget_next_name, nextNameFormatted)
            views.setTextColor(R.id.widget_next_name, colors.textPrimary)

            views.setTextViewText(R.id.widget_countdown_text, countdownText)
            views.setTextColor(R.id.widget_countdown_text, colors.accent)

            // Mini times bar
            val imsak = times.find { it.type == PrayerType.IMSAK }?.timeFormatted ?: "--:--"
            val gunes = times.find { it.type == PrayerType.GUNES }?.timeFormatted ?: "--:--"
            val ogle = times.find { it.type == PrayerType.OGLE }?.timeFormatted ?: "--:--"
            val ikindi = times.find { it.type == PrayerType.IKINDI }?.timeFormatted ?: "--:--"
            val aksam = times.find { it.type == PrayerType.AKSAM }?.timeFormatted ?: "--:--"
            val yatsi = times.find { it.type == PrayerType.YATSI }?.timeFormatted ?: "--:--"

            views.setTextViewText(R.id.widget_time_imsak, "İms\n$imsak")
            views.setTextViewText(R.id.widget_time_gunes, "Güneş\n$gunes")
            views.setTextViewText(R.id.widget_time_ogle, "Öğle\n$ogle")
            views.setTextViewText(R.id.widget_time_ikindi, "İkindi\n$ikindi")
            views.setTextViewText(R.id.widget_time_aksam, "Akşam\n$aksam")
            views.setTextViewText(R.id.widget_time_yatsi, "Yatsı\n$yatsi")

            // Default & highlighted colors in bottom row
            val defaultColor = colors.textMuted
            val currentColor = colors.primary
            val nextColor = colors.accent

            views.setTextColor(R.id.widget_time_imsak, defaultColor)
            views.setTextColor(R.id.widget_time_gunes, defaultColor)
            views.setTextColor(R.id.widget_time_ogle, defaultColor)
            views.setTextColor(R.id.widget_time_ikindi, defaultColor)
            views.setTextColor(R.id.widget_time_aksam, defaultColor)
            views.setTextColor(R.id.widget_time_yatsi, defaultColor)

            // Apply highlight
            when (currentItem?.type) {
                PrayerType.IMSAK -> views.setTextColor(R.id.widget_time_imsak, currentColor)
                PrayerType.GUNES -> views.setTextColor(R.id.widget_time_gunes, currentColor)
                PrayerType.OGLE -> views.setTextColor(R.id.widget_time_ogle, currentColor)
                PrayerType.IKINDI -> views.setTextColor(R.id.widget_time_ikindi, currentColor)
                PrayerType.AKSAM -> views.setTextColor(R.id.widget_time_aksam, currentColor)
                PrayerType.YATSI -> views.setTextColor(R.id.widget_time_yatsi, currentColor)
                null -> {}
            }

            when (nextItem?.type) {
                PrayerType.IMSAK -> views.setTextColor(R.id.widget_time_imsak, nextColor)
                PrayerType.GUNES -> views.setTextColor(R.id.widget_time_gunes, nextColor)
                PrayerType.OGLE -> views.setTextColor(R.id.widget_time_ogle, nextColor)
                PrayerType.IKINDI -> views.setTextColor(R.id.widget_time_ikindi, nextColor)
                PrayerType.AKSAM -> views.setTextColor(R.id.widget_time_aksam, nextColor)
                PrayerType.YATSI -> views.setTextColor(R.id.widget_time_yatsi, nextColor)
                null -> {}
            }

            // Click Intent for whole widget: Open MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingLaunch = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingLaunch)

            // Click Intent for Refresh button
            val refreshIntent = Intent(context, PrayerWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            val pendingRefresh = PendingIntent.getBroadcast(
                context,
                1,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_button_refresh, pendingRefresh)

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
