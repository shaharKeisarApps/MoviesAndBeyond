package com.keisardev.moviesandbeyond.feature.movies

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory

/**
 * Screenshot tests for the Movies Feed screen.
 *
 * These tests capture visual snapshots of the FeedScreen in various states.
 *
 * Run commands:
 * - Generate reference images: ./gradlew :feature:movies:updateDebugScreenshotTest
 * - Validate against references: ./gradlew :feature:movies:validateDebugScreenshotTest
 */
private val previewItems =
    listOf(
        ContentItem(
            id = 1,
            imagePath = "/poster1.jpg",
            name = "Dune: Part Two",
            backdropPath = "/backdrop1.jpg",
            rating = 8.5,
            releaseDate = "2024-03-01",
            overview = "Paul Atreides unites with Chani and the Fremen.",
        ),
        ContentItem(
            id = 2,
            imagePath = "/poster2.jpg",
            name = "Oppenheimer",
            backdropPath = "/backdrop2.jpg",
            rating = 8.3,
            releaseDate = "2023-07-21",
            overview = "The story of J. Robert Oppenheimer.",
        ),
        ContentItem(
            id = 3,
            imagePath = "/poster3.jpg",
            name = "The Batman",
            backdropPath = "/backdrop3.jpg",
            rating = 7.8,
            releaseDate = "2022-03-04",
            overview = "Batman ventures into Gotham City's underworld.",
        ),
        ContentItem(
            id = 4,
            imagePath = "/poster4.jpg",
            name = "Avatar: The Way of Water",
            backdropPath = "/backdrop4.jpg",
            rating = 7.6,
            releaseDate = "2022-12-16",
            overview = "Jake Sully and Ney'tiri have formed a family.",
        ),
        ContentItem(
            id = 5,
            imagePath = "/poster5.jpg",
            name = "Top Gun: Maverick",
            backdropPath = "/backdrop5.jpg",
            rating = 8.2,
            releaseDate = "2022-05-27",
            overview = "After thirty years, Maverick is still pushing the envelope.",
        ),
    )

private fun createPreviewContentState(category: MovieListCategory) =
    ContentUiState(
        items = previewItems,
        isLoading = false,
        endReached = false,
        page = 1,
        category = category,
    )

@PreviewTest
@Preview(showBackground = true, name = "FeedScreen - Loading State")
@Composable
fun FeedScreenLoading() {
    MaterialTheme {
        FeedScreen(
            nowPlayingMovies = ContentUiState(MovieListCategory.NOW_PLAYING),
            popularMovies = ContentUiState(MovieListCategory.POPULAR),
            topRatedMovies = ContentUiState(MovieListCategory.TOP_RATED),
            upcomingMovies = ContentUiState(MovieListCategory.UPCOMING),
            errorMessage = null,
            appendItems = {},
            onItemClick = {},
            onSeeAllClick = {},
            onErrorShown = {},
        )
    }
}

@PreviewTest
@Preview(showBackground = true, name = "FeedScreen - With Content")
@Composable
fun FeedScreenWithContent() {
    MaterialTheme {
        FeedScreen(
            nowPlayingMovies = createPreviewContentState(MovieListCategory.NOW_PLAYING),
            popularMovies = createPreviewContentState(MovieListCategory.POPULAR),
            topRatedMovies = createPreviewContentState(MovieListCategory.TOP_RATED),
            upcomingMovies = createPreviewContentState(MovieListCategory.UPCOMING),
            errorMessage = null,
            appendItems = {},
            onItemClick = {},
            onSeeAllClick = {},
            onErrorShown = {},
        )
    }
}

@PreviewTest
@Preview(showBackground = true, name = "FeedScreen - Empty State")
@Composable
fun FeedScreenEmpty() {
    MaterialTheme {
        FeedScreen(
            nowPlayingMovies =
                ContentUiState(
                    items = emptyList(),
                    isLoading = false,
                    endReached = true,
                    page = 1,
                    category = MovieListCategory.NOW_PLAYING,
                ),
            popularMovies =
                ContentUiState(
                    items = emptyList(),
                    isLoading = false,
                    endReached = true,
                    page = 1,
                    category = MovieListCategory.POPULAR,
                ),
            topRatedMovies =
                ContentUiState(
                    items = emptyList(),
                    isLoading = false,
                    endReached = true,
                    page = 1,
                    category = MovieListCategory.TOP_RATED,
                ),
            upcomingMovies =
                ContentUiState(
                    items = emptyList(),
                    isLoading = false,
                    endReached = true,
                    page = 1,
                    category = MovieListCategory.UPCOMING,
                ),
            errorMessage = null,
            appendItems = {},
            onItemClick = {},
            onSeeAllClick = {},
            onErrorShown = {},
        )
    }
}

@PreviewTest
@Preview(showBackground = true, name = "FeedScreen - Partial Loading")
@Composable
fun FeedScreenPartialLoading() {
    MaterialTheme {
        FeedScreen(
            nowPlayingMovies = createPreviewContentState(MovieListCategory.NOW_PLAYING),
            popularMovies =
                createPreviewContentState(MovieListCategory.POPULAR).copy(isLoading = true),
            topRatedMovies = ContentUiState(MovieListCategory.TOP_RATED),
            upcomingMovies = ContentUiState(MovieListCategory.UPCOMING),
            errorMessage = null,
            appendItems = {},
            onItemClick = {},
            onSeeAllClick = {},
            onErrorShown = {},
        )
    }
}
