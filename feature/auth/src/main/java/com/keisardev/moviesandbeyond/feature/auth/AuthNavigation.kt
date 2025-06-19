package com.keisardev.moviesandbeyond.feature.auth

import androidx.compose.runtime.Composable
// import androidx.navigation.NavController // Removed
// import androidx.navigation.NavGraphBuilder // Removed
// import androidx.navigation.compose.composable // Removed
import com.keisardev.moviesandbeyond.ui.navigation.NavManager

// private const val authScreenNavigationRoute = "auth" // Removed

// This is the main entry composable for the Auth feature,
// called from NavDisplay when AuthKey is active.
@Composable
fun authScreen(
    // onBackClick: () -> Unit, // Replaced by direct NavManager call in AuthRoute/AuthScreen
) {
    // AuthRoute will now handle its own back navigation via NavManager
    AuthRoute(onBackClick = { NavManager.navigateUp() })
}

// fun NavController.navigateToAuth() { // Removed
//    navigate(authScreenNavigationRoute)
// }