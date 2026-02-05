plugins {
    id("moviesandbeyond.android.feature")
    // Temporarily disabled - screenshot test discovery issue
    // alias(libs.plugins.screenshot)
}

android {
    namespace = "com.keisardev.moviesandbeyond.feature.movies"

    // Temporarily disabled - screenshot test discovery issue
    // @Suppress("UnstableApiUsage")
    // experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

dependencies {
    implementation(projects.data)

    testImplementation(projects.core.testing)
    api(libs.haze)
    api(libs.haze.materials)
    androidTestImplementation(projects.core.testing)

    // Screenshot testing - temporarily disabled
    // screenshotTestImplementation(libs.screenshot.validation.api)
}
