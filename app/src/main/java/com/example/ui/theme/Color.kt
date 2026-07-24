package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Rainbow Spectrum Colors (Clean & Human-Crafted)
val RainbowRose = Color(0xFFF43F5E)
val RainbowOrange = Color(0xFFFB923C)
val RainbowAmber = Color(0xFFFBBF24)
val RainbowEmerald = Color(0xFF10B981)
val RainbowCyan = Color(0xFF06B6D4)
val RainbowIndigo = Color(0xFF6366F1)
val RainbowViolet = Color(0xFF8B5CF6)
val RainbowPink = Color(0xFFEC4899)

// Theme Canvas & Surface Colors
val SurfaceDarkBg = Color(0xFF0B0F19)
val SurfaceDarkCard = Color(0xFF151D30)
val SurfaceDarkCardHover = Color(0xFF1E2942)
val BorderSubtleDark = Color(0xFF2A3859)

val SurfaceLightBg = Color(0xFFF8FAFC)
val SurfaceLightCard = Color(0xFFFFFFFF)
val SurfaceLightCardHover = Color(0xFFF1F5F9)
val BorderSubtleLight = Color(0xFFE2E8F0)

// Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)

// Status Colors
val StatusSuccess = Color(0xFF10B981)
val StatusError = Color(0xFFEF4444)
val StatusWarning = Color(0xFFF59E0B)

// Standard Rainbow Gradient Brushes
val RainbowBrushHorizontal = Brush.horizontalGradient(
    colors = listOf(
        RainbowRose,
        RainbowOrange,
        RainbowAmber,
        RainbowEmerald,
        RainbowCyan,
        RainbowIndigo,
        RainbowViolet
    )
)

val RainbowBrushSoft = Brush.horizontalGradient(
    colors = listOf(
        RainbowRose.copy(alpha = 0.8f),
        RainbowAmber.copy(alpha = 0.8f),
        RainbowCyan.copy(alpha = 0.8f),
        RainbowViolet.copy(alpha = 0.8f)
    )
)

// Backward compatibility alias colors for legacy references
val RgbCyan = RainbowCyan
val RgbCyanOn = Color(0xFF043842)
val RgbMagenta = RainbowRose
val RgbLime = RainbowEmerald
val RgbPurple = RainbowViolet
val RgbYellow = RainbowAmber
val DarkBackground = SurfaceDarkBg
val DarkSurface = SurfaceDarkCard
val DarkSurfaceVariant = SurfaceDarkCardHover
val DarkBorder = BorderSubtleDark
val GlowCyanBorder = RainbowCyan
val GlowMagentaBorder = RainbowRose
val TextPrimary = TextPrimaryDark
val TextSecondary = TextSecondaryDark
val StatusGreen = StatusSuccess
val StatusRed = StatusError
val StatusAmber = StatusWarning


