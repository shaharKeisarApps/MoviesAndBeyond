package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.keisardev.moviesandbeyond.core.ui.loading.ShimmerCard
import com.keisardev.moviesandbeyond.core.ui.loading.ShimmerGridCard
import com.keisardev.moviesandbeyond.core.ui.loading.ShimmerPersonCard
import com.keisardev.moviesandbeyond.core.ui.theme.PosterSize
import com.keisardev.moviesandbeyond.core.ui.theme.RatingBadgeSize

/**
 * Screenshot tests for core UI components.
 *
 * These tests capture visual snapshots of reusable UI components in various states. Each component
 * is tested in light mode, dark mode, and with large text.
 *
 * Run commands:
 * - Generate reference images: ./gradlew :core:ui:updateDebugScreenshotTest
 * - Validate against references: ./gradlew :core:ui:validateDebugScreenshotTest
 */

// ============================================================================
// Helper wrappers for theme variants
// ============================================================================

@Composable
private fun LightTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(), content = content)
}

@Composable
private fun DarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(), content = content)
}

@Composable
private fun LargeTextTheme(content: @Composable () -> Unit) {
    val largeDensity = Density(density = LocalDensity.current.density, fontScale = 1.5f)
    CompositionLocalProvider(LocalDensity provides largeDensity) {
        MaterialTheme(content = content)
    }
}

