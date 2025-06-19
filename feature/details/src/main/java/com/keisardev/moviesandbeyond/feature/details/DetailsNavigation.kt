package com.keisardev.moviesandbeyond.feature.details

// NavController and related imports are removed
// import androidx.navigation.NavController
// import androidx.navigation.NavGraphBuilder
// import androidx.navigation.compose.composable
// import androidx.navigation.navigation

// import com.keisardev.moviesandbeyond.ui.navigation.NavManager // Removed
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.keisardev.moviesandbeyond.core.model.AuthKey
import com.keisardev.moviesandbeyond.core.model.CreditsKey
import com.keisardev.moviesandbeyond.core.model.DetailsKey

// Old route constants are no longer used by this file for defining navigation graph structure
// private const val detailsNavigationRoute = "details"
// private const val creditsNavigationRoute = "credits"
 internal const val idNavigationArgument = "id" // ViewModel uses this for SavedStateHandle
// private const val detailsNavigationRouteWithArg = "$detailsNavigationRoute/{$idNavigationArgument}"


// This function is the main entry composable for the Details feature,
// called from the app's NavDisplay with a DetailsKey.
@Composable
fun detailsScreen(
    itemId: String, // From DetailsKey
    detailsViewModel: DetailsViewModel = hiltViewModel(),
    // Navigation Lambdas passed from NavDisplay entry
    onBackClick: () -> Unit,
    navigateToDetails: (DetailsKey) -> Unit,
    navigateToCredits: (CreditsKey) -> Unit,
    navigateToAuth: (AuthKey) -> Unit
) {
    DetailsRoute(
        viewModel = detailsViewModel, // ViewModel gets itemId, itemType from SavedStateHandle
        onBackClick = onBackClick,
        onItemClick = { id -> navigateToDetails(DetailsKey(itemId = id)) },
        onSeeAllCastClick = {
            // Use current itemId and itemType for CreditsKey context
            navigateToCredits(CreditsKey(itemId = itemId))
        },
        navigateToAuth = { navigateToAuth(AuthKey) } // Pass AuthKey directly
    )
}

// CreditsScreenRoute would be defined here or in its own file,
// and registered with NavDisplay via CreditsKey
// For now, we assume CreditsScreen.kt contains CreditsRoute.


// NavController extensions are no longer needed.
// fun NavController.navigateToDetails(id: String) {
//    navigate("$detailsNavigationRoute/$id")
// }


