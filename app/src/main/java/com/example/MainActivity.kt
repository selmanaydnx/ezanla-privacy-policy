package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainScreen
import com.example.ui.PrayerViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val prayerViewModel: PrayerViewModel = viewModel()
      val uiState by prayerViewModel.uiState.collectAsStateWithLifecycle()

      val currentDensity = LocalDensity.current
      val effectiveFontScale = if (uiState.isLargeFontEnabled) currentDensity.fontScale * 1.25f else currentDensity.fontScale

      CompositionLocalProvider(
        LocalDensity provides Density(
          density = currentDensity.density,
          fontScale = effectiveFontScale
        )
      ) {
        MyApplicationTheme(
          selectedTheme = uiState.selectedTheme,
          darkModePreference = uiState.darkModePreference,
          isEyeComfortEnabled = uiState.isEyeComfortEnabled
        ) {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
          ) {
            MainScreen(viewModel = prayerViewModel)
          }
        }
      }
    }
  }

  override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
    if (event.action == android.view.KeyEvent.ACTION_DOWN) {
      val keyCode = event.keyCode
      if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP ||
          keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN ||
          keyCode == android.view.KeyEvent.KEYCODE_VOLUME_MUTE ||
          keyCode == android.view.KeyEvent.KEYCODE_POWER
      ) {
        if (com.example.audio.AdhanAudioPlayer.isPlaying) {
          com.example.audio.AdhanAudioPlayer.stop()
          com.example.alarm.PrayerNotificationHelper.dismissEzanNotification(this)
          return true
        }
      }
    }
    return super.dispatchKeyEvent(event)
  }

  override fun onStop() {
    super.onStop()
    // Kullanıcı Talebi: "ezan okunurken telefon kapatılırsa yada uygulama dan çıkılırsa yine işlemleri kapatıcak şekilde revize et."
    com.example.audio.AdhanAudioPlayer.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    com.example.audio.AdhanAudioPlayer.stop()
  }
}

