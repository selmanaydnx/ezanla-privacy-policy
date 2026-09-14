package com.example.ui

import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.QuranAudioPlayer
import com.example.model.PrayerType
import com.example.ui.components.CelestialMosqueHeader
import com.example.ui.components.CityPickerSheet
import com.example.ui.components.DailyWisdomHomeCard
import com.example.ui.components.NearbyMosquesHomeSection
import com.example.ui.components.PrayerCardList
import com.example.ui.components.PrayerCountdownGauge
import com.example.ui.components.StartupPermissionHandler
import com.example.ui.components.ThemeMotifWatermark
import com.example.ui.components.ThemeMotifBadge
import com.example.ui.screens.*
import com.example.ui.theme.LocalAppPalette

/**
 * Kullanıcı Talepleri:
 * 1. "Ana sayfadaki islami hizmetler bölümü ilen günlük namaz vakitleri bölümünün yerini değiştir"
 * 2. "alt da ki menüler bölümünün isimlerini ve ikonlarını hizala düzelt"
 * 3. "namaz ve kaza borçları defterini boş bırak ve kendi yapsın ve kişi yaptığında kayıtlı kalsın"
 * 4. "kıble kısmıda tam zamanlı gps e bağlı kalsın düzgün göstersin"
 * 5. "tema yı da kişi ne seçerse o kalsın kişinin uygulamada yaptığı eylemler kayıtlı kalsın"
 */
enum class AppNavTab(val title: String, val icon: ImageVector) {
    VAKITLER("Vakitler", Icons.Default.AccessTime),
    KURAN("Kur'ân", Icons.Default.MenuBook),
    KIBLE("Kıble", Icons.Default.Explore),
    ZIKIRMATIK("Zikir", Icons.Default.TouchApp),
    HIZMETLER("Hizmetler", Icons.Default.DashboardCustomize),
    AYARLAR("Ayarlar", Icons.Default.Settings)
}

enum class SubServiceView {
    NONE,
    NEARBY_MOSQUES,
    HATIM,
    RELIGIOUS_DAYS,
    ASMA_UL_HUSNA,
    VIDEOS,
    WALLPAPERS,
    PRAYER_TRACKER,
    WISDOM_DUAS,
    RAMADAN,
    SETTINGS
}

