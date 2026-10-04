package com.example.audio

import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure Kotlin mathematical acoustic synthesizer.
 * Generates continuous, non-repeating psychoacoustic waveforms with seamless loop boundaries.
 * Zero CPU overhead during playback when loaded into an AudioTrack static buffer.
 */
object NoiseSynthesizer {

  const val SAMPLE_RATE = 44100
  // 3.5 seconds of PCM mono data is ~154k samples (308 KB in RAM) - perfect loop length
  const val LOOP_DURATION_SECONDS = 3.5
  val TOTAL_SAMPLES = (SAMPLE_RATE * LOOP_DURATION_SECONDS).toInt()
  private const val CROSSFADE_SAMPLES = 2048

  fun generateBuffer(
    type: NoiseType,
    speakerMode: SpeakerMode,
    toneWarmth: Float // 0f = very dark/deep, 0.5f = balanced, 1f = bright
  ): ShortArray {
    val random = Random(42L) // Consistent pseudo-random seed for fast reproducible generation
    val rawSamples = FloatArray(TOTAL_SAMPLES)

    when (type) {
      NoiseType.WHITE -> generateWhiteNoise(rawSamples, random)
      NoiseType.PINK -> generatePinkNoise(rawSamples, random)
      NoiseType.BROWN -> generateBrownNoise(rawSamples, random)
      NoiseType.GREEN -> generateGreenNoise(rawSamples, random)
      NoiseType.BOX_FAN -> generateBoxFanNoise(rawSamples, random)
      NoiseType.RAIN -> generateRainNoise(rawSamples, random)
      NoiseType.OCEAN -> generateOceanNoise(rawSamples, random)
      NoiseType.CABIN -> generateCabinNoise(rawSamples, random)
    }

    // Apply tone warmth filter (low-pass filter with cutoff dependent on slider)
    // 0f -> ~700 Hz cutoff (dark), 0.5f -> ~3800 Hz, 1f -> ~16000 Hz (bright)
    val cutoffHz = 700.0 + (15300.0 * toneWarmth.toDouble().pow(1.8))
    applyLowPassFilter(rawSamples, cutoffHz)

    // Apply speaker optimization profile
    if (speakerMode == SpeakerMode.PHONE_OPTIMIZED) {
      // 1. High-pass filter at 180 Hz to eliminate cone bottoming out and battery heat
      applyHighPassFilter(rawSamples, 180.0)
      // 2. Presence contour boost in 800 Hz - 2.8 kHz speech masking band
      applyPeakingFilter(rawSamples, centerFreq = 1600.0, gainDb = 3.5, q = 1.0)
    }

    // Apply seamless boundary crossfade to eliminate any click on loop restart
    applySeamlessCrossfade(rawSamples, CROSSFADE_SAMPLES)

    // Peak normalize to -1.5 dB (0.84) to eliminate clipping
    normalize(rawSamples, targetPeak = 0.85f)

    // Convert to 16-bit PCM shorts
    val pcmShorts = ShortArray(TOTAL_SAMPLES)
    for (i in 0 until TOTAL_SAMPLES) {
      val clamped = max(-1.0f, min(1.0f, rawSamples[i]))
      pcmShorts[i] = (clamped * 32767.0f).toInt().toShort()
    }

    return pcmShorts
  }

  private fun generateWhiteNoise(buffer: FloatArray, random: Random) {
    for (i in buffer.indices) {
      buffer[i] = (random.nextFloat() * 2.0f - 1.0f)
    }
  }

  private fun generatePinkNoise(buffer: FloatArray, random: Random) {
    // Paul Kellet's refined 6-pole pinking filter
    var b0 = 0.0f
    var b1 = 0.0f
    var b2 = 0.0f
    var b3 = 0.0f
    var b4 = 0.0f
    var b5 = 0.0f
    var b6 = 0.0f

    for (i in buffer.indices) {
      val white = random.nextFloat() * 2.0f - 1.0f
      b0 = 0.99886f * b0 + white * 0.0555179f
      b1 = 0.99332f * b1 + white * 0.0750759f
      b2 = 0.96900f * b2 + white * 0.1538520f
      b3 = 0.86650f * b3 + white * 0.3104856f
      b4 = 0.55000f * b4 + white * 0.5329522f
      b5 = -0.7616f * b5 - white * 0.0168980f
      val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f
      b6 = white * 0.115926f
      buffer[i] = pink * 0.11f
    }
  }

