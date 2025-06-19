package com.keisardev.moviesandbeyond.feature.search

import androidx.compose.runtime.Composable
// import androidx.navigation.NavController // Removed
// import androidx.navigation.NavGraphBuilder // Removed
// import androidx.navigation.NavOptions // Removed
// import androidx.navigation.compose.composable // Removed

import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.DetailsKey // Added for lambda signature

// const val searchNavigationRoute = "search" // Removed

// This is the main entry composable for the Search feature,
// called from NavDisplay when SearchKey is active.
@Composable
fun searchScreen(
    navigateToDetail: (DetailsKey) -> Unit // Added parameter
) {
    SearchRoute(navigateToDetail = navigateToDetail) // Pass lambda to SearchRoute
}

// fun NavController.navigateToSearch(navOptions: NavOptions) { // Removed
//    navigate(searchNavigationRoute, navOptions)
// }