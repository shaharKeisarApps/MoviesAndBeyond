plugins {
    id("moviesandbeyond.android.application")
    id("moviesandbeyond.android.application.compose")
    id("moviesandbeyond.android.hilt")
    alias(libs.plugins.kotlinSerialization) // Added Kotlin serialization plugin
}

android {
    namespace = "com.keisardev.moviesandbeyond"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.keisardev.moviesandbeyond"
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data)
    implementation(projects.feature.auth)
    implementation(projects.feature.details)
    implementation(projects.feature.movies)
    implementation(projects.feature.search)
    implementation(projects.feature.tv)
    implementation(projects.feature.you)
    implementation(projects.sync)

    implementation(libs.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.work)
    // implementation(libs.androidx.navigation.compose) // Corrected comment
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.kt.compose)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.ui)
    implementation(libs.ui.graphics)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    testImplementation(libs.junit)
}