package com.keisardev.moviesandbeyond.feature.movies.di

import androidx.hilt.navigation.compose.hiltViewModel
import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.feature.details.DetailsRoute
import com.keisardev.moviesandbeyond.feature.movies.FeedRoute
import com.keisardev.moviesandbeyond.feature.movies.ItemsRoute
import com.keisardev.moviesandbeyond.feature.movies.MoviesFeedRoute
import com.keisardev.moviesandbeyond.feature.movies.MoviesItemsRoute
import com.keisardev.moviesandbeyond.feature.movies.MoviesViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object MoviesNavModule {
    @IntoSet
    @Provides
    fun provide(navigator: AppNavigator): EntryProviderInstaller = {
        entry<MoviesFeedRoute> {
            val viewModel = hiltViewModel<MoviesViewModel>()
            FeedRoute(
                navigateToDetails = { id -> navigator.navigateTo(DetailsRoute(id)) },
                navigateToItems = { category -> navigator.navigateTo(MoviesItemsRoute(category)) },
                viewModel = viewModel,
            )
        }
        entry<MoviesItemsRoute> { key ->
            val viewModel = hiltViewModel<MoviesViewModel>()
            ItemsRoute(
                categoryName = key.category,
                onItemClick = { id -> navigator.navigateTo(DetailsRoute(id)) },
                onBackClick = { navigator.goBack() },
                viewModel = viewModel,
            )
        }
    }
}
