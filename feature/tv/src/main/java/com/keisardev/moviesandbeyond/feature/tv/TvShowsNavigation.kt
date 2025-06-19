package com.keisardev.moviesandbeyond.feature.tv

import androidx.compose.runtime.Composable
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
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.TvItemsKey // Assuming this key

// private const val tvShowsNavigationRoute = "tv_shows" // No longer used

// This function is the main entry composable for the TV Shows feature,
// called from the app's NavDisplay.
@Composable
fun tvShowsScreen(
    // navigateToDetails: (itemId: String, itemType: String) -> Unit, // Removed, will use NavManager from within
    viewModel: TvShowsViewModel = hiltViewModel()
) {
    // Similar to moviesScreen, tvShowsScreen will directly show FeedRoute.
    // The original navigation graph had FEED as the start destination for "tv_shows" route.
    FeedRoute(
        // navigateToDetails is handled by NavManager from where the click occurs
        navigateToItems = { category ->
            NavManager.navigateTo(TvItemsKey(category = category)) // Use TvItemsKey
        },
        viewModel = viewModel
    )
}

// Screen internal routes are no longer needed here as NavManager uses keys.
// internal object TvShowsScreenRoutes {
//    const val FEED = "tv_shows_feed"
//    const val ITEMS = "tv_shows_items"
// }

// This extension is no longer needed as NavManager handles navigation.
// fun NavController.navigateToTvShows(navOptions: NavOptions) {
//    navigate(tvShowsNavigationRoute, navOptions)
// }