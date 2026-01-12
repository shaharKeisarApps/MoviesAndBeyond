package com.keisardev.moviesandbeyond.core.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocals for shared element transitions. These are provided at the root of the app and
 * used by components that participate in shared element transitions.
 *
 * Usage:
 * 1. Wrap your app with SharedTransitionLayout and provide LocalSharedTransitionScope
 * 2. In each navigation entry, provide LocalAnimatedVisibilityScope
 * 3. Components can then access these scopes to use sharedElement/sharedBounds modifiers
 */

/**
 * Provides the SharedTransitionScope from SharedTransitionLayout. This scope enables shared element
 * transitions between different screens.
 *
 * Must be provided at the root of the navigation hierarchy.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Provides the AnimatedVisibilityScope for the current screen/entry. This scope is required by
 * sharedElement and sharedBounds modifiers to coordinate enter/exit animations.
 *
 * Must be provided for each navigation entry that participates in shared element transitions.
 */
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
