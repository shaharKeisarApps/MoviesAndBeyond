package com.keisardev.moviesandbeyond.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.keisardev.moviesandbeyond.core.ui.AnimatedNavigationItem
import com.keisardev.moviesandbeyond.core.ui.FloatingNavigationBar
import com.keisardev.moviesandbeyond.core.ui.HazeScaffold
import com.keisardev.moviesandbeyond.core.ui.LocalSharedTransitionScope
import com.keisardev.moviesandbeyond.core.ui.LocalUseNavigationRail
import com.keisardev.moviesandbeyond.core.ui.LocalWindowSizeClass
import com.keisardev.moviesandbeyond.core.ui.isExpandedOrMediumWidth
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondDestination
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondNav3
import com.keisardev.moviesandbeyond.ui.navigation.NavigationState
import com.keisardev.moviesandbeyond.ui.navigation.TopLevelRoute
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState

@OptIn(
    ExperimentalHazeApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun MoviesAndBeyondApp(hideOnboarding: Boolean) {
    // Wrap entire app with SharedTransitionLayout for shared element transitions
    SharedTransitionLayout {
        // Provide SharedTransitionScope to all child composables
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val hazeState = remember { HazeState() }

            // Navigation 3: Use NavigationState instead of NavController
            val navigationState = remember(hideOnboarding) { NavigationState(hideOnboarding) }

            val destinations = remember { MoviesAndBeyondDestination.entries }

            val showNavigation =
                navigationState.hasCompletedOnboarding &&
                    isTopLevelDestination(navigationState.topLevelBackStack.topLevelKey)

            val selectedDestination =
                topLevelRouteToDestination(navigationState.topLevelBackStack.topLevelKey)

            val onNavigate = { destination: MoviesAndBeyondDestination ->
                navigationState.topLevelBackStack.switchToTopLevel(
                    destinationToTopLevelRoute(destination)
                )
            }

            val useRail = LocalWindowSizeClass.current.isExpandedOrMediumWidth()

            CompositionLocalProvider(LocalUseNavigationRail provides (useRail && showNavigation)) {
                if (useRail) {
                    RailContent(
                        hazeState = hazeState,
                        navigationState = navigationState,
                        destinations = destinations,
                        selectedDestination = selectedDestination,
                        onNavigate = onNavigate,
                        showNavigation = showNavigation,
                    )
                } else {
                    CompactContent(
                        hazeState = hazeState,
                        navigationState = navigationState,
                        destinations = destinations,
                        selectedDestination = selectedDestination,
                        onNavigate = onNavigate,
                        showNavigation = showNavigation,
                    )
                }
            }
        }
    }
}

/** Compact layout: HazeScaffold with FloatingNavigationBar at the bottom. */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun CompactContent(
    hazeState: HazeState,
    navigationState: NavigationState,
    destinations: List<MoviesAndBeyondDestination>,
    selectedDestination: MoviesAndBeyondDestination?,
    onNavigate: (MoviesAndBeyondDestination) -> Unit,
    showNavigation: Boolean,
) {
    HazeScaffold(
        hazeState = hazeState,
        blurBottomBar = false,
        bottomBar = {
            if (showNavigation) {
                MoviesAndBeyondNavigationBar(
                    hazeState = hazeState,
                    destinations = destinations,
                    selectedDestination = selectedDestination,
                    onNavigateToDestination = onNavigate,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        MoviesAndBeyondNav3(navigationState = navigationState, paddingValues = padding)
    }
}

/** Medium+ layout: NavigationRail on the left with HazeScaffold filling the remaining width. */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun RailContent(
    hazeState: HazeState,
    navigationState: NavigationState,
    destinations: List<MoviesAndBeyondDestination>,
    selectedDestination: MoviesAndBeyondDestination?,
    onNavigate: (MoviesAndBeyondDestination) -> Unit,
    showNavigation: Boolean,
) {
    Row(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            )
            .clipToBounds()
    ) {
        if (showNavigation) {
            MoviesAndBeyondNavigationRail(
                destinations = destinations,
                selectedDestination = selectedDestination,
                onNavigateToDestination = onNavigate,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            )
        }
        HazeScaffold(
            hazeState = hazeState,
            blurBottomBar = false,
            contentWindowInsets = WindowInsets.statusBars,
            modifier = Modifier.weight(1f),
        ) { padding ->
            MoviesAndBeyondNav3(navigationState = navigationState, paddingValues = padding)
        }
    }
}

/**
 * A TIVI-style floating navigation bar with animated navigation items.
 *
 * Features:
 * - Blur effect on the bar itself (not surrounding area)
 * - Transparent padding for true floating effect
 * - Spring scale animation on selected items
 * - Crossfade icon transitions between selected/unselected states
 */
@Composable
fun MoviesAndBeyondNavigationBar(
    hazeState: HazeState,
    destinations: List<MoviesAndBeyondDestination>,
    selectedDestination: MoviesAndBeyondDestination?,
    onNavigateToDestination: (MoviesAndBeyondDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingNavigationBar(
        hazeState = hazeState,
        modifier = modifier,
        containerColor = Color.Transparent,
    ) {
        destinations.forEach { destination ->
            val selected = destination == selectedDestination
            AnimatedNavigationItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                selectedIcon = {
                    Icon(imageVector = destination.selectedIcon, contentDescription = null)
                },
                unselectedIcon = {
                    Icon(imageVector = destination.icon, contentDescription = null)
                },
                label = { Text(stringResource(id = destination.titleId)) },
            )
        }
    }
}

/** M3 NavigationRail shown on the left edge for medium+ width windows. */
@Composable
fun MoviesAndBeyondNavigationRail(
    destinations: List<MoviesAndBeyondDestination>,
    selectedDestination: MoviesAndBeyondDestination?,
    onNavigateToDestination: (MoviesAndBeyondDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationRail(modifier = modifier) {
        destinations.forEach { destination ->
            val selected = destination == selectedDestination
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(stringResource(id = destination.titleId)) },
            )
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
