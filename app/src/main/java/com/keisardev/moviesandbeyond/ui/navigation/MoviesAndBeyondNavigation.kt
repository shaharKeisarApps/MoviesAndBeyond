package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.keisardev.moviesandbeyond.feature.auth.authScreen
import com.keisardev.moviesandbeyond.feature.auth.navigateToAuth
import com.keisardev.moviesandbeyond.feature.details.detailsScreen
import com.keisardev.moviesandbeyond.feature.details.navigateToDetails
import com.keisardev.moviesandbeyond.feature.movies.moviesNavigationRoute
import com.keisardev.moviesandbeyond.feature.movies.moviesScreen
import com.keisardev.moviesandbeyond.feature.search.searchScreen
import com.keisardev.moviesandbeyond.feature.tv.tvShowsScreen
import com.keisardev.moviesandbeyond.feature.you.youScreen
import com.keisardev.moviesandbeyond.ui.OnboardingScreen
import com.keisardev.moviesandbeyond.ui.onboardingNavigationRoute

@Composable
fun MoviesAndBeyondNavigation(
    hideOnboarding: Boolean,
    navController: NavHostController,
    paddingValues: PaddingValues,
) {
    val startDestination =
        if (hideOnboarding) {
            moviesNavigationRoute
        } else {
            onboardingNavigationRoute
        }

    NavHost(
        modifier = Modifier.padding(paddingValues),
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = onboardingNavigationRoute) {
            OnboardingScreen(navigateToAuth = navController::navigateToAuth)
        }

        authScreen(onBackClick = navController::navigateUp)

        moviesScreen(
            navController = navController,
            navigateToDetails = navController::navigateToDetails,
        )

        tvShowsScreen(
            navController = navController,
            navigateToDetails = navController::navigateToDetails,
        )

        searchScreen(navigateToDetail = navController::navigateToDetails)

        youScreen(
            navController = navController,
            navigateToAuth = navController::navigateToAuth,
            navigateToDetails = navController::navigateToDetails,
        )

        detailsScreen(navController = navController, navigateToAuth = navController::navigateToAuth)
    }
}
