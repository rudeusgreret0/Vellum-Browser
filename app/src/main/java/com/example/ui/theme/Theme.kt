package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Legacy compatibility enum mapped to VellumAmbientTheme
enum class VellumThemeMode {
    SYSTEM,
    ALABASTER_DAY,
    CARBON_NIGHT
}

val LocalAmbientTheme = staticCompositionLocalOf { VellumAmbientTheme.AURORA_NIGHT }

@Composable
fun VellumTheme(
    ambientTheme: VellumAmbientTheme = VellumAmbientTheme.AURORA_NIGHT,
    isPrivateMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        isPrivateMode -> darkColorScheme(
            primary = PrivateVaultAccent,
            onPrimary = Color(0xFF1A0933),
            primaryContainer = Color(0xFF2E1B59),
            onPrimaryContainer = Color(0xFFF3E8FF),
            secondary = Color(0xFF38BDF8),
            onSecondary = Color(0xFF062030),
            tertiary = PrivateVaultCrimson,
            background = PrivateVaultSkyTop,
            onBackground = Color(0xFFF8FAFC),
            surface = PrivateVaultSurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF251848),
            onSurfaceVariant = Color(0xFFC4B5FD),
            outline = Color(0x45C084FC),
            outlineVariant = Color(0x25C084FC)
        )
        ambientTheme.isDark -> darkColorScheme(
            primary = ambientTheme.primaryAccent,
            onPrimary = Color(0xFF04151F),
            primaryContainer = ambientTheme.primaryAccent.copy(alpha = 0.2f),
            onPrimaryContainer = ambientTheme.textPrimary,
            secondary = ambientTheme.secondaryAccent,
            onSecondary = Color(0xFF04151F),
            tertiary = Color(0xFFFB7185),
            background = ambientTheme.skyTop,
            onBackground = ambientTheme.textPrimary,
            surface = ambientTheme.surfaceColor,
            onSurface = ambientTheme.textPrimary,
            surfaceVariant = ambientTheme.skyMid,
            onSurfaceVariant = ambientTheme.textSecondary,
            outline = ambientTheme.cardBorder,
            outlineVariant = ambientTheme.cardBorder.copy(alpha = 0.15f)
        )
        else -> lightColorScheme(
            primary = ambientTheme.primaryAccent,
            onPrimary = Color.White,
            primaryContainer = ambientTheme.primaryAccent.copy(alpha = 0.14f),
            onPrimaryContainer = ambientTheme.textPrimary,
            secondary = ambientTheme.secondaryAccent,
            onSecondary = Color.White,
            tertiary = Color(0xFFE11D48),
            background = ambientTheme.skyBottom,
            onBackground = ambientTheme.textPrimary,
            surface = ambientTheme.surfaceColor,
            onSurface = ambientTheme.textPrimary,
            surfaceVariant = ambientTheme.skyMid,
            onSurfaceVariant = ambientTheme.textSecondary,
            outline = ambientTheme.cardBorder,
            outlineVariant = ambientTheme.cardBorder.copy(alpha = 0.15f)
        )
    }

    CompositionLocalProvider(LocalAmbientTheme provides ambientTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
