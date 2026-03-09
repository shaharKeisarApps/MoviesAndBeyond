plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.you" }

dependencies {
    api(projects.feature.you.api)
    implementation(projects.feature.details.api)
    implementation(projects.data)

    // Landscapist comes from core:ui via the feature plugin

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
