package com.keisardev.moviesandbeyond.feature.tv

// NavController and related imports are removed
// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.NavOptions
// import androidx.navigation.NavType
// import androidx.navigation.compose.composable
// import androidx.navigation.navArgument
// import androidx.navigation.navigation

// import com.keisardev.moviesandbeyond.ui.navigation.NavManager // Removed
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.keisardev.moviesandbeyond.core.model.DetailsKey
import com.keisardev.moviesandbeyond.core.model.TvItemsKey

// private const val tvShowsNavigationRoute = "tv_shows" // No longer used

// This function is the main entry composable for the TV Shows feature,
// called from the app's NavDisplay.
@Composable
fun tvShowsScreen(
    viewModel: TvShowsViewModel = hiltViewModel(),
    navigateToDetails: (DetailsKey) -> Unit, // Added
    navigateToItems: (TvItemsKey) -> Unit // Added
) {
    // Similar to moviesScreen, tvShowsScreen will directly show FeedRoute.
    FeedRoute(
        navigateToDetails = {
            navigateToDetails(DetailsKey(itemId = it)) // Pass down
        }, // Pass down
        navigateToItems = { category ->      // Adapt to create TvItemsKey
            navigateToItems(TvItemsKey(category = category))
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