  private fun generateBrownNoise(buffer: FloatArray, random: Random) {
    // Leaky brownian walk for deep sub-bass and waterfall cascade
    var brown = 0.0f
    for (i in buffer.indices) {
      val white = random.nextFloat() * 2.0f - 1.0f
      brown = (brown + (0.045f * white)) / 1.035f
      buffer[i] = brown * 3.8f
    }
  }

  private fun generateGreenNoise(buffer: FloatArray, random: Random) {
    // Centered around 500 Hz (350 Hz to 900 Hz bandpass)
    generatePinkNoise(buffer, random)
    applyBandPassFilter(buffer, centerFreq = 520.0, bandwidthHz = 500.0)
  }

  private fun generateBoxFanNoise(buffer: FloatArray, random: Random) {
    var brown = 0.0f
    val flutterFreq = 12.0 // 12 Hz blade passage rate
    val humFreq = 118.0    // ~120 Hz electric motor fundamental
    val twoPi = 2.0 * PI

    for (i in buffer.indices) {
      val t = i.toDouble() / SAMPLE_RATE
      val white = random.nextFloat() * 2.0f - 1.0f
      brown = (brown + (0.04f * white)) / 1.035f

      // Subtle aerodynamic flutter
      val flutter = 1.0f + 0.14f * sin(twoPi * flutterFreq * t).toFloat()
      val hum = 0.07f * sin(twoPi * humFreq * t).toFloat()

      buffer[i] = (brown * 3.2f * flutter) + hum
    }
  }

  private fun generateRainNoise(buffer: FloatArray, random: Random) {
    // Soft pink noise bed + Poisson droplet impacts
    generatePinkNoise(buffer, random)
    for (i in buffer.indices) {
      buffer[i] *= 0.72f
      if (random.nextFloat() < 0.007f) {
        // High frequency droplet burst
        val droplet = (random.nextFloat() * 2.0f - 1.0f) * 0.45f
        buffer[i] += droplet
      }
    }
  }

  private fun generateOceanNoise(buffer: FloatArray, random: Random) {
    generatePinkNoise(buffer, random)
    val twoPi = 2.0 * PI
    // Smooth repeating swell cycles across the buffer duration
    val cycles = 1.0
    val period = TOTAL_SAMPLES.toDouble() / cycles

    for (i in buffer.indices) {
      val phase = (i.toDouble() % period) / period
      // Smooth sinusoidal surge and wash back
      val swell = ((1.0 - cos(twoPi * phase)) * 0.5).pow(1.6).toFloat()
      val baseBed = 0.28f
      buffer[i] *= (baseBed + (1.0f - baseBed) * swell)
    }
  }

  private fun generateCabinNoise(buffer: FloatArray, random: Random) {
    var brown = 0.0f
    var pinkB0 = 0.0f
    var pinkB1 = 0.0f
    val twoPi = 2.0 * PI

    for (i in buffer.indices) {
      val t = i.toDouble() / SAMPLE_RATE
      val white = random.nextFloat() * 2.0f - 1.0f

      // Low rumble
      brown = (brown + (0.05f * white)) / 1.04f

      // Mid air rush
      pinkB0 = 0.99f * pinkB0 + white * 0.08f
      pinkB1 = 0.95f * pinkB1 + white * 0.12f
      val airRush = (pinkB0 + pinkB1) * 0.15f

      // Jet engine turbine dual drone
      val jetTone1 = 0.05f * sin(twoPi * 92.0 * t).toFloat()
      val jetTone2 = 0.025f * sin(twoPi * 184.0 * t).toFloat()

      buffer[i] = (brown * 2.6f) + (airRush * 0.7f) + jetTone1 + jetTone2
    }
  }

