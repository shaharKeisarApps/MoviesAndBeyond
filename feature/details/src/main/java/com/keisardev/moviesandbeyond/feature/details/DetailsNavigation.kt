package com.keisardev.moviesandbeyond.feature.details

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel // Hilt ViewModel still fine
// NavController and related imports are removed
// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.compose.composable
// import androidx.navigation.navigation

// import com.keisardev.moviesandbeyond.ui.navigation.NavManager // Removed
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.AuthKey // For lambda signature
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.CreditsKey // For lambda signature
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.DetailsKey // For lambda signature

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
    detailsViewModel: DetailsViewModel = hiltViewModel(),
    // Navigation Lambdas passed from NavDisplay entry
    onNavigateUp: () -> Unit,
    onNavigateToDetails: (DetailsKey) -> Unit,
    onNavigateToCredits: (CreditsKey) -> Unit,
    onNavigateToAuth: (AuthKey) -> Unit
) {
    DetailsRoute(
        viewModel = detailsViewModel, // ViewModel gets itemId, itemType from SavedStateHandle
        onBackClick = onNavigateUp,
        onItemClick = { id, type -> onNavigateToDetails(DetailsKey(itemId = id, itemType = type)) },
        onSeeAllCastClick = {
            // Use current itemId and itemType for CreditsKey context
            onNavigateToCredits(CreditsKey(itemId = itemId, itemType = itemType))
        },
        navigateToAuth = { onNavigateToAuth(AuthKey) } // Pass AuthKey directly
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