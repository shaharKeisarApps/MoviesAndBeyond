package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.keisardev.moviesandbeyond.feature.auth.AuthRoute
import com.keisardev.moviesandbeyond.feature.details.CreditsRoute
import com.keisardev.moviesandbeyond.feature.details.DetailsRoute
import com.keisardev.moviesandbeyond.feature.details.DetailsViewModel
import com.keisardev.moviesandbeyond.feature.movies.FeedRoute as MoviesFeedScreen
import com.keisardev.moviesandbeyond.feature.movies.ItemsRoute as MoviesItemsScreen
import com.keisardev.moviesandbeyond.feature.movies.MoviesViewModel
import com.keisardev.moviesandbeyond.feature.search.SearchRoute as SearchScreen
import com.keisardev.moviesandbeyond.feature.tv.FeedRoute as TvFeedScreen
import com.keisardev.moviesandbeyond.feature.tv.ItemsRoute as TvItemsScreen
import com.keisardev.moviesandbeyond.feature.tv.TvShowsViewModel
import com.keisardev.moviesandbeyond.feature.you.YouRoute as YouScreen
import com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute as LibraryItemsScreen
import com.keisardev.moviesandbeyond.ui.OnboardingScreen

/**
 * Main Navigation 3 navigation component for the app. Manages all navigation using type-safe routes
 * and a developer-owned back stack.
 *
 * @param navigationState The navigation state holder managing back stacks
 * @param paddingValues Padding from the scaffold (for bottom bar)
 */
@Composable
fun MoviesAndBeyondNav3(navigationState: NavigationState, paddingValues: PaddingValues) {
    val backStack = navigationState.currentBackStack

    NavDisplay(
        modifier = Modifier.padding(paddingValues),
        backStack = backStack,
        onBack = { navigationState.handleBack() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()),
        entryProvider =
            entryProvider {
                // ================================================================
                // Onboarding
                // ================================================================
                entry<OnboardingRoute> {
                    OnboardingScreen(navigateToAuth = { navigationState.navigateToAuth() })
                }

                // ================================================================
                // Auth
                // ================================================================
                entry<com.keisardev.moviesandbeyond.ui.navigation.AuthRoute> {
                    AuthRoute(onBackClick = { navigationState.handleBack() })
                }

                // ================================================================
                // Movies Feature
                // ================================================================
                entry<TopLevelRoute.Movies> {
                    val viewModel = hiltViewModel<MoviesViewModel>()
                    MoviesFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(MoviesItemsRoute(category))
                        },
                        viewModel = viewModel)
                }

                entry<MoviesFeedRoute> {
                    val viewModel = hiltViewModel<MoviesViewModel>()
                    MoviesFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(MoviesItemsRoute(category))
                        },
                        viewModel = viewModel)
                }

                entry<MoviesItemsRoute> { key ->
                    val viewModel = hiltViewModel<MoviesViewModel>()
                    MoviesItemsScreen(
                        categoryName = key.category,
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel)
                }

                // ================================================================
                // TV Shows Feature
                // ================================================================
                entry<TopLevelRoute.TvShows> {
                    val viewModel = hiltViewModel<TvShowsViewModel>()
                    TvFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(
                                TvShowsItemsRoute(category))
                        },
                        viewModel = viewModel)
                }

                entry<TvShowsFeedRoute> {
                    val viewModel = hiltViewModel<TvShowsViewModel>()
                    TvFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(
                                TvShowsItemsRoute(category))
                        },
                        viewModel = viewModel)
                }

                entry<TvShowsItemsRoute> { key ->
                    val viewModel = hiltViewModel<TvShowsViewModel>()
                    TvItemsScreen(
                        categoryName = key.category,
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel)
                }

                // ================================================================
                // Search Feature
                // ================================================================
                entry<TopLevelRoute.Search> {
                    SearchScreen(
                        navigateToDetail = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        })
                }

                entry<SearchRoute> {
                    SearchScreen(
                        navigateToDetail = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        })
                }

                // ================================================================
                // You / Profile Feature
                // ================================================================
                entry<TopLevelRoute.You> {
                    YouScreen(
                        navigateToAuth = {
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.AuthRoute)
                        },
                        navigateToLibraryItem = { type ->
                            navigationState.topLevelBackStack.navigateTo(LibraryItemsRoute(type))
                        })
                }

                entry<YouRoute> {
                    YouScreen(
                        navigateToAuth = {
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.AuthRoute)
                        },
                        navigateToLibraryItem = { type ->
                            navigationState.topLevelBackStack.navigateTo(LibraryItemsRoute(type))
                        })
                }

                entry<LibraryItemsRoute> {
                    LibraryItemsScreen(
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        })
                }

                // ================================================================
                // Details Feature
                // ================================================================
                entry<com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute> { key ->
                    // Create ViewModel with the id from the route
                    // Note: For Nav3, we pass the id directly instead of via SavedStateHandle
                    val viewModel = hiltViewModel<DetailsViewModel>()
                    DetailsRoute(
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        onSeeAllCastClick = {
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.CreditsRoute(key.id))
                        },
                        navigateToAuth = {
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.AuthRoute)
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel,
                        detailsId = key.id)
                }

                entry<com.keisardev.moviesandbeyond.ui.navigation.CreditsRoute> { key ->
                    val viewModel = hiltViewModel<DetailsViewModel>()
                    CreditsRoute(
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id))
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel,
                        detailsId = key.id)
                }
            })
}
