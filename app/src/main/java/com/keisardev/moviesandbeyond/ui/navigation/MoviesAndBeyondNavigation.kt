package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
// Assuming NavDisplay and entry are from androidx.navigation3.ui
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.entry
import com.keisardev.moviesandbeyond.feature.auth.authScreen
// import com.keisardev.moviesandbeyond.feature.auth.navigateToAuth // Will use NavManager
import com.keisardev.moviesandbeyond.feature.details.detailsScreen
// import com.keisardev.moviesandbeyond.feature.details.navigateToDetails // Will use NavManager
// import com.keisardev.moviesandbeyond.feature.movies.moviesNavigationRoute // Routes are now keys
import com.keisardev.moviesandbeyond.feature.movies.moviesScreen
import com.keisardev.moviesandbeyond.feature.search.searchScreen
import com.keisardev.moviesandbeyond.feature.tv.tvShowsScreen
import com.keisardev.moviesandbeyond.feature.you.youScreen
import com.keisardev.moviesandbeyond.ui.OnboardingScreen
// import com.keisardev.moviesandbeyond.ui.onboardingNavigationRoute // Routes are now keys

// Import NavManager and assumed keys
import com.keisardev.moviesandbeyond.ui.navigation.NavManager
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.OnboardingKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.AuthKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.MoviesKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.TvShowsKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.SearchKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.YouKey
import com.keisardev.moviesandbeyond.ui.navigation.NavigationKeys.DetailsKey

@Composable
fun MoviesAndBeyondNavigation(
    hideOnboarding: Boolean,
    // navController: NavHostController, // Removed
    paddingValues: PaddingValues
) {
    // Initialize NavManager based on hideOnboarding.
    // This is a temporary placement as per prompt. Ideally, this is done once in a ViewModel or app init.
    LaunchedEffect(hideOnboarding) {
        if (hideOnboarding) {
            NavManager.replaceAll(MoviesKey)
        } else {
            NavManager.replaceAll(OnboardingKey)
        }
    }

    val backStackEntries by NavManager.backStack.collectAsState()

    NavDisplay(
        modifier = Modifier.padding(paddingValues),
        backStack = backStackEntries, // Pass collected back stack
        entryProvider = {
            entry<OnboardingKey> {
                OnboardingScreen(navigateToAuth = { NavManager.navigateTo(AuthKey) })
            }
            entry<AuthKey> {
                authScreen(onBackClick = { NavManager.navigateUp() })
            }
            entry<MoviesKey> {
                moviesScreen(
                    navController = null, // Or refactor moviesScreen
                    navigateToDetails = { itemId, itemType -> NavManager.navigateTo(DetailsKey(itemId = itemId, itemType = itemType)) }
                )
            }
            entry<TvShowsKey> {
                tvShowsScreen(
                    navController = null, // Or refactor tvShowsScreen
                    navigateToDetails = { itemId, itemType -> NavManager.navigateTo(DetailsKey(itemId = itemId, itemType = itemType)) }
                )
            }
            entry<SearchKey> {
                searchScreen(
                    navigateToDetail = { itemId, itemType -> NavManager.navigateTo(DetailsKey(itemId = itemId, itemType = itemType)) }
                )
            }
            entry<YouKey> {
                youScreen(
                    navController = null, // Or refactor youScreen
                    navigateToAuth = { NavManager.navigateTo(AuthKey) },
                    navigateToDetails = { itemId, itemType -> NavManager.navigateTo(DetailsKey(itemId = itemId, itemType = itemType)) }
                )
            }
            entry<DetailsKey> { key -> // The key itself is available
                detailsScreen(
                    navController = null, // Or refactor detailsScreen
                    navigateToAuth = { NavManager.navigateTo(AuthKey) },
                    itemId = key.itemId,
                    itemType = key.itemType
                )
            }
        }
    )
}