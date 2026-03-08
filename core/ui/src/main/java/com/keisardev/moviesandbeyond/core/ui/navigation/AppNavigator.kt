package com.keisardev.moviesandbeyond.core.ui.navigation

/** Navigation contract exposed to feature modules for type-safe navigation. */
interface AppNavigator {
    fun navigateTo(key: Any)

    fun goBack()

    fun navigateToAuth()

    fun switchTab(tab: TopLevelRoute)

    val currentTab: TopLevelRoute
}
