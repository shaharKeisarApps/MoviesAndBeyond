package com.keisardev.moviesandbeyond.sync.di

import com.keisardev.moviesandbeyond.data.util.SyncScheduler
import com.keisardev.moviesandbeyond.sync.SyncSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SyncSchedulerModule{
    @Binds
    abstract fun provideSyncScheduler(
        syncSchedulerImpl: SyncSchedulerImpl
    ): SyncScheduler
}