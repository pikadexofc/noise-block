package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Ultra-low-power Audio Engine.
 * Uses static looping AudioTracks to offload playback completely to the hardware mixer/DSP.
 */
class NoiseEngine(private val context: Context) {

  private val tag = "NoiseEngine"
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
  private var wakeLock: PowerManager.WakeLock? = null
  private var audioFocusRequest: AudioFocusRequest? = null

  private var activeTrack: AudioTrack? = null
  private var generationJob: Job? = null
  private var timerJob: Job? = null
  private var fadeJob: Job? = null

  // State flows
  private val _isPlaying = MutableStateFlow(false)
  val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

  private val _selectedNoise = MutableStateFlow(NoiseType.BROWN)
  val selectedNoise: StateFlow<NoiseType> = _selectedNoise.asStateFlow()

  private val _speakerMode = MutableStateFlow(SpeakerMode.PHONE_OPTIMIZED)
  val speakerMode: StateFlow<SpeakerMode> = _speakerMode.asStateFlow()

  private val _volume = MutableStateFlow(0.75f)
  val volume: StateFlow<Float> = _volume.asStateFlow()

  private val _toneWarmth = MutableStateFlow(0.45f)
  val toneWarmth: StateFlow<Float> = _toneWarmth.asStateFlow()

  // Sleep timer in seconds remaining (-1 = disabled / continuous)
  private val _timerSecondsRemaining = MutableStateFlow<Int>(-1)
  val timerSecondsRemaining: StateFlow<Int> = _timerSecondsRemaining.asStateFlow()

  private val _isLoadingBuffer = MutableStateFlow(false)
  val isLoadingBuffer: StateFlow<Boolean> = _isLoadingBuffer.asStateFlow()

  // Audio cache to prevent re-generating buffers when switching back and forth
  private val bufferCache = mutableMapOf<String, ShortArray>()

