package com.keisardev.moviesandbeyond.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Cinematic color palette optimized for movie discovery experience. Dark theme is primary - movies
 * look best on dark backgrounds.
 */
object CinematicColors {
    // ==========================================================================
    // BRAND COLORS
    // ==========================================================================

    // Primary - Electric blue for CTAs and highlights
    val Primary = Color(0xFF3D5AFE)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1A237E)
    val OnPrimaryContainer = Color(0xFFB6C4FF)

    // Secondary - Cinema red accent
    val Secondary = Color(0xFFFF5252)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFF5C0011)
    val OnSecondaryContainer = Color(0xFFFFDAD6)

    // Tertiary - Gold for ratings and stars
    val Tertiary = Color(0xFFFFD700)
    val OnTertiary = Color(0xFF000000)
    val TertiaryContainer = Color(0xFF4A3D00)
    val OnTertiaryContainer = Color(0xFFFFE082)

    // ==========================================================================
    // SURFACE HIERARCHY (Dark Theme - Cinematic)
    // ==========================================================================

    val Background = Color(0xFF0A0A0F)
    val OnBackground = Color(0xFFE6E1E5)

    val Surface = Color(0xFF0F0F14)
    val OnSurface = Color(0xFFE6E1E5)

    val SurfaceVariant = Color(0xFF1A1A22)
    val OnSurfaceVariant = Color(0xFFCAC4D0)

    val SurfaceDim = Color(0xFF050508)
    val SurfaceBright = Color(0xFF38383F)

    val SurfaceContainerLowest = Color(0xFF0A0A0F)
    val SurfaceContainerLow = Color(0xFF111117)
    val SurfaceContainer = Color(0xFF141419)
    val SurfaceContainerHigh = Color(0xFF1E1E24)
    val SurfaceContainerHighest = Color(0xFF28282F)

    // ==========================================================================
    // SEMANTIC COLORS
    // ==========================================================================

    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)

    val Outline = Color(0xFF938F99)
    val OutlineVariant = Color(0xFF49454F)

    val Scrim = Color(0xFF000000)

    val InverseSurface = Color(0xFFE6E1E5)
    val InverseOnSurface = Color(0xFF313033)
    val InversePrimary = Color(0xFF1A4BD2)

    // ==========================================================================
    // RATING COLORS (For visual quality indicators)
    // ==========================================================================

    val RatingExcellent = Color(0xFF4CAF50) // Green - 8.0+
    val RatingGood = Color(0xFFFFC107) // Amber - 6.0-7.9
    val RatingAverage = Color(0xFFFF9800) // Orange - 4.0-5.9
    val RatingPoor = Color(0xFFF44336) // Red - <4.0

    // Star colors
    val StarGold = Color(0xFFFFD700)
    val StarGoldDim = Color(0xFFB8860B)
    val StarEmpty = Color(0xFF49454F)

    // ==========================================================================
    // GRADIENT COLORS (For hero overlays)
    // ==========================================================================

    val GradientStart = Color(0x00000000) // Transparent
    val GradientEnd = Color(0xCC000000) // 80% black
    val GradientEndStrong = Color(0xE6000000) // 90% black
}

/** Light theme colors (secondary option for users who prefer light mode) */
object CinematicColorsLight {
    val Primary = Color(0xFF1A4BD2)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFDEE1FF)
    val OnPrimaryContainer = Color(0xFF00105C)

    val Secondary = Color(0xFFB3261E)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFF9DEDC)
    val OnSecondaryContainer = Color(0xFF410E0B)

    val Tertiary = Color(0xFF7D5700)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFFFDEA6)
    val OnTertiaryContainer = Color(0xFF271900)

    val Background = Color(0xFFFFFBFF)
    val OnBackground = Color(0xFF1C1B1E)

    val Surface = Color(0xFFFFFBFF)
    val OnSurface = Color(0xFF1C1B1E)

    val SurfaceVariant = Color(0xFFE7E0EC)
    val OnSurfaceVariant = Color(0xFF49454F)

    val SurfaceDim = Color(0xFFDED8E1)
    val SurfaceBright = Color(0xFFFFFBFF)

    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF7F2FA)
    val SurfaceContainer = Color(0xFFF3EDF7)
    val SurfaceContainerHigh = Color(0xFFECE6F0)
    val SurfaceContainerHighest = Color(0xFFE6E0E9)

    val Error = Color(0xFFB3261E)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFF9DEDC)
    val OnErrorContainer = Color(0xFF410E0B)

    val Outline = Color(0xFF79747E)
    val OutlineVariant = Color(0xFFCAC4D0)

    val Scrim = Color(0xFF000000)

    val InverseSurface = Color(0xFF313033)
    val InverseOnSurface = Color(0xFFF4EFF4)
    val InversePrimary = Color(0xFFB6C4FF)

    // Rating colors (same for light theme)
    val RatingExcellent = Color(0xFF388E3C)
    val RatingGood = Color(0xFFFFA000)
    val RatingAverage = Color(0xFFF57C00)
    val RatingPoor = Color(0xFFD32F2F)

    val StarGold = Color(0xFFFFD700)
    val StarGoldDim = Color(0xFFB8860B)
    val StarEmpty = Color(0xFFCAC4D0)
}

/** Helper function to get rating color based on score. */
fun getRatingColor(score: Double, isDarkTheme: Boolean = true): Color {
    return if (isDarkTheme) {
        when {
            score >= 8.0 -> CinematicColors.RatingExcellent
            score >= 6.0 -> CinematicColors.RatingGood
            score >= 4.0 -> CinematicColors.RatingAverage
            else -> CinematicColors.RatingPoor
        }
    } else {
        when {
            score >= 8.0 -> CinematicColorsLight.RatingExcellent
            score >= 6.0 -> CinematicColorsLight.RatingGood
            score >= 4.0 -> CinematicColorsLight.RatingAverage
            else -> CinematicColorsLight.RatingPoor
        }
    }
}
