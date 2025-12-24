package com.keisardev.moviesandbeyond.core.local.di

import android.content.Context
import androidx.room.Room
import com.keisardev.moviesandbeyond.core.local.database.MoviesAndBeyondDatabase
import com.keisardev.moviesandbeyond.core.local.database.dao.AccountDetailsDao
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
                context,
                MoviesAndBeyondDatabase::class.java,
                "movies_and_beyond.db",
            )
            /*.addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12
            )*/
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
}
