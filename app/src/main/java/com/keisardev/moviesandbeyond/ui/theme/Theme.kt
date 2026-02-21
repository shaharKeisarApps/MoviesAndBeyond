package com.keisardev.moviesandbeyond.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.keisardev.moviesandbeyond.core.ui.LocalThemePreviewState
import com.keisardev.moviesandbeyond.core.ui.ThemePreviewState
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeTonalSpot

/**
 * Default seed color ARGB for the fallback M3 palette on pre-Android 12 devices. A cinema-inspired
 * blue that generates a harmonious tonal palette via Material Design color utilities.
 */
private const val DEFAULT_SEED_COLOR_ARGB = 0xFF3D5AFEL

/**
 * Fallback light scheme generated from [DEFAULT_SEED_COLOR_ARGB] for devices without dynamic color.
 */
private val fallbackLightScheme: ColorScheme by lazy {
    generateColorSchemeFromSeed(DEFAULT_SEED_COLOR_ARGB, isDark = false)
}

/**
 * Fallback dark scheme generated from [DEFAULT_SEED_COLOR_ARGB] for devices without dynamic color.
 */
private val fallbackDarkScheme: ColorScheme by lazy {
    generateColorSchemeFromSeed(DEFAULT_SEED_COLOR_ARGB, isDark = true)
}

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
            surfaceContainerHighest = Color(dynamicColors.surfaceContainerHighest().getArgb(scheme)),
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
            surfaceContainerHighest = Color(dynamicColors.surfaceContainerHighest().getArgb(scheme)),
        )
    }
}

/**
 * MoviesAndBeyond app theme.
 *
 * @param darkTheme Whether to use dark theme (default: system preference)
 * @param dynamicColor Whether to use Material You dynamic colors on Android 12+ (with fallback on
 *   older devices)
 * @param customColorArgb The custom seed color ARGB when [dynamicColor] is false
 * @param content The composable content to apply theme to
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MoviesAndBeyondTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    customColorArgb: Long = DEFAULT_SEED_COLOR_ARGB,
    content: @Composable () -> Unit,
) {
    val themePreviewState = remember { ThemePreviewState() }
    val previewColor = themePreviewState.previewColorArgb

    val colorScheme =
        when {
            // Live preview from color picker — overrides everything
            previewColor != null -> {
                remember(previewColor, darkTheme) {
                    generateColorSchemeFromSeed(previewColor, darkTheme)
                }
            }
            dynamicColor && supportsDynamicColorTheme() -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            dynamicColor -> {
                // Pre-Android 12 fallback for "Dynamic Color" option
                if (darkTheme) fallbackDarkScheme else fallbackLightScheme
            }
            else -> {
                // User-chosen seed color — cached to avoid recomputing on every recomposition
                remember(customColorArgb, darkTheme) {
                    generateColorSchemeFromSeed(customColorArgb, darkTheme)
                }
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

    CompositionLocalProvider(LocalThemePreviewState provides themePreviewState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CinematicM3Typography,
            shapes = CinematicM3Shapes,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
