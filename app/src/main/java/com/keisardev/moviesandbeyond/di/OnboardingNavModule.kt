package com.keisardev.moviesandbeyond.di

import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.core.ui.navigation.OnboardingRoute
import com.keisardev.moviesandbeyond.ui.OnboardingScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object OnboardingNavModule {
    @IntoSet
    @Provides
    fun provide(navigator: AppNavigator): EntryProviderInstaller = {
        entry<OnboardingRoute> { OnboardingScreen(navigateToAuth = { navigator.navigateToAuth() }) }
    }
}
