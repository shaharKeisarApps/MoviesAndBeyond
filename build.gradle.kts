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

true // Needed to make the Suppress annotation work for the plugins block
