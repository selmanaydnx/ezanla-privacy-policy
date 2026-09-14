package com.example.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.model.AppTheme

data class AppPalette(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isDark: Boolean,
    val isEyeComfort: Boolean = false,
    val motif: com.example.model.ThemeMotif = com.example.model.ThemeMotif.KISVE_GOLD,
    val theme: AppTheme = AppTheme.MIDNIGHT
)

// 1. Kâbe-i Muazzama (Gece Siyahı & Altın Varak)
val MidnightPalette = AppPalette(
    background = Color(0xFF090D16),
    surface = Color(0xFF111726),
    surfaceVariant = Color(0xFF1A2238),
    primary = Color(0xFFE5B842),
    secondary = Color(0xFFF9D342),
    accent = Color(0xFF10B981),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    isDark = true
)

// 2. Mescid-i Nebevî (Zümrüt Yeşili & Altın)
val EmeraldPalette = AppPalette(
    background = Color(0xFF061A12),
    surface = Color(0xFF0C2A1E),
    surfaceVariant = Color(0xFF143B2C),
    primary = Color(0xFF10B981),
    secondary = Color(0xFF34D399),
    accent = Color(0xFFFBBF24),
    textPrimary = Color(0xFFF0FDF4),
    textSecondary = Color(0xFFA7F3D0),
    textMuted = Color(0xFF6EE7B7),
    isDark = true
)

// 3. Ravza Kehribarı (Çöl Kumu & Sıcak Altın Bal)
val AmberPalette = AppPalette(
    background = Color(0xFF191008),
    surface = Color(0xFF281B0E),
    surfaceVariant = Color(0xFF3A2816),
    primary = Color(0xFFF59E0B),
    secondary = Color(0xFFFBBF24),
    accent = Color(0xFFFB923C),
    textPrimary = Color(0xFFFFFBEB),
    textSecondary = Color(0xFFFDE68A),
    textMuted = Color(0xFFD97706),
    isDark = true
)

// 4. Asr-ı Saâdet Nur (Aydınlık Mermer & Zarif Altın - Light)
val DawnLightPalette = AppPalette(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    primary = Color(0xFFB45309),
    secondary = Color(0xFFD97706),
    accent = Color(0xFF059669),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    isDark = false
)

// 5. Selçuklu Çinisi (Turkuaz & Kobalt)
val TurquoiseSeljukPalette = AppPalette(
    background = Color(0xFF061826),
    surface = Color(0xFF0C253B),
    surfaceVariant = Color(0xFF133654),
    primary = Color(0xFF06B6D4),
    secondary = Color(0xFF38BDF8),
    accent = Color(0xFF34D399),
    textPrimary = Color(0xFFF0FDF4),
    textSecondary = Color(0xFFBAE6FD),
    textMuted = Color(0xFF7DD3FC),
    isDark = true
)

// 6. Osmanlı Lâli (Koyu Bordo Lal & Varak Sarısı)
val RubyOttomanPalette = AppPalette(
    background = Color(0xFF1C0810),
    surface = Color(0xFF2C0F1C),
    surfaceVariant = Color(0xFF3F172A),
    primary = Color(0xFFE11D48),
    secondary = Color(0xFFFB7185),
    accent = Color(0xFFFBBF24),
    textPrimary = Color(0xFFFFF1F2),
    textSecondary = Color(0xFFFECDD3),
    textMuted = Color(0xFFFDA4AF),
    isDark = true
)

// 7. Endülüs Sarayı (Elhamra Laciverti & Yıldız Sarısı)
val IndigoAndalusiaPalette = AppPalette(
    background = Color(0xFF090B24),
    surface = Color(0xFF12163B),
    surfaceVariant = Color(0xFF1B2054),
    primary = Color(0xFF6366F1),
    secondary = Color(0xFF818CF8),
    accent = Color(0xFFFCD34D),
    textPrimary = Color(0xFFEEF2FF),
    textSecondary = Color(0xFFC7D2FE),
    textMuted = Color(0xFFA5B4FC),
    isDark = true
)

// 8. Medine Gülü (Gül Kurusu & Mürdüm)
val RoseMadinahPalette = AppPalette(
    background = Color(0xFF180A15),
    surface = Color(0xFF281223),
    surfaceVariant = Color(0xFF3A1B33),
    primary = Color(0xFFEC4899),
    secondary = Color(0xFFF472B6),
    accent = Color(0xFFFDE047),
    textPrimary = Color(0xFFFDF2F8),
    textSecondary = Color(0xFFFBCFE8),
    textMuted = Color(0xFFF472B6),
    isDark = true
)

