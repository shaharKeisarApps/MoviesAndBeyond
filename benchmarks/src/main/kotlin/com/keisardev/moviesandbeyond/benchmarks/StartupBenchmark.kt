package com.keisardev.moviesandbeyond.benchmarks

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Startup benchmark tests for MoviesAndBeyond app.
 *
 * Run this benchmark from Android Studio to see startup metrics, or run the following command from
 * the command line:
 * ```
 * ./gradlew :benchmarks:connectedBenchmarkAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.keisardev.moviesandbeyond.benchmarks.StartupBenchmark
 * ```
 *
 * Note: The benchmark must run on a physical device (not emulator) for accurate results.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule val benchmarkRule = MacrobenchmarkRule()

    /**
     * Measures cold startup time without any pre-compilation. This represents the worst-case
     * startup scenario.
     */
    @Test
    fun startupColdNoCompilation() =
        startup(
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
        )

    /**
     * Measures cold startup time with baseline profile compilation. This represents typical
     * production startup with baseline profiles installed.
     */
    @Test
    fun startupColdWithBaselineProfile() =
        startup(
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.COLD,
        )

    /**
     * Measures warm startup time. This represents startup when the app was recently used and is
     * still in memory.
     */
    @Test
    fun startupWarm() =
        startup(
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.WARM,
        )

    /**
     * Measures hot startup time. This represents startup when the app's activity is recreated but
     * the process is still warm.
     */
    @Test
    fun startupHot() =
        startup(
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.HOT,
        )

    private fun startup(
        compilationMode: CompilationMode,
        startupMode: StartupMode,
    ) =
        benchmarkRule.measureRepeated(
            packageName = "com.keisardev.moviesandbeyond",
            metrics = listOf(StartupTimingMetric()),
            iterations = 10,
            compilationMode = compilationMode,
            startupMode = startupMode,
            setupBlock = { pressHome() },
        ) {
            startActivityAndWait()
        }
}
