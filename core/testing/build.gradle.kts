plugins {
    id("moviesandbeyond.android.library")
}

android {
    namespace = "com.keisardev.moviesandbeyond.core.testing"
}

dependencies {
    api(libs.androidx.test.ext.junit)
    api(libs.androidx.test.runner)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}