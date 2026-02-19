package com.keisardev.moviesandbeyond.feature.movies

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.MovieListCategory
import org.junit.Rule
import org.junit.Test

class FeedScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun feedScreen_whenLoading_showsSectionTitles() {
        val nowPlayingText = composeTestRule.activity.getString(R.string.now_playing)
        val popularText = composeTestRule.activity.getString(R.string.popular)

        composeTestRule.setContent {
            FeedScreen(
                nowPlayingMovies = ContentUiState(category = MovieListCategory.NOW_PLAYING),
                popularMovies = ContentUiState(category = MovieListCategory.POPULAR),
                topRatedMovies = ContentUiState(category = MovieListCategory.TOP_RATED),
                upcomingMovies = ContentUiState(category = MovieListCategory.UPCOMING),
                errorMessage = null,
                appendItems = {},
                onItemClick = {},
                onSeeAllClick = {},
                onErrorShown = {},
            )
        }

        composeTestRule.onNodeWithText(nowPlayingText).assertIsDisplayed()
        composeTestRule.onNodeWithText(popularText).assertIsDisplayed()
    }

    @Test
    fun feedScreen_withContent_showsHeroTitle() {
        val testMovie = ContentItem(id = 1, imagePath = "", name = "Test Movie", rating = 8.5)

        composeTestRule.setContent {
            FeedScreen(
                nowPlayingMovies = ContentUiState(category = MovieListCategory.NOW_PLAYING),
                popularMovies =
                    ContentUiState(
                        items = listOf(testMovie),
                        isLoading = false,
                        endReached = false,
                        page = 1,
                        category = MovieListCategory.POPULAR,
                    ),
                topRatedMovies = ContentUiState(category = MovieListCategory.TOP_RATED),
                upcomingMovies = ContentUiState(category = MovieListCategory.UPCOMING),
                errorMessage = null,
                appendItems = {},
                onItemClick = {},
                onSeeAllClick = {},
                onErrorShown = {},
            )
        }

        composeTestRule.onNodeWithText("Test Movie").assertIsDisplayed()
    }

    @Test
    fun feedScreen_whenError_showsSnackbar() {
        val errorMessage = "Network error"

        composeTestRule.setContent {
            FeedScreen(
                nowPlayingMovies = ContentUiState(category = MovieListCategory.NOW_PLAYING),
                popularMovies = ContentUiState(category = MovieListCategory.POPULAR),
                topRatedMovies = ContentUiState(category = MovieListCategory.TOP_RATED),
                upcomingMovies = ContentUiState(category = MovieListCategory.UPCOMING),
                errorMessage = errorMessage,
                appendItems = {},
                onItemClick = {},
                onSeeAllClick = {},
                onErrorShown = {},
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
