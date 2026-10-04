package com.example.audio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Surfing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Noise profiles engineered for outside noise cancellation and cognitive calm.
 */
enum class NoiseType(
  val id: String,
  val displayName: String,
  val tag: String,
  val description: String,
  val targetNoise: String,
  val accentColor: Color,
  val icon: ImageVector
) {
  BROWN(
    id = "brown",
    displayName = "Brown Noise",
    tag = "Deep Rumble",
    description = "Rich, heavy low-frequency cascade reminiscent of a distant waterfall.",
    targetNoise = "Blocks traffic rumble, construction & talking through walls",
    accentColor = Color(0xFFD97706), // Warm amber/brown
    icon = Icons.Default.GraphicEq
  ),
  PINK(
    id = "pink",
    displayName = "Pink Noise",
    tag = "Balanced 1/f",
    description = "Equal energy per octave. Smooth, organic, and deeply calming.",
    targetNoise = "Masks office chatter, keyboard clicks & steady background noise",
    accentColor = Color(0xFFF472B6), // Soft pink
    icon = Icons.Default.Waves
  ),
  WHITE(
    id = "white",
    displayName = "White Noise",
    tag = "Full Spectrum",
    description = "Equal energy across all audible frequencies like gentle TV static.",
    targetNoise = "Cancels sharp sudden sounds, squeaks & barking dogs",
    accentColor = Color(0xFF38BDF8), // Crisp cyan
    icon = Icons.Default.Air
  ),
  GREEN(
    id = "green",
    displayName = "Green Noise",
    tag = "Nature Mid-Band",
    description = "Centered around 500 Hz. Ambient breeze and tranquil woodland presence.",
    targetNoise = "Ideal for study, focused reading & soothing tinnitus",
    accentColor = Color(0xFF34D399), // Gentle emerald green
    icon = Icons.Default.Forest
  ),
  BOX_FAN(
    id = "box_fan",
    displayName = "Box Fan",
    tag = "Airflow Hum",
    description = "Low-frequency steady fan drone with subtle aerodynamic airflow flutter.",
    targetNoise = "Classic bedroom sleep sound, drowns out nighttime creaks",
    accentColor = Color(0xFF60A5FA), // Air blue
    icon = Icons.Default.Air
  ),
  RAIN(
    id = "rain",
    displayName = "Steady Rain",
    tag = "Droplet Ambiance",
    description = "Layered soothing rain patter with deep ambient atmospheric wash.",
    targetNoise = "Relaxing mask for open windows, noisy streets & anxiety",
    accentColor = Color(0xFF818CF8), // Indigo rain
    icon = Icons.Default.WaterDrop
  ),
  OCEAN(
    id = "ocean",
    displayName = "Ocean Surf",
    tag = "Rhythmic Swells",
    description = "Slow, tidal crests and receding foam that ease breathing and heart rate.",
    targetNoise = "Eases insomnia, helps breathing regulation & blocks uneven noise",
    accentColor = Color(0xFF2DD4BF), // Ocean teal
    icon = Icons.Default.Surfing
  ),
  CABIN(
    id = "cabin",
    displayName = "Airplane Cabin",
    tag = "Pressurized Drone",
    description = "Steady, enveloping jet airliner cabin cruise drone with muted airflow.",
    targetNoise = "Deep focus isolation chamber for open offices and busy spaces",
    accentColor = Color(0xFFA78BFA), // Lavender cabin
    icon = Icons.Default.Flight
  )
}