// 9. Kudüs Zeytuni (Mescid-i Aksâ Zeytini & Bronz)
val OliveQudsPalette = AppPalette(
    background = Color(0xFF101609),
    surface = Color(0xFF1C2612),
    surfaceVariant = Color(0xFF2B3A1C),
    primary = Color(0xFF84CC16),
    secondary = Color(0xFFA3E635),
    accent = Color(0xFFEAB308),
    textPrimary = Color(0xFFF7FEE7),
    textSecondary = Color(0xFFD9F99D),
    textMuted = Color(0xFFBEF264),
    isDark = true
)

// 10. Topkapı Safiri (Kraliyet Safiri & Zümrüt)
val SapphireTopkapiPalette = AppPalette(
    background = Color(0xFF061324),
    surface = Color(0xFF0D213E),
    surfaceVariant = Color(0xFF16325B),
    primary = Color(0xFF2563EB),
    secondary = Color(0xFF60A5FA),
    accent = Color(0xFF10B981),
    textPrimary = Color(0xFFEFF6FF),
    textSecondary = Color(0xFFBFDBFE),
    textMuted = Color(0xFF93C5FD),
    isDark = true
)

// Light Palettes for themes when light mode is selected
val LightEmeraldPalette = AppPalette(
    background = Color(0xFFF5FAF7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6F4ED),
    primary = Color(0xFF059669),
    secondary = Color(0xFF10B981),
    accent = Color(0xFFD97706),
    textPrimary = Color(0xFF064E3B),
    textSecondary = Color(0xFF047857),
    textMuted = Color(0xFF6EE7B7),
    isDark = false
)

val LightAmberPalette = AppPalette(
    background = Color(0xFFFAF7F0),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5EDE0),
    primary = Color(0xFFD97706),
    secondary = Color(0xFFF59E0B),
    accent = Color(0xFFEA580C),
    textPrimary = Color(0xFF451A03),
    textSecondary = Color(0xFF78350F),
    textMuted = Color(0xFFB45309),
    isDark = false
)

val LightTurquoisePalette = AppPalette(
    background = Color(0xFFF0F9FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE0F2F7),
    primary = Color(0xFF0891B2),
    secondary = Color(0xFF06B6D4),
    accent = Color(0xFF059669),
    textPrimary = Color(0xFF164E63),
    textSecondary = Color(0xFF155E75),
    textMuted = Color(0xFF0E7490),
    isDark = false
)

val LightRubyPalette = AppPalette(
    background = Color(0xFFFFF5F7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE4E8),
    primary = Color(0xFFE11D48),
    secondary = Color(0xFFF43F5E),
    accent = Color(0xFFD97706),
    textPrimary = Color(0xFF881337),
    textSecondary = Color(0xFF9F1239),
    textMuted = Color(0xFFBE123C),
    isDark = false
)

val LightIndigoPalette = AppPalette(
    background = Color(0xFFF5F6FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEBEFFF),
    primary = Color(0xFF4F46E5),
    secondary = Color(0xFF6366F1),
    accent = Color(0xFFD97706),
    textPrimary = Color(0xFF1E1B4B),
    textSecondary = Color(0xFF312E81),
    textMuted = Color(0xFF3730A3),
    isDark = false
)

val LightRosePalette = AppPalette(
    background = Color(0xFFFFF5F9),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE4F0),
    primary = Color(0xFFDB2777),
    secondary = Color(0xFFEC4899),
    accent = Color(0xFFD97706),
    textPrimary = Color(0xFF831843),
    textSecondary = Color(0xFF9D174D),
    textMuted = Color(0xFFBE185D),
    isDark = false
)

val LightOlivePalette = AppPalette(
    background = Color(0xFFF8FAF2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEEF5DE),
    primary = Color(0xFF65A30D),
    secondary = Color(0xFF84CC16),
    accent = Color(0xFFD97706),
    textPrimary = Color(0xFF365314),
    textSecondary = Color(0xFF4D7C0F),
    textMuted = Color(0xFF65A30D),
    isDark = false
)

val LightSapphirePalette = AppPalette(
    background = Color(0xFFF2F7FD),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE2EDFA),
    primary = Color(0xFF1D4ED8),
    secondary = Color(0xFF2563EB),
    accent = Color(0xFF059669),
    textPrimary = Color(0xFF1E3A8A),
    textSecondary = Color(0xFF1E40AF),
    textMuted = Color(0xFF1D4ED8),
    isDark = false
)

// 0. Kâbe-i Muazzama Aydınlık Varyantı (Sıcak Fildişi & Altın Varak)
val LightMidnightPalette = AppPalette(
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1F5F9),
    primary = Color(0xFFB45309),
    secondary = Color(0xFFD97706),
    accent = Color(0xFF059669),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    isDark = false
)

