plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.movies" }

dependencies {
    implementation(projects.data)

    testImplementation(projects.core.testing)
    api(libs.haze)
    api(libs.haze.materials)
    androidTestImplementation(projects.core.testing)
    // Landscapist comes from core:ui via the feature plugin
}
