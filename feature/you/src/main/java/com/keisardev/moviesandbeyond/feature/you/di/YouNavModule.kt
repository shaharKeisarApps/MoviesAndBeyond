package com.keisardev.moviesandbeyond.feature.you.di

import androidx.hilt.navigation.compose.hiltViewModel
import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.core.ui.navigation.DetailsRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.EntryProviderInstaller
import com.keisardev.moviesandbeyond.core.ui.navigation.LibraryItemsRoute
import com.keisardev.moviesandbeyond.core.ui.navigation.YouRoute
import com.keisardev.moviesandbeyond.feature.you.YouRoute as YouScreen
import com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsRoute as LibraryItemsScreen
import com.keisardev.moviesandbeyond.feature.you.library_items.LibraryItemsViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(ActivityRetainedComponent::class)
object YouNavModule {
    @IntoSet
    @Provides
    fun provide(navigator: AppNavigator): EntryProviderInstaller = {
        entry<YouRoute> {
            YouScreen(
                navigateToAuth = { navigator.navigateToAuth() },
                navigateToLibraryItem = { type -> navigator.navigateTo(LibraryItemsRoute(type)) },
            )
        }
        entry<LibraryItemsRoute> { key ->
            val viewModel = hiltViewModel<LibraryItemsViewModel>()
            LibraryItemsScreen(
                onBackClick = { navigator.goBack() },
                navigateToDetails = { id -> navigator.navigateTo(DetailsRoute(id)) },
                viewModel = viewModel,
                libraryItemType = key.type,
            )
        }
    }
}
