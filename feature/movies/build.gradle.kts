plugins {
    id("moviesandbeyond.android.feature")
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.keisardev.moviesandbeyond.feature.movies"

    @Suppress("UnstableApiUsage")
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    implementation(projects.data)

    testImplementation(projects.core.testing)
    api(libs.haze)
    api(libs.haze.materials)
    androidTestImplementation(projects.core.testing)

    // Screenshot testing
    screenshotTestImplementation(libs.screenshot.validation.api)
}
