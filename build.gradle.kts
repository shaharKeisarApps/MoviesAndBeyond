// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.androidTest) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    jacoco
}

// Configure Spotless for code formatting
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")

        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()

        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")

        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()

        trimTrailingWhitespace()
        endWithNewline()
    }
}

// Configure Detekt for static analysis
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    baseline = file("$projectDir/config/detekt/baseline.xml")

    source.setFrom(
        "app/src/main/java",
        "core/local/src/main/java",
        "core/model/src/main/java",
        "core/network/src/main/java",
        "core/testing/src/main/java",
        "core/ui/src/main/java",
        "data/src/main/java",
        "feature/auth/src/main/java",
        "feature/details/src/main/java",
        "feature/movies/src/main/java",
        "feature/search/src/main/java",
        "feature/tv/src/main/java",
        "feature/you/src/main/java",
        "sync/src/main/java",
    )
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// =============================================================================
// JaCoCo Merged Coverage Report
// =============================================================================

val jacocoExcludes =
    listOf(
        "**/Hilt_*.*",
        "**/*_HiltModules*.*",
        "**/*_ComponentTreeDeps*.*",
        "**/*_GeneratedInjector*.*",
        "**/dagger/**",
        "**/hilt_aggregated_deps/**",
        "**/*_Factory.*",
        "**/*_MembersInjector.*",
        "**/BuildConfig.*",
        "**/databinding/**",
        "**/BR.*",
        "**/*_Impl.*",
        "**/*_Impl\$*.*",
        "**/R.class",
        "**/R\$*.class",
        "**/Manifest*.*",
        "**/*\$Lambda\$*.*",
        "**/*Companion*.*",
        "**/*\$\$serializer.*",
        "**/*Directions*.*",
        "**/*Args*.*",
    )

// Modules that apply the jacoco plugin (via the library convention plugin)
val jacocoModules =
    listOf(
        ":core:local",
        ":core:model",
        ":core:network",
        ":core:testing",
        ":core:ui",
        ":data",
        ":feature:auth",
        ":feature:details",
        ":feature:movies",
        ":feature:search",
        ":feature:tv",
        ":feature:you",
        ":sync",
    )

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "Verification"
    description = "Merge JaCoCo coverage from all modules into a single report"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val classDirs = files()
    val srcDirs = files()
    val execDirs = files()

    jacocoModules.forEach { modulePath ->
        val moduleProject = project(modulePath)
        classDirs.from(
            fileTree(moduleProject.layout.buildDirectory.dir("intermediates/javac/debug")) {
                exclude(jacocoExcludes)
            },
            fileTree(moduleProject.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(jacocoExcludes)
            },
        )
        srcDirs.from(
            files(
                "${moduleProject.projectDir}/src/main/java",
                "${moduleProject.projectDir}/src/main/kotlin",
            )
        )
        execDirs.from(
            fileTree(moduleProject.layout.buildDirectory) {
                include(
                    "jacoco/testDebugUnitTest.exec",
                    "outputs/unit_test_code_coverage/debugUnitTest/**/*.exec",
                )
            }
        )
    }

    classDirectories.setFrom(classDirs)
    sourceDirectories.setFrom(srcDirs)
    executionData.setFrom(execDirs)

    dependsOn(jacocoModules.map { "$it:testDebugUnitTest" })
}

// =============================================================================
// JaCoCo Coverage Verification
// =============================================================================

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "Verification"
    description = "Verify minimum code coverage thresholds per module tier"

    dependsOn("jacocoTestReport")

    // Coverage thresholds by module tier
    val dataModuleThreshold = 0.80
    val featureModuleThreshold = 0.70
    val coreModuleThreshold = 0.60

    jacocoModules.forEach { modulePath ->
        val moduleProject = project(modulePath)
        val threshold =
            when {
                modulePath.startsWith(":data") -> dataModuleThreshold
                modulePath.startsWith(":feature:") -> featureModuleThreshold
                modulePath.startsWith(":core:") -> coreModuleThreshold
                else -> coreModuleThreshold
            }

        classDirectories.from(
            fileTree(moduleProject.layout.buildDirectory.dir("intermediates/javac/debug")) {
                exclude(jacocoExcludes)
            },
            fileTree(moduleProject.layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
                exclude(jacocoExcludes)
            },
        )
        executionData.from(
            fileTree(moduleProject.layout.buildDirectory) {
                include(
                    "jacoco/testDebugUnitTest.exec",
                    "outputs/unit_test_code_coverage/debugUnitTest/**/*.exec",
                )
            }
        )

        violationRules { rule { limit { minimum = threshold.toBigDecimal() } } }
    }
}

true // Needed to make the Suppress annotation work for the plugins block
