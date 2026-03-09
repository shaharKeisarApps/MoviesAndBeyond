plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.details" }

dependencies {
    api(projects.feature.details.api)
    implementation(projects.data)
    implementation(libs.androidx.navigation3.ui)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