  // Single-pole IIR low-pass filter
  private fun applyLowPassFilter(buffer: FloatArray, cutoffHz: Double) {
    val dt = 1.0 / SAMPLE_RATE
    val rc = 1.0 / (2.0 * PI * cutoffHz)
    val alpha = (dt / (rc + dt)).toFloat()

    var prev = buffer[0]
    for (i in buffer.indices) {
      prev += alpha * (buffer[i] - prev)
      buffer[i] = prev
    }
  }

  // Single-pole IIR high-pass filter
  private fun applyHighPassFilter(buffer: FloatArray, cutoffHz: Double) {
    val dt = 1.0 / SAMPLE_RATE
    val rc = 1.0 / (2.0 * PI * cutoffHz)
    val alpha = (rc / (rc + dt)).toFloat()

    var prevOut = 0.0f
    var prevIn = buffer[0]
    for (i in buffer.indices) {
      val inSample = buffer[i]
      prevOut = alpha * (prevOut + inSample - prevIn)
      prevIn = inSample
      buffer[i] = prevOut
    }
  }

  // 2nd-order Biquad Bandpass Filter
  private fun applyBandPassFilter(buffer: FloatArray, centerFreq: Double, bandwidthHz: Double) {
    val w0 = 2.0 * PI * centerFreq / SAMPLE_RATE
    val q = centerFreq / bandwidthHz
    val alpha = sin(w0) / (2.0 * q)

    val b0 = alpha
    val b1 = 0.0
    val b2 = -alpha
    val a0 = 1.0 + alpha
    val a1 = -2.0 * cos(w0)
    val a2 = 1.0 - alpha

    var x1 = 0.0; var x2 = 0.0
    var y1 = 0.0; var y2 = 0.0

    for (i in buffer.indices) {
      val x0 = buffer[i].toDouble()
      val y0 = (b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2) / a0
      x2 = x1; x1 = x0
      y2 = y1; y1 = y0
      buffer[i] = y0.toFloat()
    }
  }

  // 2nd-order Peaking EQ Filter
  private fun applyPeakingFilter(buffer: FloatArray, centerFreq: Double, gainDb: Double, q: Double) {
    val a = 10.0.pow(gainDb / 40.0)
    val w0 = 2.0 * PI * centerFreq / SAMPLE_RATE
    val alpha = sin(w0) / (2.0 * q)

    val b0 = 1.0 + alpha * a
    val b1 = -2.0 * cos(w0)
    val b2 = 1.0 - alpha * a
    val a0 = 1.0 + alpha / a
    val a1 = -2.0 * cos(w0)
    val a2 = 1.0 - alpha / a

    var x1 = 0.0; var x2 = 0.0
    var y1 = 0.0; var y2 = 0.0

    for (i in buffer.indices) {
      val x0 = buffer[i].toDouble()
      val y0 = (b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2) / a0
      x2 = x1; x1 = x0
      y2 = y1; y1 = y0
      buffer[i] = y0.toFloat()
    }
  }

  // Equal-power crossfade between the end and the beginning of the buffer
  private fun applySeamlessCrossfade(buffer: FloatArray, crossfadeLength: Int) {
    val n = buffer.size
    val halfPi = PI / 2.0
    for (i in 0 until crossfadeLength) {
      val progress = i.toDouble() / crossfadeLength
      val fadeInWeight = sin(progress * halfPi).toFloat()
      val fadeOutWeight = cos(progress * halfPi).toFloat()

      val tailIndex = n - crossfadeLength + i
      buffer[tailIndex] = buffer[tailIndex] * fadeOutWeight + buffer[i] * fadeInWeight
    }
  }

  private fun normalize(buffer: FloatArray, targetPeak: Float) {
    var maxVal = 0.0001f
    for (sample in buffer) {
      val abs = kotlin.math.abs(sample)
      if (abs > maxVal) maxVal = abs
    }
    val multiplier = targetPeak / maxVal
    for (i in buffer.indices) {
      buffer[i] *= multiplier
    }
  }
}
