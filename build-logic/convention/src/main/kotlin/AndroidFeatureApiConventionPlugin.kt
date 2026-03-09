import com.keisardev.moviesandbeyond.findLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("moviesandbeyond.android.library")
                apply(SerializationGradleSubplugin::class.java)
            }

            dependencies {
                add("implementation", findLibrary("androidx-navigation3-runtime"))
                add("implementation", findLibrary("kotlinx-serialization-json"))
            }
        }
    }
}
