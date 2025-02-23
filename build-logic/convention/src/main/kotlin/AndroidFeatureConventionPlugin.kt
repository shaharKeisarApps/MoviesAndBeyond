
import com.android.build.gradle.LibraryExtension
import com.keisardev.moviesandbeyond.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin: Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("moviesandbeyond.android.library")
                apply("moviesandbeyond.android.library.compose")
                apply("moviesandbeyond.android.hilt")
            }

            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }

                dependencies {
                    add("implementation", project(":core:ui"))

                    add("implementation", findLibrary("androidx-lifecycle-runtime-compose"))
                    add("implementation", findLibrary("androidx-hilt-navigation-compose"))
                }
            }
        }
    }
}