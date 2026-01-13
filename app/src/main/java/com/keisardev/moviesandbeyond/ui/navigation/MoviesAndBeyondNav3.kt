package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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

// Animation duration constants
private const val NAVIGATION_ANIM_DURATION = 400
private const val DETAILS_ANIM_DURATION = 350
private const val PREDICTIVE_BACK_ANIM_DURATION = 300

// Predictive back scale values - subtle to avoid jarring effect
private const val PREDICTIVE_BACK_SCALE_INCOMING = 0.9f
private const val PREDICTIVE_BACK_SCALE_OUTGOING = 0.85f

/**
 * Main Navigation 3 navigation component for the app. Manages all navigation using type-safe routes
 * and a developer-owned back stack.
 *
 * Features:
 * - Smooth horizontal slide animations for standard navigation
 * - Vertical slide animations for details screens (modal-like experience)
 * - Predictive back gesture support for Android 13+
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
        // Global forward navigation animation: slide in from right with subtle fade
        transitionSpec = {
            (slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing)) +
                    fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec =
                            tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing)) +
                        fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
        },
        // Global back navigation animation: slide in from left
        popTransitionSpec = {
            (slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing)) +
                    fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec =
                            tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing)) +
                        fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
        },
        // Predictive back gesture animation: smooth, gesture-driven transition
        // Uses LinearEasing since the user controls the animation timing via gesture
        // No fade to avoid harsh visual transitions
        predictivePopTransitionSpec = {
            // Incoming screen: scales up from slightly smaller, no harsh movements
            scaleIn(
                    initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                    animationSpec = tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing))
                .togetherWith(
                    // Outgoing screen: slides out to the right with slight scale down
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec =
                            tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing)) +
                        scaleOut(
                            targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                            animationSpec =
                                tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing)))
        },
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
                // Details Feature - Uses vertical slide for modal-like experience
                // ================================================================
                entry<com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute>(
                    metadata =
                        NavDisplay.transitionSpec {
                            // Slide up from bottom with scale for a modal-like entrance
                            (slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight },
                                    animationSpec =
                                        tween(
                                            DETAILS_ANIM_DURATION, easing = FastOutSlowInEasing)) +
                                    fadeIn(animationSpec = tween(DETAILS_ANIM_DURATION / 2)))
                                .togetherWith(ExitTransition.KeepUntilTransitionsFinished)
                        } +
                            NavDisplay.popTransitionSpec {
                                // Reveal previous screen while sliding down
                                fadeIn(animationSpec = tween(DETAILS_ANIM_DURATION / 2))
                                    .togetherWith(
                                        slideOutVertically(
                                            targetOffsetY = { fullHeight -> fullHeight },
                                            animationSpec =
                                                tween(
                                                    DETAILS_ANIM_DURATION,
                                                    easing = FastOutSlowInEasing)) +
                                            fadeOut(
                                                animationSpec = tween(DETAILS_ANIM_DURATION / 2)))
                            } +
                            NavDisplay.predictivePopTransitionSpec {
                                // Predictive back: smooth vertical slide with scale
                                // Uses LinearEasing for gesture-driven feel, no harsh fades
                                scaleIn(
                                        initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                                        animationSpec =
                                            tween(
                                                PREDICTIVE_BACK_ANIM_DURATION,
                                                easing = LinearEasing))
                                    .togetherWith(
                                        slideOutVertically(
                                            targetOffsetY = { fullHeight -> fullHeight },
                                            animationSpec =
                                                tween(
                                                    PREDICTIVE_BACK_ANIM_DURATION,
                                                    easing = LinearEasing)) +
                                            scaleOut(
                                                targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                                                animationSpec =
                                                    tween(
                                                        PREDICTIVE_BACK_ANIM_DURATION,
                                                        easing = LinearEasing)))
                            }) { key ->
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
                                    com.keisardev.moviesandbeyond.ui.navigation.CreditsRoute(
                                        key.id))
                            },
                            navigateToAuth = {
                                navigationState.topLevelBackStack.navigateTo(
                                    com.keisardev.moviesandbeyond.ui.navigation.AuthRoute)
                            },
                            onBackClick = { navigationState.topLevelBackStack.goBack() },
                            viewModel = viewModel,
                            detailsId = key.id)
                    }

                // Credits screen - horizontal slide from right (standard list screen)
                entry<com.keisardev.moviesandbeyond.ui.navigation.CreditsRoute>(
                    metadata =
                        NavDisplay.transitionSpec {
                            // Slide in from right (standard navigation)
                            (slideInHorizontally(
                                    initialOffsetX = { fullWidth -> fullWidth },
                                    animationSpec =
                                        tween(
                                            NAVIGATION_ANIM_DURATION,
                                            easing = FastOutSlowInEasing)) +
                                    fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                        animationSpec =
                                            tween(
                                                NAVIGATION_ANIM_DURATION,
                                                easing = FastOutSlowInEasing)) +
                                        fadeOut(
                                            animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                        } +
                            NavDisplay.popTransitionSpec {
                                // Slide back (reveal details underneath)
                                fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec =
                                                tween(
                                                    NAVIGATION_ANIM_DURATION,
                                                    easing = FastOutSlowInEasing)) +
                                            fadeOut(
                                                animationSpec =
                                                    tween(NAVIGATION_ANIM_DURATION / 2)))
                            } +
                            NavDisplay.predictivePopTransitionSpec {
                                // Predictive back: smooth slide with scale, no harsh fades
                                scaleIn(
                                        initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                                        animationSpec =
                                            tween(
                                                PREDICTIVE_BACK_ANIM_DURATION,
                                                easing = LinearEasing))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec =
                                                tween(
                                                    PREDICTIVE_BACK_ANIM_DURATION,
                                                    easing = LinearEasing)) +
                                            scaleOut(
                                                targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                                                animationSpec =
                                                    tween(
                                                        PREDICTIVE_BACK_ANIM_DURATION,
                                                        easing = LinearEasing)))
                            }) { key ->
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
