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
import androidx.compose.runtime.collectAsState
// import androidx.navigation.NavController // Not needed
// import androidx.navigation.NavDestination // Not needed
// import androidx.navigation.NavDestination.Companion.hierarchy // Not needed
// import androidx.navigation.NavGraph.Companion.findStartDestination // Not needed
// import androidx.navigation.NavHostController // Not needed
// import androidx.navigation.compose.currentBackStackEntryAsState // Not needed
// import androidx.navigation.compose.rememberNavController // Not needed
// import androidx.navigation.navOptions // Not needed
import com.keisardev.moviesandbeyond.core.ui.HazeScaffold
// Feature imports for navigateToMovies etc. are removed as NavManager handles navigation with keys
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondDestination
import com.keisardev.moviesandbeyond.ui.navigation.MoviesAndBeyondNavigation
// Import NavManager and Keys
import com.keisardev.moviesandbeyond.ui.navigation.NavManager
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys // Assuming this is where all keys are
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.MoviesKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.TvShowsKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.SearchKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.YouKey
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
// Hypothetical extension property/function to map enum to key
// This would ideally be in NavigationKeys.kt or a similar utility file.
// For now, conceptualizing it here for the purpose of refactoring MoviesAndBeyondApp.
val MoviesAndBeyondDestination.navigationKey: Any
    get() = when (this) {
        MoviesAndBeyondDestination.MOVIES -> MoviesKey
        MoviesAndBeyondDestination.TV_SHOWS -> TvShowsKey
        MoviesAndBeyondDestination.SEARCH -> SearchKey
        MoviesAndBeyondDestination.YOU -> YouKey
    }

@Composable
fun MoviesAndBeyondApp(
    hideOnboarding: Boolean,
    // navController: NavHostController = rememberNavController() // Removed
) {
    val hazeState = remember { HazeState() }
    val inputScale: HazeInputScale = HazeInputScale.Auto
    val style = CupertinoMaterials.ultraThin()

    val bottomBarDestinations = remember { MoviesAndBeyondDestination.entries }
    val currentBackStack by NavManager.backStack.collectAsState()
    val currentKey = currentBackStack.lastOrNull()

    val showBottomBar = bottomBarDestinations.any { destination ->
        currentKey == destination.navigationKey
    }

    HazeScaffold(
        hazeState = hazeState,
        bottomBar = {
            if (showBottomBar)
            MoviesAndBeyondNavigationBar(
                destinations = bottomBarDestinations,
                currentKey = currentKey, // Pass currentKey
                onNavigateToDestination = { destination ->
                    // Use replaceAll for bottom bar navigation as per prompt
                    NavManager.replaceAll(destination.navigationKey)
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
            // navController = navController, // Removed
            paddingValues = padding
        )
    }
}

@Composable
fun MoviesAndBeyondNavigationBar(
    destinations: List<MoviesAndBeyondDestination>,
    currentKey: Any?, // Changed from NavDestination
    onNavigateToDestination: (MoviesAndBeyondDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier, containerColor = Color.Transparent) {
        destinations.forEach { destination ->
            val selected = currentKey == destination.navigationKey // Compare keys
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

// Removed private fun NavDestination?.isDestinationInHierarchy
// Removed private fun NavController.navigateToBottomBarDestination