plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.library.compose")
}

android {
    namespace = "com.keisardev.moviesandbeyond.core.ui"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api(libs.compose.material.iconsExtended)
    implementation(libs.coil.kt.compose)
    api(libs.haze)
    api(libs.haze.materials)


    androidTestImplementation(projects.core.testing)
}