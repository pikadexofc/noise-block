package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.NoiseType

@Composable
fun NoisePresetCard(
  noise: NoiseType,
  isSelected: Boolean,
  isPlaying: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor by animateColorAsState(
    targetValue = if (isSelected) noise.accentColor else Color.White.copy(alpha = 0.08f),
    animationSpec = tween(durationMillis = 200),
    label = "border_color"
  )

  val containerColor by animateColorAsState(
    targetValue = if (isSelected) {
      noise.accentColor.copy(alpha = 0.14f)
    } else {
      Color(0xFF131A26)
    },
    animationSpec = tween(durationMillis = 200),
    label = "container_color"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("preset_${noise.id}")
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = borderColor
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon badge
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(
            if (isSelected) noise.accentColor.copy(alpha = 0.28f)
            else Color.White.copy(alpha = 0.06f)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = noise.icon,
          contentDescription = "${noise.displayName} icon",
          tint = if (isSelected) noise.accentColor else Color(0xFF94A3B8),
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = noise.displayName,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
              fontSize = 15.sp
            ),
            color = if (isSelected) Color.White else Color(0xFFE2E8F0)
          )

          Spacer(modifier = Modifier.width(6.dp))

          // Tag badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(noise.accentColor.copy(alpha = if (isSelected) 0.22f else 0.12f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = noise.tag,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              ),
              color = noise.accentColor
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = noise.targetNoise,
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 11.5.sp,
            lineHeight = 14.sp
          ),
          color = if (isSelected) Color(0xFFCBD5E1) else Color(0xFF64748B),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
