package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TradePilotDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepNavy,
    primaryContainer = DarkNavySurface,
    onPrimaryContainer = ElectricCyan,
    secondary = SkyBlueAccent,
    onSecondary = DeepNavy,
    secondaryContainer = MidnightBlue,
    onSecondaryContainer = SkyBlueAccent,
    tertiary = NeonPurple,
    onTertiary = DeepNavy,
    tertiaryContainer = DarkNavySurface,
    onTertiaryContainer = NeonPurple,
    background = DeepNavy,
    onBackground = TextPrimary,
    surface = MidnightBlue,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavySurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = CoralRed,
    onError = Color.White
)

@Composable
fun TradePilotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TradePilotDarkColorScheme,
        typography = Typography,
        content = content
    )
}
