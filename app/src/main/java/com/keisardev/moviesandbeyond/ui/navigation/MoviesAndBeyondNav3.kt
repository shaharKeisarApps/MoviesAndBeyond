package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller

private const val NAVIGATION_ANIM_DURATION = 400
private const val PREDICTIVE_BACK_ANIM_DURATION = 300
private const val PREDICTIVE_BACK_SCALE_INCOMING = 0.9f
private const val PREDICTIVE_BACK_SCALE_OUTGOING = 0.85f

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MoviesAndBeyondNav3(
    navigator: AppNavigatorImpl,
    entryProviders: Set<EntryProviderInstaller>,
    paddingValues: PaddingValues,
) {
    NavDisplay(
        modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        backStack = navigator.backStack,
        onBack = { navigator.handleBack() },
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
        predictivePopTransitionSpec = {
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
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider = entryProvider { entryProviders.forEach { installer -> this.installer() } },
    )
}
