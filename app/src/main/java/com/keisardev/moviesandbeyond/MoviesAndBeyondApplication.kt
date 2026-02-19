package com.keisardev.moviesandbeyond

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.LandscapistConfig
import com.skydoves.landscapist.core.builder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MoviesAndBeyondApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        initLandscapist()
    }

    /**
     * Initialize Landscapist with optimized cache sizes for image-heavy app.
     *
     * Memory cache: 128 MB - supports ~85 poster images (500x750 @ 1.5MB each) Disk cache: 256 MB -
     * persists images across navigation and app restarts
     */
    private fun initLandscapist() {
        Landscapist.builder(this)
            .config(
                LandscapistConfig(
                    memoryCacheSize = 128 * 1024 * 1024L, // 128 MB
                    diskCacheSize = 256 * 1024 * 1024L, // 256 MB
                )
            )
            .build()
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .apply {
                    setWorkerFactory(workerFactory)
                    if (BuildConfig.DEBUG) {
                        setMinimumLoggingLevel(android.util.Log.DEBUG)
                    } else {
                        setMinimumLoggingLevel(android.util.Log.ERROR)
                    }
                }
                .build()
}
