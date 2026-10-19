package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// 5 Scenic Ambient Wallpaper Themes for Vellum Browser
enum class VellumAmbientTheme(
    val id: String,
    val label: String,
    val isDark: Boolean,
    val skyTop: Color,
    val skyMid: Color,
    val skyBottom: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val horizonBack: Color,
    val horizonFront: Color,
    val surfaceColor: Color,
    val surfaceGlass: Color,
    val cardBorder: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color
) {
    AURORA_NIGHT(
        id = "AURORA_NIGHT",
        label = "Aurora Silk",
        isDark = true,
        skyTop = Color(0xFF0A1128),
        skyMid = Color(0xFF132247),
        skyBottom = Color(0xFF1D3557),
        glowPrimary = Color(0xFF00F5D4),
        glowSecondary = Color(0xFF7B2CBF),
        horizonBack = Color(0xFF14243E),
        horizonFront = Color(0xFF0C1629),
        surfaceColor = Color(0xFF121C34),
        surfaceGlass = Color(0xCC162340),
        cardBorder = Color(0x3864DFDF),
        primaryAccent = Color(0xFF00E5C3),
        secondaryAccent = Color(0xFF4EA8DE),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8)
    ),
    SUNSET_DUNES(
        id = "SUNSET_DUNES",
        label = "Sunset Velvet",
        isDark = true,
        skyTop = Color(0xFF1F1129),
        skyMid = Color(0xFF431C46),
        skyBottom = Color(0xFF7A2E4D),
        glowPrimary = Color(0xFFFF9E64),
        glowSecondary = Color(0xFFF72585),
        horizonBack = Color(0xFF3C1638),
        horizonFront = Color(0xFF210C22),
        surfaceColor = Color(0xFF26122B),
        surfaceGlass = Color(0xCC2E1734),
        cardBorder = Color(0x40FF9E64),
        primaryAccent = Color(0xFFFF9E64),
        secondaryAccent = Color(0xFFF77F00),
        textPrimary = Color(0xFFFFF8F5),
        textSecondary = Color(0xFFD8B4C8)
    ),
    OCEAN_SAPPHIRE(
        id = "OCEAN_SAPPHIRE",
        label = "Ocean Sapphire",
        isDark = true,
        skyTop = Color(0xFF061A2E),
        skyMid = Color(0xFF0B3152),
        skyBottom = Color(0xFF134B75),
        glowPrimary = Color(0xFF38BDF8),
        glowSecondary = Color(0xFF34D399),
        horizonBack = Color(0xFF0D2B47),
        horizonFront = Color(0xFF071B2F),
        surfaceColor = Color(0xFF0B2239),
        surfaceGlass = Color(0xCC0F2B46),
        cardBorder = Color(0x3838BDF8),
        primaryAccent = Color(0xFF38BDF8),
        secondaryAccent = Color(0xFF2DD4BF),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFF93C5FD)
    ),
    EMERALD_FOREST(
        id = "EMERALD_FOREST",
        label = "Emerald Mist",
        isDark = true,
        skyTop = Color(0xFF071C18),
        skyMid = Color(0xFF0E342D),
        skyBottom = Color(0xFF1A4E43),
        glowPrimary = Color(0xFF34D399),
        glowSecondary = Color(0xFFA7F3D0),
        horizonBack = Color(0xFF112E28),
        horizonFront = Color(0xFF091D19),
        surfaceColor = Color(0xFF0C2420),
        surfaceGlass = Color(0xCC11302A),
        cardBorder = Color(0x3834D399),
        primaryAccent = Color(0xFF34D399),
        secondaryAccent = Color(0xFF10B981),
        textPrimary = Color(0xFFECFDF5),
        textSecondary = Color(0xFF86EFAC)
    ),
    PEARL_DAYLIGHT(
        id = "PEARL_DAYLIGHT",
        label = "Pearl Breeze",
        isDark = false,
        skyTop = Color(0xFFE0ECFF),
        skyMid = Color(0xFFEDF2FB),
        skyBottom = Color(0xFFF8F9FD),
        glowPrimary = Color(0xFF4F46E5),
        glowSecondary = Color(0xFF06B6D4),
        horizonBack = Color(0xFFD7E3FC),
        horizonFront = Color(0xFFEBF2FA),
        surfaceColor = Color(0xFFFFFFFF),
        surfaceGlass = Color(0xE6FFFFFF),
        cardBorder = Color(0x284F46E5),
        primaryAccent = Color(0xFF4F46E5),
        secondaryAccent = Color(0xFF0891B2),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF475569)
    );

    companion object {
        fun fromId(id: String?): VellumAmbientTheme =
            entries.firstOrNull { it.id == id } ?: AURORA_NIGHT
    }
}

// Private Vault Palette (Deep Royal Amethyst)
val PrivateVaultSkyTop = Color(0xFF120B29)
val PrivateVaultSkyBottom = Color(0xFF24154A)
val PrivateVaultSurface = Color(0xFF191033)
val PrivateVaultAccent = Color(0xFFC084FC)
val PrivateVaultCrimson = Color(0xFFFB7185)

// Reader Paper Themes
val AlabasterCanvas = Color(0xFFF8F7F3)
val ObsidianInk = Color(0xFF141413)
val ReaderSepiaBg = Color(0xFFF4ECD8)
val ReaderSepiaInk = Color(0xFF2C241B)
val CarbonCanvas = Color(0xFF10141E)
val ParchmentInk = Color(0xFFECEAE4)
