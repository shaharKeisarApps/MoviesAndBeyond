package com.keisardev.moviesandbeyond.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized dimension tokens for consistent sizing throughout the app. Includes cinematic card
 * sizes for premium movie discovery experience.
 *
 * Usage:
 * ```
 * Modifier.size(width = Dimens.cardWidth, height = Dimens.cardHeight)
 * ```
 */
object Dimens {
    // ==========================================================================
    // POSTER CARDS (2:3 aspect ratio)
    // ==========================================================================

    /** Small poster card - 100.dp x 150.dp - for compact rows */
    val posterSmallWidth = 100.dp
    val posterSmallHeight = 150.dp

    /** Medium poster card - 120.dp x 180.dp - standard rows */
    val posterMediumWidth = 120.dp
    val posterMediumHeight = 180.dp

    /** Large poster card - 140.dp x 210.dp - featured rows */
    val posterLargeWidth = 140.dp
    val posterLargeHeight = 210.dp

    // Legacy aliases for compatibility
    val cardWidth = posterMediumWidth
    val cardHeight = posterMediumHeight

    // ==========================================================================
    // BACKDROP CARDS (16:9 aspect ratio)
    // ==========================================================================

    /** Backdrop card height for trending/featured sections */
    val backdropCardHeight = 180.dp

    /** Hero carousel minimum height - 55% viewport equivalent */
    val heroMinHeight = 300.dp

    /** Hero carousel maximum height */
    val heroMaxHeight = 400.dp

    /** Featured/carousel card height - 200.dp */
    val featuredCardHeight = 200.dp

    // ==========================================================================
    // INFO CARDS (horizontal layout with poster + text)
    // ==========================================================================

    /** Info card height for search results */
    val infoCardHeight = 100.dp

    /** Info card poster width */
    val infoCardPosterWidth = 60.dp

    /** Info card poster height */
    val infoCardPosterHeight = 90.dp

    // ==========================================================================
    // RATING BADGES
    // ==========================================================================

    /** Small rating badge height - for card overlays */
    val ratingBadgeSmall = 28.dp

    /** Medium rating badge height - standard display */
    val ratingBadgeMedium = 36.dp

    /** Large rating badge height - featured/hero areas */
    val ratingBadgeLarge = 44.dp

    // ==========================================================================
    // PERSON CARDS
    // ==========================================================================

    /** Person card width */
    val personCardWidth = 80.dp

    /** Person card total height (image + text) */
    val personCardHeight = 120.dp

    /** Person avatar diameter */
    val personAvatarSize = 64.dp

    // ==========================================================================
    // LOADING
    // ==========================================================================

    /** Loading indicator container width - 110.dp */
    val loadingIndicatorWidth = 110.dp

    // ==========================================================================
    // PROFILE/AVATAR
    // ==========================================================================

    /** User avatar size - 64.dp */
    val avatarSize = 64.dp

    /** Standard icon size - 48.dp */
    val iconSize = 48.dp

    /** Small icon size - 24.dp */
    val iconSizeSmall = 24.dp

    /** Medium icon size - 32.dp */
    val iconSizeMedium = 32.dp

    /** Large icon size for empty states - 80.dp */
    val iconSizeLarge = 80.dp

    // ==========================================================================
    // TOUCH TARGETS
    // ==========================================================================

    /** Minimum touch target size per Material guidelines - 48.dp */
    val minTouchTarget = 48.dp

    /** List item minimum height - 42.dp */
    val listItemMinHeight = 42.dp

    // ==========================================================================
    // CHIPS
    // ==========================================================================

    /** Genre chip height - 32.dp */
    val chipHeight = 32.dp

    /** Filter chip height - 36.dp */
    val filterChipHeight = 36.dp

    // ==========================================================================
    // DETAIL SCREEN
    // ==========================================================================

    /** Detail backdrop collapsed height */
    val detailBackdropCollapsed = 100.dp

    /** Detail backdrop expanded height - 60% viewport equivalent */
    val detailBackdropExpanded = 360.dp

    /** Floating poster width on detail screen */
    val detailPosterWidth = 120.dp

    /** Floating poster height on detail screen */
    val detailPosterHeight = 180.dp

    // ==========================================================================
    // GRID
    // ==========================================================================

    /** Number of columns for content grid */
    const val gridColumns = 3

    /** Number of columns for search results */
    const val searchGridColumns = 2
}

/** Poster card size variants for different contexts. */
enum class PosterSize(val width: Dp, val height: Dp) {
    /** Small - 100x150dp - compact rows, recommendations */
    SMALL(Dimens.posterSmallWidth, Dimens.posterSmallHeight),

    /** Medium - 120x180dp - standard content rows */
    MEDIUM(Dimens.posterMediumWidth, Dimens.posterMediumHeight),

    /** Large - 140x210dp - featured content, first items */
    LARGE(Dimens.posterLargeWidth, Dimens.posterLargeHeight)
}

/** Rating badge size variants. */
enum class RatingBadgeSize(val height: Dp) {
    /** Small - for card overlays */
    SMALL(Dimens.ratingBadgeSmall),

    /** Medium - standard display */
    MEDIUM(Dimens.ratingBadgeMedium),

    /** Large - hero/featured areas */
    LARGE(Dimens.ratingBadgeLarge)
}
