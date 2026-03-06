package com.keisardev.moviesandbeyond.feature.search

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val searchNavigationRoute = "search"

/** Registers the multi-search screen composable in the navigation graph. */
fun NavGraphBuilder.searchScreen(navigateToDetail: (String) -> Unit) {
    composable(route = searchNavigationRoute) { SearchRoute(navigateToDetail = navigateToDetail) }
}

/** Navigates to the search tab, applying the given [navOptions] for bottom bar behavior. */
fun NavController.navigateToSearch(navOptions: NavOptions) {
    navigate(searchNavigationRoute, navOptions)
}
