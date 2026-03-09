package com.keisardev.moviesandbeyond.feature.search

import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.keisardev.moviesandbeyond.core.model.SearchItem
import com.keisardev.moviesandbeyond.core.ui.LocalWindowSizeClass
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class SearchScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchScreen_initialState_showsSearchHint() {
        val searchHint = composeTestRule.activity.getString(R.string.search_hint)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides calculateWindowSizeClass(composeTestRule.activity)
            ) {
                SearchScreen(
                    searchQuery = "",
                    errorMessage = null,
                    searchSuggestions = persistentListOf(),
                    onSearchQueryChange = {},
                    onBack = {},
                    onSearchResultClick = {},
                    onErrorShown = {},
                )
            }
        }

        composeTestRule.onNodeWithText(searchHint).assertIsDisplayed()
    }

    @Test
    fun searchScreen_withResults_showsItems() {
        val testItem = SearchItem(id = 1, name = "Inception", mediaType = "movie", imagePath = "")

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides calculateWindowSizeClass(composeTestRule.activity)
            ) {
                SearchScreen(
                    searchQuery = "inc",
                    errorMessage = null,
                    searchSuggestions = persistentListOf(testItem),
                    onSearchQueryChange = {},
                    onBack = {},
                    onSearchResultClick = {},
                    onErrorShown = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Inception").assertIsDisplayed()
    }

    @Test
    fun searchScreen_whenError_showsSnackbar() {
        val errorMessage = "Search failed"

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides calculateWindowSizeClass(composeTestRule.activity)
            ) {
                SearchScreen(
                    searchQuery = "test",
                    errorMessage = errorMessage,
                    searchSuggestions = persistentListOf(),
                    onSearchQueryChange = {},
                    onBack = {},
                    onSearchResultClick = {},
                    onErrorShown = {},
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun searchScreen_emptyResults_showsNoResultsMessage() {
        val noResultsTitle = composeTestRule.activity.getString(R.string.no_results_title)

        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowSizeClass provides calculateWindowSizeClass(composeTestRule.activity)
            ) {
                SearchScreen(
                    searchQuery = "xyznonexistent",
                    errorMessage = null,
                    searchSuggestions = persistentListOf(),
                    onSearchQueryChange = {},
                    onBack = {},
                    onSearchResultClick = {},
                    onErrorShown = {},
                )
            }
        }

        composeTestRule.onNodeWithText(noResultsTitle).assertIsDisplayed()
    }
}
