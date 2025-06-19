package com.keisardev.moviesandbeyond.feature.search

// import androidx.navigation.NavController // Removed
// import androidx.navigation.NavGraphBuilder // Removed
// import androidx.navigation.NavOptions // Removed
// import androidx.navigation.compose.composable // Removed

import androidx.compose.runtime.Composable
import com.keisardev.moviesandbeyond.core.model.DetailsKey

// const val searchNavigationRoute = "search" // Removed

// This is the main entry composable for the Search feature,
// called from NavDisplay when SearchKey is active.
@Composable
fun searchScreen(
    navigateToDetails: (DetailsKey) -> Unit // Added parameter
) {
    SearchRoute(navigateToDetail = {
        DetailsKey(it)
    }) // Pass lambda to SearchRoute
}

// fun NavController.navigateToSearch(navOptions: NavOptions) { // Removed
//    navigate(searchNavigationRoute, navOptions)
// }