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
    implementation(libs.haze)
    implementation(libs.haze.materials)
    androidTestImplementation(projects.core.testing)

    screenshotTestImplementation(libs.screenshot.validation.api)
}
