package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekBluePrimaryDark,
    onPrimary = Color.White,
    primaryContainer = SleekBlueContainerDark,
    onPrimaryContainer = SleekOnBlueContainerDark,
    secondary = SleekEmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekEmeraldContainerDark,
    onSecondaryContainer = Color(0xFFA7F3D0),
    background = SleekDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = SleekDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SleekDarkCard,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = SleekDarkBorder,
    error = SleekSosRed
)

private val LightColorScheme = lightColorScheme(
    primary = SleekBluePrimary,
    onPrimary = Color.White,
    primaryContainer = SleekBlueContainerLight,
    onPrimaryContainer = SleekOnBlueContainerLight,
    secondary = SleekEmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = SleekEmeraldContainerLight,
    onSecondaryContainer = Color(0xFF065F46),
    background = SleekLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = SleekLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = SleekLightCard,
    onSurfaceVariant = Color(0xFF64748B),
    outline = SleekLightBorder,
    error = SleekSosRed
)

@Composable
fun FamilyGuardianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

