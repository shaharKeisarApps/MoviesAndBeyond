package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel // Hilt ViewModel still fine
// NavController and related imports are removed
// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.compose.composable
// import androidx.navigation.navigation

import com.keisardev.moviesandbeyond.ui.navigation.NavManager
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.AuthKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.CreditsKey // Assuming this key
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.DetailsKey // For navigating to other details screens if needed

// Old route constants are no longer used by this file for defining navigation graph structure
// private const val detailsNavigationRoute = "details"
// private const val creditsNavigationRoute = "credits"
// internal const val idNavigationArgument = "id" // ViewModel uses this for SavedStateHandle
// private const val detailsNavigationRouteWithArg = "$detailsNavigationRoute/{$idNavigationArgument}"


// This function is the main entry composable for the Details feature,
// called from the app's NavDisplay with a DetailsKey.
@Composable
fun detailsScreen(
    itemId: String, // From DetailsKey
    itemType: String, // From DetailsKey
    // navigateToAuth: () -> Unit // Replaced by NavManager call
    // HiltViewModel can be instantiated here or directly in DetailsRoute/CreditsRoute
    // For DetailsViewModel to get itemId and itemType, it relies on SavedStateHandle,
    // which should be populated by the navigation library from the arguments of the DetailsKey.
    // This requires that DetailsKey is @Serializable and the nav library supports this.
    // If not, itemId and itemType might need to be manually passed to the ViewModel constructor
    // (which is complex with Hilt unless using assisted inject factories more directly).
    // For now, assume ViewModel gets it from SavedStateHandle as before.
    detailsViewModel: DetailsViewModel = hiltViewModel()
) {
    // The DetailsKey directly leads to showing the DetailsRoute.
    // If Credits screen is active (e.g. NavManager.currentKey is CreditsKey),
    // the main NavDisplay would show CreditsRoute.
    // This composable is specifically for when DetailsKey is the current route.
    DetailsRoute(
        viewModel = detailsViewModel,
        onBackClick = { NavManager.navigateUp() },
        // onItemClick is for navigating to another details screen (e.g. person from movie cast)
        onItemClick = { id, type -> NavManager.navigateTo(DetailsKey(itemId = id, itemType = type)) },
        onSeeAllCastClick = {
            // Navigate to Credits screen, passing current item's ID and Type for context
            NavManager.navigateTo(CreditsKey(itemId = itemId, itemType = itemType))
        },
        navigateToAuth = { NavManager.navigateTo(AuthKey) }
    )
}

// CreditsScreenRoute would be defined here or in its own file,
// and registered with NavDisplay via CreditsKey
// For now, we assume CreditsScreen.kt contains CreditsRoute.


// NavController extensions are no longer needed.
// fun NavController.navigateToDetails(id: String) {
//    navigate("$detailsNavigationRoute/$id")
// }

// private fun NavController.navigateToCredits() {
//    navigate(creditsNavigationRoute)
// }