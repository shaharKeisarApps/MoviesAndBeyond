package com.keisardev.moviesandbeyond.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.keisardev.moviesandbeyond.core.ui.HazeScaffold
import com.keisardev.moviesandbeyond.feature.movies.navigateToMovies
import com.keisardev.moviesandbeyond.feature.search.navigateToSearch
import com.keisardev.moviesandbeyond.feature.tv.navigateToTvShows
import com.keisardev.moviesandbeyond.feature.you.navigateToYou
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondDestination
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondNavigation
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi


@OptIn(
    ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MoviesAndBeyondApp(
    hideOnboarding: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val hazeState = remember { HazeState() }
    val inputScale: HazeInputScale = HazeInputScale.Auto
    val style = CupertinoMaterials.ultraThin()

    val bottomBarDestinations = remember { MoviesAndBeyondDestination.entries }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomBarDestinations.any { destination ->
        currentDestination?.route?.contains(destination.name, true) == true
    }

    HazeScaffold(
        hazeState = hazeState,
        bottomBar = {
            if (showBottomBar)
            MoviesAndBeyondNavigationBar(
                destinations = bottomBarDestinations,
                currentDestination = currentDestination,
                onNavigateToDestination = { destination ->
                    navController.navigateToBottomBarDestination(destination)
                },
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = style)
                    .fillMaxWidth()
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        MoviesAndBeyondNavigation(
            hideOnboarding = hideOnboarding,
            navController = navController,
            paddingValues = padding
        )
    }
}

@Composable
fun MoviesAndBeyondNavigationBar(
    destinations: List<MoviesAndBeyondDestination>,
    currentDestination: NavDestination?,
    onNavigateToDestination: (MoviesAndBeyondDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier, containerColor = Color.Transparent) {
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