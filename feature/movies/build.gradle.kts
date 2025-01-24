plugins {
    id("moviesandbeyond.android.feature")
}

android {
    namespace = "com.keisardev.moviesandbeyond.feature.movies"
}

dependencies {
    implementation(projects.data)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}