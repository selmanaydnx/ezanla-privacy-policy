package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calc.PrayerCalculationEngine
import com.example.data.CityDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Ezanla", appName)
  }

  @Test
  fun `prayer calculation returns six times`() {
    val istanbul = CityDatabase.defaultCity
    val times = PrayerCalculationEngine.calculateTimesForDate(2026, 9, 8, istanbul)
    assertEquals(6, times.size)
  }

  @Test
  fun `qibla bearing for istanbul is approximately 152 degrees`() {
    val istanbul = CityDatabase.defaultCity
    val bearing = PrayerCalculationEngine.calculateQiblaBearing(istanbul.latitude, istanbul.longitude)
    assertTrue("Bearing should be near 152 deg", bearing in 148.0..156.0)
  }
}

