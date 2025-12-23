plugins { id("moviesandbeyond.android.feature") }

android { namespace = "com.keisardev.moviesandbeyond.feature.auth" }

dependencies {
  implementation(projects.data)

  testImplementation(projects.core.testing)

  androidTestImplementation(projects.core.testing)
}
