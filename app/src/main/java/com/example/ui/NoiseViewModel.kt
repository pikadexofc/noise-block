package com.example.ui

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.NoiseEngine
import com.example.audio.NoisePlaybackService
import com.example.audio.NoiseType
import com.example.audio.SpeakerMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoiseViewModel(application: Application) : AndroidViewModel(application) {

  private val engine: NoiseEngine = NoisePlaybackService.getEngine(application)

  val isPlaying: StateFlow<Boolean> = engine.isPlaying
  val selectedNoise: StateFlow<NoiseType> = engine.selectedNoise
  val speakerMode: StateFlow<SpeakerMode> = engine.speakerMode
  val volume: StateFlow<Float> = engine.volume
  val toneWarmth: StateFlow<Float> = engine.toneWarmth
  val timerSecondsRemaining: StateFlow<Int> = engine.timerSecondsRemaining
  val isLoadingBuffer: StateFlow<Boolean> = engine.isLoadingBuffer

  // AMOLED True Black Eco Mode for maximum screen power savings
  private val _isEcoOled = MutableStateFlow(true)
  val isEcoOled: StateFlow<Boolean> = _isEcoOled.asStateFlow()

  // Selected quick timer minutes (0 = continuous)
  private val _activeTimerSetting = MutableStateFlow(0)
  val activeTimerSetting: StateFlow<Int> = _activeTimerSetting.asStateFlow()

  fun togglePlay() {
    val willPlay = !isPlaying.value
    if (willPlay) {
      startServiceIfNeeded()
    }
    engine.togglePlay()
  }

  fun selectNoise(type: NoiseType) {
    engine.selectNoise(type)
  }

  fun setSpeakerMode(mode: SpeakerMode) {
    engine.setSpeakerMode(mode)
  }

  fun toggleSpeakerMode() {
    val nextMode = if (speakerMode.value == SpeakerMode.PHONE_OPTIMIZED) {
      SpeakerMode.FULL_RANGE
    } else {
      SpeakerMode.PHONE_OPTIMIZED
    }
    engine.setSpeakerMode(nextMode)
  }

  fun setVolume(vol: Float) {
    engine.setVolume(vol)
  }

  fun setToneWarmth(warmth: Float) {
    engine.setToneWarmth(warmth)
  }

  fun setTimerMinutes(minutes: Int) {
    _activeTimerSetting.value = minutes
    engine.setSleepTimerMinutes(minutes)
  }

  fun toggleEcoOled() {
    _isEcoOled.value = !_isEcoOled.value
  }

  private fun startServiceIfNeeded() {
    try {
      val intent = Intent(getApplication(), NoisePlaybackService::class.java).apply {
        action = NoisePlaybackService.ACTION_PLAY
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getApplication<Application>().startForegroundService(intent)
      } else {
        getApplication<Application>().startService(intent)
      }
    } catch (e: Exception) {
      // In case background execution is delayed or restricted, engine continues playing locally
    }
  }

  fun formattedRemainingTimer(): String? {
    val sec = timerSecondsRemaining.value
    if (sec <= 0) return null
    val m = sec / 60
    val s = sec % 60
    return String.format("%02d:%02d", m, s)
  }
}
