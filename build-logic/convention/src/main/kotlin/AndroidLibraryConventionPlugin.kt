import com.android.build.api.dsl.LibraryExtension
import com.keisardev.moviesandbeyond.ProjectConfig
import com.keisardev.moviesandbeyond.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("moviesandbeyond.android.jacoco")

            val extension = extensions.getByType<LibraryExtension>()
            configureKotlinAndroid(extension)
            extension.apply {
                defaultConfig { minSdk = ProjectConfig.MIN_SDK }
                compileOptions {
                    sourceCompatibility = ProjectConfig.JAVA_VERSION
                    targetCompatibility = ProjectConfig.JAVA_VERSION
                }
            }
        }
    }
}
