package com.keisardev.moviesandbeyond.feature.tv.di

import androidx.hilt.navigation.compose.hiltViewModel
import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.feature.details.DetailsRoute
import com.keisardev.moviesandbeyond.feature.tv.FeedRoute
import com.keisardev.moviesandbeyond.feature.tv.ItemsRoute
import com.keisardev.moviesandbeyond.feature.tv.TvShowsFeedRoute
import com.keisardev.moviesandbeyond.feature.tv.TvShowsItemsRoute
import com.keisardev.moviesandbeyond.feature.tv.TvShowsViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object TvNavModule {
    @IntoSet
    @Provides
    fun provide(navigator: AppNavigator): EntryProviderInstaller = {
        entry<TvShowsFeedRoute> {
            val viewModel = hiltViewModel<TvShowsViewModel>()
            FeedRoute(
                navigateToDetails = { id -> navigator.navigateTo(DetailsRoute(id)) },
                navigateToItems = { category -> navigator.navigateTo(TvShowsItemsRoute(category)) },
                viewModel = viewModel,
            )
        }
        entry<TvShowsItemsRoute> { key ->
            val viewModel = hiltViewModel<TvShowsViewModel>()
            ItemsRoute(
                categoryName = key.category,
                onItemClick = { id -> navigator.navigateTo(DetailsRoute(id)) },
                onBackClick = { navigator.goBack() },
                viewModel = viewModel,
            )
        }
    }
}
