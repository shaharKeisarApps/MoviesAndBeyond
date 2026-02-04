package com.keisardev.moviesandbeyond.core.ui.theme

/**
 * Content-specific dimension standards for consistent sizing throughout the app.
 *
 * This object provides semantic dimension values organized by content type. Use these values
 * instead of hardcoding dimensions for:
 * - Consistent visual appearance across screens
 * - Easy maintenance and design updates
 * - Proper aspect ratio handling
 *
 * ## Aspect Ratios
 * - **Poster**: 2:3 (0.667) - Standard movie/TV poster format
 * - **Backdrop**: 16:9 (1.778) - Standard widescreen format
 * - **Profile**: 1:1 (1.0) - Circular avatar format
 *
 * ## Usage Examples
 *
 * ### For LazyRow items (fixed size):
 * ```kotlin
 * MediaItemCard(
 *     posterPath = item.imagePath,
 *     size = PosterSize.MEDIUM,
 *     modifier = Modifier.size(PosterSize.MEDIUM.width, PosterSize.MEDIUM.height)
 * )
 * ```
 *
 * ### For LazyVerticalGrid items (adaptive size):
 * ```kotlin
 * MediaItemCard(
 *     posterPath = item.imagePath,
 *     modifier = Modifier.fillMaxWidth().aspectRatio(ContentDimensions.POSTER_ASPECT_RATIO)
 * )
 * ```
 *
 * ### For backdrop cards:
 * ```kotlin
 * MediaBackdropCard(
 *     backdropPath = item.backdropPath,
 *     modifier = Modifier.size(BackdropSize.MEDIUM.width, BackdropSize.MEDIUM.height)
 * )
 * ```
 */
object ContentDimensions {
    // ==========================================================================
    // ASPECT RATIOS
    // ==========================================================================

    /**
     * Standard movie/TV poster aspect ratio (2:3). Use with `Modifier.aspectRatio()` for responsive
     * sizing.
     */
    const val POSTER_ASPECT_RATIO = 2f / 3f

    /**
     * Standard widescreen backdrop aspect ratio (16:9). Use with `Modifier.aspectRatio()` for
     * responsive sizing.
     */
    const val BACKDROP_ASPECT_RATIO = 16f / 9f

    /**
     * Square profile image aspect ratio (1:1). Use with `Modifier.aspectRatio()` for responsive
     * sizing.
     */
    const val PROFILE_ASPECT_RATIO = 1f

    // ==========================================================================
    // GRID CONFIGURATION
    // ==========================================================================

    /**
     * Minimum cell width for adaptive grids. Grid will automatically calculate number of columns
     * based on available width.
     */
    val gridMinCellWidth = Dimens.gridCellMinWidth

    /** Standard spacing between grid items. */
    val gridSpacing = Dimens.gridItemSpacing

    /** Standard padding around grid content. */
    val gridPadding = Dimens.gridContentPadding

    // ==========================================================================
    // ROW CONFIGURATION
    // ==========================================================================

    /** Standard spacing between items in horizontal rows. */
    val rowItemSpacing = Spacing.itemSpacing

    /** Standard horizontal padding for row content (screen edges). */
    val rowHorizontalPadding = Spacing.screenPadding
}
