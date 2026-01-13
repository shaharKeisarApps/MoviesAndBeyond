plugins { alias(libs.plugins.moviesandbeyond.android.feature) }

android { namespace = "com.keisardev.moviesandbeyond.feature.tv" }

dependencies {
    implementation(projects.data)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}
