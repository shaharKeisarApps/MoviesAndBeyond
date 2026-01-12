plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.library.compose")
}

android {
    namespace = "com.keisardev.moviesandbeyond.core.ui"

    defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
}

dependencies {
    api(libs.compose.material.iconsExtended)

    // Landscapist image loading (KMP core - no Coil/Glide)
    api(libs.landscapist.core)
    api(libs.landscapist.image)
    api(libs.landscapist.animation)
    api(libs.landscapist.placeholder)
    api(libs.landscapist.palette)

    api(libs.haze)
    api(libs.haze.materials)

    androidTestImplementation(projects.core.testing)
}
