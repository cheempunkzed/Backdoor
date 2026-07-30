package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AbyssColorScheme = darkColorScheme(
    primary = TerminalGreen,
    onPrimary = AbyssBackground,
    primaryContainer = TerminalGreenBg,
    onPrimaryContainer = TerminalGreen,
    secondary = CyberCyan,
    onSecondary = AbyssBackground,
    secondaryContainer = AbyssSurfaceVariant,
    onSecondaryContainer = CyberCyan,
    tertiary = AmberAlert,
    onTertiary = AbyssBackground,
    background = AbyssBackground,
    onBackground = TextPrimary,
    surface = AbyssSurface,
    onSurface = TextPrimary,
    surfaceVariant = AbyssSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = AbyssCardBorder,
    error = NeonRed
)

@Composable
fun BackdoorTheme(
    darkTheme: Boolean = true, // Force dark theme for cyber OS experience
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AbyssColorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backwards compatibility if needed
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BackdoorTheme(darkTheme = true, content = content)
}
