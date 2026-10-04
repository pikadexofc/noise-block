package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NoiseVisualizer(
  isPlaying: Boolean,
  accentColor: Color,
  modifier: Modifier = Modifier,
  visualizerSize: Dp = 190.dp,
  isEcoMode: Boolean = true,
  content: @Composable () -> Unit
) {
  val transition = rememberInfiniteTransition(label = "pulse_transition")

  val pulseProgress by transition.animateFloat(
    initialValue = 0.0f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulse_progress"
  )

  val breathingScale by transition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathing_scale"
  )

  Box(
    modifier = modifier.size(visualizerSize),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val maxRadius = size.width / 2f
      val innerCoreRadius = maxRadius * 0.58f

      if (isPlaying) {
        // Draw soft expanding concentric acoustic waves
        val waveCount = 2
        for (i in 0 until waveCount) {
          val waveOffset = (pulseProgress + (i.toFloat() / waveCount)) % 1.0f
          val waveRadius = innerCoreRadius + (maxRadius - innerCoreRadius) * waveOffset
          val alpha = (1.0f - waveOffset).coerceIn(0.0f, 1.0f) * 0.35f

          drawCircle(
            color = accentColor.copy(alpha = alpha),
            radius = waveRadius,
            center = center,
            style = Stroke(width = if (isEcoMode) 2.5f else 4f)
          )
        }

        // Inner glowing ambient halo
        val ambientRadius = innerCoreRadius * breathingScale
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(
              accentColor.copy(alpha = 0.22f),
              accentColor.copy(alpha = 0.06f),
              Color.Transparent
            ),
            center = center,
            radius = ambientRadius * 1.35f
          ),
          radius = ambientRadius * 1.35f,
          center = center
        )
      } else {
        // Idle gentle guide ring
        drawCircle(
          color = Color.White.copy(alpha = 0.08f),
          radius = innerCoreRadius,
          center = center,
          style = Stroke(width = 2.0f)
        )
      }
    }

    content()
  }
}
