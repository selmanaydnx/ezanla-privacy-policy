package com.example.model

enum class CompassDialStyle(
    val id: String,
    val title: String,
    val subtitle: String
) {
    CLASSIC_KAABA(
        id = "classic",
        title = "Altın & Kâbe Klasik",
        subtitle = "Geleneksel dereceli pusula gülü, altın Kâbe görseli ve sırma motifler"
    ),
    MODERN_MINIMAL(
        id = "modern",
        title = "Modern & Dijital",
        subtitle = "Yüksek kontrastlı büyük açı göstergesi, neon yön çizgisi ve dijital pusula"
    ),
    RADAR_QIBLA(
        id = "radar",
        title = "Kâbe Radarı & Yörünge",
        subtitle = "Mesafe halkaları, Kâbe hedef kilidi (HUD) ve dinamik radar dalgası"
    ),
    SOLAR_CELESTIAL(
        id = "solar",
        title = "Güneş ile Kıble Tayini",
        subtitle = "Güneş'in anlık azimut konumu ve gölge yöntemiyle Kıble doğrulaması"
    ),
    OTTOMAN_ASTROLABE(
        id = "astrolabe",
        title = "Osmanlı Usturlâbı",
        subtitle = "Tarihi pirinç usturlap gravürü, sülüs astronomi yayları ve saray sırması"
    ),
    EMERALD_RAWDA(
        id = "emerald_rawda",
        title = "Zümrüt Ravza Kadranı",
        subtitle = "Mescid-i Nebevî yeşili zemin, altın varak ibre ve sedef sırma süsleme"
    ),
    SELJUK_GEOMETRIC(
        id = "seljuk_geo",
        title = "Selçuklu Geometrik",
        subtitle = "Sekizgen Selçuklu yıldız kadranı, turkuaz çini bordürü ve geometrik hedefleme"
    ),
    NIGHT_NAVIGATOR(
        id = "night_navigator",
        title = "Gece Seyrüseferi",
        subtitle = "Kutup Yıldızı kılavuzlu gece mavisi kadran ve neon fosforlu hedef ibresi"
    )
}
