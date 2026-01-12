plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.hilt")
}

android { namespace = "com.keisardev.moviesandbeyond.data" }

dependencies {
    api(projects.core.model)

    implementation(projects.core.local)
    implementation(projects.core.network)

    testImplementation(projects.core.testing)
}
