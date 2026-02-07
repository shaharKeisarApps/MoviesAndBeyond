package com.keisardev.moviesandbeyond.feature.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.keisardev.moviesandbeyond.core.model.SearchItem
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchScreen_initialState_showsSearchHint() {
        val searchHint = composeTestRule.activity.getString(R.string.search_hint)

        composeTestRule.setContent {
            SearchScreen(
                searchQuery = "",
                errorMessage = null,
                searchSuggestions = emptyList(),
                onSearchQueryChange = {},
                onBack = {},
                onSearchResultClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText(searchHint).assertIsDisplayed()
    }

    @Test
    fun searchScreen_withResults_showsItems() {
        val testItem = SearchItem(id = 1, name = "Inception", mediaType = "movie", imagePath = "")

        composeTestRule.setContent {
            SearchScreen(
                searchQuery = "Inception",
                errorMessage = null,
                searchSuggestions = listOf(testItem),
                onSearchQueryChange = {},
                onBack = {},
                onSearchResultClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText("Inception").assertIsDisplayed()
    }

    @Test
    fun searchScreen_whenError_showsSnackbar() {
        val errorMessage = "Search failed"

        composeTestRule.setContent {
            SearchScreen(
                searchQuery = "test",
                errorMessage = errorMessage,
                searchSuggestions = emptyList(),
                onSearchQueryChange = {},
                onBack = {},
                onSearchResultClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun searchScreen_emptyResults_showsNoResultsMessage() {
        val noResultsTitle = composeTestRule.activity.getString(R.string.no_results_title)

        composeTestRule.setContent {
            SearchScreen(
                searchQuery = "xyznonexistent",
                errorMessage = null,
                searchSuggestions = emptyList(),
                onSearchQueryChange = {},
                onBack = {},
                onSearchResultClick = {},
                onErrorShown = {})
        }

        composeTestRule.onNodeWithText(noResultsTitle).assertIsDisplayed()
    }
}
