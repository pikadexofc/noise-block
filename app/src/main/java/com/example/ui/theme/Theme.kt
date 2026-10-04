package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = CyanOnPrimary,
  primaryContainer = CyanContainer,
  onPrimaryContainer = CyanOnContainer,
  secondary = SlateSecondary,
  background = SlateBackground,
  surface = SlateSurface,
  surfaceVariant = SlateSurfaceVariant,
  onBackground = Color(0xFFF1F5F9),
  onSurface = Color(0xFFF1F5F9)
)

@Composable
fun NoiseBlockTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
