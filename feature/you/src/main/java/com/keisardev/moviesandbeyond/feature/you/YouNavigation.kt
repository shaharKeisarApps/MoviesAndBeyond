package com.keisardev.moviesandbeyond.feature.you

import androidx.compose.runtime.Composable
// NavController and related imports are removed
// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.NavOptions
// import androidx.navigation.NavType
// import androidx.navigation.compose.composable
// import androidx.navigation.navArgument
// import androidx.navigation.navigation
// import com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute // This will be handled by NavDisplay

// import com.keisardev.moviesandbeyond.ui.navigation.NavManager // Removed
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.AuthKey // For lambda signature
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.LibraryItemsKey // For lambda signature

// Old route constants are no longer needed here
// private const val youNavigationGraphRoute = "you_nav_graph"
// private const val youNavigationRoute = "you"
// private const val libraryItemsNavigationRoute = "library_items"
// const val libraryItemTypeNavigationArgument = "type" // ViewModel may use this for SavedStateHandle

// This is the main entry composable for the You feature,
// called from NavDisplay when YouKey is active.
@Composable
fun youScreen(
    navigateToAuth: (AuthKey) -> Unit, // Added parameter
    navigateToLibraryItem: (LibraryItemsKey) -> Unit // Added parameter
) {
    YouRoute(
        navigateToAuth = { navigateToAuth(AuthKey) }, // Call passed lambda
        navigateToLibraryItem = { itemType -> navigateToLibraryItem(LibraryItemsKey(type = itemType)) } // Call passed lambda
        // navigateToDetails is handled within LibraryItemsScreen or directly if YouScreen shows items
    )
}

// NavController extensions are no longer needed.
// fun NavController.navigateToYou(navOptions: NavOptions) {
//    navigate(youNavigationRoute, navOptions)
// }

// fun NavController.navigateToLibraryItem(type: String) {
//    navigate("$libraryItemsNavigationRoute/$type")
// }