@Composable
fun MainScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LocalAppPalette.current
    val lastSavedSubServiceName = remember { viewModel.getLastSavedSubService() }
    val initialSubService = remember {
        try {
            if (lastSavedSubServiceName != "NONE" && lastSavedSubServiceName != "SETTINGS") {
                SubServiceView.valueOf(lastSavedSubServiceName)
            } else {
                SubServiceView.NONE
            }
        } catch (_: Exception) {
            SubServiceView.NONE
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = AppNavTab.VAKITLER.ordinal) { AppNavTab.entries.size }

    var activeSubService by remember { mutableStateOf(SubServiceView.NONE) }
    var lastHizmetlerSubService by remember { mutableStateOf(initialSubService) }

    val navigateToTab: (AppNavTab) -> Unit = { tab ->
        activeSubService = SubServiceView.NONE
        coroutineScope.launch {
            pagerState.animateScrollToPage(tab.ordinal)
        }
    }

    val openSubService: (SubServiceView) -> Unit = { service ->
        if (service == SubServiceView.SETTINGS) {
            // Kullanıcı isteği: Ayarlar bölümünü ayır alt menüye koy
            activeSubService = SubServiceView.NONE
            navigateToTab(AppNavTab.AYARLAR)
        } else {
            activeSubService = service
            if (service != SubServiceView.NONE) {
                lastHizmetlerSubService = service
                viewModel.saveActiveSubService(service.name)
            }
        }
    }

    // Persistent Audio Player for Quran Recitations
    val quranAudioPlayer = remember { QuranAudioPlayer(context) }

    // Smooth system back-press handling
    BackHandler(enabled = activeSubService != SubServiceView.NONE) {
        activeSubService = SubServiceView.NONE
        lastHizmetlerSubService = SubServiceView.NONE
        viewModel.saveActiveSubService("NONE")
    }
    BackHandler(enabled = activeSubService == SubServiceView.NONE && pagerState.currentPage != AppNavTab.VAKITLER.ordinal) {
        navigateToTab(AppNavTab.VAKITLER)
    }

    // Compass sensor lifecycle: only active when on KIBLE tab and no subservice is opened
    LaunchedEffect(pagerState.currentPage, activeSubService) {
        if (activeSubService == SubServiceView.NONE && pagerState.currentPage == AppNavTab.KIBLE.ordinal) {
            viewModel.startCompassSensor()
        } else {
            viewModel.stopCompassSensor()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopCompassSensor()
        }
    }

    // Startup Permission Dialog (Location & Notifications)
    StartupPermissionHandler(
        onPermissionsHandled = {
            viewModel.verifyLocation()
        }
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = palette.background,
        bottomBar = {
            NavigationBar(
                containerColor = palette.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Seçili sekme göstergesi kaydırma sırasında hedef sayfayı eş zamanlı takip eder
                val selectedTabOrdinal = if (pagerState.isScrollInProgress) {
                    pagerState.targetPage
                } else {
                    pagerState.currentPage
                }

                AppNavTab.entries.forEach { tab ->
                    val isSelected = if (activeSubService != SubServiceView.NONE) {
                        if (activeSubService == SubServiceView.SETTINGS) false else tab == AppNavTab.HIZMETLER
                    } else {
                        selectedTabOrdinal == tab.ordinal
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (tab == AppNavTab.HIZMETLER) {
                                if (activeSubService == SubServiceView.NONE && selectedTabOrdinal == AppNavTab.HIZMETLER.ordinal && lastHizmetlerSubService != SubServiceView.NONE) {
                                    activeSubService = lastHizmetlerSubService
                                } else {
                                    navigateToTab(AppNavTab.HIZMETLER)
                                }
                            } else {
                                navigateToTab(tab)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (palette.isDark) Color(0xFF1E1400) else Color.White,
                            selectedTextColor = palette.primary,
                            indicatorColor = palette.primary,
                            unselectedIconColor = palette.textSecondary,
                            unselectedTextColor = palette.textMuted
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Seçilen temanın motifi uygulamanın her tarafında zarifçe arka planda yer alır
            ThemeMotifWatermark(
                motif = palette.motif,
                primaryColor = palette.primary,
                accentColor = palette.accent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(250.dp)
                    .offset(x = 60.dp, y = (-20).dp),
                alpha = if (palette.isDark) 0.055f else 0.04f
            )

            // Compose content transition animations when switching between the main dashboard and the services menu
            AnimatedContent(
                targetState = activeSubService,
                transitionSpec = {
                    if (initialState == SubServiceView.NONE && targetState != SubServiceView.NONE) {
                        // Dashboard -> Alt Hizmet Menüsü: Yumuşak sağdan kayma, zarif derinlik ölçeklemesi ve fade
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.22f).toInt() },
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 300)
                        ) + scaleIn(
                            initialScale = 0.97f,
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                        )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 200)
                        ))
                    } else if (initialState != SubServiceView.NONE && targetState == SubServiceView.NONE) {
                        // Alt Hizmet Menüsü -> Dashboard (Geri Dönüş): Doğal geri geliş ve yumuşak fade
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> -(fullWidth * 0.12f).toInt() },
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        ) + fadeIn(
                            animationSpec = tween(durationMillis = 300)
                        ) + scaleIn(
                            initialScale = 0.98f,
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        )) togetherWith
                        (slideOutHorizontally(
                            targetOffsetX = { fullWidth -> (fullWidth * 0.22f).toInt() },
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 200)
                        ))
                    } else {
                        // Alt hizmetler arası geçişte
                        (fadeIn(
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        ) + scaleIn(
                            initialScale = 0.98f,
                            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
                        )) togetherWith
                        (fadeOut(
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                        ))
                    }
                },
                label = "DashboardSubServiceAnimatedTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetService ->
                if (targetService != SubServiceView.NONE) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(palette.background)
                    ) {
                        // Sub-service top back bar
                        Surface(
                            color = palette.surface,
                            tonalElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    activeSubService = SubServiceView.NONE
                                    lastHizmetlerSubService = SubServiceView.NONE
                                    viewModel.saveActiveSubService("NONE")
                                }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri Dön", tint = palette.textPrimary)
                                }
                                Text(
                                    text = when (targetService) {
                                        SubServiceView.NEARBY_MOSQUES -> "Yakın Camiler"
                                        SubServiceView.HATIM -> "Hatim Takibi"
                                        SubServiceView.RELIGIOUS_DAYS -> "Dini Günler"
                                        SubServiceView.ASMA_UL_HUSNA -> "Esmâü'l-Hüsnâ"
                                        SubServiceView.VIDEOS -> "Dini Videolar"
                                        SubServiceView.WALLPAPERS -> "İslâmî Duvar Kağıtları"
                                        SubServiceView.PRAYER_TRACKER -> "Namaz & Kaza Borç Takibi"
                                        SubServiceView.WISDOM_DUAS -> "Günün Hikmeti & Dualar"
                                        SubServiceView.RAMADAN -> "Ramazan İmsakiyesi"
                                        SubServiceView.SETTINGS -> "Uygulama Ayarları"
                                        SubServiceView.NONE -> "Geri"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.textPrimary
                                    )
                                )
                            }
                        }

                        when (targetService) {
                        SubServiceView.NEARBY_MOSQUES -> NearbyMosquesScreen(currentCity = uiState.selectedCity)
                        SubServiceView.HATIM -> HatimScreen(audioPlayer = quranAudioPlayer)
                        SubServiceView.RELIGIOUS_DAYS -> ReligiousDaysScreen()
                        SubServiceView.ASMA_UL_HUSNA -> AsmaUlHusnaScreen(
                            onSelectForDhikr = { asma ->
                                viewModel.selectDhikrByAsma(asma.number)
                                activeSubService = SubServiceView.NONE
                                navigateToTab(AppNavTab.ZIKIRMATIK)
                            }
                        )
                        SubServiceView.VIDEOS -> IslamicVideosScreen()
                        SubServiceView.WALLPAPERS -> WallpapersScreen()
                        SubServiceView.PRAYER_TRACKER -> PrayerTrackerScreen(
                            dailyCheck = uiState.dailyPrayerCheck,
                            kazaTracker = uiState.kazaTracker,
                            kazaBookEntries = uiState.kazaBookEntries,
                            onToggleDailyPrayer = { viewModel.toggleDailyPrayer(it) },
                            onUpdateKazaTracker = { viewModel.updateKazaTracker(it) },
                            onLogKazaAction = { title, desc, change, prayer, updated ->
                                viewModel.logKazaAction(title, desc, change, prayer, updated)
                            },
                            onClearKazaBook = { viewModel.clearKazaBook() },
                            prayerTimes = uiState.prayerTimes
                        )
                        SubServiceView.WISDOM_DUAS -> WisdomAndPrayersScreen(wisdom = uiState.dailyWisdom)
                        SubServiceView.RAMADAN -> RamadanScreen(
                            currentCity = uiState.selectedCity,
                            prayerTimes = uiState.prayerTimes
                        )
                        SubServiceView.SETTINGS -> SettingsScreen(
                            selectedTheme = uiState.selectedTheme,
                            onThemeChange = { viewModel.setTheme(it) },
                            darkModePreference = uiState.darkModePreference,
                            onDarkModePreferenceChange = { viewModel.setDarkModePreference(it) },
                            isEyeComfortEnabled = uiState.isEyeComfortEnabled,
                            onToggleEyeComfort = { viewModel.toggleEyeComfort() },
                            isLargeFontEnabled = uiState.isLargeFontEnabled,
                            onToggleLargeFont = { viewModel.toggleLargeFont() },
                            selectedCity = uiState.selectedCity,
                            onCityChange = { viewModel.setCity(it) },
                            calculationMethod = uiState.calculationMethod,
                            onCalculationMethodChange = { viewModel.setCalculationMethod(it) },
                            precautionMinutes = uiState.precautionMinutes,
                            onPrecautionMinutesChange = { viewModel.setPrecautionMinutes(it) },
                            isEarlyReminderEnabled = uiState.isEarlyReminderEnabled,
                            onToggleEarlyReminder = { viewModel.toggleEarlyReminder() },
                            selectedEzanVoice = uiState.selectedEzanVoice,
                            onEzanVoiceChange = { viewModel.setEzanVoice(it) },
                            prayerEzanVoices = uiState.prayerEzanVoices,
                            onPrayerEzanVoiceChange = { type, voice -> viewModel.setPrayerEzanVoice(type, voice) },
                            isPlayingEzan = uiState.isPlayingEzan,
                            activePlayingVoiceName = uiState.activePlayingVoiceName,
                            onTestSound = { voice -> viewModel.testSoundAndVibration(voice) },
                            onStopSound = { viewModel.stopEzanPlayback() },
                            isVibrationEnabled = uiState.isVibrationEnabled,
                            onToggleVibration = { viewModel.toggleVibration() },
                            locationState = uiState.locationVerificationState,
                            onVerifyLocation = { viewModel.verifyLocation() },
                            widgetThemeOption = uiState.widgetThemeOption,
                            onWidgetThemeOptionChange = { viewModel.setWidgetThemeOption(it) },
                            widgetMotifOption = uiState.widgetMotifOption,
                            onWidgetMotifOptionChange = { viewModel.setWidgetMotifOption(it) },
                            widgetBackgroundOpacity = uiState.widgetBackgroundOpacity,
                            onWidgetBackgroundOpacityChange = { viewModel.setWidgetBackgroundOpacity(it) },
                            widgetMotifWatermarkEnabled = uiState.widgetMotifWatermarkEnabled,
                            onToggleWidgetMotifWatermark = { viewModel.toggleWidgetMotifWatermark() },
                            currentPrayerTimeItem = uiState.currentPrayer,
                            nextPrayerTimeItem = uiState.nextPrayer,
                            countdownText = uiState.countdownText
                        )
                        SubServiceView.NONE -> {}
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(palette.background)
                ) {
                    HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { page -> AppNavTab.entries[page].name }
                ) { page ->
                    when (AppNavTab.entries[page]) {
                        AppNavTab.VAKITLER -> {
                            VakitlerScreen(
                                uiState = uiState,
                                onCityClick = { viewModel.showCityPicker() },
                                onDateOffsetChange = { offset ->
                                    viewModel.setDateOffset(offset)
                                },
                                onToggleNotification = { viewModel.toggleNotification(it) },
                                onNextWisdom = { viewModel.nextWisdom() },
                                onNavigateToTab = { tab -> navigateToTab(tab) },
                                onOpenSubService = { service -> openSubService(service) },
                                onOpenSettings = { navigateToTab(AppNavTab.AYARLAR) }
                            )
                        }

                        AppNavTab.KURAN -> {
                            QuranScreen(audioPlayer = quranAudioPlayer)
                        }

                        AppNavTab.KIBLE -> {
                            val compassAzimuth by viewModel.compassAzimuthFlow.collectAsStateWithLifecycle()
                            QiblaCompassScreen(
                                city = uiState.selectedCity,
                                compassAzimuth = compassAzimuth,
                                isSensorAvailable = uiState.isSensorAvailable,
                                onManualAzimuthChange = { viewModel.setManualCompassAzimuth(it) },
                                liveGpsState = uiState.liveGpsState,
                                onRefreshGps = { viewModel.startGpsTracking() },
                                compassDialStyle = uiState.compassDialStyle,
                                onSelectDialStyle = { viewModel.setCompassDialStyle(it) },
                                isHapticEnabled = uiState.isCompassHapticEnabled,
                                onToggleHaptic = { viewModel.toggleCompassHaptic() },
                                isSoundEnabled = uiState.isCompassSoundEnabled,
                                onToggleSound = { viewModel.toggleCompassSound() },
                                isLevelEnabled = uiState.isCompassLevelEnabled,
                                onToggleLevel = { viewModel.toggleCompassLevel() },
                                isTrueNorthEnabled = uiState.isCompassTrueNorthEnabled,
                                onToggleTrueNorth = { viewModel.toggleCompassTrueNorth() },
                                pitch = uiState.compassPitch,
                                roll = uiState.compassRoll,
                                isDeviceFlat = uiState.isDeviceFlat,
                                sunAzimuth = uiState.sunAzimuth,
                                sunAltitude = uiState.sunAltitude,
                                magneticDeclination = uiState.magneticDeclination,
                                onQiblaAligned = { viewModel.triggerQiblaAlignmentFeedback() }
                            )
                        }

                        AppNavTab.ZIKIRMATIK -> {
                            ZikirmatikScreen(
                                currentDhikrIndex = uiState.currentDhikrIndex,
                                dhikrCount = uiState.dhikrCount,
                                completedLaps = uiState.dhikrCompletedLaps,
                                isVibrationEnabled = uiState.isVibrationEnabled,
                                dhikrBookEntries = uiState.dhikrBookEntries,
                                onIncrement = { viewModel.incrementDhikr() },
                                onReset = { viewModel.resetDhikr() },
                                onSelectPreset = { viewModel.selectDhikrPreset(it) },
                                onSelectPresetById = { viewModel.selectDhikrById(it) },
                                onResetBookEntry = { viewModel.resetDhikrBookEntry(it) },
                                onToggleVibration = { viewModel.toggleVibration() }
                            )
                        }

                        AppNavTab.HIZMETLER -> {
                            ServicesCatalogScreen(
                                onOpenService = { service -> openSubService(service) },
                                onNavigateToTab = { tab -> navigateToTab(tab) }
                            )
                        }

                        AppNavTab.AYARLAR -> {
                            SettingsScreen(
                                selectedTheme = uiState.selectedTheme,
                                onThemeChange = { viewModel.setTheme(it) },
                                darkModePreference = uiState.darkModePreference,
                                onDarkModePreferenceChange = { viewModel.setDarkModePreference(it) },
                                isEyeComfortEnabled = uiState.isEyeComfortEnabled,
                                onToggleEyeComfort = { viewModel.toggleEyeComfort() },
                                isLargeFontEnabled = uiState.isLargeFontEnabled,
                                onToggleLargeFont = { viewModel.toggleLargeFont() },
                                selectedCity = uiState.selectedCity,
                                onCityChange = { viewModel.setCity(it) },
                                calculationMethod = uiState.calculationMethod,
                                onCalculationMethodChange = { viewModel.setCalculationMethod(it) },
                                precautionMinutes = uiState.precautionMinutes,
                                onPrecautionMinutesChange = { viewModel.setPrecautionMinutes(it) },
                                isEarlyReminderEnabled = uiState.isEarlyReminderEnabled,
                                onToggleEarlyReminder = { viewModel.toggleEarlyReminder() },
                                selectedEzanVoice = uiState.selectedEzanVoice,
                                onEzanVoiceChange = { viewModel.setEzanVoice(it) },
                                prayerEzanVoices = uiState.prayerEzanVoices,
                                onPrayerEzanVoiceChange = { type, voice -> viewModel.setPrayerEzanVoice(type, voice) },
                                isPlayingEzan = uiState.isPlayingEzan,
                                activePlayingVoiceName = uiState.activePlayingVoiceName,
                                onTestSound = { voice -> viewModel.testSoundAndVibration(voice) },
                                onStopSound = { viewModel.stopEzanPlayback() },
                                isVibrationEnabled = uiState.isVibrationEnabled,
                                onToggleVibration = { viewModel.toggleVibration() },
                                locationState = uiState.locationVerificationState,
                                onVerifyLocation = { viewModel.verifyLocation() },
                                widgetThemeOption = uiState.widgetThemeOption,
                                onWidgetThemeOptionChange = { viewModel.setWidgetThemeOption(it) },
                                widgetMotifOption = uiState.widgetMotifOption,
                                onWidgetMotifOptionChange = { viewModel.setWidgetMotifOption(it) },
                                widgetBackgroundOpacity = uiState.widgetBackgroundOpacity,
                                onWidgetBackgroundOpacityChange = { viewModel.setWidgetBackgroundOpacity(it) },
                                widgetMotifWatermarkEnabled = uiState.widgetMotifWatermarkEnabled,
                                onToggleWidgetMotifWatermark = { viewModel.toggleWidgetMotifWatermark() },
                                currentPrayerTimeItem = uiState.currentPrayer,
                                nextPrayerTimeItem = uiState.nextPrayer,
                                countdownText = uiState.countdownText,
                                compassDialStyle = uiState.compassDialStyle,
                                onCompassDialStyleChange = { viewModel.setCompassDialStyle(it) },
                                isCompassHapticEnabled = uiState.isCompassHapticEnabled,
                                onToggleCompassHaptic = { viewModel.toggleCompassHaptic() },
                                isCompassSoundEnabled = uiState.isCompassSoundEnabled,
                                onToggleCompassSound = { viewModel.toggleCompassSound() },
                                isCompassLevelEnabled = uiState.isCompassLevelEnabled,
                                onToggleCompassLevel = { viewModel.toggleCompassLevel() },
                                isCompassTrueNorthEnabled = uiState.isCompassTrueNorthEnabled,
                                onToggleCompassTrueNorth = { viewModel.toggleCompassTrueNorth() }
                            )
                        }
                    }
                }
            }
        }
    }

            // City Picker Bottom Sheet
            if (uiState.isCityPickerVisible) {
                CityPickerSheet(
                    selectedCity = uiState.selectedCity,
                    searchQuery = uiState.citySearchQuery,
                    filteredCities = uiState.filteredCities,
                    onQueryChange = { viewModel.searchCity(it) },
                    onCitySelected = { viewModel.setCity(it) },
                    onDismiss = { viewModel.hideCityPicker() }
                )
            }
        }
    }
}

