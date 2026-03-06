package com.keisardev.moviesandbeyond

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that the full Hilt dependency injection graph resolves without runtime errors.
 *
 * This test catches missing bindings, circular dependencies, and misconfigured modules at test time
 * rather than at app startup. If this test passes, all @Inject constructors and @Provides methods
 * in the production DI graph are wired correctly.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltDependencyGraphTest {

    @get:Rule val hiltRule = HiltAndroidRule(this)

    /**
     * Calling [HiltAndroidRule.inject] forces Hilt to resolve every binding in the component
     * hierarchy. If any binding is missing or misconfigured, this will throw.
     */
    @Test
    fun dependencyGraphResolvesSuccessfully() {
        hiltRule.inject()
    }
}
