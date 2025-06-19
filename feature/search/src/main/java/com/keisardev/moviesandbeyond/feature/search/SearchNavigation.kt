package com.keisardev.moviesandbeyond.feature.search

import androidx.compose.runtime.Composable
// import androidx.navigation.NavController // Removed
// import androidx.navigation.NavGraphBuilder // Removed
// import androidx.navigation.NavOptions // Removed
// import androidx.navigation.compose.composable // Removed

// const val searchNavigationRoute = "search" // Removed

// This is the main entry composable for the Search feature,
// called from NavDisplay when SearchKey is active.
@Composable
fun searchScreen(
    // navigateToDetail: (String) -> Unit // Removed, SearchRoute will use NavManager
) {
    SearchRoute(/*navigateToDetail is handled within SearchRoute/SearchScreenInternal now*/)
}

// fun NavController.navigateToSearch(navOptions: NavOptions) { // Removed
//    navigate(searchNavigationRoute, navOptions)
// }