/**
 * Kullanıcı Talebi:
 * "Ana sayfadaki islami hizmetler bölümü ilen günlük namaz vakitleri bölümünün yerini değiştir"
 * "günün hikmeti kısmındaki yazılar ve ikonlar kayık onları düzelt"
 */
@Composable
private fun VakitlerScreen(
    uiState: PrayerUiState,
    onCityClick: () -> Unit,
    onDateOffsetChange: (Int) -> Unit,
    onToggleNotification: (PrayerType) -> Unit,
    onNextWisdom: () -> Unit,
    onNavigateToTab: (AppNavTab) -> Unit,
    onOpenSubService: (SubServiceView) -> Unit,
    onOpenSettings: () -> Unit
) {
    val scrollState = rememberScrollState()
    val palette = LocalAppPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // Dynamic Celestial Mosque Sky Header
        CelestialMosqueHeader(
            cityName = uiState.selectedCity.name,
            hijriDateStr = uiState.hijriDate.format(),
            gregorianDateStr = uiState.gregorianDateText,
            currentPrayerType = uiState.currentPrayer?.type,
            onCityClick = onCityClick,
            onOpenSettings = onOpenSettings
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Countdown Progress Circular Gauge
        PrayerCountdownGauge(
            nextPrayer = uiState.nextPrayer,
            currentPrayer = uiState.currentPrayer,
            countdownText = uiState.countdownText,
            progress = uiState.progressToNextPrayer,
            dateOffsetDays = uiState.dateOffsetDays,
            onDateOffsetChange = onDateOffsetChange,
            gregorianDateStr = uiState.gregorianDateText,
            hijriDateStr = uiState.hijriDate.format()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================================================
        // 1. GÜNLÜK NAMAZ VAKİTLERİ BÖLÜMÜ (KULLANICI TALEBİYLE İLKE ALINDI)
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GÜNLÜK NAMAZ VAKİTLERİ",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = uiState.selectedCity.name,
                style = MaterialTheme.typography.labelSmall.copy(color = palette.textSecondary)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 6 Prayer Cards List (İmsak, Güneş, Öğle, İkindi, Akşam, Yatsı)
        PrayerCardList(
            prayerTimes = uiState.prayerTimes,
            notificationSettings = uiState.notificationSettings,
            onToggleNotification = onToggleNotification
        )

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 2. İSLÂMÎ HİZMETLER BÖLÜMÜ (KULLANICI TALEBİYLE VAKİTLERDEN SONRAYA ALINDI)
        // =========================================================================
        QuickServicesSection(
            onNavigateToTab = onNavigateToTab,
            onOpenSubService = onOpenSubService
        )

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 3. YAKIN CAMİLER BÖLÜMÜ (KULLANICI TALEBİYLE EKLENDİ)
        // =========================================================================
        NearbyMosquesHomeSection(
            currentCity = uiState.selectedCity,
            onOpenMosquesScreen = { onOpenSubService(SubServiceView.NEARBY_MOSQUES) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // =========================================================================
        // 4. GÜNÜN HİKMETİ (ÂYET & HADÎS-İ ŞERÎF) - DÜZELTİLMİŞ & HİZALANMIŞ
        // =========================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GÜNÜN HİKMETİ",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "Âyet & Hadîs-i Şerîf",
                style = MaterialTheme.typography.labelSmall.copy(color = palette.secondary)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        DailyWisdomHomeCard(
            wisdom = uiState.dailyWisdom,
            onNextWisdom = onNextWisdom,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun QuickServicesSection(
    onNavigateToTab: (AppNavTab) -> Unit,
    onOpenSubService: (SubServiceView) -> Unit
) {
    val palette = LocalAppPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "İSLÂMÎ HİZMETLER VE ARAÇLAR",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = palette.primary,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "Tümünü Gör",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = palette.accent,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onNavigateToTab(AppNavTab.HIZMETLER) }
            )
        }

        // 2x3 Grid of Primary Features
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickServiceCard(
                title = "Kıble Pusulası",
                subtitle = "GPS Destekli",
                icon = Icons.Default.Explore,
                color = palette.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToTab(AppNavTab.KIBLE) }
            )
            QuickServiceCard(
                title = "Namaz & Kaza",
                subtitle = "Borç Çetelesi",
                icon = Icons.Default.FactCheck,
                color = palette.accent,
                modifier = Modifier.weight(1f),
                onClick = { onOpenSubService(SubServiceView.PRAYER_TRACKER) }
            )
            QuickServiceCard(
                title = "Duvar Kağıdı",
                subtitle = "HD & Ayarla",
                icon = Icons.Default.Wallpaper,
                color = palette.secondary,
                modifier = Modifier.weight(1f),
                onClick = { onOpenSubService(SubServiceView.WALLPAPERS) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickServiceCard(
                title = "Esmâü'l-Hüsnâ",
                subtitle = "Sesli Okunuş",
                icon = Icons.Default.Stars,
                color = palette.primary,
                modifier = Modifier.weight(1f),
                onClick = { onOpenSubService(SubServiceView.ASMA_UL_HUSNA) }
            )
            QuickServiceCard(
                title = "Dini Videolar",
                subtitle = "Sohbet & Tefsir",
                icon = Icons.Default.VideoLibrary,
                color = palette.secondary,
                modifier = Modifier.weight(1f),
                onClick = { onOpenSubService(SubServiceView.VIDEOS) }
            )
            QuickServiceCard(
                title = "Dini Günler",
                subtitle = "2025/2026 Takvim",
                icon = Icons.Default.CalendarMonth,
                color = palette.accent,
                modifier = Modifier.weight(1f),
                onClick = { onOpenSubService(SubServiceView.RELIGIOUS_DAYS) }
            )
        }
    }
}

@Composable
private fun QuickServiceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = palette.surface,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(color.copy(alpha = 0.35f), Color.Transparent)
            )
        ),
        modifier = modifier
            .height(98.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = palette.textMuted,
                        fontSize = 9.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ServicesCatalogScreen(
    onOpenService: (SubServiceView) -> Unit,
    onNavigateToTab: (AppNavTab) -> Unit
) {
    val palette = LocalAppPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "İslâmî Hizmetler",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                )
                Text(
                    text = "Kapsamlı ibadet, ilim ve maneviyat rehberi",
                    style = MaterialTheme.typography.bodySmall.copy(color = palette.textSecondary)
                )
            }

            ThemeMotifBadge(
                motif = palette.motif,
                primaryColor = palette.primary,
                accentColor = palette.accent,
                size = 44.dp,
                backgroundColor = palette.surface
            )
        }

        // 1. Namaz ve Kaza Borç Takibi (En Yüksek Günlük Kullanım)
        ServiceCatalogItem(
            title = "Namaz ve Kaza Borç Takibi",
            description = "5 vakit günlük eda kontrolü ve boş bırakılmış, kalıcı kaydedilen kaza borç defteri",
            icon = Icons.Default.FactCheck,
            badge = "Kalıcı Kayıt",
            color = palette.accent,
            onClick = { onOpenService(SubServiceView.PRAYER_TRACKER) }
        )

        // 2. Günün Hikmeti, Âyet & Dualar (Günlük Maneviyat & Dua Mecmuası)
        ServiceCatalogItem(
            title = "Günün Hikmeti, Âyet & Dualar",
            description = "Günün âyet-i kerîmesi, hadîs-i şerîfi, esmâ zikri ve meâsir dualar mecmuası",
            icon = Icons.Default.AutoStories,
            badge = "Âyet & Hadis",
            color = palette.accent,
            onClick = { onOpenService(SubServiceView.WISDOM_DUAS) }
        )

        // 3. Ramazan, İmsakiye & Oruç Takibi
        ServiceCatalogItem(
            title = "Ramazan, İmsakiye & Oruç",
            description = "Canlı iftar ve sahur geri sayımı, 30 günlük imsakiye takvimi ve oruç çetelesi",
            icon = Icons.Default.Bedtime,
            badge = "Canlı Sayaç",
            color = palette.primary,
            onClick = { onOpenService(SubServiceView.RAMADAN) }
        )

        // 4. Esmâü'l-Hüsnâ (99 İsim)
        ServiceCatalogItem(
            title = "Esmâü'l-Hüsnâ (99 İsim)",
            description = "Allah'ın 99 ismi, manaları, tıklandığı gibi sesli okunuş ve zikir sayacı",
            icon = Icons.Default.Stars,
            badge = "Sesli Zikir",
            color = palette.secondary,
            onClick = { onOpenService(SubServiceView.ASMA_UL_HUSNA) }
        )

        // 5. Gelecek Dini Günler & Kandiller
        ServiceCatalogItem(
            title = "Gelecek Dini Günler & Kandiller",
            description = "Günümüz 2025/2026 takvimine göre güncellenmiş kandiller, Ramazan ve bayramlar",
            icon = Icons.Default.CalendarMonth,
            badge = "2025/2026",
            color = palette.accent,
            onClick = { onOpenService(SubServiceView.RELIGIOUS_DAYS) }
        )

        // 6. Yakın Camiler & Mescitler
        ServiceCatalogItem(
            title = "Yakın Camiler & Mescitler",
            description = "Bulunduğunuz konuma göre canlı mesafeler, tarihi camiler ve Google Haritalar yol tarifi",
            icon = Icons.Default.Mosque,
            badge = "Canlı GPS",
            color = palette.secondary,
            onClick = { onOpenService(SubServiceView.NEARBY_MOSQUES) }
        )

        // 7. Hatim Takibi & Mukabele
        ServiceCatalogItem(
            title = "Hatim Takibi & Mukabele",
            description = "30 Cüz mukabele çetelesi, cüz sahiplenme ve hatim duâ-i şerîfi",
            icon = Icons.Default.MenuBook,
            badge = "30 Cüz",
            color = palette.primary,
            onClick = { onOpenService(SubServiceView.HATIM) }
        )

        // 8. Dini Videolar & Sohbetler
        ServiceCatalogItem(
            title = "Dini Videolar & Sohbetler",
            description = "Genişletilmiş sohbetler, tefsir dersleri, siyer dersleri ve Kuran tilavetleri",
            icon = Icons.Default.VideoLibrary,
            badge = "Zengin Video",
            color = palette.primary,
            onClick = { onOpenService(SubServiceView.VIDEOS) }
        )

        // 9. İslâmî Duvar Kağıtları
        ServiceCatalogItem(
            title = "İslâmî Duvar Kağıtları",
            description = "Kâbe, Mescid-i Nebevi, hat sanatı HD duvar kağıtları. Tek dokunuşla arka plan yapma",
            icon = Icons.Default.Wallpaper,
            badge = "Duvar Kağıdı",
            color = palette.secondary,
            onClick = { onOpenService(SubServiceView.WALLPAPERS) }
        )

        // 10. Ana Ekran Widget'ları (Geniş 4x2 & Küçük 2x2)
        ServiceCatalogItem(
            title = "Ana Ekran Widget'ları (Geniş & Küçük)",
            description = "Canlı 4x2 geniş ve 2x2 küçük namaz vakti widget'ı, tema & motif seçimi ve tek tıkla ekleme",
            icon = Icons.Default.Widgets,
            badge = "4x2 & 2x2",
            color = palette.accent,
            onClick = { onOpenService(SubServiceView.SETTINGS) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ServiceCatalogItem(
    title: String,
    description: String,
    icon: ImageVector,
    badge: String,
    color: Color,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        softWrap = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = color,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = palette.textSecondary,
                        lineHeight = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = palette.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
