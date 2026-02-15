package com.keisardev.moviesandbeyond.feature.tv

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.core.model.content.TvShowListCategory
import org.junit.Rule
import org.junit.Test

class FeedScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun feedScreen_whenLoading_showsSectionTitles() {
        val airingTodayText = composeTestRule.activity.getString(R.string.airing_today)
        val onAirText = composeTestRule.activity.getString(R.string.on_air)

        composeTestRule.setContent {
            FeedScreen(
                airingTodayTvShows = ContentUiState(category = TvShowListCategory.AIRING_TODAY),
                onAirTvShows = ContentUiState(category = TvShowListCategory.ON_THE_AIR),
                topRatedTvShows = ContentUiState(category = TvShowListCategory.TOP_RATED),
                popularTvShows = ContentUiState(category = TvShowListCategory.POPULAR),
                errorMessage = null,
                appendItems = {},
                onItemClick = {},
                onSeeAllClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText(airingTodayText).assertIsDisplayed()
        composeTestRule.onNodeWithText(onAirText).assertIsDisplayed()
    }

    @Test
    fun feedScreen_withContent_showsHeroTitle() {
        val testShow = ContentItem(id = 1, imagePath = "", name = "Test TV Show", rating = 9.0)

        composeTestRule.setContent {
            FeedScreen(
                airingTodayTvShows = ContentUiState(category = TvShowListCategory.AIRING_TODAY),
                onAirTvShows = ContentUiState(category = TvShowListCategory.ON_THE_AIR),
                topRatedTvShows = ContentUiState(category = TvShowListCategory.TOP_RATED),
                popularTvShows =
                    ContentUiState(
                        items = listOf(testShow),
                        isLoading = false,
                        endReached = false,
                        page = 1,
                        category = TvShowListCategory.POPULAR),
                errorMessage = null,
                appendItems = {},
                onItemClick = {},
                onSeeAllClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText("Test TV Show").assertIsDisplayed()
    }

    @Test
    fun feedScreen_whenError_showsSnackbar() {
        val errorMessage = "Connection failed"

        composeTestRule.setContent {
            FeedScreen(
                airingTodayTvShows = ContentUiState(category = TvShowListCategory.AIRING_TODAY),
                onAirTvShows = ContentUiState(category = TvShowListCategory.ON_THE_AIR),
                topRatedTvShows = ContentUiState(category = TvShowListCategory.TOP_RATED),
                popularTvShows = ContentUiState(category = TvShowListCategory.POPULAR),
                errorMessage = errorMessage,
                appendItems = {},
                onItemClick = {},
                onSeeAllClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
