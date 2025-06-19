package com.keisardev.moviesandbeyond.ui.navigation

// import androidx.navigation3.NavBackStack // To be replaced with SnapshotStateList
// Import hiltViewModel for cases where ViewModel is instantiated here

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.keisardev.moviesandbeyond.core.model.AuthKey
import com.keisardev.moviesandbeyond.core.model.CreditsKey
import com.keisardev.moviesandbeyond.core.model.DetailsKey
import com.keisardev.moviesandbeyond.core.model.LibraryItemsKey
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

// Assuming backStack manipulation extensions are in a runtime package
// For example: import androidx.navigation3.runtime.*


@Composable
fun MoviesAndBeyondNavigation(
    backStack: NavBackStack, // Changed parameter type
    paddingValues: PaddingValues
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() }, // Use NavBackStack's removeLastOrNull
        entryProvider = entryProvider {
            entry<OnboardingKey> {
                OnboardingScreen(navigateToAuth = { backStack.add(AuthKey) })
            }
            entry<AuthKey> {
                authScreen(onBackClick = { backStack.removeLastOrNull() }) // Pass lambda
            }
            entry<MoviesKey> {
                moviesScreen(
                    navigateToDetails = { itemId -> backStack.add(itemId) },
                    navigateToItems = { category -> backStack.add(category) }
                )
            }
            entry<TvShowsKey> {
                tvShowsScreen(
                    navigateToDetails = { details -> backStack.add(details) },
                    navigateToItems = { category -> backStack.add(category) }
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
                searchScreen(
                    navigateToDetails = { details -> backStack.add(details) },
                )
            }
            entry<YouKey> {
                youScreen(
                    navigateToAuth = { backStack.add(AuthKey) },
                    navigateToLibraryItem = { libraryItemsKey -> backStack.add((libraryItemsKey)) }
                )
            }
            entry<LibraryItemsKey> { key ->
                com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute(
                    onBackClick = { backStack.removeLastOrNull() },
                    navigateToDetails = { details -> backStack.add(details) },
                )
            }
            entry<DetailsKey> { key ->
                detailsScreen(
                    itemId = key.itemId,
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


    /*
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
                        navigateToDetails = { itemId -> backStack.add(
                            DetailsKey(
                                itemId)
                        ) },
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
                        navigateToDetails = { itemId, itemType -> backStack.add(
                            DetailsKey(
                                itemId,
                                itemType
                            )
                        ) },
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
                        navigateToDetail = { itemId, itemType -> backStack.add(
                            DetailsKey(
                                itemId,
                                itemType
                            )
                        ) }
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
                        navigateToDetails = { itemId, itemType -> backStack.add(
                            DetailsKey(
                                itemId,
                                itemType
                            )
                        ) }
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
    */
}