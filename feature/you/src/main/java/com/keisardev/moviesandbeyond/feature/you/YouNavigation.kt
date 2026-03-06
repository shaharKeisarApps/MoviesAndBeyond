package com.keisardev.moviesandbeyond.feature.you

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute

private const val youNavigationGraphRoute = "you_nav_graph"
private const val youNavigationRoute = "you"
private const val libraryItemsNavigationRoute = "library_items"
const val libraryItemTypeNavigationArgument = "type"

/** Registers the "You" nested navigation graph (profile + library items screens). */
fun NavGraphBuilder.youScreen(
    navController: NavController,
    navigateToAuth: () -> Unit,
    navigateToDetails: (String) -> Unit,
) {
    navigation(route = youNavigationGraphRoute, startDestination = youNavigationRoute) {
        composable(route = youNavigationRoute) {
            YouRoute(
                navigateToAuth = navigateToAuth,
                navigateToLibraryItem = navController::navigateToLibraryItem,
            )
        }
        composable(
            route = "$libraryItemsNavigationRoute/{$libraryItemTypeNavigationArgument}",
            arguments =
                listOf(navArgument(libraryItemTypeNavigationArgument) { type = NavType.StringType }),
        ) { backStackEntry ->
            val libraryItemType =
                backStackEntry.arguments?.getString(libraryItemTypeNavigationArgument)
            LibraryItemsRoute(
                onBackClick = navController::navigateUp,
                navigateToDetails = navigateToDetails,
                libraryItemType = libraryItemType,
            )
        }
    }
}

/** Navigates to the "You" profile tab, applying the given [navOptions] for bottom bar behavior. */
fun NavController.navigateToYou(navOptions: NavOptions) {
    navigate(youNavigationRoute, navOptions)
}

/** Navigates to the library items list for the given [type] (FAVORITE or WATCHLIST). */
fun NavController.navigateToLibraryItem(type: String) {
    navigate("$libraryItemsNavigationRoute/$type")
}
