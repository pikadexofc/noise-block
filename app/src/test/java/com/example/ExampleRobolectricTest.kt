package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.NoiseSynthesizer
import com.example.audio.NoiseType
import com.example.audio.SpeakerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NoiseBlock", appName)
  }

  @Test
  fun `generate brown noise buffer with phone speaker optimization`() {
    val pcmData = NoiseSynthesizer.generateBuffer(
      type = NoiseType.BROWN,
      speakerMode = SpeakerMode.PHONE_OPTIMIZED,
      toneWarmth = 0.5f
    )
    assertNotNull(pcmData)
    assertEquals(NoiseSynthesizer.TOTAL_SAMPLES, pcmData.size)

    // Verify buffer has non-zero amplitude
    var nonZeroCount = 0
    for (sample in pcmData) {
      if (sample != 0.toShort()) nonZeroCount++
    }
    assertTrue("Buffer must contain synthesized audio samples", nonZeroCount > 1000)
  }

  @Test
  fun `generate all noise profiles successfully`() {
    for (type in NoiseType.values()) {
      val buffer = NoiseSynthesizer.generateBuffer(
        type = type,
        speakerMode = SpeakerMode.FULL_RANGE,
        toneWarmth = 0.5f
      )
      assertEquals(NoiseSynthesizer.TOTAL_SAMPLES, buffer.size)
    }
  }
}
