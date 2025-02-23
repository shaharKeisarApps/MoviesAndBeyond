import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.hilt")
}

android {
    namespace = "com.keisardev.moviesandbeyond.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val baseUrl = gradleLocalProperties(rootDir, providers).getProperty("BASE_URL") ?: ""
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        val accessToken = gradleLocalProperties(rootDir, providers)
            .getProperty("ACCESS_TOKEN") ?: ""
        buildConfigField("String", "ACCESS_TOKEN", "\"$accessToken\"")
    }
}

dependencies {
    implementation(projects.core.model)

    api(libs.retrofit)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android) // or ktor.client.android/ktor.client.ios
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.converter.moshi)
    implementation(libs.okhttp.logging.interceptor)
    ksp(libs.moshi.kotlin.codegen)

    testImplementation(projects.core.testing)
    testImplementation(libs.okhttp.mockwebserver)
}