import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

class AndroidJacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")

            extensions.configure<JacocoPluginExtension>("jacoco") { toolVersion = "0.8.12" }

            val androidComponents = extensions.findByType(AndroidComponentsExtension::class.java)
            androidComponents?.onVariants { variant ->
                val variantName = variant.name
                val capitalizedVariant = variantName.replaceFirstChar { it.uppercaseChar() }

                tasks.register<JacocoReport>("jacoco${capitalizedVariant}Report") {
                    dependsOn("test${capitalizedVariant}UnitTest")

                    group = "Verification"
                    description = "Generate JaCoCo coverage report for $variantName"

                    reports {
                        xml.required.set(true)
                        html.required.set(true)
                        csv.required.set(false)
                    }

                    val javaClasses =
                        fileTree(layout.buildDirectory.dir("intermediates/javac/$variantName")) {
                            exclude(jacocoExcludes)
                        }
                    val kotlinClasses =
                        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/$variantName")) {
                            exclude(jacocoExcludes)
                        }

                    classDirectories.setFrom(javaClasses, kotlinClasses)

                    sourceDirectories.setFrom(
                        files("$projectDir/src/main/java", "$projectDir/src/main/kotlin")
                    )

                    executionData.setFrom(
                        fileTree(layout.buildDirectory) {
                            include(
                                "jacoco/test${capitalizedVariant}UnitTest.exec",
                                "outputs/unit_test_code_coverage/${variantName}UnitTest/**/*.exec",
                            )
                        }
                    )
                }
            }
        }
    }

    companion object {
        /** Classes excluded from coverage reports (generated code). */
        val jacocoExcludes =
            listOf(
                // Hilt / Dagger
                "**/Hilt_*.*",
                "**/*_HiltModules*.*",
                "**/*_ComponentTreeDeps*.*",
                "**/*_GeneratedInjector*.*",
                "**/dagger/**",
                "**/hilt_aggregated_deps/**",
                "**/*_Factory.*",
                "**/*_MembersInjector.*",
                // BuildConfig
                "**/BuildConfig.*",
                // Data Binding
                "**/databinding/**",
                "**/BR.*",
                // Room
                "**/*_Impl.*",
                "**/*_Impl\$*.*",
                // Android generated
                "**/R.class",
                "**/R\$*.class",
                "**/Manifest*.*",
                // Kotlin
                "**/*\$Lambda\$*.*",
                "**/*Companion*.*",
                // Navigation / Serialization generated
                "**/*\$\$serializer.*",
                "**/*Directions*.*",
                "**/*Args*.*",
            )
    }
}
