import java.util.Properties

plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

android {
    namespace = "com.keisardev.moviesandbeyond.core.network"

    buildFeatures { buildConfig = true }

    defaultConfig {
        val baseUrl = localProperties.getProperty("BASE_URL") ?: ""
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        val accessToken = localProperties.getProperty("ACCESS_TOKEN") ?: ""
        buildConfigField("String", "ACCESS_TOKEN", "\"$accessToken\"")
    }
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)
    testImplementation(libs.ktor.client.mock)
}
