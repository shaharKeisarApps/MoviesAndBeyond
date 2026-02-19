package com.keisardev.moviesandbeyond.benchmarks

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates baseline profiles for MoviesAndBeyond app.
 *
 * Baseline profiles improve app startup and runtime performance by pre-compiling critical code
 * paths. Run this generator to create profiles that will be included in release builds.
 *
 * Run from command line:
 * ```
 * ./gradlew :app:generateBaselineProfile
 * ```
 *
 * Or run from Android Studio by right-clicking on the test class.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            maxIterations = 8,
            includeInStartupProfile = true,
            profileBlock = {
                // Cold start - captures startup code paths
                startActivityAndWait()

                // Wait for initial content to load
                device.waitForIdle()
                device.wait(Until.hasObject(By.res("content_list")), WAIT_TIMEOUT)

                // Navigate through main tabs to capture common user journeys
                navigateToTab("TV Shows")
                navigateToTab("Search")
                navigateToTab("You")
                navigateToTab("Movies")

                // Scroll content to capture lazy loading paths
                scrollContent()

                // Open detail screen if content is visible
                openFirstContentItem()
            },
        )
    }

    @Test
    fun generateStartupProfile() {
        rule.collect(
            packageName = PACKAGE_NAME,
            maxIterations = 5,
            includeInStartupProfile = true,
            profileBlock = {
                // Minimal startup path - just launch and wait for first frame
                startActivityAndWait()

                // Wait for initial content load
                device.waitForIdle()
            },
        )
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.navigateToTab(tabName: String) {
        val tab = device.findObject(By.text(tabName))
        tab?.click()
        device.waitForIdle()
        // Give time for content to start loading
        Thread.sleep(NAVIGATION_DELAY)
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.scrollContent() {
        // Scroll down to trigger lazy loading
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            device.displayWidth / 2,
            device.displayHeight / 4,
            SCROLL_STEPS,
        )
        device.waitForIdle()

        // Scroll back up
        device.swipe(
            device.displayWidth / 2,
            device.displayHeight / 4,
            device.displayWidth / 2,
            device.displayHeight * 3 / 4,
            SCROLL_STEPS,
        )
        device.waitForIdle()
    }

    private fun androidx.benchmark.macro.MacrobenchmarkScope.openFirstContentItem() {
        // Try to find and click on a content card
        val contentCard = device.findObject(By.res("content_card"))
        if (contentCard != null) {
            contentCard.click()
            device.waitForIdle()
            Thread.sleep(CONTENT_LOAD_DELAY)

            // Navigate back
            device.pressBack()
            device.waitForIdle()
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.keisardev.moviesandbeyond"
        private const val WAIT_TIMEOUT = 5000L
        private const val NAVIGATION_DELAY = 500L
        private const val CONTENT_LOAD_DELAY = 1000L
        private const val SCROLL_STEPS = 10
    }
}
