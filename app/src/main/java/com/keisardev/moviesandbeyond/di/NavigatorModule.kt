package com.keisardev.moviesandbeyond.di

import com.keisardev.moviesandbeyond.core.ui.navigation.AppNavigator
import com.keisardev.moviesandbeyond.ui.navigation.AppNavigatorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class NavigatorModule {
    @Binds abstract fun bindNavigator(impl: AppNavigatorImpl): AppNavigator
}
