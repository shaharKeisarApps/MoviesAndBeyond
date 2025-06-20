package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.keisardev.moviesandbeyond.core.model.AuthKey
import com.keisardev.moviesandbeyond.core.model.CreditsKey
import com.keisardev.moviesandbeyond.core.model.DetailsKey
import com.keisardev.moviesandbeyond.core.model.LibraryItemsKey
import com.keisardev.moviesandbeyond.core.model.MoviesItemsKey
import com.keisardev.moviesandbeyond.core.model.MoviesKey
import com.keisardev.moviesandbeyond.core.model.OnboardingKey
import com.keisardev.moviesandbeyond.core.model.SearchKey
import com.keisardev.moviesandbeyond.core.model.TvItemsKey
import com.keisardev.moviesandbeyond.core.model.TvShowsKey
import com.keisardev.moviesandbeyond.core.model.YouKey
import com.keisardev.moviesandbeyond.feature.auth.AuthScreen
import com.keisardev.moviesandbeyond.feature.details.CreditsRoute
import com.keisardev.moviesandbeyond.feature.details.DetailsScreen
import com.keisardev.moviesandbeyond.feature.movies.MoviesScreen
import com.keisardev.moviesandbeyond.feature.search.SearchScreen
import com.keisardev.moviesandbeyond.feature.tv.ItemsRoute
import com.keisardev.moviesandbeyond.feature.tv.TvShowsScreen
import com.keisardev.moviesandbeyond.feature.you.YouScreen
import com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute
import com.keisardev.moviesandbeyond.ui.OnboardingScreen

@Composable
fun MoviesAndBeyondNavigation(
    backStack: NavBackStack,
    paddingValues: PaddingValues
) {
    NavDisplay(
        modifier = Modifier.padding(paddingValues),
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<OnboardingKey> {
                OnboardingScreen(navigateToAuth = { backStack.add(AuthKey) })
            }
            entry<AuthKey> {
                AuthScreen(onBackClick = { backStack.removeLastOrNull() })
            }
            entry<MoviesKey> {
                MoviesScreen(
                    navigateToDetails = { detailsKey -> backStack.add(detailsKey) },
                    navigateToItems = { categoryKey -> backStack.add(categoryKey) }
                )
            }
            entry<MoviesItemsKey> { key ->
                ItemsRoute(
                    categoryName = key.category,
                    onItemClick = { itemId -> backStack.add(DetailsKey(itemId)) },
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }
            entry<TvShowsKey> {
                TvShowsScreen(
                    navigateToDetails = { detailsKey -> backStack.add(detailsKey) },
                    navigateToItems = { categoryKey -> backStack.add(categoryKey) }
                )
            }
            entry<TvItemsKey> { key ->
                ItemsRoute(
                    categoryName = key.category,
                    onItemClick = { itemId -> backStack.add(DetailsKey(itemId)) },
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }
            entry<SearchKey> {
                SearchScreen(navigateToDetails = { detailsKey -> backStack.add(detailsKey) })
            }
            entry<YouKey> {
                YouScreen(
                    navigateToAuth = { backStack.add(AuthKey) },
                    navigateToLibraryItem = { libraryItemsKey -> backStack.add(libraryItemsKey) }
                )
            }
            entry<LibraryItemsKey> { key ->
                LibraryItemsRoute(
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { detailsKey -> backStack.add(detailsKey) }
                )
            }
            entry<DetailsKey> { key ->
                DetailsScreen(
                    itemId = key.itemId,
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { newDetailsKey -> backStack.add(newDetailsKey) },
                    navigateToCredits = { creditsKey -> backStack.add(creditsKey) },
                    navigateToAuth = { backStack.add(AuthKey) }
                )
            }
            entry<CreditsKey> { key ->
                CreditsRoute(
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToPersonDetails = { detailsKey -> backStack.add(detailsKey) }
                )
            }
        }
    )
}