package com.keisardev.moviesandbeyond.feature.details.di

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.ui.NavDisplay
import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.CreditsRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.DetailsRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.feature.details.CreditsRoute as CreditsScreen
import com.keisardev.moviesandbeyond.feature.details.DetailsRoute as DetailsScreen
import com.keisardev.moviesandbeyond.feature.details.DetailsViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

// Material 3 Emphasized Easing Curves
private val EmphasizedDecelerateEasing: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerateEasing: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

private const val NAVIGATION_ANIM_DURATION = 400
private const val FADE_THROUGH_EXIT_DURATION = 250
private const val FADE_THROUGH_ENTER_DURATION = 300
private const val FADE_THROUGH_GAP = 50
private const val PREDICTIVE_BACK_ANIM_DURATION = 300
private const val PREDICTIVE_BACK_SCALE_INCOMING = 0.9f
private const val PREDICTIVE_BACK_SCALE_OUTGOING = 0.85f

@Module
@InstallIn(ActivityRetainedComponent::class)
object DetailsNavModule {
    @IntoSet
    @Provides
    fun provideDetailsEntry(navigator: AppNavigator): EntryProviderInstaller = {
        entry<DetailsRoute>(metadata = detailsTransitionMetadata()) { key ->
            val viewModel = hiltViewModel<DetailsViewModel>()
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Box(modifier = Modifier.offset { IntOffset(0, -statusBarTop.roundToPx()) }) {
                DetailsScreen(
                    onItemClick = { id -> navigator.navigateTo(DetailsRoute(id)) },
                    onSeeAllCastClick = { navigator.navigateTo(CreditsRoute(key.id)) },
                    navigateToAuth = { navigator.navigateToAuth() },
                    onBackClick = { navigator.goBack() },
                    viewModel = viewModel,
                    detailsId = key.id,
                )
            }
        }
    }

    @IntoSet
    @Provides
    fun provideCreditsEntry(navigator: AppNavigator): EntryProviderInstaller = {
        entry<CreditsRoute>(metadata = creditsTransitionMetadata()) { key ->
            val viewModel = hiltViewModel<DetailsViewModel>()
            CreditsScreen(
                onItemClick = { id -> navigator.navigateTo(DetailsRoute(id)) },
                onBackClick = { navigator.goBack() },
                viewModel = viewModel,
                detailsId = key.id,
            )
        }
    }
}

/** Fade-through transition metadata for the details screen. */
private fun detailsTransitionMetadata() =
    NavDisplay.transitionSpec {
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
        NavDisplay.popTransitionSpec {
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
            scaleIn(
                    initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                    animationSpec = tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing),
                )
                .togetherWith(
                    fadeOut(
                        animationSpec = tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing)
                    ) +
                        scaleOut(
                            targetScale = PREDICTIVE_BACK_SCALE_OUTGOING,
                            animationSpec =
                                tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing),
                        )
                )
        }

/** Horizontal slide transition metadata for the credits screen. */
private fun creditsTransitionMetadata() =
    NavDisplay.transitionSpec {
        (slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
            ) + fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2)))
            .togetherWith(
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
                ) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
            )
    } +
        NavDisplay.popTransitionSpec {
            fadeIn(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
                .togetherWith(
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec =
                            tween(NAVIGATION_ANIM_DURATION, easing = FastOutSlowInEasing),
                    ) + fadeOut(animationSpec = tween(NAVIGATION_ANIM_DURATION / 2))
                )
        } +
        NavDisplay.predictivePopTransitionSpec {
            scaleIn(
                    initialScale = PREDICTIVE_BACK_SCALE_INCOMING,
                    animationSpec = tween(PREDICTIVE_BACK_ANIM_DURATION, easing = LinearEasing),
                )
                .togetherWith(
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
        }
