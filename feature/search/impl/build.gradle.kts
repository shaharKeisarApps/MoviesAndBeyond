plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.search" }

dependencies {
    api(projects.feature.search.api)
    implementation(projects.feature.details.api)
    implementation(projects.data)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
