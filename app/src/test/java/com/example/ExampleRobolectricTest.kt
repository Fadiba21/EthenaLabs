package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("AI Crypto Signal Trader", appName)
  }

  @Test
  fun `test telegram signal parsing`() {
    val signalText = """
      BTC LONG
      Entry: 62000
      TP: 63000, 64000
      SL: 61000
      Leverage: 20x
    """.trimIndent()
    val parsed = com.example.data.telegram.TelegramSignalParser.parse(signalText)
    org.junit.Assert.assertNotNull(parsed)
    assertEquals("BTCUSDT", parsed?.symbol)
    assertEquals("LONG", parsed?.side)
    assertEquals(62000.0, parsed?.entryPrice ?: 0.0, 0.01)
    assertEquals(20, parsed?.leverage)
  }
}
