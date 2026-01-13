package com.keisardev.moviesandbeyond.data.di

import com.keisardev.moviesandbeyond.core.model.content.ContentItem
import com.keisardev.moviesandbeyond.data.store.MovieContentKey
import com.keisardev.moviesandbeyond.data.store.MovieContentStoreFactory
import com.keisardev.moviesandbeyond.data.store.TvContentKey
import com.keisardev.moviesandbeyond.data.store.TvContentStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import org.mobilenativefoundation.store.store5.Store

/** Hilt module providing Store5 instances for offline-first caching. */
@Module
@InstallIn(SingletonComponent::class)
object StoreModule {

    @Singleton
    @Provides
    fun provideMovieContentStore(
        factory: MovieContentStoreFactory
    ): Store<MovieContentKey, List<ContentItem>> = factory.create()

    @Singleton
    @Provides
    fun provideTvContentStore(
        factory: TvContentStoreFactory
    ): Store<TvContentKey, List<ContentItem>> = factory.create()
}
