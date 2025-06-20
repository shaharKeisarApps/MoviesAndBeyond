package com.keisardev.moviesandbeyond.ui

// import androidx.compose.runtime.collectAsState // May not be needed if backStack properties are states
// import androidx.navigation.NavController // Not needed
// import androidx.navigation.NavDestination // Not needed
// import androidx.navigation.NavDestination.Companion.hierarchy // Not needed
// import androidx.navigation.NavGraph.Companion.findStartDestination // Not needed
// import androidx.navigation.NavHostController // Not needed
// import androidx.navigation.compose.currentBackStackEntryAsState // Not needed
// import androidx.navigation.compose.rememberNavController // Not needed
// import androidx.navigation.navOptions // Not needed
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.keisardev.moviesandbeyond.core.model.MoviesKey
import com.keisardev.moviesandbeyond.core.model.OnboardingKey
import com.keisardev.moviesandbeyond.core.model.SearchKey
import com.keisardev.moviesandbeyond.core.model.TvShowsKey
import com.keisardev.moviesandbeyond.core.model.YouKey
import com.keisardev.moviesandbeyond.core.ui.HazeScaffold
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
// Hypothetical extension property/function to map enum to key
// This would ideally be in NavigationKeys.kt or a similar utility file.
// For now, conceptualizing it here for the purpose of refactoring MoviesAndBeyondApp.
// This should ideally live with NavigationKeys or MoviesAndBeyondDestination
val MoviesAndBeyondDestination.navigationKey: NavKey
    get() = when (this) {
        MoviesAndBeyondDestination.MOVIES -> MoviesKey
        MoviesAndBeyondDestination.TV_SHOWS -> TvShowsKey
        MoviesAndBeyondDestination.SEARCH -> SearchKey
        MoviesAndBeyondDestination.YOU -> YouKey
    }

@Composable
fun MoviesAndBeyondApp(
    hideOnboarding: Boolean,
) {
    val hazeState = remember { HazeState() }
    val inputScale: HazeInputScale = HazeInputScale.Auto
    val style = CupertinoMaterials.ultraThin()

    val initialKey: NavKey = if (hideOnboarding) MoviesKey else OnboardingKey
    val backStack: NavBackStack = rememberNavBackStack(initialKey) // Use new NavBackStack

    val bottomBarDestinations = remember { MoviesAndBeyondDestination.entries }
    val currentKey = backStack.lastOrNull() // Get current key from new backStack

    val showBottomBar = bottomBarDestinations.any { destination ->
        currentKey == destination.navigationKey
    }

    HazeScaffold(
        hazeState = hazeState,
        bottomBar = {
            if (showBottomBar)
            MoviesAndBeyondNavigationBar(
                destinations = bottomBarDestinations,
                currentKey = currentKey,
                onNavigateToDestination = { destination ->
                    backStack.replaceAll { destination.navigationKey }// Use new backStack for navigation
                },
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = style)
                    .fillMaxWidth()
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        // MoviesAndBeyondNavigation needs to accept NavBackStack and paddingValues
        // hideOnboarding is used for initialKey, so might not be needed by MoviesAndBeyondNavigation directly
        // for start destination logic anymore.
        MoviesAndBeyondNavigation(
            backStack = backStack,
            paddingValues = padding
            // hideOnboarding = hideOnboarding, // Potentially remove if only used for start dest
        )
    }
}

@Composable
fun MoviesAndBeyondNavigationBar(
    destinations: List<MoviesAndBeyondDestination>,
    currentKey: NavKey?, // Changed to NavKey?
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