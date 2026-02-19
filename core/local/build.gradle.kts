import com.google.protobuf.gradle.GenerateProtoTask

plugins {
    id("moviesandbeyond.android.library")
    id("moviesandbeyond.android.hilt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.keisardev.moviesandbeyond.core.local"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        consumerProguardFiles("consumer-proguard-rules.pro")
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { option("lite") }
                create("kotlin") { option("lite") }
            }
        }
    }
}

androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val protoTask =
                project.tasks.getByName(
                    "generate" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Proto"
                ) as GenerateProtoTask

            project.tasks.getByName(
                "ksp" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Kotlin"
            ) {
                dependsOn(protoTask)
                // Remove the casting and use a different approach to set source
                inputs.files(protoTask.outputBaseDir)
            }
        }
    }
}

dependencies {
    implementation(projects.core.model)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.security.crypto)
    implementation(libs.protobuf.kotlin.lite)
    ksp(libs.androidx.room.compiler)

    androidTestImplementation(projects.core.testing)
}
