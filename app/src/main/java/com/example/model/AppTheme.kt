package com.example.model

import androidx.compose.ui.graphics.Color

enum class ThemeMotif {
    KISVE_GOLD,        // Kâbe örtüsü altın sırmalı geometrik bordür & hat
    RAVZA_DOME,        // Mescid-i Nebevî yeşil kubbe & selvi motifi
    ARABESQUE_SUN,     // Sıcak İslami geometrik güneş & şemse rozeti
    TEZHIP_MARBLE,     // Asr-ı Saadet mermer kakma ve altın çiçek tezhibi
    SELJUK_STAR,       // Meşhur Selçuklu 8 köşeli yıldızı (Sekizgen Geometri)
    OTTOMAN_TULIP,     // Klasik Osmanlı lâlesi, rûmî ve hatayî tezhip
    ALHAMBRA_ARCH,     // Endülüs Elhamra mukarnas kemeri & yıldızlı mozaik
    MADINAH_ROSE,      // Gül-i Muhammedî (Peygamber gülü) 8 yapraklı rozet
    QUDS_OCTAGON,      // Mescid-i Aksâ & Kubbet-üs Sahrâ sekizgen motifi
    TOPKAPI_RUMI       // Topkapı Sarayı çinisi çintemani & rumi motifi
}

enum class AppTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewBg: Color,
    val previewPrimary: Color,
    val previewAccent: Color,
    val isDark: Boolean = true,
    val motif: ThemeMotif = ThemeMotif.KISVE_GOLD,
    val motifTitle: String = "Kisve-i Şerîf Bordürü",
    val motifDesc: String = "Kâbe örtüsü altın sırmalı geometrik kuşak"
) {
    MIDNIGHT(
        id = "midnight",
        title = "Kâbe-i Muazzama",
        subtitle = "Gece Siyahı & Altın Varak",
        previewBg = Color(0xFF090D16),
        previewPrimary = Color(0xFFE5B842),
        previewAccent = Color(0xFF10B981),
        isDark = true,
        motif = ThemeMotif.KISVE_GOLD,
        motifTitle = "Kisve-i Şerîf Bordürü",
        motifDesc = "Kâbe altın sırmalı hat & geometrik kuşak"
    ),
    EMERALD(
        id = "emerald",
        title = "Mescid-i Nebevî",
        subtitle = "Nebevî Zümrüdü & Altın İşleme",
        previewBg = Color(0xFF061A12),
        previewPrimary = Color(0xFF10B981),
        previewAccent = Color(0xFFFBBF24),
        isDark = true,
        motif = ThemeMotif.RAVZA_DOME,
        motifTitle = "Kubbe-i Hadrâ & Selvi",
        motifDesc = "Nebevî yeşil kubbe ve sabır selvisi motifi"
    ),
    AMBER(
        id = "amber",
        title = "Ravza Kehribarı",
        subtitle = "Çöl Kumu & Sıcak Altın Bal",
        previewBg = Color(0xFF1C120A),
        previewPrimary = Color(0xFFF59E0B),
        previewAccent = Color(0xFFFB923C),
        isDark = true,
        motif = ThemeMotif.ARABESQUE_SUN,
        motifTitle = "Geometrik Güneş Şemse",
        motifDesc = "Sıcak altın ışınlı İslami geometrik rozet"
    ),
    DAWN_LIGHT(
        id = "dawn_light",
        title = "Asr-ı Saâdet Nur",
        subtitle = "Aydınlık Mermer & Zarif Altın",
        previewBg = Color(0xFFF8FAFC),
        previewPrimary = Color(0xFFB45309),
        previewAccent = Color(0xFF059669),
        isDark = false,
        motif = ThemeMotif.TEZHIP_MARBLE,
        motifTitle = "Mermer Kakma & Tezhip",
        motifDesc = "Ak mermer üzerine zarif altın varak hatayi"
    ),
    TURQUOISE_SELJUK(
        id = "seljuk_turquoise",
        title = "Selçuklu Çinisi",
        subtitle = "Gök Kubbe Turkuazı & Kobalt",
        previewBg = Color(0xFF061826),
        previewPrimary = Color(0xFF06B6D4),
        previewAccent = Color(0xFF38BDF8),
        isDark = true,
        motif = ThemeMotif.SELJUK_STAR,
        motifTitle = "Selçuklu 8 Köşeli Yıldızı",
        motifDesc = "Merhamet ve adaleti simgeleyen 8 köşeli yıldız"
    ),
    RUBY_OTTOMAN(
        id = "ottoman_ruby",
        title = "Osmanlı Lâli",
        subtitle = "Koyu Bordo Lal & Varak Sarısı",
        previewBg = Color(0xFF1D0910),
        previewPrimary = Color(0xFFE11D48),
        previewAccent = Color(0xFFFBBF24),
        isDark = true,
        motif = ThemeMotif.OTTOMAN_TULIP,
        motifTitle = "Klasik Osmanlı Lâlesi",
        motifDesc = "Tevhidi simgeleyen Osmanlı saray lâlesi & rûmî"
    ),
    INDIGO_ANDALUSIA(
        id = "andalusia_indigo",
        title = "Endülüs Sarayı",
        subtitle = "Elhamra Laciverti & Yıldız Sarısı",
        previewBg = Color(0xFF0A0C27),
        previewPrimary = Color(0xFF6366F1),
        previewAccent = Color(0xFFFCD34D),
        isDark = true,
        motif = ThemeMotif.ALHAMBRA_ARCH,
        motifTitle = "Elhamra Kemer & Mozaik",
        motifDesc = "Endülüs mukarnaslı at nalı kemeri ve yıldız çinisi"
    ),
    ROSE_MADINAH(
        id = "madinah_rose",
        title = "Medine Gülü",
        subtitle = "Gül Kurusu & Mürdüm Çiçeği",
        previewBg = Color(0xFF1B0C18),
        previewPrimary = Color(0xFFEC4899),
        previewAccent = Color(0xFFF472B6),
        isDark = true,
        motif = ThemeMotif.MADINAH_ROSE,
        motifTitle = "Gül-i Muhammedî Şemse",
        motifDesc = "8 taç yapraklı Nebevi gül rozeti motifi"
    ),
    OLIVE_QUDS(
        id = "quds_olive",
        title = "Kudüs Zeytuni",
        subtitle = "Mescid-i Aksâ Zeytini & Bronz",
        previewBg = Color(0xFF11170A),
        previewPrimary = Color(0xFF84CC16),
        previewAccent = Color(0xFFEAB308),
        isDark = true,
        motif = ThemeMotif.QUDS_OCTAGON,
        motifTitle = "Kubbet-üs Sahrâ Sekizgeni",
        motifDesc = "Aksâ avlusu sekizgen mimari ve zeytin dalı motifi"
    ),
    SAPPHIRE_TOPKAPI(
        id = "topkapi_sapphire",
        title = "Topkapı Safiri",
        subtitle = "Kraliyet Safiri & Zümrüt Işıltısı",
        previewBg = Color(0xFF071424),
        previewPrimary = Color(0xFF2563EB),
        previewAccent = Color(0xFF10B981),
        isDark = true,
        motif = ThemeMotif.TOPKAPI_RUMI,
        motifTitle = "Topkapı Çintemani & Çini",
        motifDesc = "Kudret simgesi üç benek çintemani & rûmî kıvrımları"
    )
}
