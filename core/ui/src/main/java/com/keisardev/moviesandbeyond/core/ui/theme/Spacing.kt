package com.keisardev.moviesandbeyond.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Cinematic spacing tokens for premium, breathable UI layouts.
 *
 * Usage:
 * ```
 * Modifier.padding(horizontal = Spacing.screenPadding)
 * Arrangement.spacedBy(Spacing.itemSpacing)
 * ```
 */
object Spacing {
    // ========== BASE SCALE ==========

    /** 2.dp - Minimal spacing (tight text, inline elements) */
    val xxxs = 2.dp

    /** 4.dp - Extra small gaps */
    val xxs = 4.dp

    /** 8.dp - Small spacing, inline elements */
    val xs = 8.dp

    /** 12.dp - Standard item spacing */
    val sm = 12.dp

    /** 16.dp - Medium spacing, generous padding */
    val md = 16.dp

    /** 24.dp - Large spacing, section internal */
    val lg = 24.dp

    /** 32.dp - Extra large, section-to-section */
    val xl = 32.dp

    /** 48.dp - Major separations */
    val xxl = 48.dp

    /** 64.dp - Huge spacing */
    val xxxl = 64.dp

    /** 80.dp - Maximum spacing */
    val huge = 80.dp

    // ========== Semantic Aliases ==========

    /** Screen edge horizontal padding - 16.dp for premium feel */
    val screenPadding = md

    /** Screen edge vertical padding - 12.dp */
    val screenPaddingVertical = sm

    /** Spacing between major content sections - 32.dp for breathing room */
    val sectionSpacing = xl

    /** Spacing between list/grid items - 12.dp */
    val itemSpacing = sm

    /** Spacing between cards in grids - 16.dp */
    val cardSpacing = md

    /** Spacing from section header to content - 16.dp */
    val headerSpacing = md

    /** Content padding for feed screens top - 8.dp */
    val feedTopPadding = xs

    /**
     * Content padding for feed screens bottom - accounts for floating navigation bar. The bar is
     * 80.dp height + 8.dp vertical padding + navigation bar insets (~48dp on most devices). Using
     * 100.dp ensures content isn't hidden behind the floating bar.
     */
    val feedBottomPadding = 100.dp

    /** Spacing between inline elements (text, icons) - 8.dp */
    val inlineSpacing = xs

    /** Spacing between chips - 8.dp */
    val chipSpacing = xs

    /** Spacing in hero/carousel areas - 24.dp */
    val heroSpacing = lg
}
