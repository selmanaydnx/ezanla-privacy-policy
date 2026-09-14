package com.example.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.example.model.AppTheme
import com.example.model.ThemeMotif
import kotlin.math.cos
import kotlin.math.sin

data class WidgetColors(
    val bgStart: Int,
    val bgEnd: Int,
    val surface: Int,
    val primary: Int,
    val secondary: Int,
    val accent: Int,
    val textPrimary: Int,
    val textSecondary: Int,
    val textMuted: Int,
    val isDark: Boolean
)

object WidgetThemeRenderer {

    fun resolveWidgetTheme(themeOption: String, currentAppTheme: AppTheme): AppTheme {
        if (themeOption == "FOLLOW_APP") return currentAppTheme
        return try {
            AppTheme.valueOf(themeOption)
        } catch (_: Exception) {
            currentAppTheme
        }
    }

    fun resolveWidgetMotif(motifOption: String, selectedTheme: AppTheme): ThemeMotif {
        if (motifOption == "FOLLOW_THEME") return selectedTheme.motif
        return try {
            ThemeMotif.valueOf(motifOption)
        } catch (_: Exception) {
            selectedTheme.motif
        }
    }

    fun getWidgetColors(theme: AppTheme): WidgetColors {
        return when (theme) {
            AppTheme.MIDNIGHT -> WidgetColors(
                bgStart = Color.parseColor("#151D2A"),
                bgEnd = Color.parseColor("#090D16"),
                surface = Color.parseColor("#162032"),
                primary = Color.parseColor("#E5B842"),
                secondary = Color.parseColor("#F9D342"),
                accent = Color.parseColor("#10B981"),
                textPrimary = Color.parseColor("#F8FAFC"),
                textSecondary = Color.parseColor("#94A3B8"),
                textMuted = Color.parseColor("#64748B"),
                isDark = true
            )
            AppTheme.EMERALD -> WidgetColors(
                bgStart = Color.parseColor("#0F3124"),
                bgEnd = Color.parseColor("#061A12"),
                surface = Color.parseColor("#143B2C"),
                primary = Color.parseColor("#10B981"),
                secondary = Color.parseColor("#34D399"),
                accent = Color.parseColor("#FBBF24"),
                textPrimary = Color.parseColor("#F0FDF4"),
                textSecondary = Color.parseColor("#A7F3D0"),
                textMuted = Color.parseColor("#6EE7B7"),
                isDark = true
            )
            AppTheme.AMBER -> WidgetColors(
                bgStart = Color.parseColor("#2C1D10"),
                bgEnd = Color.parseColor("#191008"),
                surface = Color.parseColor("#3A2816"),
                primary = Color.parseColor("#F59E0B"),
                secondary = Color.parseColor("#FBBF24"),
                accent = Color.parseColor("#FB923C"),
                textPrimary = Color.parseColor("#FFFBEB"),
                textSecondary = Color.parseColor("#FDE68A"),
                textMuted = Color.parseColor("#D97706"),
                isDark = true
            )
            AppTheme.DAWN_LIGHT -> WidgetColors(
                bgStart = Color.parseColor("#FFFFFF"),
                bgEnd = Color.parseColor("#F1F5F9"),
                surface = Color.parseColor("#E2E8F0"),
                primary = Color.parseColor("#B45309"),
                secondary = Color.parseColor("#D97706"),
                accent = Color.parseColor("#059669"),
                textPrimary = Color.parseColor("#0F172A"),
                textSecondary = Color.parseColor("#475569"),
                textMuted = Color.parseColor("#64748B"),
                isDark = false
            )
            AppTheme.TURQUOISE_SELJUK -> WidgetColors(
                bgStart = Color.parseColor("#0C283F"),
                bgEnd = Color.parseColor("#061826"),
                surface = Color.parseColor("#133654"),
                primary = Color.parseColor("#06B6D4"),
                secondary = Color.parseColor("#38BDF8"),
                accent = Color.parseColor("#34D399"),
                textPrimary = Color.parseColor("#F0FDF4"),
                textSecondary = Color.parseColor("#BAE6FD"),
                textMuted = Color.parseColor("#7DD3FC"),
                isDark = true
            )
            AppTheme.RUBY_OTTOMAN -> WidgetColors(
                bgStart = Color.parseColor("#2D0D1D"),
                bgEnd = Color.parseColor("#1C0810"),
                surface = Color.parseColor("#3F172A"),
                primary = Color.parseColor("#E11D48"),
                secondary = Color.parseColor("#FB7185"),
                accent = Color.parseColor("#FBBF24"),
                textPrimary = Color.parseColor("#FFF1F2"),
                textSecondary = Color.parseColor("#FECDD3"),
                textMuted = Color.parseColor("#FDA4AF"),
                isDark = true
            )
            AppTheme.INDIGO_ANDALUSIA -> WidgetColors(
                bgStart = Color.parseColor("#141940"),
                bgEnd = Color.parseColor("#090B24"),
                surface = Color.parseColor("#1B2054"),
                primary = Color.parseColor("#818CF8"),
                secondary = Color.parseColor("#A5B4FC"),
                accent = Color.parseColor("#FCD34D"),
                textPrimary = Color.parseColor("#EEF2FF"),
                textSecondary = Color.parseColor("#C7D2FE"),
                textMuted = Color.parseColor("#818CF8"),
                isDark = true
            )
            AppTheme.ROSE_MADINAH -> WidgetColors(
                bgStart = Color.parseColor("#2B1226"),
                bgEnd = Color.parseColor("#180A15"),
                surface = Color.parseColor("#3A1B33"),
                primary = Color.parseColor("#EC4899"),
                secondary = Color.parseColor("#F472B6"),
                accent = Color.parseColor("#FDE047"),
                textPrimary = Color.parseColor("#FDF2F8"),
                textSecondary = Color.parseColor("#FBCFE8"),
                textMuted = Color.parseColor("#F472B6"),
                isDark = true
            )
            AppTheme.OLIVE_QUDS -> WidgetColors(
                bgStart = Color.parseColor("#1E2813"),
                bgEnd = Color.parseColor("#101609"),
                surface = Color.parseColor("#2B3A1C"),
                primary = Color.parseColor("#84CC16"),
                secondary = Color.parseColor("#A3E635"),
                accent = Color.parseColor("#EAB308"),
                textPrimary = Color.parseColor("#F7FEE7"),
                textSecondary = Color.parseColor("#D9F99D"),
                textMuted = Color.parseColor("#BEF264"),
                isDark = true
            )
            AppTheme.SAPPHIRE_TOPKAPI -> WidgetColors(
                bgStart = Color.parseColor("#0F284B"),
                bgEnd = Color.parseColor("#061324"),
                surface = Color.parseColor("#16325B"),
                primary = Color.parseColor("#3B82F6"),
                secondary = Color.parseColor("#60A5FA"),
                accent = Color.parseColor("#10B981"),
                textPrimary = Color.parseColor("#EFF6FF"),
                textSecondary = Color.parseColor("#BFDBFE"),
                textMuted = Color.parseColor("#93C5FD"),
                isDark = true
            )
        }
    }

