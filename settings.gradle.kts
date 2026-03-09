pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "MoviesAndBeyond"

include(":app")

include(":core:local")

include(":core:model")

include(":core:network")

include(":core:testing")

include(":core:ui")

include(":data")

include(":feature:auth:api")

include(":feature:auth:impl")

include(":feature:details:api")

include(":feature:details:impl")

include(":feature:movies:api")

include(":feature:movies:impl")

include(":feature:search:api")

include(":feature:search:impl")

include(":feature:tv:api")

include(":feature:tv:impl")

include(":feature:you:api")

include(":feature:you:impl")

include(":sync")

include(":benchmarks")
