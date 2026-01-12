package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

/**
 * Manages multiple independent back stacks for top-level navigation destinations. Each top-level
 * destination (tab) maintains its own back stack, and state is preserved when switching between
 * tabs.
 *
 * This implementation follows the Navigation 3 pattern for bottom navigation where each tab has
 * independent navigation history.
 *
 * @param startKey The initial top-level destination to show
 */
class TopLevelBackStack(startKey: NavKey) {
    /**
     * Map of top-level keys to their respective back stacks. Using LinkedHashMap to maintain
     * insertion order.
     */
    private val topLevelStacks: LinkedHashMap<NavKey, SnapshotStateList<NavKey>> =
        linkedMapOf(startKey to mutableStateListOf(startKey))

    /** The currently active top-level destination. */
    var topLevelKey: NavKey by mutableStateOf(startKey)
        private set

    /**
     * The current back stack for the active top-level destination. This is the stack that
     * NavDisplay should observe.
     */
    val backStack: SnapshotStateList<NavKey>
        get() = topLevelStacks[topLevelKey]!!

    /**
     * Switch to a different top-level destination (tab). Creates a new back stack for the
     * destination if it doesn't exist.
     *
     * @param key The top-level destination to switch to
     */
    fun switchToTopLevel(key: NavKey) {
        if (key == topLevelKey) {
            // If already on this tab, pop to the root
            val currentStack = topLevelStacks[key]!!
            while (currentStack.size > 1) {
                currentStack.removeLastOrNull()
            }
            return
        }

        // Create new stack if this top-level destination hasn't been visited
        if (!topLevelStacks.containsKey(key)) {
            topLevelStacks[key] = mutableStateListOf(key)
        }

        topLevelKey = key
    }

    /**
     * Navigate to a new destination within the current top-level stack.
     *
     * @param key The destination to navigate to
     */
    fun navigateTo(key: NavKey) {
        backStack.add(key)
    }

    /**
     * Pop the current destination from the back stack.
     *
     * @return The popped key, or null if we're at the root of the current stack
     */
    fun goBack(): NavKey? {
        // Don't pop if we're at the root of the current top-level stack
        if (backStack.size <= 1) {
            return null
        }
        return backStack.removeLastOrNull()
    }

    /**
     * Check if we can go back within the current stack.
     *
     * @return true if there are destinations to pop, false if at root
     */
    fun canGoBack(): Boolean = backStack.size > 1

    /** Get the current destination (top of the current back stack). */
    val currentDestination: NavKey?
        get() = backStack.lastOrNull()

    /** Check if a given top-level route is currently selected. */
    fun isTopLevelSelected(key: NavKey): Boolean =
        topLevelKey == key ||
            (topLevelKey is TopLevelRoute.Movies && key == MoviesFeedRoute) ||
            (topLevelKey is TopLevelRoute.TvShows && key == TvShowsFeedRoute) ||
            (topLevelKey is TopLevelRoute.Search && key == SearchRoute) ||
            (topLevelKey is TopLevelRoute.You && key == YouRoute)

    /** Get the feed route for a given top-level route. */
    fun getFeedRouteForTopLevel(topLevel: TopLevelRoute): NavKey =
        when (topLevel) {
            TopLevelRoute.Movies -> MoviesFeedRoute
            TopLevelRoute.TvShows -> TvShowsFeedRoute
            TopLevelRoute.Search -> SearchRoute
            TopLevelRoute.You -> YouRoute
        }
}

/**
 * Navigation state holder that manages the app's navigation including optional onboarding flow and
 * the main top-level navigation.
 *
 * @param hideOnboarding Whether to skip onboarding and go directly to main content
 */
class NavigationState(hideOnboarding: Boolean) {
    /** Whether onboarding has been completed or skipped. */
    var hasCompletedOnboarding: Boolean by mutableStateOf(hideOnboarding)
        private set

    /** The back stack for pre-main-content flow (onboarding, auth, etc.) */
    val preMainBackStack: SnapshotStateList<NavKey> =
        mutableStateListOf<NavKey>().apply {
            if (!hideOnboarding) {
                add(OnboardingRoute)
            }
        }

    /** The top-level back stack manager for main content. */
    val topLevelBackStack: TopLevelBackStack = TopLevelBackStack(TopLevelRoute.Movies)

    /** Mark onboarding as completed and transition to main content. */
    fun completeOnboarding() {
        hasCompletedOnboarding = true
        preMainBackStack.clear()
    }

    /** Navigate to auth screen. */
    fun navigateToAuth() {
        if (hasCompletedOnboarding) {
            topLevelBackStack.navigateTo(AuthRoute)
        } else {
            preMainBackStack.add(AuthRoute)
        }
    }

    /**
     * Handle back navigation.
     *
     * @return true if back was handled, false if we should exit
     */
    fun handleBack(): Boolean {
        return if (hasCompletedOnboarding) {
            topLevelBackStack.goBack() != null
        } else {
            preMainBackStack.removeLastOrNull() != null
        }
    }

    /** Get the current back stack based on app state. */
    val currentBackStack: SnapshotStateList<NavKey>
        get() =
            if (hasCompletedOnboarding) {
                topLevelBackStack.backStack
            } else {
                preMainBackStack
            }
}
