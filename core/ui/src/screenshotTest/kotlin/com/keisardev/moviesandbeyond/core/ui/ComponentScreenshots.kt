package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.RatingBadgeSize

/**
 * Screenshot tests for core UI components.
 *
 * These tests capture visual snapshots of reusable UI components in various states.
 *
 * Run commands:
 * - Generate reference images: ./gradlew :core:ui:updateDebugScreenshotTest
 * - Validate against references: ./gradlew :core:ui:validateDebugScreenshotTest
 */

// ============================================================================
// Rating Badge Screenshots
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Excellent (8.5)")
@Composable
fun RatingBadgeExcellent() {
    MaterialTheme { RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Good (7.2)")
@Composable
fun RatingBadgeGood() {
    MaterialTheme { RatingBadge(rating = 7.2, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Average (5.5)")
@Composable
fun RatingBadgeAverage() {
    MaterialTheme { RatingBadge(rating = 5.5, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Poor (3.2)")
@Composable
fun RatingBadgePoor() {
    MaterialTheme { RatingBadge(rating = 3.2, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - All Sizes")
@Composable
fun RatingBadgeSizes() {
    MaterialTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            RatingBadge(rating = 8.5, size = RatingBadgeSize.SMALL)
            RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM)
            RatingBadge(rating = 8.5, size = RatingBadgeSize.LARGE)
        }
    }
}

@PreviewTest
@Preview(showBackground = true, name = "CompactRatingBadge")
@Composable
fun CompactRatingBadgePreview() {
    MaterialTheme { CompactRatingBadge(rating = 7.8) }
}

@PreviewTest
@Preview(showBackground = true, name = "StarRating - Various")
@Composable
fun StarRatingVariants() {
    MaterialTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            StarRating(rating = 10.0) // 5 stars
            StarRating(rating = 7.0) // 3.5 stars
            StarRating(rating = 4.0) // 2 stars
            StarRating(rating = 2.0) // 1 star
        }
    }
}

// ============================================================================
// Genre Chip Screenshots
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "GenreChip - Default")
@Composable
fun GenreChipDefault() {
    MaterialTheme { GenreChip(text = "Action") }
}

@PreviewTest
@Preview(showBackground = true, name = "GenreChip - Selected")
@Composable
fun GenreChipSelected() {
    MaterialTheme { GenreChip(text = "Action", selected = true) }
}

@PreviewTest
@Preview(showBackground = true, name = "OutlinedGenreChip")
@Composable
fun OutlinedGenreChipPreview() {
    MaterialTheme { OutlinedGenreChip(text = "Science Fiction") }
}

@PreviewTest
@Preview(showBackground = true, name = "GenreChipRow")
@Composable
fun GenreChipRowPreview() {
    MaterialTheme {
        GenreChipRow(
            genres = listOf("Action", "Adventure", "Sci-Fi", "Drama", "Thriller"),
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Media Card Screenshots
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - Small")
@Composable
fun MediaItemCardSmall() {
    MaterialTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.SMALL) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - Medium")
@Composable
fun MediaItemCardMedium() {
    MaterialTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - Large")
@Composable
fun MediaItemCardLarge() {
    MaterialTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.LARGE) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - With Rating")
@Composable
fun MediaItemCardWithRating() {
    MaterialTheme {
        MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM, rating = 8.5)
    }
}

@PreviewTest
@Preview(showBackground = true, name = "SimpleMediaItemCard")
@Composable
fun SimpleMediaItemCardPreview() {
    MaterialTheme { SimpleMediaItemCard(posterPath = "/poster.jpg", size = PosterSize.SMALL) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaBackdropCard")
@Composable
fun MediaBackdropCardPreview() {
    MaterialTheme {
        MediaBackdropCard(
            backdropPath = "/backdrop.jpg",
            title = "The Dark Knight",
            year = "2008",
            rating = 9.0,
            genres = listOf("Action", "Crime", "Drama"),
        )
    }
}

// ============================================================================
// Person Card Screenshots
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "PersonCard - With Image")
@Composable
fun PersonCardWithImage() {
    MaterialTheme {
        PersonCard(imagePath = "/profile.jpg", name = "Christian Bale", role = "Bruce Wayne")
    }
}

@PreviewTest
@Preview(showBackground = true, name = "PersonCard - No Image")
@Composable
fun PersonCardNoImage() {
    MaterialTheme { PersonCard(imagePath = null, name = "Christian Bale", role = "Bruce Wayne") }
}

// ============================================================================
// Empty State Screenshots
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "EmptyState - With Subtitle")
@Composable
fun EmptyStateWithSubtitle() {
    MaterialTheme { EmptyState(title = "No results found", subtitle = "Try a different search") }
}

@PreviewTest
@Preview(showBackground = true, name = "EmptyState - Title Only")
@Composable
fun EmptyStateTitleOnly() {
    MaterialTheme { EmptyState(title = "No favorites yet") }
}
