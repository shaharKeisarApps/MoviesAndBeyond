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
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.core.ui.navigation.TopLevelRoute
import com.keisardev.moviesandbeyond.ui.navigation.AppNavigatorImpl
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondDestination
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondNav3
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeState

@OptIn(
    ExperimentalHazeApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun MoviesAndBeyondApp(
    navigator: AppNavigatorImpl,
    entryProviders: Set<EntryProviderInstaller>,
    hideOnboarding: Boolean,
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            val hazeState = remember { HazeState() }

            // Communicate the hideOnboarding value to the navigator
            navigator.setHideOnboarding(hideOnboarding)

            val destinations = remember { MoviesAndBeyondDestination.entries }

            val showNavigation = navigator.hasCompletedOnboarding && navigator.isAtTabRoot()

            val selectedDestination = topLevelRouteToDestination(navigator.currentTab)

            val onNavigate = { destination: MoviesAndBeyondDestination ->
                navigator.switchTab(destinationToTopLevelRoute(destination))
            }

            val useRail = LocalWindowSizeClass.current.isExpandedOrMediumWidth()

            CompositionLocalProvider(LocalUseNavigationRail provides (useRail && showNavigation)) {
                if (useRail) {
                    RailContent(
                        hazeState = hazeState,
                        navigator = navigator,
                        entryProviders = entryProviders,
                        destinations = destinations,
                        selectedDestination = selectedDestination,
                        onNavigate = onNavigate,
                        showNavigation = showNavigation,
                    )
                } else {
                    CompactContent(
                        hazeState = hazeState,
                        navigator = navigator,
                        entryProviders = entryProviders,
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
    navigator: AppNavigatorImpl,
    entryProviders: Set<EntryProviderInstaller>,
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
        MoviesAndBeyondNav3(
            navigator = navigator,
            entryProviders = entryProviders,
            paddingValues = padding,
        )
    }
}

/** Medium+ layout: NavigationRail on the left with HazeScaffold filling the remaining width. */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun RailContent(
    hazeState: HazeState,
    navigator: AppNavigatorImpl,
    entryProviders: Set<EntryProviderInstaller>,
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
            MoviesAndBeyondNav3(
                navigator = navigator,
                entryProviders = entryProviders,
                paddingValues = padding,
            )
        }
    }
}

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
private fun topLevelRouteToDestination(route: TopLevelRoute): MoviesAndBeyondDestination? {
    return when (route) {
        is TopLevelRoute.Movies -> MoviesAndBeyondDestination.MOVIES
        is TopLevelRoute.TvShows -> MoviesAndBeyondDestination.TV_SHOWS
        is TopLevelRoute.Search -> MoviesAndBeyondDestination.SEARCH
        is TopLevelRoute.You -> MoviesAndBeyondDestination.YOU
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
