package com.keisardev.moviesandbeyond.core.local.di

import android.content.Context
import androidx.room.Room
import com.keisardev.moviesandbeyond.core.local.database.MoviesAndBeyondDatabase
import com.keisardev.moviesandbeyond.core.local.database.MoviesAndBeyondDatabase.Companion.MIGRATION_1_2
import com.keisardev.moviesandbeyond.core.local.database.MoviesAndBeyondDatabase.Companion.MIGRATION_2_3
import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedMovieDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.CachedTvDetailsDao
import com.keisardev.moviesandbeyond.core.local.database.dao.FavoriteContentDao
import com.keisardev.moviesandbeyond.core.local.database.dao.WatchlistContentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Singleton
    @Provides
    fun provideMoviesAndBeyondDatabase(
        @ApplicationContext context: Context
    ): MoviesAndBeyondDatabase {
        return Room.databaseBuilder(
                context, MoviesAndBeyondDatabase::class.java, "movies_and_beyond.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Singleton
    @Provides
    fun provideFavoriteContentDao(db: MoviesAndBeyondDatabase): FavoriteContentDao {
        return db.favoriteContentDao()
    }

    @Singleton
    @Provides
    fun provideWatchlistContentDao(db: MoviesAndBeyondDatabase): WatchlistContentDao {
        return db.watchlistContentDao()
    }

    @Singleton
    @Provides
    fun provideAccountDetailsDao(db: MoviesAndBeyondDatabase): AccountDetailsDao {
        return db.accountDetailsDao()
    }

    @Singleton
    @Provides
    fun provideCachedContentDao(db: MoviesAndBeyondDatabase): CachedContentDao {
        return db.cachedContentDao()
    }

    @Singleton
    @Provides
    fun provideCachedMovieDetailsDao(db: MoviesAndBeyondDatabase): CachedMovieDetailsDao {
        return db.cachedMovieDetailsDao()
    }

    @Singleton
    @Provides
    fun provideCachedTvDetailsDao(db: MoviesAndBeyondDatabase): CachedTvDetailsDao {
        return db.cachedTvDetailsDao()
    }
}
