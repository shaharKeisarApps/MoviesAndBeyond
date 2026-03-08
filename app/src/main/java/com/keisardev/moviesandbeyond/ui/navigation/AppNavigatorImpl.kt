package com.keisardev.moviesandbeyond.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.AuthRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.MoviesFeedRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.OnboardingRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.SearchRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.TopLevelRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.TvShowsFeedRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.YouRoute
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Hilt-managed navigator that owns the back stack and tab switching logic. Survives configuration
 * changes via [ActivityRetainedScoped].
 */
@ActivityRetainedScoped
class AppNavigatorImpl @Inject constructor() : AppNavigator {

    /** Whether onboarding has been completed or skipped. */
    var hasCompletedOnboarding: Boolean by mutableStateOf(false)
        private set

    /** Back stack for pre-main-content flow (onboarding, auth). */
    val preMainBackStack: SnapshotStateList<NavKey> = mutableStateListOf()

    /** Per-tab back stacks keyed by feed route. */
    private val topLevelStacks: LinkedHashMap<NavKey, SnapshotStateList<NavKey>> =
        linkedMapOf(MoviesFeedRoute to mutableStateListOf<NavKey>(MoviesFeedRoute))

    /** Currently active tab. */
    override var currentTab: TopLevelRoute by mutableStateOf(TopLevelRoute.Movies)
        private set

    /** The back stack that NavDisplay should observe. */
    val backStack: SnapshotStateList<NavKey>
        get() =
            if (hasCompletedOnboarding) {
                topLevelStacks[feedRouteForTab(currentTab)]!!
            } else {
                preMainBackStack
            }

    /**
     * Called from composable when MainActivityUiState.Success provides the hideOnboarding value.
     * Only takes effect once.
     */
    fun setHideOnboarding(hide: Boolean) {
        if (hasCompletedOnboarding) return
        if (hide) {
            hasCompletedOnboarding = true
            preMainBackStack.clear()
        } else if (preMainBackStack.isEmpty()) {
            preMainBackStack.add(OnboardingRoute)
        }
    }

    /** Mark onboarding as completed and transition to main content. */
    fun completeOnboarding() {
        hasCompletedOnboarding = true
        preMainBackStack.clear()
    }

    override fun navigateTo(key: Any) {
        val navKey = key as NavKey
        if (hasCompletedOnboarding) {
            topLevelStacks[feedRouteForTab(currentTab)]!!.add(navKey)
        } else {
            preMainBackStack.add(navKey)
        }
    }

    override fun goBack() {
        handleBack()
    }

    override fun navigateToAuth() {
        if (hasCompletedOnboarding) {
            topLevelStacks[feedRouteForTab(currentTab)]!!.add(AuthRoute)
        } else {
            preMainBackStack.add(AuthRoute)
        }
    }

    override fun switchTab(tab: TopLevelRoute) {
        val feedRoute = feedRouteForTab(tab)

        if (tab == currentTab) {
            // Already on this tab — pop to root
            val stack = topLevelStacks[feedRoute]!!
            while (stack.size > 1) {
                stack.removeLastOrNull()
            }
            return
        }

        // Create new stack if needed
        if (!topLevelStacks.containsKey(feedRoute)) {
            topLevelStacks[feedRoute] = mutableStateListOf<NavKey>(feedRoute)
        }

        currentTab = tab
    }

    /**
     * Handle back navigation.
     *
     * @return true if back was handled, false if we should exit
     */
    fun handleBack(): Boolean {
        return if (hasCompletedOnboarding) {
            val stack = topLevelStacks[feedRouteForTab(currentTab)]!!
            if (stack.size > 1) {
                stack.removeLastOrNull()
                true
            } else {
                false
            }
        } else {
            preMainBackStack.removeLastOrNull() != null
        }
    }

    /** Whether the current tab's back stack is at root (for showing/hiding bottom bar). */
    fun isAtTabRoot(): Boolean {
        if (!hasCompletedOnboarding) return false
        return topLevelStacks[feedRouteForTab(currentTab)]?.size == 1
    }

    private fun feedRouteForTab(tab: TopLevelRoute): NavKey =
        when (tab) {
            TopLevelRoute.Movies -> MoviesFeedRoute
            TopLevelRoute.TvShows -> TvShowsFeedRoute
            TopLevelRoute.Search -> SearchRoute
            TopLevelRoute.You -> YouRoute
        }
}
