package com.keisardev.moviesandbeyond.core.ui

/**
 * Type-safe shared element keys to prevent key mismatches between screens.
 *
 * Using data classes for keys ensures:
 * 1. Type safety - compiler catches mismatches
 * 2. Uniqueness - includes all relevant identifiers
 * 3. Equality - data class equals/hashCode work correctly
 *
 * Example usage:
 * ```
 * Modifier.sharedElement(
 *     state = rememberSharedContentState(
 *         key = MediaSharedElementKey(
 *             mediaId = movie.id,
 *             mediaType = MediaType.Movie,
 *             origin = "movies_feed",
 *             elementType = SharedElementType.Image
 *         )
 *     ),
 *     animatedVisibilityScope = animatedVisibilityScope
 * )
 * ```
 */
data class MediaSharedElementKey(
    val mediaId: Long,
    val mediaType: MediaType,
    val origin: String,
    val elementType: SharedElementType,
)

/**
 * Type of media content. Used to differentiate between movies, TV shows, and people in shared
 * element transitions.
 */
enum class MediaType {
    Movie,
    TvShow,
    Person,
}

/**
 * Type of shared element within a media item. Used to create unique keys for different parts of the
 * same item that transition between screens.
 */
enum class SharedElementType {
    /** The poster/backdrop image */
    Image,
    /** The title text */
    Title,
    /** The entire card container */
    Card,
    /** The rating display */
    Rating,
    /** The backdrop image (detail screens) */
    Backdrop,
}

/** Common origin values for shared element transitions. */
object SharedElementOrigin {
    const val MOVIES_FEED = "movies_feed"
    const val MOVIES_ITEMS = "movies_items"
    const val TV_FEED = "tv_feed"
    const val TV_ITEMS = "tv_items"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val DETAILS = "details"
}

/**
 * Helper function to parse a media ID string into its type and numeric ID.
 *
 * @param id The media ID in format "movie_{id}", "tv_{id}", or "person_{id}"
 * @return Pair of MediaType and Long ID, or null if parsing fails
 */
fun parseMediaId(id: String): Pair<MediaType, Long>? {
    return when {
        id.startsWith("movie_") -> {
            id.removePrefix("movie_").toLongOrNull()?.let { MediaType.Movie to it }
        }
        id.startsWith("tv_") -> {
            id.removePrefix("tv_").toLongOrNull()?.let { MediaType.TvShow to it }
        }
        id.startsWith("person_") -> {
            id.removePrefix("person_").toLongOrNull()?.let { MediaType.Person to it }
        }
        else -> null
    }
}
