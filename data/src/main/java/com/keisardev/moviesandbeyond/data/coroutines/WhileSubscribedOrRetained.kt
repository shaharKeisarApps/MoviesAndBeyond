/*
 * Copyright 2024 keisardev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keisardev.moviesandbeyond.data.coroutines

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingCommand
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

/**
 * A [SharingStarted] strategy that starts sharing when the first subscriber appears and immediately
 * stops when the last subscriber disappears, but keeps the replay cache forever even if
 * configuration changes happen.
 *
 * This is a safer alternative to [SharingStarted.WhileSubscribed] with a timeout, which can cause
 * issues during configuration changes (rotation, dark mode toggle, etc.) where the 5-second timeout
 * might:
 * - Restart expensive operations unnecessarily
 * - Cause UI flicker
 * - Create redundant network requests
 *
 * How it works:
 * 1. When subscription count goes from 0 to 1, emits START immediately
 * 2. When subscription count goes from 1 to 0, waits for:
 *     - Choreographer frame callback (ensures UI has time to re-subscribe after config change)
 *     - Handler message queue to clear (ensures pending re-subscriptions are processed)
 * 3. Only then emits STOP
 *
 * Reference: https://blog.p-y.wtf/whilesubscribed5000 Implementation based on:
 * https://github.com/skydoves/chatgpt-android
 */
@OptIn(ExperimentalCoroutinesApi::class)
object WhileSubscribedOrRetained : SharingStarted {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    /** Check if running in a unit test environment where Android framework is not available. */
    private val isInTestEnvironment: Boolean by lazy {
        try {
            // This will fail in unit tests where Android framework is not mocked
            Looper.getMainLooper()
            false
        } catch (_: RuntimeException) {
            true
        }
    }

    override fun command(subscriptionCount: StateFlow<Int>): Flow<SharingCommand> =
        subscriptionCount
            .transformLatest { count ->
                if (count > 0) {
                    emit(SharingCommand.START)
                } else {
                    if (!isInTestEnvironment) {
                        val posted = CompletableDeferred<Unit>()
                        // Wait for frame + handler queue to clear before stopping
                        // This gives the UI time to re-subscribe during configuration changes
                        Choreographer.getInstance().postFrameCallback {
                            handler.postAtFrontOfQueue { handler.post { posted.complete(Unit) } }
                        }
                        posted.await()
                    }
                    emit(SharingCommand.STOP)
                }
            }
            .dropWhile { it != SharingCommand.START }
            .distinctUntilChanged()

    override fun toString(): String = "WhileSubscribedOrRetained"
}

/**
 * Converts a [Flow] to a [StateFlow] using [SharingStarted.WhileSubscribed] sharing strategy.
 *
 * This is a convenience extension that wraps [stateIn] with the recommended sharing strategy for
 * ViewModels, ensuring proper behavior during configuration changes and navigation.
 *
 * Uses WhileSubscribed(5000) to:
 * - Keep the upstream flow active for 5 seconds after the last subscriber disappears
 * - Allow configuration changes to re-subscribe without restarting the flow
 * - Ensure navigation argument changes propagate to UI when screen becomes visible
 *
 * Note: Changed from WhileSubscribedOrRetained to fix bug where StateFlow would cache empty values
 * when navigation arguments changed while screen was not visible.
 *
 * @param scope The [CoroutineScope] in which sharing is started (typically viewModelScope)
 * @param initialValue The initial value of the StateFlow before any emissions
 * @return A [StateFlow] that shares emissions using WhileSubscribed(5000) strategy
 */
@JvmSynthetic
fun <T> Flow<T>.stateInWhileSubscribed(scope: CoroutineScope, initialValue: T): StateFlow<T> =
    stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = initialValue,
    )
