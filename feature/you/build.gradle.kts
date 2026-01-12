plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.you" }

dependencies {
    implementation(projects.data)

    // Landscapist comes from core:ui via the feature plugin

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
