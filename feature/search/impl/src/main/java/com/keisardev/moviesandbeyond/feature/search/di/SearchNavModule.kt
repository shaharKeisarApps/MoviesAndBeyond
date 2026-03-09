package com.keisardev.moviesandbeyond.feature.search.di

import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.feature.details.DetailsRoute
import com.keisardev.moviesandbeyond.feature.search.SearchRoute
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object SearchNavModule {
    @IntoSet
    @Provides
    fun provide(navigator: AppNavigator): EntryProviderInstaller = {
        entry<SearchRoute> {
            SearchRoute(navigateToDetail = { id -> navigator.navigateTo(DetailsRoute(id)) })
        }
    }
}
