package com.keisardev.moviesandbeyond.feature.auth.di

import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.AuthRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.feature.auth.AuthRoute as AuthScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object AuthNavModule {
    @IntoSet
    @Provides
    fun provide(navigator: AppNavigator): EntryProviderInstaller = {
        entry<AuthRoute> { AuthScreen(onBackClick = { navigator.goBack() }) }
    }
}
