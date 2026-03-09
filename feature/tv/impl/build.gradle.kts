plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.tv" }

dependencies {
    api(projects.feature.tv.api)
    implementation(projects.feature.details.api)
    implementation(projects.data)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
