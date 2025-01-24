package com.keisardev.moviesandbeyond.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.keisardev.moviesandbeyond.feature.movies.navigateToMovies
import com.keisardev.moviesandbeyond.feature.search.navigateToSearch
import com.keisardev.moviesandbeyond.feature.tv.navigateToTvShows
import com.keisardev.moviesandbeyond.feature.you.navigateToYou
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondDestination
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondNavigation

@Composable
fun MoviesAndBeyondApp(
    hideOnboarding: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val bottomBarDestinations = remember { MoviesAndBeyondDestination.entries }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomBarDestinations.any { destination ->
        currentDestination?.route?.contains(destination.name, true) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MoviesAndBeyondNavigationBar(
                    destinations = bottomBarDestinations,
                    currentDestination = currentDestination,
                    onNavigateToDestination = { destination ->
                        navController.navigateToBottomBarDestination(destination)
                    }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MoviesAndBeyondNavigation(
                hideOnboarding = hideOnboarding,
                navController = navController
            )
        }
    }
}

@Composable
fun MoviesAndBeyondNavigationBar(
    destinations: List<MoviesAndBeyondDestination>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (MoviesAndBeyondDestination) -> Unit
) {
    NavigationBar {
        destinations.forEach { destination ->
            val selected = currentDestination.isDestinationInHierarchy(destination)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(id = destination.titleId)) }
            )
        }
    }
}

private fun NavDestination?.isDestinationInHierarchy(destination: MoviesAndBeyondDestination): Boolean {
    return this?.hierarchy?.any {
        it.route?.contains(destination.name, true) == true
    } == true
}

private fun NavController.navigateToBottomBarDestination(destination: MoviesAndBeyondDestination) {
    val navOptions = navOptions {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

    when (destination) {
        MoviesAndBeyondDestination.MOVIES -> navigateToMovies(navOptions)
        MoviesAndBeyondDestination.TV_SHOWS -> navigateToTvShows(navOptions)
        MoviesAndBeyondDestination.SEARCH -> navigateToSearch(navOptions)
        MoviesAndBeyondDestination.YOU -> navigateToYou(navOptions)
    }
}