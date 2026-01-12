package com.keisardev.moviesandbeyond.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized spacing tokens for consistent UI throughout the app.
 *
 * Usage:
 * ```
 * Modifier.padding(horizontal = Spacing.screenPadding)
 * Arrangement.spacedBy(Spacing.itemSpacing)
 * ```
 */
object Spacing {
    /** 2.dp - Minimal spacing (text gaps, tight elements) */
    val xxs = 2.dp

    /** 4.dp - Small gaps, list separators */
    val xs = 4.dp

    /** 8.dp - Standard item spacing, horizontal padding */
    val sm = 8.dp

    /** 12.dp - Section internal spacing, header to content */
    val md = 12.dp

    /** 16.dp - Larger spacing, dialog padding */
    val lg = 16.dp

    /** 24.dp - Section-to-section spacing */
    val xl = 24.dp

    /** 32.dp - Large spacing, major separations */
    val xxl = 32.dp

    // ========== Semantic Aliases ==========

    /** Screen edge horizontal padding - 8.dp */
    val screenPadding = sm

    /** Spacing between major content sections - 24.dp */
    val sectionSpacing = xl

    /** Spacing between list/grid items - 8.dp */
    val itemSpacing = sm

    /** Spacing from section header to content - 12.dp */
    val headerSpacing = md

    /** Content padding for feed screens top - 4.dp */
    val feedTopPadding = xs

    /** Content padding for feed screens bottom - 8.dp */
    val feedBottomPadding = sm
}
