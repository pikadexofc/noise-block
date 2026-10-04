package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.NoiseType
import com.example.audio.SpeakerMode
import com.example.ui.components.NoisePresetCard
import com.example.ui.components.NoiseVisualizer

@Composable
fun MainNoiseScreen(
  viewModel: NoiseViewModel,
  modifier: Modifier = Modifier
) {
  val isPlaying by viewModel.isPlaying.collectAsState()
  val selectedNoise by viewModel.selectedNoise.collectAsState()
  val speakerMode by viewModel.speakerMode.collectAsState()
  val volume by viewModel.volume.collectAsState()
  val toneWarmth by viewModel.toneWarmth.collectAsState()
  val timerSeconds by viewModel.timerSecondsRemaining.collectAsState()
  val activeTimerSetting by viewModel.activeTimerSetting.collectAsState()
  val isLoadingBuffer by viewModel.isLoadingBuffer.collectAsState()
  val isEcoOled by viewModel.isEcoOled.collectAsState()

  var showInfoDialog by remember { mutableStateOf(false) }

  val backgroundColor = if (isEcoOled) Color(0xFF06090E) else Color(0xFF0F172A)
  val surfaceCardColor = if (isEcoOled) Color(0xFF0D121B) else Color(0xFF1E293B)

  Surface(
    modifier = modifier.fillMaxSize(),
    color = backgroundColor
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
        .navigationBarsPadding()
        .padding(horizontal = 18.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Top Header Bar
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "NoiseBlock",
                style = MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 24.sp
                ),
                color = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFF10B981).copy(alpha = 0.16f))
                  .padding(horizontal = 7.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "LOW POWER",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF10B981)
                  )
                }
              }
            }
            Text(
              text = "Pure acoustic outside noise canceller",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
              color = Color(0xFF94A3B8)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Eco OLED theme toggle
            IconButton(
              onClick = { viewModel.toggleEcoOled() },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .testTag("eco_toggle_button")
            ) {
              Icon(
                imageVector = if (isEcoOled) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = "Toggle OLED Eco Mode",
                tint = if (isEcoOled) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Info guide button
            IconButton(
              onClick = { showInfoDialog = true },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .testTag("info_button")
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Noise cancellation guide",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      // 2. Central Acoustic Visualizer & Main Play/Pause Button
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          NoiseVisualizer(
            isPlaying = isPlaying,
            accentColor = selectedNoise.accentColor,
            isEcoMode = isEcoOled,
            visualizerSize = 184.dp
          ) {
            // Master Play / Pause Button
            Box(
              modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(
                  if (isPlaying) selectedNoise.accentColor else Color(0xFF1E293B)
                )
                .border(
                  width = 2.dp,
                  color = if (isPlaying) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                  shape = CircleShape
                )
                .clickable { viewModel.togglePlay() }
                .testTag("play_pause_button"),
              contentAlignment = Alignment.Center
            ) {
              if (isLoadingBuffer) {
                CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(34.dp),
                  strokeWidth = 3.dp
                )
              } else {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isPlaying) "Pause" else "Play",
                  tint = if (isPlaying) Color(0xFF090D16) else Color.White,
                  modifier = Modifier.size(46.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Currently playing noise label
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(selectedNoise.accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = selectedNoise.displayName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
              ),
              color = Color.White
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (isPlaying) "Active Masking • ${selectedNoise.tag}" else "Tap circle to start masking",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
            color = if (isPlaying) selectedNoise.accentColor else Color(0xFF64748B)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Quick speaker mode pill toggle
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color.White.copy(alpha = 0.07f))
              .clickable { viewModel.toggleSpeakerMode() }
              .padding(horizontal = 14.dp, vertical = 7.dp)
              .testTag("speaker_mode_pill")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (speakerMode == SpeakerMode.PHONE_OPTIMIZED) Icons.Default.Speaker else Icons.Default.Headphones,
                contentDescription = null,
                tint = if (speakerMode == SpeakerMode.PHONE_OPTIMIZED) Color(0xFF38BDF8) else Color(0xFFA78BFA),
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = speakerMode.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Medium,
                  fontSize = 12.sp
                ),
                color = Color(0xFFE2E8F0)
              )
            }
          }
        }
      }

      // 3. Audio Tuning Controls Card (Volume, Warmth/Tone, Speaker Optimization)
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(surfaceCardColor)
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .padding(16.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Tune,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Acoustic Tuning",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = Color.White
                )
              }

              Text(
                text = "${(volume * 100).toInt()}% Vol",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF94A3B8)
              )
            }

            // Volume Slider
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.VolumeDown,
                  contentDescription = null,
                  tint = Color(0xFF64748B),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                  value = volume,
                  onValueChange = { viewModel.setVolume(it) },
                  valueRange = 0.0f..1.0f,
                  modifier = Modifier
                    .weight(1f)
                    .testTag("volume_slider"),
                  colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = selectedNoise.accentColor,
                    inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = null,
                  tint = Color(0xFF64748B),
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            // Tone / Warmth Slider
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Tone Color",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF94A3B8)
                )
                Text(
                  text = if (toneWarmth < 0.35f) "Deep & Warm" else if (toneWarmth > 0.65f) "Bright & Crisp" else "Balanced",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                  color = selectedNoise.accentColor
                )
              }
              Slider(
                value = toneWarmth,
                onValueChange = { viewModel.setToneWarmth(it) },
                valueRange = 0.0f..1.0f,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("tone_slider"),
                colors = SliderDefaults.colors(
                  thumbColor = selectedNoise.accentColor,
                  activeTrackColor = selectedNoise.accentColor.copy(alpha = 0.6f),
                  inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                )
              )
            }

            // Speaker Hardware Optimization Toggle
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Mobile Speaker Clarity Boost",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                  color = Color.White
                )
                Text(
                  text = "Cuts battery-draining <180Hz sub-bass; amplifies vocal masking clarity on phone/laptop speakers.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                  color = Color(0xFF94A3B8)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Switch(
                checked = speakerMode == SpeakerMode.PHONE_OPTIMIZED,
                onCheckedChange = { checked ->
                  viewModel.setSpeakerMode(if (checked) SpeakerMode.PHONE_OPTIMIZED else SpeakerMode.FULL_RANGE)
                },
                modifier = Modifier.testTag("speaker_mode_switch"),
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = Color(0xFF0284C7),
                  uncheckedThumbColor = Color(0xFF94A3B8),
                  uncheckedTrackColor = Color(0xFF334155)
                )
              )
            }
          }
        }
      }

      // 4. Sleep & Focus Timer Section
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(surfaceCardColor)
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .padding(16.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Timer,
                  contentDescription = null,
                  tint = Color(0xFFA78BFA),
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Sleep & Focus Timer",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = Color.White
                )
              }

              viewModel.formattedRemainingTimer()?.let { countdown ->
                Text(
                  text = "$countdown left",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                  )
                )
              }
            }

            // Quick Timer Options
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val timerOptions = listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "1h", 90 to "1.5h")
              timerOptions.forEach { (minutes, label) ->
                val isSelected = activeTimerSetting == minutes
                FilterChip(
                  selected = isSelected,
                  onClick = { viewModel.setTimerMinutes(minutes) },
                  label = {
                    Text(
                      text = label,
                      fontSize = 11.5.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("timer_chip_$minutes"),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFA78BFA).copy(alpha = 0.25f),
                    selectedLabelColor = Color(0xFFA78BFA),
                    containerColor = Color.White.copy(alpha = 0.05f),
                    labelColor = Color(0xFF94A3B8)
                  ),
                  border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.White.copy(alpha = 0.1f),
                    selectedBorderColor = Color(0xFFA78BFA)
                  )
                )
              }
            }

            AnimatedVisibility(
              visible = timerSeconds > 0,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Text(
                text = "Audio will gently fade out over the final 15 seconds before sleeping.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Color(0xFF94A3B8)
              )
            }
          }
        }
      }

      // 5. Sound Presets List
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Masking Profiles (8)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 16.sp
            ),
            color = Color.White
          )
          Text(
            text = "Select the tone that cancels your specific outside noise.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = Color(0xFF94A3B8)
          )
        }
      }

      items(NoiseType.values().size) { index ->
        val noise = NoiseType.values()[index]
        val isSelected = noise == selectedNoise
        NoisePresetCard(
          noise = noise,
          isSelected = isSelected,
          isPlaying = isPlaying,
          onClick = { viewModel.selectNoise(noise) }
        )
      }

      // 6. Power & Offline Efficiency Footer Card
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .padding(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.BatteryChargingFull,
              contentDescription = null,
              tint = Color(0xFF10B981),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Ultra-Low Power Synthesis",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.White
              )
              Text(
                text = "100% offline local DSP looping • 0% CPU streaming • Works seamlessly with screen locked.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                color = Color(0xFF64748B)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(18.dp))
      }
    }
  }

  // Info Guide Dialog
  if (showInfoDialog) {
    AlertDialog(
      onDismissRequest = { showInfoDialog = false },
      containerColor = Color(0xFF1E293B),
      titleContentColor = Color.White,
      textContentColor = Color(0xFFCBD5E1),
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Noise Cancellation Guide")
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "How to block outside sounds:",
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            fontSize = 14.sp
          )
          Text(
            text = "• Brown Noise: Heaviest low rumble. Best for heavy road traffic, rumbling air conditioners, vibrating walls, and loud bass music.",
            fontSize = 12.5.sp
          )
          Text(
            text = "• Pink & Rain: Balanced 1/f falloff. Natural sound of steady rain, ideal for office chatter and keyboard typing.",
            fontSize = 12.5.sp
          )
          Text(
            text = "• White Noise: Full spectrum. Best for screeching noises, dog barks, and loud voices.",
            fontSize = 12.5.sp
          )
          Text(
            text = "• Mobile Speaker Boost: Phone and laptop speakers cannot reproduce <180Hz bass. Enabling this filters out useless sub-bass to save battery and amplifies mid-band vocal masking frequencies so it sounds loud and crystal clear on portable speakers.",
            fontSize = 12.5.sp
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = { showInfoDialog = false },
          modifier = Modifier.testTag("dialog_close_button")
        ) {
          Text("Got It", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
        }
      }
    )
  }
}
