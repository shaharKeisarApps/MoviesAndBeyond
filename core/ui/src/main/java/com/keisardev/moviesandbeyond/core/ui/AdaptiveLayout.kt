package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.keisardev.moviesandbeyond.core.ui.theme.Dimens
import com.keisardev.moviesandbeyond.core.ui.theme.Spacing

/**
 * CompositionLocal that provides the current [WindowSizeClass] to descendant composables.
 *
 * Set at the app root via CompositionLocalProvider in MainActivity. Fails fast if the provider is
 * missing (e.g., in tests or previews) to prevent silent fallback to incorrect defaults.
 */
val LocalWindowSizeClass =
    staticCompositionLocalOf<WindowSizeClass> {
        error(
            "LocalWindowSizeClass not provided — wrap with CompositionLocalProvider in MainActivity"
        )
    }

/**
 * Returns true when the window width class is Medium or Expanded. Used to drive navigation rail
 * decisions and wider layout adaptations.
 */
fun WindowSizeClass.isExpandedOrMediumWidth(): Boolean {
    return widthSizeClass != WindowWidthSizeClass.Compact
}

/**
 * Returns the appropriate [GridCells] count for a vertical content grid based on the current
 * [WindowSizeClass].
 * - Compact: 3 columns (portrait phone)
 * - Medium: 4 columns (landscape phone / small tablet)
 * - Expanded: 6 columns (large tablet / foldable expanded)
 */
fun WindowSizeClass.adaptiveContentGridColumns(): GridCells {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> GridCells.Fixed(3)
        WindowWidthSizeClass.Medium -> GridCells.Fixed(4)
        else -> GridCells.Fixed(6)
    }
}

/**
 * Returns the appropriate search grid column count based on window width.
 * - Compact: 2 columns
 * - Medium: 3 columns
 * - Expanded: 4 columns
 */
fun WindowSizeClass.adaptiveSearchGridColumns(): GridCells {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> GridCells.Fixed(2)
        WindowWidthSizeClass.Medium -> GridCells.Fixed(3)
        else -> GridCells.Fixed(4)
    }
}

/**
 * Returns true if the window width is Compact (typical portrait phone). Used to determine if the
 * floating bottom nav bar should be shown vs. a navigation rail.
 */
fun WindowSizeClass.isCompactWidth(): Boolean {
    return widthSizeClass == WindowWidthSizeClass.Compact
}

/**
 * CompositionLocal indicating whether a NavigationRail is currently visible. When true, feature
 * screens should use reduced bottom padding since the floating bottom bar is not present.
 */
val LocalUseNavigationRail = staticCompositionLocalOf { false }

/**
 * Returns the appropriate bottom padding for feed/list content based on the current navigation
 * mode. When a NavigationRail is active, returns a smaller padding since there is no floating
 * bottom bar to clear.
 */
@Composable
fun adaptiveFeedBottomPadding(): Dp {
    return if (LocalUseNavigationRail.current) {
        Spacing.feedBottomPaddingRail
    } else {
        Spacing.feedBottomPadding
    }
}

/**
 * Returns the hero carousel max height, reduced in landscape/rail mode where vertical space is
 * limited (~443dp on a phone vs ~986dp in portrait).
 */
@Composable
fun adaptiveHeroMaxHeight(): Dp {
    return if (LocalUseNavigationRail.current) {
        Dimens.heroMaxHeightRail
    } else {
        Dimens.heroMaxHeight
    }
}

/**
 * Returns the detail backdrop expanded height, reduced in landscape/rail mode to avoid the backdrop
 * consuming the entire screen.
 */
@Composable
fun adaptiveDetailBackdropExpanded(): Dp {
    return if (LocalUseNavigationRail.current) {
        Dimens.detailBackdropExpandedRail
    } else {
        Dimens.detailBackdropExpanded
    }
}