    /**
     * Widget arka planı için yüksek kaliteli, kenarları yuvarlatılmış ve
     * seçilen İslami geometrik motifi filigran olarak içeren Bitmap üretir.
     */
    fun generateWidgetBackgroundBitmap(
        width: Int,
        height: Int,
        theme: AppTheme,
        motif: ThemeMotif,
        showMotif: Boolean,
        opacityPercent: Int
    ): Bitmap {
        val w = if (width > 0) width else 760
        val h = if (height > 0) height else 340
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors = getWidgetColors(theme)
        val alphaMultiplier = (opacityPercent.coerceIn(40, 100)) / 100f

        val startColorWithAlpha = applyAlpha(colors.bgStart, alphaMultiplier)
        val endColorWithAlpha = applyAlpha(colors.bgEnd, alphaMultiplier)

        val cornerRadius = 38f
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())

        // 1. Arka plan Gradyanı
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                startColorWithAlpha, endColorWithAlpha,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)

        // 2. Motif Filigranı (Arka planda hafifçe parlayan zarif İslami motif)
        if (showMotif) {
            val motifPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.primary
                alpha = if (colors.isDark) 35 else 25
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
            }
            val accentMotifPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.accent
                alpha = if (colors.isDark) 28 else 20
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }

            // Motifi sağ tarafa, kartın içine taşmayacak şekilde konumlandır
            val motifSize = (h * 0.95f)
            val cx = w - motifSize * 0.58f
            val cy = h * 0.55f
            drawMotifOnCanvas(canvas, motif, cx, cy, motifSize / 2f, motifPaint, accentMotifPaint)
        }

        // 3. Zarif Dış Çerçeve Kenarlığı (Border Stroke)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                applyAlpha(colors.primary, 0.45f * alphaMultiplier),
                applyAlpha(colors.secondary, 0.15f * alphaMultiplier),
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val borderRect = RectF(1.5f, 1.5f, w.toFloat() - 1.5f, h.toFloat() - 1.5f)
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)

        return bitmap
    }

    /**
     * Vakit kartları için iç gölgeli veya renkli kutucuk Bitmap'i
     */
    fun generateCardBadgeBitmap(
        width: Int,
        height: Int,
        solidColor: Int,
        strokeColor: Int,
        cornerRadius: Float = 22f
    ): Bitmap {
        val w = if (width > 0) width else 320
        val h = if (height > 0) height else 160
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = solidColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val strokeRect = RectF(1f, 1f, w.toFloat() - 1f, h.toFloat() - 1f)
        canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint)

        return bitmap
    }

    /**
     * Motif rozeti veya mini simgesi için vektörel ikon çizer
     */
    fun generateMotifIconBitmap(
        motif: ThemeMotif,
        primaryColor: Int,
        accentColor: Int,
        sizePx: Int = 72
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryColor
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val r = (sizePx / 2f) * 0.82f

        drawMotifOnCanvas(canvas, motif, cx, cy, r, paint, accentPaint)
        return bitmap
    }

    private fun drawMotifOnCanvas(
        canvas: Canvas,
        motif: ThemeMotif,
        cx: Float,
        cy: Float,
        radius: Float,
        primaryPaint: Paint,
        accentPaint: Paint
    ) {
        when (motif) {
            ThemeMotif.SELJUK_STAR -> {
                // Selçuklu 8 Köşeli Yıldızı: İki kare üst üste (biri 45 derece dönük)
                val half = radius * 0.72f
                val r1 = RectF(cx - half, cy - half, cx + half, cy + half)
                canvas.drawRect(r1, primaryPaint)

                canvas.save()
                canvas.rotate(45f, cx, cy)
                canvas.drawRect(r1, accentPaint)
                canvas.restore()

                canvas.drawCircle(cx, cy, radius * 0.32f, primaryPaint)
                val fillPaint = Paint(primaryPaint).apply { style = Paint.Style.FILL }
                canvas.drawCircle(cx, cy, radius * 0.12f, fillPaint)
            }

            ThemeMotif.KISVE_GOLD -> {
                // Kâbe Kisve-i Şerîf Baklava ve Altın Bordür Kuşağı
                val diamond = Path().apply {
                    moveTo(cx, cy - radius)
                    lineTo(cx + radius * 0.85f, cy)
                    lineTo(cx, cy + radius)
                    lineTo(cx - radius * 0.85f, cy)
                    close()
                }
                canvas.drawPath(diamond, primaryPaint)

                val innerDiamond = Path().apply {
                    moveTo(cx, cy - radius * 0.55f)
                    lineTo(cx + radius * 0.45f, cy)
                    lineTo(cx, cy + radius * 0.55f)
                    lineTo(cx - radius * 0.45f, cy)
                    close()
                }
                canvas.drawPath(innerDiamond, accentPaint)
                canvas.drawLine(cx - radius, cy, cx + radius, cy, primaryPaint)
                canvas.drawLine(cx, cy - radius, cx, cy + radius, accentPaint)
            }

            ThemeMotif.OTTOMAN_TULIP -> {
                // Klasik Osmanlı Saray Lâlesi
                val tulip = Path().apply {
                    moveTo(cx, cy + radius * 0.8f)
                    cubicTo(cx - radius * 0.7f, cy + radius * 0.4f, cx - radius * 0.8f, cy - radius * 0.4f, cx - radius * 0.3f, cy - radius)
                    cubicTo(cx - radius * 0.1f, cy - radius * 0.4f, cx, cy - radius * 0.2f, cx, cy + radius * 0.2f)
                    cubicTo(cx, cy - radius * 0.2f, cx + radius * 0.1f, cy - radius * 0.4f, cx + radius * 0.3f, cy - radius)
                    cubicTo(cx + radius * 0.8f, cy - radius * 0.4f, cx + radius * 0.7f, cy + radius * 0.4f, cx, cy + radius * 0.8f)
                    close()
                }
                canvas.drawPath(tulip, primaryPaint)

                // Lale içi damar ve rûmî kıvrımı
                canvas.drawLine(cx, cy - radius * 0.3f, cx, cy + radius * 0.7f, accentPaint)
                canvas.drawCircle(cx, cy - radius * 0.15f, radius * 0.12f, accentPaint)
            }

            ThemeMotif.RAVZA_DOME -> {
                // Mescid-i Nebevî Kubbe-i Hadrâ kemeri ve hilali
                val dome = Path().apply {
                    moveTo(cx - radius * 0.75f, cy + radius * 0.65f)
                    lineTo(cx + radius * 0.75f, cy + radius * 0.65f)
                    lineTo(cx + radius * 0.75f, cy + radius * 0.2f)
                    cubicTo(cx + radius * 0.75f, cy - radius * 0.45f, cx + radius * 0.25f, cy - radius * 0.85f, cx, cy - radius)
                    cubicTo(cx - radius * 0.25f, cy - radius * 0.85f, cx - radius * 0.75f, cy - radius * 0.45f, cx - radius * 0.75f, cy + radius * 0.2f)
                    close()
                }
                canvas.drawPath(dome, primaryPaint)
                // Kubbe alem hilali
                canvas.drawCircle(cx, cy - radius * 1.15f, radius * 0.12f, accentPaint)
            }

            ThemeMotif.ALHAMBRA_ARCH -> {
                // Endülüs Elhamra At Nalı Kemeri
                val arch = Path().apply {
                    moveTo(cx - radius * 0.7f, cy + radius * 0.8f)
                    lineTo(cx - radius * 0.7f, cy)
                    cubicTo(cx - radius * 0.85f, cy - radius * 0.5f, cx - radius * 0.3f, cy - radius, cx, cy - radius)
                    cubicTo(cx + radius * 0.3f, cy - radius, cx + radius * 0.85f, cy - radius * 0.5f, cx + radius * 0.7f, cy)
                    lineTo(cx + radius * 0.7f, cy + radius * 0.8f)
                }
                canvas.drawPath(arch, primaryPaint)

                // İç kemer
                val innerArch = Path().apply {
                    moveTo(cx - radius * 0.45f, cy + radius * 0.8f)
                    lineTo(cx - radius * 0.45f, cy)
                    cubicTo(cx - radius * 0.55f, cy - radius * 0.35f, cx - radius * 0.2f, cy - radius * 0.7f, cx, cy - radius * 0.7f)
                    cubicTo(cx + radius * 0.2f, cy - radius * 0.7f, cx + radius * 0.55f, cy - radius * 0.35f, cx + radius * 0.45f, cy)
                    lineTo(cx + radius * 0.45f, cy + radius * 0.8f)
                }
                canvas.drawPath(innerArch, accentPaint)
            }

            ThemeMotif.ARABESQUE_SUN -> {
                // 12 Işınlı Geometrik Güneş Şemse
                canvas.drawCircle(cx, cy, radius * 0.35f, primaryPaint)
                canvas.drawCircle(cx, cy, radius * 0.75f, accentPaint)
                for (i in 0 until 12) {
                    val angle = Math.toRadians((i * 30).toDouble())
                    val x1 = (cx + cos(angle) * (radius * 0.35f)).toFloat()
                    val y1 = (cy + sin(angle) * (radius * 0.35f)).toFloat()
                    val x2 = (cx + cos(angle) * radius).toFloat()
                    val y2 = (cy + sin(angle) * radius).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, if (i % 2 == 0) primaryPaint else accentPaint)
                }
            }

            ThemeMotif.MADINAH_ROSE -> {
                // 8 Yapraklı Medine Peygamber Gülü Rozeti
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val px = (cx + cos(angle) * (radius * 0.55f)).toFloat()
                    val py = (cy + sin(angle) * (radius * 0.55f)).toFloat()
                    canvas.drawCircle(px, py, radius * 0.35f, if (i % 2 == 0) primaryPaint else accentPaint)
                }
                canvas.drawCircle(cx, cy, radius * 0.25f, primaryPaint)
            }

            ThemeMotif.QUDS_OCTAGON -> {
                // Kubbet-üs Sahrâ Sekizgeni
                val oct1 = Path()
                val oct2 = Path()
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45 - 22.5).toDouble())
                    val x = (cx + cos(angle) * radius).toFloat()
                    val y = (cy + sin(angle) * radius).toFloat()
                    if (i == 0) oct1.moveTo(x, y) else oct1.lineTo(x, y)

                    val ix = (cx + cos(angle) * (radius * 0.6f)).toFloat()
                    val iy = (cy + sin(angle) * (radius * 0.6f)).toFloat()
                    if (i == 0) oct2.moveTo(ix, iy) else oct2.lineTo(ix, iy)
                }
                oct1.close()
                oct2.close()
                canvas.drawPath(oct1, primaryPaint)
                canvas.drawPath(oct2, accentPaint)
                canvas.drawCircle(cx, cy, radius * 0.22f, primaryPaint)
            }

            ThemeMotif.TEZHIP_MARBLE -> {
                // Tezhip Mermer Arabesk Motifi
                canvas.drawCircle(cx, cy, radius * 0.75f, primaryPaint)
                val wave = Path().apply {
                    moveTo(cx - radius * 0.7f, cy)
                    cubicTo(cx - radius * 0.3f, cy - radius * 0.7f, cx + radius * 0.3f, cy + radius * 0.7f, cx + radius * 0.7f, cy)
                }
                canvas.drawPath(wave, accentPaint)

                val wave2 = Path().apply {
                    moveTo(cx, cy - radius * 0.7f)
                    cubicTo(cx - radius * 0.7f, cy - radius * 0.3f, cx + radius * 0.7f, cy + radius * 0.3f, cx, cy + radius * 0.7f)
                }
                canvas.drawPath(wave2, primaryPaint)
                canvas.drawCircle(cx, cy, radius * 0.2f, accentPaint)
            }

            ThemeMotif.TOPKAPI_RUMI -> {
                // Topkapı Üç Benek Çintemani & Rûmî
                val spotRadius = radius * 0.24f
                // Üstteki benek
                canvas.drawCircle(cx, cy - radius * 0.35f, spotRadius, primaryPaint)
                canvas.drawCircle(cx, cy - radius * 0.35f, spotRadius * 0.45f, accentPaint)
                // Sol alttaki benek
                canvas.drawCircle(cx - radius * 0.42f, cy + radius * 0.32f, spotRadius, accentPaint)
                canvas.drawCircle(cx - radius * 0.42f, cy + radius * 0.32f, spotRadius * 0.45f, primaryPaint)
                // Sağ alttaki benek
                canvas.drawCircle(cx + radius * 0.42f, cy + radius * 0.32f, spotRadius, accentPaint)
                canvas.drawCircle(cx + radius * 0.42f, cy + radius * 0.32f, spotRadius * 0.45f, primaryPaint)
            }
        }
    }

    private fun applyAlpha(color: Int, factor: Float): Int {
        val a = ((Color.alpha(color) * factor.coerceIn(0f, 1f))).toInt()
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)
    }
}
