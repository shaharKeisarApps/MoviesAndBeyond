plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.hilt")
}

android {
    namespace = "com.keisardev.moviesandbeyond.data"
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    api(projects.core.model)

    implementation(projects.core.local)
    implementation(projects.core.network)

    // Store5 for offline-first caching - exposed as API for ViewModels to handle StoreReadResponse
    api(libs.store5)

    testImplementation(projects.core.testing)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.androidx.datastore)
    testImplementation(libs.protobuf.kotlin.lite)
}