// ============================================================================
// Rating Badge Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Excellent (8.5)")
@Composable
fun RatingBadgeExcellent() {
    LightTheme { RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Good (7.2)")
@Composable
fun RatingBadgeGood() {
    LightTheme { RatingBadge(rating = 7.2, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Average (5.5)")
@Composable
fun RatingBadgeAverage() {
    LightTheme { RatingBadge(rating = 5.5, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - Poor (3.2)")
@Composable
fun RatingBadgePoor() {
    LightTheme { RatingBadge(rating = 3.2, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "RatingBadge - All Sizes")
@Composable
fun RatingBadgeSizes() {
    LightTheme {
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
    LightTheme { CompactRatingBadge(rating = 7.8) }
}

@PreviewTest
@Preview(showBackground = true, name = "StarRating - Various")
@Composable
fun StarRatingVariants() {
    LightTheme {
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
// Rating Badge Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - RatingBadge Excellent")
@Composable
fun RatingBadgeExcellentDark() {
    DarkTheme { RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - RatingBadge All Sizes")
@Composable
fun RatingBadgeSizesDark() {
    DarkTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            RatingBadge(rating = 8.5, size = RatingBadgeSize.SMALL)
            RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM)
            RatingBadge(rating = 8.5, size = RatingBadgeSize.LARGE)
        }
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - StarRating")
@Composable
fun StarRatingDark() {
    DarkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            StarRating(rating = 10.0)
            StarRating(rating = 7.0)
            StarRating(rating = 4.0)
        }
    }
}

// ============================================================================
// Rating Badge Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - RatingBadge")
@Composable
fun RatingBadgeLargeText() {
    LargeTextTheme { RatingBadge(rating = 8.5, size = RatingBadgeSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "LargeText - CompactRatingBadge")
@Composable
fun CompactRatingBadgeLargeText() {
    LargeTextTheme { CompactRatingBadge(rating = 7.8) }
}

// ============================================================================
// Genre Chip Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "GenreChip - Default")
@Composable
fun GenreChipDefault() {
    LightTheme { GenreChip(text = "Action") }
}

@PreviewTest
@Preview(showBackground = true, name = "GenreChip - Selected")
@Composable
fun GenreChipSelected() {
    LightTheme { GenreChip(text = "Action", selected = true) }
}

@PreviewTest
@Preview(showBackground = true, name = "OutlinedGenreChip")
@Composable
fun OutlinedGenreChipPreview() {
    LightTheme { OutlinedGenreChip(text = "Science Fiction") }
}

@PreviewTest
@Preview(showBackground = true, name = "GenreChipRow")
@Composable
fun GenreChipRowPreview() {
    LightTheme {
        GenreChipRow(
            genres = listOf("Action", "Adventure", "Sci-Fi", "Drama", "Thriller"),
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Genre Chip Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - GenreChip Default")
@Composable
fun GenreChipDefaultDark() {
    DarkTheme { GenreChip(text = "Action") }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - GenreChip Selected")
@Composable
fun GenreChipSelectedDark() {
    DarkTheme { GenreChip(text = "Action", selected = true) }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - OutlinedGenreChip")
@Composable
fun OutlinedGenreChipDark() {
    DarkTheme { OutlinedGenreChip(text = "Science Fiction") }
}

// ============================================================================
// Genre Chip Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - GenreChip")
@Composable
fun GenreChipLargeText() {
    LargeTextTheme { GenreChip(text = "Action") }
}

@PreviewTest
@Preview(showBackground = true, name = "LargeText - GenreChipRow")
@Composable
fun GenreChipRowLargeText() {
    LargeTextTheme {
        GenreChipRow(
            genres = listOf("Action", "Adventure", "Sci-Fi"),
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Media Card Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - Small")
@Composable
fun MediaItemCardSmall() {
    LightTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.SMALL) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - Medium")
@Composable
fun MediaItemCardMedium() {
    LightTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - Large")
@Composable
fun MediaItemCardLarge() {
    LightTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.LARGE) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaItemCard - With Rating")
@Composable
fun MediaItemCardWithRating() {
    LightTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM, rating = 8.5) }
}

@PreviewTest
@Preview(showBackground = true, name = "SimpleMediaItemCard")
@Composable
fun SimpleMediaItemCardPreview() {
    LightTheme { SimpleMediaItemCard(posterPath = "/poster.jpg", size = PosterSize.SMALL) }
}

@PreviewTest
@Preview(showBackground = true, name = "MediaBackdropCard")
@Composable
fun MediaBackdropCardPreview() {
    LightTheme {
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
// Media Card Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - MediaItemCard Medium")
@Composable
fun MediaItemCardMediumDark() {
    DarkTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM) }
}

@PreviewTest
@Preview(
    showBackground = true,
    backgroundColor = 0xFF121212,
    name = "Dark - MediaItemCard With Rating",
)
@Composable
fun MediaItemCardWithRatingDark() {
    DarkTheme { MediaItemCard(posterPath = "/poster.jpg", size = PosterSize.MEDIUM, rating = 8.5) }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - MediaBackdropCard")
@Composable
fun MediaBackdropCardDark() {
    DarkTheme {
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
// Person Card Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "PersonCard - With Image")
@Composable
fun PersonCardWithImage() {
    LightTheme {
        PersonCard(imagePath = "/profile.jpg", name = "Christian Bale", role = "Bruce Wayne")
    }
}

@PreviewTest
@Preview(showBackground = true, name = "PersonCard - No Image")
@Composable
fun PersonCardNoImage() {
    LightTheme { PersonCard(imagePath = null, name = "Christian Bale", role = "Bruce Wayne") }
}

// ============================================================================
// Person Card Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - PersonCard")
@Composable
fun PersonCardDark() {
    DarkTheme {
        PersonCard(imagePath = "/profile.jpg", name = "Christian Bale", role = "Bruce Wayne")
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - PersonCard No Image")
@Composable
fun PersonCardNoImageDark() {
    DarkTheme { PersonCard(imagePath = null, name = "Christian Bale", role = "Bruce Wayne") }
}

// ============================================================================
// Person Card Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - PersonCard")
@Composable
fun PersonCardLargeText() {
    LargeTextTheme {
        PersonCard(imagePath = "/profile.jpg", name = "Christian Bale", role = "Bruce Wayne")
    }
}

// ============================================================================
// Empty State Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "EmptyState - With Subtitle")
@Composable
fun EmptyStateWithSubtitle() {
    LightTheme { EmptyState(title = "No results found", subtitle = "Try a different search") }
}

@PreviewTest
@Preview(showBackground = true, name = "EmptyState - Title Only")
@Composable
fun EmptyStateTitleOnly() {
    LightTheme { EmptyState(title = "No favorites yet") }
}

// ============================================================================
// Empty State Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - EmptyState")
@Composable
fun EmptyStateDark() {
    DarkTheme { EmptyState(title = "No results found", subtitle = "Try a different search") }
}

// ============================================================================
// Empty State Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - EmptyState")
@Composable
fun EmptyStateLargeText() {
    LargeTextTheme { EmptyState(title = "No results found", subtitle = "Try a different search") }
}

// ============================================================================
// Content Section Header Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "ContentSectionHeader - With Arrow")
@Composable
fun ContentSectionHeaderWithArrow() {
    LightTheme {
        ContentSectionHeader(
            sectionName = "Popular Movies",
            onSeeAllClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@PreviewTest
@Preview(showBackground = true, name = "ContentSectionHeader - No Arrow")
@Composable
fun ContentSectionHeaderNoArrow() {
    LightTheme {
        ContentSectionHeader(
            sectionName = "Featured",
            onSeeAllClick = null,
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Content Section Header Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - ContentSectionHeader")
@Composable
fun ContentSectionHeaderDark() {
    DarkTheme {
        ContentSectionHeader(
            sectionName = "Popular Movies",
            onSeeAllClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Content Section Header Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - ContentSectionHeader")
@Composable
fun ContentSectionHeaderLargeText() {
    LargeTextTheme {
        ContentSectionHeader(
            sectionName = "Popular Movies",
            onSeeAllClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Button Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LibraryActionButton")
@Composable
fun LibraryActionButtonPreview() {
    LightTheme {
        LibraryActionButton(
            name = "Favorite",
            icon = Icons.Rounded.Favorite,
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Button Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - LibraryActionButton")
@Composable
fun LibraryActionButtonDark() {
    DarkTheme {
        LibraryActionButton(
            name = "Favorite",
            icon = Icons.Rounded.Favorite,
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Button Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - LibraryActionButton")
@Composable
fun LibraryActionButtonLargeText() {
    LargeTextTheme {
        LibraryActionButton(
            name = "Favorite",
            icon = Icons.Rounded.Favorite,
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

// ============================================================================
// Search Bar Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "SearchBar - Empty")
@Composable
fun SearchBarEmpty() {
    LightTheme { MoviesAndBeyondSearchBar(value = "", onQueryChange = {}) }
}

@PreviewTest
@Preview(showBackground = true, name = "SearchBar - With Text")
@Composable
fun SearchBarWithText() {
    LightTheme { MoviesAndBeyondSearchBar(value = "The Dark Knight", onQueryChange = {}) }
}

// ============================================================================
// Search Bar Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - SearchBar Empty")
@Composable
fun SearchBarEmptyDark() {
    DarkTheme { MoviesAndBeyondSearchBar(value = "", onQueryChange = {}) }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - SearchBar With Text")
@Composable
fun SearchBarWithTextDark() {
    DarkTheme { MoviesAndBeyondSearchBar(value = "The Dark Knight", onQueryChange = {}) }
}

// ============================================================================
// Search Bar Screenshots - Large Text
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "LargeText - SearchBar")
@Composable
fun SearchBarLargeText() {
    LargeTextTheme { MoviesAndBeyondSearchBar(value = "Batman", onQueryChange = {}) }
}

// ============================================================================
// Shimmer / Loading Screenshots - Light
// ============================================================================

@PreviewTest
@Preview(showBackground = true, name = "ShimmerCard - Medium")
@Composable
fun ShimmerCardMediumPreview() {
    LightTheme { ShimmerCard(size = PosterSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, name = "ShimmerCard - Small")
@Composable
fun ShimmerCardSmallPreview() {
    LightTheme { ShimmerCard(size = PosterSize.SMALL) }
}

@PreviewTest
@Preview(showBackground = true, name = "ShimmerGridCard")
@Composable
fun ShimmerGridCardPreview() {
    LightTheme { ShimmerGridCard() }
}

@PreviewTest
@Preview(showBackground = true, name = "ShimmerPersonCard")
@Composable
fun ShimmerPersonCardPreview() {
    LightTheme { ShimmerPersonCard() }
}

// ============================================================================
// Shimmer / Loading Screenshots - Dark
// ============================================================================

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - ShimmerCard")
@Composable
fun ShimmerCardDark() {
    DarkTheme { ShimmerCard(size = PosterSize.MEDIUM) }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - ShimmerGridCard")
@Composable
fun ShimmerGridCardDark() {
    DarkTheme { ShimmerGridCard() }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark - ShimmerPersonCard")
@Composable
fun ShimmerPersonCardDark() {
    DarkTheme { ShimmerPersonCard() }
}
