package com.keisardev.moviesandbeyond.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized dimension tokens for consistent sizing throughout the app.
 *
 * Usage:
 * ```
 * Modifier.size(width = Dimens.cardWidth, height = Dimens.cardHeight)
 * ```
 */
object Dimens {
    // ========== Cards ==========

    /** Standard media card width - 120.dp */
    val cardWidth = 120.dp

    /** Standard media card height - 160.dp */
    val cardHeight = 160.dp

    /** Featured/carousel card height - 200.dp */
    val featuredCardHeight = 200.dp

    // ========== Loading ==========

    /** Loading indicator container width - 110.dp */
    val loadingIndicatorWidth = 110.dp

    // ========== Profile/Avatar ==========

    /** User avatar size - 64.dp */
    val avatarSize = 64.dp

    /** Standard icon size - 48.dp */
    val iconSize = 48.dp

    /** Small icon size - 24.dp */
    val iconSizeSmall = 24.dp

    // ========== Touch Targets ==========

    /** Minimum touch target size per Material guidelines - 48.dp */
    val minTouchTarget = 48.dp

    /** List item minimum height - 42.dp */
    val listItemMinHeight = 42.dp

    // ========== Grid ==========

    /** Number of columns for content grid */
    const val gridColumns = 3
}
