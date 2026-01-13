package com.keisardev.moviesandbeyond

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object ProjectConfig {
    const val JAVA_VERSION_INT = 17
    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_17
    val JVM_TARGET: JvmTarget = JvmTarget.JVM_17

    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36
    const val MIN_SDK = 28
}
