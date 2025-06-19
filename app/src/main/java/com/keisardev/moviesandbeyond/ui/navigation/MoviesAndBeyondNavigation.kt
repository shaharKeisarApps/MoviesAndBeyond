package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.entry
// import androidx.navigation3.NavBackStack // To be replaced with SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateList // Import for backStack type
import com.keisardev.moviesandbeyond.feature.auth.authScreen
import com.keisardev.moviesandbeyond.feature.details.detailsScreen
// Import hiltViewModel for cases where ViewModel is instantiated here
import androidx.hilt.navigation.compose.hiltViewModel
import com.keisardev.moviesandbeyond.feature.movies.moviesScreen
import com.keisardev.moviesandbeyond.feature.search.searchScreen
import com.keisardev.moviesandbeyond.feature.tv.tvShowsScreen
import com.keisardev.moviesandbeyond.feature.you.youScreen
import com.keisardev.moviesandbeyond.ui.OnboardingScreen

// Import NavManager and assumed keys
// import com.keisardev.moviesandbeyond.ui.navigation.NavManager // Removed
import com.keisardev.moviesandbeyond.ui.navigation.NavKey
import com.keisardev.moviesandbeyond.ui.navigation.OnboardingKey
import com.keisardev.moviesandbeyond.ui.navigation.AuthKey
// Ensure all necessary keys are imported
import com.keisardev.moviesandbeyond.ui.navigation.MoviesKey
import com.keisardev.moviesandbeyond.ui.navigation.MoviesItemsKey
import com.keisardev.moviesandbeyond.ui.navigation.TvShowsKey
import com.keisardev.moviesandbeyond.ui.navigation.TvItemsKey
import com.keisardev.moviesandbeyond.ui.navigation.SearchKey
import com.keisardev.moviesandbeyond.ui.navigation.YouKey
import com.keisardev.moviesandbeyond.ui.navigation.LibraryItemsKey
import com.keisardev.moviesandbeyond.ui.navigation.MoviesKey
import com.keisardev.moviesandbeyond.ui.navigation.MoviesItemsKey
import com.keisardev.moviesandbeyond.ui.navigation.TvShowsKey
import com.keisardev.moviesandbeyond.ui.navigation.TvItemsKey
import com.keisardev.moviesandbeyond.ui.navigation.SearchKey
import com.keisardev.moviesandbeyond.ui.navigation.YouKey
import com.keisardev.moviesandbeyond.ui.navigation.LibraryItemsKey
import com.keisardev.moviesandbeyond.ui.navigation.DetailsKey
import com.keisardev.moviesandbeyond.ui.navigation.CreditsKey
// Assuming backStack manipulation extensions are in a runtime package
// For example: import androidx.navigation3.runtime.*

@Composable
fun MoviesAndBeyondNavigation(
    backStack: SnapshotStateList<NavKey>, // Changed parameter type
    paddingValues: PaddingValues
) {
    NavDisplay(
        modifier = Modifier.padding(paddingValues),
        navBackStack = backStack, // Pass SnapshotStateList
        entryProvider = {
            entry<OnboardingKey> {
                OnboardingScreen(navigateToAuth = { backStack.add(AuthKey) })
            }
            entry<AuthKey> {
                authScreen(onBackClick = { backStack.removeLastOrNull() }) // Pass lambda
            }
            entry<MoviesKey> {
                moviesScreen(
                    navigateToDetails = { itemId, itemType -> backStack.add(DetailsKey(itemId, itemType)) },
                    navigateToItems = { category -> backStack.add(MoviesItemsKey(category)) }
                )
            }
            entry<MoviesItemsKey> { key ->
                com.keisardev.moviesandbeyond.feature.movies.ItemsRoute(
                    categoryName = key.category,
                    // Lambdas for navigation from MoviesItemsScreen
                    onItemClick = { itemId, itemType -> backStack.add(DetailsKey(itemId, itemType)) },
                    onBackClick = { backStack.removeLastOrNull() }
                    // viewModel = hiltViewModel() // ViewModel is often instantiated within the Route itself
                )
            }
            entry<TvShowsKey> {
                tvShowsScreen(
                    navigateToDetails = { itemId, itemType -> backStack.add(DetailsKey(itemId, itemType)) },
                    navigateToItems = { category -> backStack.add(TvItemsKey(category)) }
                )
            }
            entry<TvItemsKey> { key ->
                com.keisardev.moviesandbeyond.feature.tv.ItemsRoute(
                    categoryName = key.category,
                    // Lambdas for navigation from TvItemsScreen
                    onItemClick = { itemId, itemType -> backStack.add(DetailsKey(itemId, itemType)) },
                    onBackClick = { backStack.removeLastOrNull() }
                    // viewModel = hiltViewModel()
                )
            }
            entry<SearchKey> {
                searchScreen(
                    navigateToDetail = { itemId, itemType -> backStack.add(DetailsKey(itemId, itemType)) }
                )
            }
            entry<YouKey> {
                youScreen(
                    navigateToAuth = { backStack.add(AuthKey) },
                    navigateToLibraryItem = { type -> backStack.add(LibraryItemsKey(type)) }
                    // navigateToDetails is likely handled within LibraryItemsScreen
                )
            }
            entry<LibraryItemsKey> { key ->
                com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute(
                    // type is usually from key, ViewModel gets it from SavedStateHandle
                    // Lambdas for navigation from LibraryItemsScreen
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { itemId, itemType -> backStack.add(DetailsKey(itemId, itemType)) }
                    // viewModel = hiltViewModel()
                )
            }
            entry<DetailsKey> { key ->
                detailsScreen(
                    itemId = key.itemId,
                    itemType = key.itemType,
                    // Lambdas for navigation from DetailsScreen
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { newDetailsKey -> backStack.add(newDetailsKey) },
                    navigateToCredits = { creditsKey -> backStack.add(creditsKey) },
                    navigateToAuth = { backStack.add(AuthKey) }
                    // viewModel = hiltViewModel()
                )
            }
            entry<CreditsKey> { key ->
                com.keisardev.moviesandbeyond.feature.details.CreditsRoute(
                    // itemId = key.itemId, itemType = key.itemType are usually for ViewModel via SavedStateHandle
                    // Lambdas for navigation from CreditsScreen
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToPersonDetails = { detailsKey -> backStack.add(detailsKey) }
                    // viewModel = hiltViewModel()
                )
            }
        }
    )
}