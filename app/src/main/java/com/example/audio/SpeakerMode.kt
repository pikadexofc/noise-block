package com.example.audio

/**
 * Output acoustic profile tailored for specific speaker hardware.
 *
 * Phone and laptop speakers physically roll off sharply below 160-200 Hz. Driving deep sub-bass
 * into micro-speakers wastes battery power and causes cone distortion. The Phone Speaker mode
 * filters out inaudible sub-bass and contours the vocal masking band (300 Hz - 3 kHz) for maximum
 * outside noise attenuation and clear volume on portable devices.
 */
enum class SpeakerMode(
  val displayName: String,
  val shortLabel: String,
  val description: String
) {
  PHONE_OPTIMIZED(
    displayName = "Phone / Laptop Speaker",
    shortLabel = "Speaker Boost",
    description = "Optimized for small speakers: cuts inaudible sub-bass power drain & enhances speech masking clarity."
  ),
  FULL_RANGE(
    displayName = "Headphones / Hi-Fi",
    shortLabel = "Full Range",
    description = "Full uncompressed low-frequency response down to 20 Hz for headphones and external sound systems."
  )
}
