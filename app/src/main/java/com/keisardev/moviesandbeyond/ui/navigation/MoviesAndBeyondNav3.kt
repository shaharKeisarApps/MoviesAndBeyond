package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
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

// ============================================================================
// Material 3 Emphasized Easing Curves
// ============================================================================
// These curves are designed for smooth, organic motion that feels effortless
// Reference: https://m3.material.io/styles/motion/easing-and-duration/tokens-specs

/**
 * Material 3 Emphasized Decelerate easing for enter transitions. Creates a smooth, elastic
 * deceleration that feels natural and premium. Used when content enters the screen or grows.
 */
private val EmphasizedDecelerateEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

/**
 * Material 3 Emphasized Accelerate easing for exit transitions. Creates a quick, confident
 * acceleration for departing content. Used when content exits the screen or shrinks.
 */
private val EmphasizedAccelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

// Animation duration constants - aligned with Material Motion specs
private const val NAVIGATION_ANIM_DURATION = 400 // Standard navigation transitions
private const val FADE_THROUGH_EXIT_DURATION = 250 // Fade out duration for departing screen
private const val FADE_THROUGH_ENTER_DURATION = 300 // Fade in duration for entering screen
private const val FADE_THROUGH_GAP =
    50 // Brief pause where only shared element is visible (hero moment)
