package com.keisardev.moviesandbeyond.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeTonalSpot

/** Cinematic Light Color Scheme. Secondary option for users who prefer light mode. */
private val cinematicLightScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )

/**
 * Cinematic Dark Color Scheme. Primary theme - movies look best on dark backgrounds. Features deep
 * blacks with blue tint for immersive cinema feel.
 */
private val cinematicDarkScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )

/** Check if the device supports dynamic color theming (Android 12+). */
fun supportsDynamicColorTheme(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Generates a Material3 ColorScheme from a seed color using Material Design color utilities.
 *
 * @param seedColorArgb The seed color ARGB value to generate the scheme from
 * @param isDark Whether to generate a dark or light color scheme
 * @return A ColorScheme generated from the seed color
 */
private fun generateColorSchemeFromSeed(seedColorArgb: Long, isDark: Boolean): ColorScheme {
    val hct = Hct.fromInt(seedColorArgb.toInt())
    val scheme = SchemeTonalSpot(hct, isDark, 0.0)
    val dynamicColors = MaterialDynamicColors()

    return if (isDark) {
        darkColorScheme(
            primary = Color(dynamicColors.primary().getArgb(scheme)),
            onPrimary = Color(dynamicColors.onPrimary().getArgb(scheme)),
            primaryContainer = Color(dynamicColors.primaryContainer().getArgb(scheme)),
            onPrimaryContainer = Color(dynamicColors.onPrimaryContainer().getArgb(scheme)),
            secondary = Color(dynamicColors.secondary().getArgb(scheme)),
            onSecondary = Color(dynamicColors.onSecondary().getArgb(scheme)),
            secondaryContainer = Color(dynamicColors.secondaryContainer().getArgb(scheme)),
            onSecondaryContainer = Color(dynamicColors.onSecondaryContainer().getArgb(scheme)),
            tertiary = Color(dynamicColors.tertiary().getArgb(scheme)),
            onTertiary = Color(dynamicColors.onTertiary().getArgb(scheme)),
            tertiaryContainer = Color(dynamicColors.tertiaryContainer().getArgb(scheme)),
            onTertiaryContainer = Color(dynamicColors.onTertiaryContainer().getArgb(scheme)),
            error = Color(dynamicColors.error().getArgb(scheme)),
            onError = Color(dynamicColors.onError().getArgb(scheme)),
            errorContainer = Color(dynamicColors.errorContainer().getArgb(scheme)),
            onErrorContainer = Color(dynamicColors.onErrorContainer().getArgb(scheme)),
            background = Color(dynamicColors.background().getArgb(scheme)),
            onBackground = Color(dynamicColors.onBackground().getArgb(scheme)),
            surface = Color(dynamicColors.surface().getArgb(scheme)),
            onSurface = Color(dynamicColors.onSurface().getArgb(scheme)),
            surfaceVariant = Color(dynamicColors.surfaceVariant().getArgb(scheme)),
            onSurfaceVariant = Color(dynamicColors.onSurfaceVariant().getArgb(scheme)),
            outline = Color(dynamicColors.outline().getArgb(scheme)),
            outlineVariant = Color(dynamicColors.outlineVariant().getArgb(scheme)),
            scrim = Color(dynamicColors.scrim().getArgb(scheme)),
            inverseSurface = Color(dynamicColors.inverseSurface().getArgb(scheme)),
            inverseOnSurface = Color(dynamicColors.inverseOnSurface().getArgb(scheme)),
            inversePrimary = Color(dynamicColors.inversePrimary().getArgb(scheme)),
            surfaceDim = Color(dynamicColors.surfaceDim().getArgb(scheme)),
            surfaceBright = Color(dynamicColors.surfaceBright().getArgb(scheme)),
            surfaceContainerLowest = Color(dynamicColors.surfaceContainerLowest().getArgb(scheme)),
            surfaceContainerLow = Color(dynamicColors.surfaceContainerLow().getArgb(scheme)),
            surfaceContainer = Color(dynamicColors.surfaceContainer().getArgb(scheme)),
            surfaceContainerHigh = Color(dynamicColors.surfaceContainerHigh().getArgb(scheme)),
            surfaceContainerHighest =
                Color(dynamicColors.surfaceContainerHighest().getArgb(scheme)),
        )
    } else {
        lightColorScheme(
            primary = Color(dynamicColors.primary().getArgb(scheme)),
            onPrimary = Color(dynamicColors.onPrimary().getArgb(scheme)),
            primaryContainer = Color(dynamicColors.primaryContainer().getArgb(scheme)),
            onPrimaryContainer = Color(dynamicColors.onPrimaryContainer().getArgb(scheme)),
            secondary = Color(dynamicColors.secondary().getArgb(scheme)),
            onSecondary = Color(dynamicColors.onSecondary().getArgb(scheme)),
            secondaryContainer = Color(dynamicColors.secondaryContainer().getArgb(scheme)),
            onSecondaryContainer = Color(dynamicColors.onSecondaryContainer().getArgb(scheme)),
            tertiary = Color(dynamicColors.tertiary().getArgb(scheme)),
            onTertiary = Color(dynamicColors.onTertiary().getArgb(scheme)),
            tertiaryContainer = Color(dynamicColors.tertiaryContainer().getArgb(scheme)),
            onTertiaryContainer = Color(dynamicColors.onTertiaryContainer().getArgb(scheme)),
            error = Color(dynamicColors.error().getArgb(scheme)),
            onError = Color(dynamicColors.onError().getArgb(scheme)),
            errorContainer = Color(dynamicColors.errorContainer().getArgb(scheme)),
            onErrorContainer = Color(dynamicColors.onErrorContainer().getArgb(scheme)),
            background = Color(dynamicColors.background().getArgb(scheme)),
            onBackground = Color(dynamicColors.onBackground().getArgb(scheme)),
            surface = Color(dynamicColors.surface().getArgb(scheme)),
            onSurface = Color(dynamicColors.onSurface().getArgb(scheme)),
            surfaceVariant = Color(dynamicColors.surfaceVariant().getArgb(scheme)),
            onSurfaceVariant = Color(dynamicColors.onSurfaceVariant().getArgb(scheme)),
            outline = Color(dynamicColors.outline().getArgb(scheme)),
            outlineVariant = Color(dynamicColors.outlineVariant().getArgb(scheme)),
            scrim = Color(dynamicColors.scrim().getArgb(scheme)),
            inverseSurface = Color(dynamicColors.inverseSurface().getArgb(scheme)),
            inverseOnSurface = Color(dynamicColors.inverseOnSurface().getArgb(scheme)),
            inversePrimary = Color(dynamicColors.inversePrimary().getArgb(scheme)),
            surfaceDim = Color(dynamicColors.surfaceDim().getArgb(scheme)),
            surfaceBright = Color(dynamicColors.surfaceBright().getArgb(scheme)),
            surfaceContainerLowest = Color(dynamicColors.surfaceContainerLowest().getArgb(scheme)),
            surfaceContainerLow = Color(dynamicColors.surfaceContainerLow().getArgb(scheme)),
            surfaceContainer = Color(dynamicColors.surfaceContainer().getArgb(scheme)),
            surfaceContainerHigh = Color(dynamicColors.surfaceContainerHigh().getArgb(scheme)),
            surfaceContainerHighest =
                Color(dynamicColors.surfaceContainerHighest().getArgb(scheme)),
        )
    }
}

/**
 * MoviesAndBeyond app theme with cinematic design system.
 *
 * Features:
 * - Dark-first theme optimized for movie content
 * - Material You dynamic colors on Android 12+ (default)
 * - Seed color customization for older Android or user preference
 * - Electric blue primary, cinema red secondary, gold tertiary (cinematic fallback)
 * - Deep surface hierarchy for immersive feel
 * - Cinematic typography scale
 * - Premium rounded shapes
 *
 * @param darkTheme Whether to use dark theme (default: system preference)
 * @param dynamicColor Whether to use Material You dynamic colors on Android 12+
 * @param seedColor The seed color to use when dynamic color is disabled or unavailable
 * @param customColorArgb The custom color ARGB value to use when seedColor is CUSTOM
 * @param content The composable content to apply theme to
 */
@Composable
fun MoviesAndBeyondTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColor: SeedColor = SeedColor.DEFAULT,
    customColorArgb: Long = SeedColor.DEFAULT_CUSTOM_COLOR_ARGB,
    content: @Composable () -> Unit
) {
    val colorScheme =
        when {
            dynamicColor && supportsDynamicColorTheme() -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            seedColor == SeedColor.DEFAULT -> {
                // Use the custom cinematic theme when DEFAULT seed color is selected
                if (darkTheme) cinematicDarkScheme else cinematicLightScheme
            }
            seedColor == SeedColor.CUSTOM -> {
                // Use custom color ARGB for CUSTOM seed color
                generateColorSchemeFromSeed(customColorArgb, darkTheme)
            }
            else -> {
                // Generate color scheme from preset seed color
                generateColorSchemeFromSeed(seedColor.argb, darkTheme)
            }
        }

    // Update system bars to match the theme in edge-to-edge mode
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // Ensure status bar icons and navigation bar icons have proper contrast
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CinematicM3Typography,
        shapes = CinematicM3Shapes,
        content = content)
}
