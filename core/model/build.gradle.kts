plugins {
    id("moviesandbeyond.android.library")
    alias(libs.plugins.kotlinSerialization)

}

android {
    namespace = "com.keisardev.moviesandbeyond.core.model"
}

dependencies {
    implementation(libs.navigation3.runtime)
}