package com.keisardev.moviesandbeyond.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.keisardev.moviesandbeyond.core.ui.HazeScaffold
import com.keisardev.moviesandbeyond.core.ui.LocalSharedTransitionScope
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondDestination
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondNav3
import com.keisardev.moviesandbeyond.ui.navigation.NavigationState
import com.keisardev.moviesandbeyond.ui.navigation.TopLevelRoute
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(
    ExperimentalHazeApi::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class)
@Composable
fun MoviesAndBeyondApp(hideOnboarding: Boolean) {
    // Wrap entire app with SharedTransitionLayout for shared element transitions
    SharedTransitionLayout {
        // Provide SharedTransitionScope to all child composables
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val hazeState = remember { HazeState() }
            val style = CupertinoMaterials.ultraThin()

            // Navigation 3: Use NavigationState instead of NavController
            val navigationState = remember { NavigationState(hideOnboarding) }

            val bottomBarDestinations = remember { MoviesAndBeyondDestination.entries }

            // Determine if bottom bar should be shown based on current navigation state
            val showBottomBar =
                navigationState.hasCompletedOnboarding &&
                    isTopLevelDestination(navigationState.topLevelBackStack.topLevelKey)

            HazeScaffold(
                hazeState = hazeState,
                bottomBar = {
                    if (showBottomBar) {
                        MoviesAndBeyondNavigationBar(
                            destinations = bottomBarDestinations,
                            selectedDestination =
                                topLevelRouteToDestination(
                                    navigationState.topLevelBackStack.topLevelKey),
                            onNavigateToDestination = { destination ->
                                navigationState.topLevelBackStack.switchToTopLevel(
                                    destinationToTopLevelRoute(destination))
                            },
                            modifier =
                                Modifier.hazeEffect(state = hazeState, style = style)
                                    .fillMaxWidth())
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing) { padding ->
                    MoviesAndBeyondNav3(navigationState = navigationState, paddingValues = padding)
                }
        }
    }
}

@Composable
fun MoviesAndBeyondNavigationBar(
    destinations: List<MoviesAndBeyondDestination>,
    selectedDestination: MoviesAndBeyondDestination?,
    onNavigateToDestination: (MoviesAndBeyondDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier, containerColor = Color.Transparent) {
        destinations.forEach { destination ->
            val selected = destination == selectedDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null)
                },
                label = { Text(stringResource(id = destination.titleId)) })
        }
    }
}

/** Convert a TopLevelRoute to a MoviesAndBeyondDestination enum. */
private fun topLevelRouteToDestination(route: Any?): MoviesAndBeyondDestination? {
    return when (route) {
        is TopLevelRoute.Movies -> MoviesAndBeyondDestination.MOVIES
        is TopLevelRoute.TvShows -> MoviesAndBeyondDestination.TV_SHOWS
        is TopLevelRoute.Search -> MoviesAndBeyondDestination.SEARCH
        is TopLevelRoute.You -> MoviesAndBeyondDestination.YOU
        else -> null
    }
}

/** Convert a MoviesAndBeyondDestination enum to a TopLevelRoute. */
private fun destinationToTopLevelRoute(destination: MoviesAndBeyondDestination): TopLevelRoute {
    return when (destination) {
        MoviesAndBeyondDestination.MOVIES -> TopLevelRoute.Movies
        MoviesAndBeyondDestination.TV_SHOWS -> TopLevelRoute.TvShows
        MoviesAndBeyondDestination.SEARCH -> TopLevelRoute.Search
        MoviesAndBeyondDestination.YOU -> TopLevelRoute.You
    }
}

/** Check if the current route is a top-level destination (for showing bottom bar). */
private fun isTopLevelDestination(route: Any?): Boolean {
    return route is TopLevelRoute
}
