package com.keisardev.moviesandbeyond.feature.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val authScreenNavigationRoute = "auth"

/** Registers the TMDB authentication screen composable in the navigation graph. */
fun NavGraphBuilder.authScreen(onBackClick: () -> Unit) {
    composable(route = authScreenNavigationRoute) { AuthRoute(onBackClick = onBackClick) }
}

/** Navigates to the TMDB authentication screen. */
fun NavController.navigateToAuth() {
    navigate(authScreenNavigationRoute)
}
