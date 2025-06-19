package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
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
import com.keisardev.moviesandbeyond.feature.auth.authScreen
import com.keisardev.moviesandbeyond.feature.details.detailsScreen
import com.keisardev.moviesandbeyond.feature.movies.moviesScreen
import com.keisardev.moviesandbeyond.feature.search.searchScreen
import com.keisardev.moviesandbeyond.feature.tv.ItemsRoute
import com.keisardev.moviesandbeyond.feature.tv.tvShowsScreen
import com.keisardev.moviesandbeyond.feature.you.youScreen
import com.keisardev.moviesandbeyond.ui.OnboardingScreen

@Composable
fun MoviesAndBeyondNavigation(
    backStack: NavBackStack,
    paddingValues: PaddingValues
) {
    NavDisplay(
        modifier = Modifier.padding(paddingValues),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<OnboardingKey> {
                OnboardingScreen(navigateToAuth = { backStack.add(AuthKey) })
            }
            entry<AuthKey> {
                authScreen(onBackClick = { backStack.removeLastOrNull() })
            }
            entry<MoviesKey> {
                moviesScreen(
                    navigateToDetails = { detailsKey -> backStack.add(detailsKey) },
                    navigateToItems = { categoryKey -> backStack.add(categoryKey) }
                )
            }
            entry<MoviesItemsKey> { key ->
                com.keisardev.moviesandbeyond.feature.movies.ItemsRoute(
                    categoryName = key.category,
                    onItemClick = { itemId -> backStack.add(DetailsKey(itemId)) },
                    onBackClick = { backStack.removeLastOrNull() }
                )
            }
            entry<TvShowsKey> {
                tvShowsScreen(
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
                searchScreen(navigateToDetails = { detailsKey -> backStack.add(detailsKey) })
            }
            entry<YouKey> {
                youScreen(
                    navigateToAuth = { backStack.add(AuthKey) },
                    navigateToLibraryItem = { libraryItemsKey -> backStack.add(libraryItemsKey) }
                )
            }
            entry<LibraryItemsKey> { key ->
                com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute(
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { detailsKey -> backStack.add(detailsKey) }
                )
            }
            entry<DetailsKey> { key ->
                detailsScreen(
                    itemId = key.itemId,
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { newDetailsKey -> backStack.add(newDetailsKey) },
                    navigateToCredits = { creditsKey -> backStack.add(creditsKey) },
                    navigateToAuth = { backStack.add(AuthKey) }
                )
            }
            entry<CreditsKey> { key ->
                com.keisardev.moviesandbeyond.feature.details.CreditsRoute(
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToPersonDetails = { detailsKey -> backStack.add(detailsKey) }
                )
            }
        }
    )
}
