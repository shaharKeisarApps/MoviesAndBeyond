package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

private const val detailsNavigationRoute = "details"
private const val creditsNavigationRoute = "credits"
internal const val idNavigationArgument = "id"
private const val detailsNavigationRouteWithArg = "$detailsNavigationRoute/{$idNavigationArgument}"

/**
 * Registers the details nested navigation graph (detail + credits screens).
 *
 * The ViewModel is scoped to the nested graph so it is shared between the detail and credits
 * screens.
 */
fun NavGraphBuilder.detailsScreen(navController: NavController, navigateToAuth: () -> Unit) {
    navigation(route = detailsNavigationRouteWithArg, startDestination = detailsNavigationRoute) {
        composable(route = detailsNavigationRoute) { backStackEntry ->
            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(detailsNavigationRouteWithArg)
                }
            val viewModel = hiltViewModel<DetailsViewModel>(parentEntry)

            DetailsRoute(
                onBackClick = navController::navigateUp,
                onItemClick = navController::navigateToDetails,
                onSeeAllCastClick = navController::navigateToCredits,
                navigateToAuth = navigateToAuth,
                viewModel = viewModel,
            )
        }

        composable(route = creditsNavigationRoute) { backStackEntry ->
            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(detailsNavigationRouteWithArg)
                }
            val viewModel = hiltViewModel<DetailsViewModel>(parentEntry)

            CreditsRoute(
                viewModel = viewModel,
                onItemClick = navController::navigateToDetails,
                onBackClick = navController::navigateUp,
            )
        }
    }
}

/**
 * Navigates to the detail screen for a movie, TV show, or person.
 *
 * @param id Encoded detail identifier in the format `"tmdbId,MEDIA_TYPE"`
 */
fun NavController.navigateToDetails(id: String) {
    navigate("$detailsNavigationRoute/$id")
}

private fun NavController.navigateToCredits() {
    navigate(creditsNavigationRoute)
}
