package com.keisardev.moviesandbeyond

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension) {
    pluginManager.apply("org.jetbrains.kotlin.android")

    commonExtension.compileSdk = ProjectConfig.COMPILE_SDK

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions { jvmTarget.set(ProjectConfig.JVM_TARGET) }
    }
}
