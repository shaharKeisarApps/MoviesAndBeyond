package com.keisardev.moviesandbeyond.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Cinematic shape tokens for consistent corner radii throughout the app. Modern, slightly rounded
 * aesthetic that feels premium.
 */
object CinematicShapes {
    // ==========================================================================
    // BASE SCALE - Generic rounded corner sizes
    // ==========================================================================

    /** 4.dp - Chips, badges, smallest elements */
    val extraSmall = RoundedCornerShape(4.dp)

    /** 8.dp - Small cards, inputs, compact elements */
    val small = RoundedCornerShape(8.dp)

    /** 12.dp - Standard cards, most components */
    val medium = RoundedCornerShape(12.dp)

    /** 16.dp - Featured cards, prominent elements */
    val large = RoundedCornerShape(16.dp)

    /** 24.dp - Bottom sheets, modals, dialogs */
    val extraLarge = RoundedCornerShape(24.dp)

    /** 50% - Pills, circular buttons, fully rounded */
    val full = RoundedCornerShape(50)

    // ==========================================================================
    // COMPONENT-SPECIFIC SHAPES
    // ==========================================================================

    /** Poster cards - standard movie/TV posters */
    val posterCard = RoundedCornerShape(12.dp)

    /** Backdrop cards - wide cards with backdrop images */
    val backdropCard = RoundedCornerShape(16.dp)

    /** Hero carousel cards - large featured content */
    val heroCard = RoundedCornerShape(20.dp)

    /** Genre/filter chips */
    val chip = RoundedCornerShape(8.dp)

    /** Standard buttons */
    val button = RoundedCornerShape(12.dp)

    /** Search bar - pill shape */
    val searchBar = RoundedCornerShape(28.dp)

    /** Bottom sheet - rounded top corners only */
    val bottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    /** Dialog containers */
    val dialog = RoundedCornerShape(28.dp)

    /** Image overlay containers (like rating badges on images) */
    val imageOverlay = RoundedCornerShape(8.dp)

    /** Person avatar - fully circular */
    val avatar = RoundedCornerShape(50)
}

/** Material3 Shapes instance using cinematic shape tokens. */
val CinematicM3Shapes =
    Shapes(
        extraSmall = CinematicShapes.extraSmall,
        small = CinematicShapes.small,
        medium = CinematicShapes.medium,
        large = CinematicShapes.large,
        extraLarge = CinematicShapes.extraLarge,
    )
