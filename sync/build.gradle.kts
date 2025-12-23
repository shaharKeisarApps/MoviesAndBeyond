plugins {
  id("moviesandbeyond.android.library")
  id("moviesandbeyond.android.hilt")
}

android { namespace = "com.keisardev.moviesandbeyond.sync" }

dependencies {
  implementation(projects.data)

  implementation(libs.androidx.hilt.work)
  implementation(libs.androidx.work.runtime.ktx)
  ksp(libs.androidx.hilt.compiler)
}
