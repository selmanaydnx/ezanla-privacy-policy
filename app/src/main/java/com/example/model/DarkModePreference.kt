package com.example.model

enum class DarkModePreference(
    val id: String,
    val title: String,
    val subtitle: String
) {
    SYSTEM(
        id = "system",
        title = "Sistemle Uyumlu (Otomatik)",
        subtitle = "Cihazınızın sistem temasını (açık/koyu) otomatik takip eder"
    ),
    DARK(
        id = "dark",
        title = "Her Zaman Koyu Mod",
        subtitle = "Gece ve loş ışıkta göz yormayan derin siyah & altın tema"
    ),
    LIGHT(
        id = "light",
        title = "Açık Mod (Gündüz)",
        subtitle = "Gündüz saatleri için aydınlık ve berrak mermer beyazı tema"
    ),
    NIGHT_SCHEDULE(
        id = "night_schedule",
        title = "Gece Saatlerinde Otomatik Koyu",
        subtitle = "Akşam ve gece saatlerinde (19:00 - 06:00) otomatik göz korumalı koyu moda geçer"
    )
}
