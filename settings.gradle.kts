rootProject.name = "LogKube"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.kotlin.link")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
//        mavenLocal()
    }
}

plugins {
    id("dev.lounres.gradle.stal") version "0.3.1"
}

stal {
    structure {
        defaultIncludeIf = { it.listFiles { file: File -> file.name != "build" || !file.isDirectory }?.isNotEmpty() ?: false }
        "libs" {
            subdirs("libs")
        }
    }

    tag {
        // Kotlin set up
        "kotlin multiplatform" since { hasAnyOf("libs") }
        "kotlin common settings" since { hasAnyOf("kotlin multiplatform", "kotlin jvm") }
        "kotlin library settings" since { has("libs") }
        // Extra
        "examples" since { has("libs") }
        "benchmark" since { has("libs") }
        "publishing" since { hasAnyOf("libs") }
//        "dokka" since { has("libs") }
        "versionCatalog" since { has("libs") }
    }

    action {
        gradle.allprojects {
            extra["artifactPrefix"] = ""
            extra["aliasPrefix"] = ""
        }
        "libs" {
            extra["artifactPrefix"] = "logKube."
            extra["aliasPrefix"] = ""
        }
    }
}