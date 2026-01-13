package com.keisardev.moviesandbeyond.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Cinematic elevation tokens for depth hierarchy. Creates subtle layering effect for a polished,
 * premium feel.
 */
object CinematicElevation {
    // ==========================================================================
    // BASE SCALE - Generic elevation levels
    // ==========================================================================

    /** 0.dp - No elevation, flat surfaces */
    val none: Dp = 0.dp

    /** 1.dp - Minimal elevation, subtle lift */
    val extraLow: Dp = 1.dp

    /** 2.dp - Low elevation, pressed states */
    val low: Dp = 2.dp

    /** 4.dp - Medium elevation, standard cards */
    val medium: Dp = 4.dp

    /** 8.dp - High elevation, floating elements */
    val high: Dp = 8.dp

    /** 12.dp - Extra high elevation, dialogs, sheets */
    val extraHigh: Dp = 12.dp

    /** 16.dp - Maximum elevation, critical overlays */
    val max: Dp = 16.dp

    // ==========================================================================
    // COMPONENT-SPECIFIC ELEVATION
    // ==========================================================================

    /** Standard card elevation */
    val card: Dp = medium // 4.dp

    /** Card when pressed (reduces to indicate press) */
    val cardPressed: Dp = low // 2.dp

    /** Card on hover/focus (increases for emphasis) */
    val cardHovered: Dp = high // 8.dp

    /** Floating action button */
    val fab: Dp = high // 8.dp

    /** Top app bar - typically flat/transparent for immersive feel */
    val topBar: Dp = none // 0.dp

    /** Bottom navigation/bar - flat, uses blur instead of elevation */
    val bottomBar: Dp = none // 0.dp

    /** Modal dialogs */
    val dialog: Dp = extraHigh // 12.dp

    /** Bottom sheets */
    val bottomSheet: Dp = extraHigh // 12.dp

    /** Dropdown menus, tooltips */
    val menu: Dp = high // 8.dp

    /** Snackbars */
    val snackbar: Dp = high // 8.dp

    /** Search bar when focused */
    val searchBarFocused: Dp = medium // 4.dp

    /** Rating badge overlay on images */
    val badge: Dp = low // 2.dp
}
