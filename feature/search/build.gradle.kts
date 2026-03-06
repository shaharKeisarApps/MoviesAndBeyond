plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.search" }

dependencies {
    implementation(projects.data)
    implementation(projects.core.network)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
