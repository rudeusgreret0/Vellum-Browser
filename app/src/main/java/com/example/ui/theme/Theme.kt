package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class VellumThemeMode {
    SYSTEM,
    ALABASTER_DAY,
    CARBON_NIGHT
}

private val AlabasterLightColorScheme = lightColorScheme(
    primary = BurnishedBronze,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7E4DC),
    onPrimaryContainer = Color(0xFF4A1D10),
    secondary = ForestShieldEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9EFE8),
    onSecondaryContainer = Color(0xFF0B3829),
    tertiary = ChampagneGold,
    background = AlabasterCanvas,
    onBackground = ObsidianInk,
    surface = IvorySurface,
    onSurface = ObsidianInk,
    surfaceVariant = StoneSurfaceVariant,
    onSurfaceVariant = SlateSecondaryInk,
    outline = HairlineLight,
    outlineVariant = Color(0xFFEAE7DF)
)

private val CarbonDarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = Color(0xFF1A140B),
    primaryContainer = Color(0xFF3A2D18),
    onPrimaryContainer = Color(0xFFF5DFB8),
    secondary = MineralJade,
    onSecondary = Color(0xFF072118),
    secondaryContainer = Color(0xFF163D31),
    onSecondaryContainer = Color(0xFFB8EBD9),
    tertiary = BurnishedBronze,
    background = CarbonCanvas,
    onBackground = ParchmentInk,
    surface = BasaltSurface,
    onSurface = ParchmentInk,
    surfaceVariant = BasaltElevated,
    onSurfaceVariant = AshSecondaryInk,
    outline = HairlineDark,
    outlineVariant = Color(0xFF22242B)
)

private val NocturnePrivateColorScheme = darkColorScheme(
    primary = NocturneAccent,
    onPrimary = Color(0xFF0A101D),
    primaryContainer = Color(0xFF1E2B45),
    onPrimaryContainer = Color(0xFFD5E3F7),
    secondary = MineralJade,
    onSecondary = Color(0xFF072118),
    secondaryContainer = Color(0xFF163D31),
    onSecondaryContainer = Color(0xFFB8EBD9),
    tertiary = NocturneCrimson,
    background = NocturneCanvas,
    onBackground = ParchmentInk,
    surface = NocturneSurface,
    onSurface = ParchmentInk,
    surfaceVariant = NocturneElevated,
    onSurfaceVariant = Color(0xFF9BA6B8),
    outline = NocturneHairline,
    outlineVariant = Color(0xFF1D243B)
)

@Composable
fun VellumTheme(
    themeMode: VellumThemeMode = VellumThemeMode.CARBON_NIGHT,
    isPrivateMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        VellumThemeMode.SYSTEM -> isSystemInDarkTheme()
        VellumThemeMode.ALABASTER_DAY -> false
        VellumThemeMode.CARBON_NIGHT -> true
    }

    val colorScheme = when {
        isPrivateMode -> NocturnePrivateColorScheme
        isDark -> CarbonDarkColorScheme
        else -> AlabasterLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
