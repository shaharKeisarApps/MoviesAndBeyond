plugins {
    id("moviesandbeyond.android.feature")
}

android {
    namespace = "com.keisardev.moviesandbeyond.feature.details"
}

dependencies {
    implementation(projects.data)

    testImplementation(projects.core.testing)

    androidTestImplementation(projects.core.testing)
}