private const val PREDICTIVE_BACK_ANIM_DURATION = 300 // Gesture-driven transitions

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
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MoviesAndBeyondNav3(navigationState: NavigationState, paddingValues: PaddingValues) {
    val backStack = navigationState.currentBackStack

    NavDisplay(
        // Only apply top padding - NOT bottom
        // This allows the floating navigation bar to overlay content
        // Content should handle its own bottom spacing via contentPadding on scrollable lists
        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        backStack = backStack,
        onBack = { navigationState.handleBack() },
        // Global forward navigation animation: slide in from right with subtle fade
        transitionSpec = {
            (slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                        animationSpec =
                            tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
                    ) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
                )
        },
        // Global back navigation animation: slide in from left
        popTransitionSpec = {
            (slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
                ) + fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec =
                            tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
                    ) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
                )
        },
        // Predictive back gesture animation: smooth, gesture-driven transition
        // Uses LinearEasing since the user controls the animation timing via gesture
        // No fade to avoid harsh visual transitions
        predictivePopTransitionSpec = {
            // Incoming screen: scales up from slightly smaller, no harsh movements
            scaleIn(
                    initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                    animationSpec = tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing),
                )
                .togetherWith(
                    // Outgoing screen: slides out to the right with slight scale down
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing),
                    ) +
                        scaleOut(
                            targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                            animationSpec =
                                tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing),
                        )
                )
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
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
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(MoviesItemsRoute(category))
                        },
                        viewModel = viewModel,
                    )
                }

                entry<MoviesFeedRoute> {
                    val viewModel = hiltViewModel<MoviesViewModel>()
                    MoviesFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(MoviesItemsRoute(category))
                        },
                        viewModel = viewModel,
                    )
                }

                entry<MoviesItemsRoute> { key ->
                    val viewModel = hiltViewModel<MoviesViewModel>()
                    MoviesItemsScreen(
                        categoryName = key.category,
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel,
                    )
                }

                // ================================================================
                // TV Shows Feature
                // ================================================================
                entry<TopLevelRoute.TvShows> {
                    val viewModel = hiltViewModel<TvShowsViewModel>()
                    TvFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(
                                TvShowsItemsRoute(category)
                            )
                        },
                        viewModel = viewModel,
                    )
                }

                entry<TvShowsFeedRoute> {
                    val viewModel = hiltViewModel<TvShowsViewModel>()
                    TvFeedScreen(
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        navigateToItems = { category ->
                            navigationState.topLevelBackStack.navigateTo(
                                TvShowsItemsRoute(category)
                            )
                        },
                        viewModel = viewModel,
                    )
                }

                entry<TvShowsItemsRoute> { key ->
                    val viewModel = hiltViewModel<TvShowsViewModel>()
                    TvItemsScreen(
                        categoryName = key.category,
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel,
                    )
                }

                // ================================================================
                // Search Feature
                // ================================================================
                entry<TopLevelRoute.Search> {
                    SearchScreen(
                        navigateToDetail = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        }
                    )
                }

                entry<SearchRoute> {
                    SearchScreen(
                        navigateToDetail = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        }
                    )
                }

                // ================================================================
                // You / Profile Feature
                // ================================================================
                entry<TopLevelRoute.You> {
                    YouScreen(
                        navigateToAuth = {
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.AuthRoute
                            )
                        },
                        navigateToLibraryItem = { type ->
                            navigationState.topLevelBackStack.navigateTo(LibraryItemsRoute(type))
                        },
                    )
                }

                entry<YouRoute> {
                    YouScreen(
                        navigateToAuth = {
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.AuthRoute
                            )
                        },
                        navigateToLibraryItem = { type ->
                            navigationState.topLevelBackStack.navigateTo(LibraryItemsRoute(type))
                        },
                    )
                }

                entry<LibraryItemsRoute> { key ->
                    LibraryItemsScreen(
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        navigateToDetails = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        libraryItemType = key.type,
                    )
                }

                // ================================================================
                // Details Feature - Fade Through + Shared Element Hero
                // ================================================================
                //
                // MATERIAL 3 FADE THROUGH PATTERN (ACTIVE) ✅
                //
                // Motion Choreography:
                //   1. List screen fades out (250ms, EmphasizedAccelerateEasing)
                //   2. Brief 50ms pause where ONLY the shared element is visible (hero moment)
                //   3. Details screen fades in (300ms, EmphasizedDecelerateEasing)
                //   4. Shared element morphs continuously with spring physics (automatic)
                //
                // Why This Works:
                //   - Zero competing spatial motion - users track only the poster morph
                //   - The 50ms gap creates a "rest" where the eye focuses on the poster
                //   - Material 3's canonical pattern for image-heavy content transitions
                //   - Used by Google Photos, Play Store for card → detail transitions
                //
                // Material 3 Compliance:
                //   - Sequential fade pattern (M3 "Fade Through")
                //   - Emphasized easing curves (accelerate exit, decelerate enter)
                //   - 250-300ms per phase (M3 standard for fades)
                //   - Shared element = primary motion, fades = secondary support
                //
                entry<com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute>(
                    metadata =
                        NavDisplay.transitionSpec {
                            // ENTER: Details screen fades in after brief pause
                            // The delay creates the "gap" where only shared element is visible
                            fadeIn(
                                    animationSpec =
                                        tween(
                                            durationMillis = FADE_THROUGH_ENTER_DURATION,
                                            delayMillis = FADE_THROUGH_GAP,
                                            easing = EmphasizedDecelerateEasing,
                                        )
                                )
                                .togetherWith(
                                    // EXIT: List screen fades out quickly
                                    fadeOut(
                                        animationSpec =
                                            tween(
                                                durationMillis = FADE_THROUGH_EXIT_DURATION,
                                                easing = EmphasizedAccelerateEasing,
                                            )
                                    )
                                )
                        } +
                            NavDisplay.popTransitionSpec {
                                // BACK NAVIGATION: Symmetric fade pattern
                                // List fades in with delay, Details fades out quickly
                                fadeIn(
                                        animationSpec =
                                            tween(
                                                durationMillis = FADE_THROUGH_ENTER_DURATION,
                                                delayMillis = FADE_THROUGH_GAP,
                                                easing = EmphasizedDecelerateEasing,
                                            )
                                    )
                                    .togetherWith(
                                        fadeOut(
                                            animationSpec =
                                                tween(
                                                    durationMillis = FADE_THROUGH_EXIT_DURATION,
                                                    easing = EmphasizedAccelerateEasing,
                                                )
                                        )
                                    )
                            } +
                            NavDisplay.predictivePopTransitionSpec {
                                // PREDICTIVE BACK: Gesture-driven fade with subtle scale
                                // Uses LinearEasing for immediate gesture response
                                scaleIn(
                                        initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                                        animationSpec =
                                            tween(
                                                PREDICTIVE_BACK_ANIM_DURATION,
                                                easing = LinearEasing,
                                            ),
                                    )
                                    .togetherWith(
                                        fadeOut(
                                            animationSpec =
                                                tween(
                                                    PREDICTIVE_BACK_ANIM_DURATION,
                                                    easing = LinearEasing,
                                                )
                                        ) +
                                            scaleOut(
                                                targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                                                animationSpec =
                                                    tween(
                                                        PREDICTIVE_BACK_ANIM_DURATION,
                                                        easing = LinearEasing,
                                                    ),
                                            )
                                    )
                            }
                ) { key ->
                    // Create ViewModel with the id from the route
                    // Note: For Nav3, we pass the id directly instead of via SavedStateHandle
                    val viewModel = hiltViewModel<DetailsViewModel>()
                    // Pull content up behind the status bar for edge-to-edge backdrop
                    val statusBarTop =
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    Box(modifier = Modifier.offset { IntOffset(0, -statusBarTop.roundToPx()) }) {
                        DetailsRoute(
                            onItemClick = { id ->
                                navigationState.topLevelBackStack.navigateTo(
                                    com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                                )
                            },
                            onSeeAllCastClick = {
                                navigationState.topLevelBackStack.navigateTo(
                                    com.keisardev.moviesandbeyond.ui.navigation.CreditsRoute(key.id)
                                )
                            },
                            navigateToAuth = {
                                navigationState.topLevelBackStack.navigateTo(
                                    com.keisardev.moviesandbeyond.ui.navigation.AuthRoute
                                )
                            },
                            onBackClick = { navigationState.topLevelBackStack.goBack() },
                            viewModel = viewModel,
                            detailsId = key.id,
                        )
                    }
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
                                            easing = FastOutSlowInEasing,
                                        ),
                                ) + fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -fullWidth / 4 },
                                        animationSpec =
                                            tween(
                                                NAVIGATION_ANIM_DURATION,
                                                easing = FastOutSlowInEasing,
                                            ),
                                    ) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
                                )
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
                                                    easing = FastOutSlowInEasing,
                                                ),
                                        ) +
                                            fadeOut(
                                                animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)
                                            )
                                    )
                            } +
                            NavDisplay.predictivePopTransitionSpec {
                                // Predictive back: smooth slide with scale, no harsh fades
                                scaleIn(
                                        initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                                        animationSpec =
                                            tween(
                                                PREDICTIVE_BACK_ANIM_DURATION,
                                                easing = LinearEasing,
                                            ),
                                    )
                                    .togetherWith(
                                        slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec =
                                                tween(
                                                    PREDICTIVE_BACK_ANIM_DURATION,
                                                    easing = LinearEasing,
                                                ),
                                        ) +
                                            scaleOut(
                                                targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                                                animationSpec =
                                                    tween(
                                                        PREDICTIVE_BACK_ANIM_DURATION,
                                                        easing = LinearEasing,
                                                    ),
                                            )
                                    )
                            }
                ) { key ->
                    val viewModel = hiltViewModel<DetailsViewModel>()
                    CreditsRoute(
                        onItemClick = { id ->
                            navigationState.topLevelBackStack.navigateTo(
                                com.keisardev.moviesandbeyond.ui.navigation.DetailsRoute(id)
                            )
                        },
                        onBackClick = { navigationState.topLevelBackStack.goBack() },
                        viewModel = viewModel,
                        detailsId = key.id,
                    )
                }
            },
    )
}
