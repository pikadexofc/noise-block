package com.example.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class NoisePlaybackService : Service() {

  companion object {
    const val CHANNEL_ID = "noiseblock_playback_channel"
    const val NOTIFICATION_ID = 1001

    const val ACTION_PLAY = "com.example.noiseblock.ACTION_PLAY"
    const val ACTION_PAUSE = "com.example.noiseblock.ACTION_PAUSE"
    const val ACTION_TOGGLE = "com.example.noiseblock.ACTION_TOGGLE"
    const val ACTION_STOP = "com.example.noiseblock.ACTION_STOP"

    private var sharedEngine: NoiseEngine? = null

    fun getEngine(context: Context): NoiseEngine {
      if (sharedEngine == null) {
        sharedEngine = NoiseEngine(context.applicationContext)
      }
      return sharedEngine!!
    }
  }

  inner class LocalBinder : Binder() {
    fun getService(): NoisePlaybackService = this@NoisePlaybackService
    fun getEngine(): NoiseEngine = sharedEngine ?: NoiseEngine(applicationContext).also { sharedEngine = it }
  }

  private val binder = LocalBinder()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private lateinit var engine: NoiseEngine

  override fun onCreate() {
    super.onCreate()
    engine = getEngine(applicationContext)
    createNotificationChannel()

    // Observe playing state to update foreground notification
    scope.launch {
      engine.isPlaying.collect { isPlaying ->
        if (isPlaying) {
          startInForeground()
        } else {
          updateNotification()
        }
      }
    }

    scope.launch {
      engine.selectedNoise.collect {
        if (engine.isPlaying.value) {
          updateNotification()
        }
      }
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_PLAY -> engine.play()
      ACTION_PAUSE -> engine.pause()
      ACTION_TOGGLE -> engine.togglePlay()
      ACTION_STOP -> {
        engine.pause()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      }
    }
    return START_STICKY
  }

  override fun onBind(intent: Intent?): IBinder = binder

  private fun startInForeground() {
    val notification = buildNotification()
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        startForeground(
          NOTIFICATION_ID,
          notification,
          ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
      } else {
        startForeground(NOTIFICATION_ID, notification)
      }
    } catch (e: Exception) {
      // Graceful fallback if background start permissions are restricted
    }
  }

  private fun updateNotification() {
    try {
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.notify(NOTIFICATION_ID, buildNotification())
    } catch (e: Exception) {
      // ignore
    }
  }

  private fun buildNotification(): Notification {
    val contentIntent = PendingIntent.getActivity(
      this,
      0,
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
      },
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val isPlaying = engine.isPlaying.value
    val currentNoise = engine.selectedNoise.value
    val speakerMode = engine.speakerMode.value

    val toggleIntent = PendingIntent.getService(
      this,
      1,
      Intent(this, NoisePlaybackService::class.java).apply { action = ACTION_TOGGLE },
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val stopIntent = PendingIntent.getService(
      this,
      2,
      Intent(this, NoisePlaybackService::class.java).apply { action = ACTION_STOP },
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val toggleActionTitle = if (isPlaying) "Pause" else "Play"

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("NoiseBlock: ${currentNoise.displayName}")
      .setContentText("${speakerMode.shortLabel} • Low-power masking active")
      .setSmallIcon(android.R.drawable.ic_media_play)
      .setContentIntent(contentIntent)
      .setOngoing(isPlaying)
      .setSilent(true)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .addAction(
        if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
        toggleActionTitle,
        toggleIntent
      )
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
      .build()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Acoustic Noise Playback",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Displays playback controls and battery status for NoiseBlock"
        setShowBadge(false)
        enableVibration(false)
        setSound(null, null)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }
}
