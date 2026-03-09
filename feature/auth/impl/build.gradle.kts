plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.auth" }

dependencies {
    api(projects.feature.auth.api)
    implementation(projects.data)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
