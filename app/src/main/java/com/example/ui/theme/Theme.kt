package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RainbowIndigo,
    onPrimary = Color.White,
    primaryContainer = SurfaceDarkCardHover,
    onPrimaryContainer = RainbowRose,
    secondary = RainbowEmerald,
    onSecondary = Color.White,
    tertiary = RainbowRose,
    onTertiary = Color.White,
    background = SurfaceDarkBg,
    surface = SurfaceDarkCard,
    surfaceVariant = SurfaceDarkCardHover,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSubtleDark,
    outlineVariant = RainbowIndigo
)

private val LightColorScheme = lightColorScheme(
    primary = RainbowIndigo,
    onPrimary = Color.White,
    primaryContainer = SurfaceLightCardHover,
    onPrimaryContainer = RainbowRose,
    secondary = RainbowEmerald,
    onSecondary = Color.White,
    tertiary = RainbowRose,
    onTertiary = Color.White,
    background = SurfaceLightBg,
    surface = SurfaceLightCard,
    surfaceVariant = SurfaceLightCardHover,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderSubtleLight,
    outlineVariant = RainbowIndigo
)

@Composable
fun PhonePilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}


