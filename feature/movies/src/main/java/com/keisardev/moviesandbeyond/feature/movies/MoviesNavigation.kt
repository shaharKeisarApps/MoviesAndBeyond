package com.keisardev.moviesandbeyond.feature.movies

import androidx.compose.runtime.Composable
// import androidx.compose.runtime.remember // Might not be needed for NavController specific remember
import androidx.hilt.navigation.compose.hiltViewModel // Hilt ViewModel still fine
// NavController and related imports are removed
// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.NavOptions
// import androidx.navigation.NavType
// import androidx.navigation.compose.composable
// import androidx.navigation.navArgument
// import androidx.navigation.navigation

import com.keisardev.moviesandbeyond.ui.navigation.NavManager
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.DetailsKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.MoviesItemsKey // Assuming this key

// const val moviesNavigationRoute = "movies" // No longer used

// This function is now the main entry composable for the Movies feature,
// called from the app's NavDisplay.
// It no longer accepts NavController or navigateToDetails lambda directly.
// Internal navigation is handled by NavManager.
@Composable
fun moviesScreen(
    // navController: NavController, // Removed
    // navigateToDetails: (itemId: String, itemType: String) -> Unit // Removed, will use NavManager from within
    // The Hilt ViewModel can be injected here or within FeedRoute/ItemsRoute directly
    viewModel: MoviesViewModel = hiltViewModel() // ViewModel can be hoisted or directly used in sub-composables
) {
    // For this refactor, moviesScreen will directly show FeedRoute.
    // If MoviesKey was meant to show a screen that then allows navigation to Feed or Items,
    // that logic would be here.
    // The original navigation graph had FEED as the start destination for the "movies" route.
    FeedRoute(
        // navigateToDetails is handled by NavManager from where the click occurs
        navigateToItems = { category ->
            NavManager.navigateTo(MoviesItemsKey(category = category))
        },
        viewModel = viewModel
    )
    // The ItemsRoute would be shown if the current key in NavManager is MoviesItemsKey.
    // This logic is now part of the main NavDisplay entryProvider.
    // If MoviesKey is the only key for this feature in NavDisplay, then moviesScreen
    // would need internal state management to show FeedRoute vs ItemsRoute based on NavManager.backStack.
    // However, the prompt implies separate entries for distinct screens (e.g. DetailsKey).
    // So, it's more likely ItemsRoute will be registered with MoviesItemsKey in the main NavDisplay.
    // For this file, we only provide the composable content.
}

// ItemsScreenRoute composable would be defined here if it's a separate screen
// registered with NavDisplay via MoviesItemsKey.
// For now, we assume FeedScreen.kt and ItemsScreen.kt contain FeedRoute and ItemsRoute.

// Screen internal routes are no longer needed here as NavManager uses keys.
// internal object MoviesScreenRoutes {
//    const val FEED = "movies_feed"
//    const val ITEMS = "movies_items"
// }

// This extension is no longer needed as NavManager handles navigation.
// fun NavController.navigateToMovies(navOptions: NavOptions) {
//    navigate(moviesNavigationRoute, navOptions)
// }