  init {
    try {
      wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NoiseBlock:AudioLock").apply {
        setReferenceCounted(false)
      }
    } catch (e: Exception) {
      Log.w(tag, "WakeLock init warning: ${e.message}")
    }
  }

  fun togglePlay() {
    if (_isPlaying.value) {
      pause()
    } else {
      play()
    }
  }

  fun play() {
    if (_isPlaying.value) return
    requestAudioFocus()
    acquireWakeLock()
    _isPlaying.value = true

    if (activeTrack != null && activeTrack?.state == AudioTrack.STATE_INITIALIZED) {
      try {
        activeTrack?.setVolume(_volume.value)
        activeTrack?.play()
      } catch (e: Exception) {
        Log.e(tag, "Error playing active track, rebuilding: ${e.message}")
        rebuildAndPlayTrack()
      }
    } else {
      rebuildAndPlayTrack()
    }
  }

  fun pause() {
    if (!_isPlaying.value) return
    _isPlaying.value = false
    try {
      activeTrack?.pause()
    } catch (e: Exception) {
      Log.e(tag, "Error pausing track: ${e.message}")
    }
    releaseWakeLock()
  }

  fun selectNoise(type: NoiseType) {
    if (_selectedNoise.value == type) return
    _selectedNoise.value = type
    if (_isPlaying.value) {
      rebuildAndPlayTrack()
    }
  }

  fun setSpeakerMode(mode: SpeakerMode) {
    if (_speakerMode.value == mode) return
    _speakerMode.value = mode
    if (_isPlaying.value) {
      rebuildAndPlayTrack()
    }
  }

  fun setToneWarmth(warmth: Float) {
    val clamped = warmth.coerceIn(0.0f, 1.0f)
    _toneWarmth.value = clamped
    if (_isPlaying.value) {
      // Debounce tone slider updates so we don't regenerate on every micro-touch
      generationJob?.cancel()
      generationJob = scope.launch {
        delay(120)
        rebuildAndPlayTrack()
      }
    }
  }

  fun setVolume(newVolume: Float) {
    val clamped = newVolume.coerceIn(0.0f, 1.0f)
    _volume.value = clamped
    try {
      activeTrack?.setVolume(clamped)
    } catch (e: Exception) {
      Log.e(tag, "Error setting volume: ${e.message}")
    }
  }

  fun setSleepTimerMinutes(minutes: Int) {
    timerJob?.cancel()
    if (minutes <= 0) {
      _timerSecondsRemaining.value = -1
      return
    }

    val totalSeconds = minutes * 60
    _timerSecondsRemaining.value = totalSeconds

    timerJob = scope.launch {
      var remaining = totalSeconds
      while (remaining > 0 && _isPlaying.value) {
        delay(1000)
        remaining--
        _timerSecondsRemaining.value = remaining

        // In the final 15 seconds, gently fade out volume
        if (remaining in 1..15) {
          val fadeRatio = remaining / 15.0f
          val fadedVol = _volume.value * fadeRatio
          try {
            activeTrack?.setVolume(fadedVol)
          } catch (e: Exception) {
            // ignore
          }
        }
      }

      if (remaining <= 0 && _isPlaying.value) {
        withContext(Dispatchers.Main) {
          pause()
          _timerSecondsRemaining.value = -1
          // Reset hardware volume back to user's setting
          try {
            activeTrack?.setVolume(_volume.value)
          } catch (e: Exception) {
            // ignore
          }
        }
      }
    }
  }

  private fun rebuildAndPlayTrack() {
    val noiseType = _selectedNoise.value
    val mode = _speakerMode.value
    val warmth = _toneWarmth.value
    val currentVol = _volume.value

    // Quantize warmth for cache key
    val warmthKey = (warmth * 10).toInt()
    val cacheKey = "${noiseType.id}_${mode.name}_$warmthKey"

    generationJob?.cancel()
    generationJob = scope.launch(Dispatchers.Default) {
      _isLoadingBuffer.value = true

      val pcmData = bufferCache.getOrPut(cacheKey) {
        NoiseSynthesizer.generateBuffer(noiseType, mode, warmth)
      }

      withContext(Dispatchers.Main) {
        try {
          val oldTrack = activeTrack
          // Create new AudioTrack in static mode
          val bufferSizeBytes = pcmData.size * 2
          val newTrack = AudioTrack.Builder()
            .setAudioAttributes(
              AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            )
            .setAudioFormat(
              AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(NoiseSynthesizer.SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            )
            .setBufferSizeInBytes(bufferSizeBytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

          // Write PCM samples and loop perpetually
          newTrack.write(pcmData, 0, pcmData.size)
          newTrack.setLoopPoints(0, pcmData.size, -1)
          newTrack.setVolume(currentVol)

          if (_isPlaying.value) {
            newTrack.play()
          }

          activeTrack = newTrack

          // Clean up old track safely
          oldTrack?.let { ot ->
            try {
              ot.stop()
              ot.release()
            } catch (e: Exception) {
              Log.w(tag, "Old track cleanup error: ${e.message}")
            }
          }
        } catch (e: Exception) {
          Log.e(tag, "Failed to create AudioTrack: ${e.message}", e)
        } finally {
          _isLoadingBuffer.value = false
        }
      }
    }
  }

  private fun requestAudioFocus() {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val playbackAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
          .setAudioAttributes(playbackAttributes)
          .setAcceptsDelayedFocusGain(true)
          .setOnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
              AudioManager.AUDIOFOCUS_LOSS -> pause()
              AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
              AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Duck volume down gently during map or call prompt
                try {
                  activeTrack?.setVolume(_volume.value * 0.3f)
                } catch (e: Exception) {
                  // ignore
                }
              }
              AudioManager.AUDIOFOCUS_GAIN -> {
                try {
                  activeTrack?.setVolume(_volume.value)
                } catch (e: Exception) {
                  // ignore
                }
              }
            }
          }
          .build()

        audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
      } else {
        @Suppress("DEPRECATION")
        audioManager.requestAudioFocus(
          { focusChange ->
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) pause()
          },
          AudioManager.STREAM_MUSIC,
          AudioManager.AUDIOFOCUS_GAIN
        )
      }
    } catch (e: Exception) {
      Log.w(tag, "Audio focus request failed: ${e.message}")
    }
  }

  private fun acquireWakeLock() {
    try {
      wakeLock?.let {
        if (!it.isHeld) {
          it.acquire(4 * 60 * 60 * 1000L) // 4 hours safety timeout
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "WakeLock acquire error: ${e.message}")
    }
  }

  private fun releaseWakeLock() {
    try {
      wakeLock?.let {
        if (it.isHeld) {
          it.release()
        }
      }
    } catch (e: Exception) {
      Log.w(tag, "WakeLock release error: ${e.message}")
    }
  }

  fun release() {
    timerJob?.cancel()
    generationJob?.cancel()
    fadeJob?.cancel()
    pause()
    try {
      activeTrack?.release()
      activeTrack = null
    } catch (e: Exception) {
      // ignore
    }
    bufferCache.clear()
  }
}
