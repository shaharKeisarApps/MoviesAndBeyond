// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.CupertinoMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun HazeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color =
        MaterialTheme.colorScheme.surface, // Use surface color for proper edge-to-edge backgrounds
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
    hazeState: HazeState,
    hazeStyle: HazeStyle = CupertinoMaterials.ultraThin(MaterialTheme.colorScheme.surface),
    blurTopBar: Boolean = true,
    blurBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit,
) {
    NestedScaffold(
        modifier = modifier,
        topBar = {
            AnimatedVisibility(
                visible = blurTopBar,
                enter =
                    slideInVertically(
                        initialOffsetY = { -it / 3 }, // Start slightly off-screen for subtlety
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy, // Subtle overshoot
                                stiffness = Spring.StiffnessMediumLow // Smooth but snappy
                                )) +
                        fadeIn(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium)),
                exit =
                    slideOutVertically(
                        targetOffsetY = { -it / 3 }, // Exit slightly off-screen
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium)) +
                        fadeOut(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium))) {
                    Box(
                        modifier = Modifier.hazeEffect(state = hazeState, style = hazeStyle),
                    ) {
                        topBar()
                    }
                }
            if (!blurTopBar) {
                topBar()
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = blurBottomBar,
                enter =
                    slideInVertically(
                        initialOffsetY = { it / 2 }, // Start from below for a grounded feel
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy, // Gentle bounce
                                stiffness = Spring.StiffnessLow // Slower, more deliberate
                                )) +
                        fadeIn(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                    visibilityThreshold = 0.01f // Ensure fade completes naturally
                                    )),
                exit =
                    slideOutVertically(
                        targetOffsetY = { it / 2 }, // Exit downward
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium)) +
                        fadeOut(
                            animationSpec =
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium))) {
                    Box(
                        modifier = Modifier.hazeEffect(state = hazeState, style = hazeStyle),
                    ) {
                        bottomBar()
                    }
                }
            if (!blurBottomBar) {
                bottomBar()
            }
        },
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = containerColor,
        contentColor = contentColor,
        contentWindowInsets = contentWindowInsets,
    ) { contentPadding ->
        Box(Modifier.hazeSource(state = hazeState)) { content(contentPadding) }
    }
}
