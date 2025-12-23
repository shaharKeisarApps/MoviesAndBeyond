package com.keisardev.moviesandbeyond.data.di

import com.keisardev.moviesandbeyond.data.repository.AuthRepository
import com.keisardev.moviesandbeyond.data.repository.ContentRepository
import com.keisardev.moviesandbeyond.data.repository.DetailsRepository
import com.keisardev.moviesandbeyond.data.repository.LibraryRepository
import com.keisardev.moviesandbeyond.data.repository.SearchRepository
import com.keisardev.moviesandbeyond.data.repository.UserRepository
import com.keisardev.moviesandbeyond.data.repository.impl.AuthRepositoryImpl
import com.keisardev.moviesandbeyond.data.repository.impl.ContentRepositoryImpl
import com.keisardev.moviesandbeyond.data.repository.impl.DetailsRepositoryImpl
import com.keisardev.moviesandbeyond.data.repository.impl.LibraryRepositoryImpl
import com.keisardev.moviesandbeyond.data.repository.impl.SearchRepositoryImpl
import com.keisardev.moviesandbeyond.data.repository.impl.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindContentRepository(
        contentRepositoryImpl: ContentRepositoryImpl
    ): ContentRepository

    @Binds
    abstract fun bindSearchRepository(searchRepositoryImpl: SearchRepositoryImpl): SearchRepository

    @Binds abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindDetailsRepository(
        detailsRepositoryImpl: DetailsRepositoryImpl
    ): DetailsRepository

    @Binds
    abstract fun bindLibraryRepository(
        libraryRepositoryImpl: LibraryRepositoryImpl
    ): LibraryRepository
}
