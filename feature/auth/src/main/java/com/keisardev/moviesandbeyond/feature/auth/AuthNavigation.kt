package com.keisardev.moviesandbeyond.feature.auth

import androidx.compose.runtime.Composable
// import androidx.navigation.NavController // Removed
// import androidx.navigation.NavGraphBuilder // Removed
// import androidx.navigation.compose.composable // Removed
// import com.keisardev.moviesandbeyond.ui.navigation.NavManager // Removed

// private const val authScreenNavigationRoute = "auth" // Removed

// This is the main entry composable for the Auth feature,
// called from NavDisplay when AuthKey is active.
@Composable
fun AuthScreen(
    onBackClick: () -> Unit // Changed to accept lambda
) {
    AuthRoute(onBackClick = onBackClick) // Pass lambda to AuthRoute
}

// fun NavController.navigateToAuth() { // Removed
//    navigate(authScreenNavigationRoute)
// }