plugins { id("moviesandbeyond.android.library") }

android { namespace = "com.keisardev.moviesandbeyond.core.model" }

dependencies {
    // @Immutable annotation for Compose compiler stability
    implementation(platform(libs.compose.bom))
    implementation("androidx.compose.runtime:runtime")
}
