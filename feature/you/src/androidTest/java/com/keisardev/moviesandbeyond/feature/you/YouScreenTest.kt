package com.keisardev.moviesandbeyond.feature.you

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.keisardev.moviesandbeyond.core.model.SeedColor
import com.keisardev.moviesandbeyond.core.model.SelectedDarkMode
import com.keisardev.moviesandbeyond.core.model.user.AccountDetails
import org.junit.Rule
import org.junit.Test

class YouScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val noopCallbacks =
        YouScreenCallbacks(
            onChangeTheme = {},
            onChangeDarkMode = {},
            onChangeSeedColor = {},
            onChangeCustomColorArgb = {},
            onChangeIncludeAdult = {},
            onChangeUseLocalOnly = {},
            onNavigateToAuth = {},
            onLibraryItemClick = {},
            onReloadAccountDetailsClick = {},
            onLogOutClick = {},
            onRefresh = {},
            onErrorShown = {})

    @Test
    fun youScreen_whenLoggedOut_showsLogInButton() {
        val logInText = composeTestRule.activity.getString(R.string.log_in)

        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(),
                isLoggedIn = false,
                userSettings = null,
                libraryItemCounts = LibraryItemCounts(favoritesCount = 0, watchlistCount = 0),
                callbacks = noopCallbacks)
        }

        composeTestRule.onNodeWithText(logInText).assertIsDisplayed()
    }

    @Test
    fun youScreen_whenLoggedIn_showsUsername() {
        composeTestRule.setContent {
            YouScreen(
                uiState =
                    YouUiState(
                        accountDetails =
                            AccountDetails(
                                id = 1,
                                name = "John Doe",
                                username = "johndoe",
                                avatar = "",
                                includeAdult = false,
                                gravatar = "",
                                iso6391 = "",
                                iso31661 = "")),
                isLoggedIn = true,
                userSettings =
                    UserSettings(
                        useDynamicColor = false,
                        includeAdultResults = false,
                        darkMode = SelectedDarkMode.SYSTEM,
                        seedColor = SeedColor.DEFAULT,
                        useLocalOnly = false,
                        customColorArgb = SeedColor.DEFAULT_CUSTOM_COLOR_ARGB),
                libraryItemCounts = LibraryItemCounts(favoritesCount = 3, watchlistCount = 5),
                callbacks = noopCallbacks)
        }

        composeTestRule.onNodeWithText("johndoe").assertIsDisplayed()
    }

    @Test
    fun youScreen_showsLibraryCounts() {
        val favoritesText = composeTestRule.activity.getString(R.string.favorites)
        val watchlistText = composeTestRule.activity.getString(R.string.watchlist)

        composeTestRule.setContent {
            YouScreen(
                uiState = YouUiState(),
                isLoggedIn = false,
                userSettings = null,
                libraryItemCounts = LibraryItemCounts(favoritesCount = 7, watchlistCount = 12),
                callbacks = noopCallbacks)
        }

        composeTestRule.onNodeWithText(favoritesText).assertIsDisplayed()
        composeTestRule.onNodeWithText(watchlistText).assertIsDisplayed()
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
        composeTestRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun youScreen_whenLoggedIn_showsLogOutButton() {
        val logOutText = composeTestRule.activity.getString(R.string.log_out)

        composeTestRule.setContent {
            YouScreen(
                uiState =
                    YouUiState(
                        accountDetails =
                            AccountDetails(
                                id = 1,
                                name = "Test",
                                username = "test",
                                avatar = "",
                                includeAdult = false,
                                gravatar = "",
                                iso6391 = "",
                                iso31661 = "")),
                isLoggedIn = true,
                userSettings = null,
                libraryItemCounts = LibraryItemCounts(favoritesCount = 0, watchlistCount = 0),
                callbacks = noopCallbacks)
        }

        composeTestRule.onNodeWithText(logOutText).assertIsDisplayed()
    }
}