val LocalAppPalette = staticCompositionLocalOf { MidnightPalette }

@Composable
fun MyApplicationTheme(
    selectedTheme: AppTheme = AppTheme.MIDNIGHT,
    darkModePreference: com.example.model.DarkModePreference = com.example.model.DarkModePreference.SYSTEM,
    isEyeComfortEnabled: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isNightTime = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        hour >= 19 || hour < 6
    }

    // Temalar eski işlevsel haline döndürüldü ve kullanıcı tercihine göre çalışır
    val isDark = when (darkModePreference) {
        com.example.model.DarkModePreference.DARK -> true
        com.example.model.DarkModePreference.LIGHT -> false
        com.example.model.DarkModePreference.SYSTEM -> systemInDark
        com.example.model.DarkModePreference.NIGHT_SCHEDULE -> isNightTime
    }

    val basePalette = if (isDark) {
        when (selectedTheme) {
            AppTheme.MIDNIGHT -> MidnightPalette
            AppTheme.EMERALD -> EmeraldPalette
            AppTheme.AMBER -> AmberPalette
            AppTheme.DAWN_LIGHT -> DawnLightPalette.copy(
                background = Color(0xFF0F141F),
                surface = Color(0xFF182030),
                surfaceVariant = Color(0xFF222B40),
                textPrimary = Color(0xFFF8FAFC),
                textSecondary = Color(0xFF94A3B8),
                isDark = true
            )
            AppTheme.TURQUOISE_SELJUK -> TurquoiseSeljukPalette
            AppTheme.RUBY_OTTOMAN -> RubyOttomanPalette
            AppTheme.INDIGO_ANDALUSIA -> IndigoAndalusiaPalette
            AppTheme.ROSE_MADINAH -> RoseMadinahPalette
            AppTheme.OLIVE_QUDS -> OliveQudsPalette
            AppTheme.SAPPHIRE_TOPKAPI -> SapphireTopkapiPalette
        }
    } else {
        when (selectedTheme) {
            AppTheme.MIDNIGHT -> LightMidnightPalette
            AppTheme.EMERALD -> LightEmeraldPalette
            AppTheme.AMBER -> LightAmberPalette
            AppTheme.DAWN_LIGHT -> DawnLightPalette
            AppTheme.TURQUOISE_SELJUK -> LightTurquoisePalette
            AppTheme.RUBY_OTTOMAN -> LightRubyPalette
            AppTheme.INDIGO_ANDALUSIA -> LightIndigoPalette
            AppTheme.ROSE_MADINAH -> LightRosePalette
            AppTheme.OLIVE_QUDS -> LightOlivePalette
            AppTheme.SAPPHIRE_TOPKAPI -> LightSapphirePalette
        }
    }

    // Göz Dinlendirme: Aşırı parlaklığı kırar, mavi ışığı filtreler, gözü asla yormaz
    val styledPalette = if (isEyeComfortEnabled) {
        if (isDark) {
            basePalette.copy(
                background = Color(0xFF0D1017),
                surface = Color(0xFF141924),
                surfaceVariant = Color(0xFF1D2433),
                textPrimary = Color(0xFFF5EFE6), // Sıcak krem tonu, gece göz kamaştırmaz
                textSecondary = Color(0xFFA8A29E),
                textMuted = Color(0xFF78716C),
                isEyeComfort = true
            )
        } else {
            basePalette.copy(
                background = Color(0xFFFBF9F4), // Yumuşak fildişi/kitap kağıdı ferahlığı
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFF3EFE6),
                textPrimary = Color(0xFF1C1917),
                textSecondary = Color(0xFF57534E),
                isEyeComfort = true
            )
        }
    } else {
        basePalette
    }

    // Seçilen temaya ait motif ve tema kimliğini her zaman palete enjekte et
    val palette = styledPalette.copy(
        motif = selectedTheme.motif,
        theme = selectedTheme
    )

    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color(0xFF1E1400),
            primaryContainer = palette.surfaceVariant,
            onPrimaryContainer = palette.secondary,
            secondary = palette.accent,
            onSecondary = Color(0xFF003822),
            secondaryContainer = palette.surfaceVariant,
            onSecondaryContainer = palette.accent,
            tertiary = palette.secondary,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceVariant,
            onSurfaceVariant = palette.textSecondary,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.surfaceVariant,
            onPrimaryContainer = palette.primary,
            secondary = palette.accent,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFD1FAE5),
            onSecondaryContainer = Color(0xFF064E3B),
            tertiary = palette.secondary,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surface,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceVariant,
            onSurfaceVariant = palette.textSecondary,
        )
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
