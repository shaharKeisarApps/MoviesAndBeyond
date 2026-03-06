plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.tv" }

dependencies {
    implementation(projects.data)
    implementation(projects.core.network)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
