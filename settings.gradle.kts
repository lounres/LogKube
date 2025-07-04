rootProject.name = "LogKube"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val projectProperties = java.util.Properties()
file("gradle.properties").inputStream().use {
    projectProperties.load(it)
}

val versions: String by projectProperties

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.kotlin.link")
        mavenLocal()
    }
    
    versionCatalogs {
        create("versions").from("dev.lounres:versions:$versions")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.lounres.gradle.stal") version "0.4.0"
}

stal {
    structure {
        taggedWith("publishing", "version catalog")
        defaultIncludeIf = { it.listFiles { file: File -> file.name != "build" || !file.isDirectory }?.isNotEmpty() ?: false }
        "libs" {
            subdirs("libs")
        }
        "docs"()
    }

    tag {
        // Kotlin set up
        "kotlin multiplatform" since { hasAnyOf("libs") }
        "kotlin common settings" since { hasAnyOf("kotlin multiplatform", "kotlin jvm") }
        "kotlin library settings" since { has("libs") }
        // Extra
        "examples" since { has("libs") }
        "benchmark" since { has("libs") }
        "kotlin multiplatform publication" since { hasAnyOf("libs") }
        "publishing" since { has("libs") }
        "dokka" since { has("libs") }
        "versionCatalog" since { has("libs") }
    }

    action {
        gradle.allprojects {
            extra["artifactId"] = ""
            extra["alias"] = ""
            extra["isDokkaConfigured"] = false
            extra["jvmTargetVersion"] = settings.extra["jvmTargetVersion"]
            extra["jvmVendor"] = settings.extra["jvmVendor"]
        }
        "libs" {
            extra["artifactId"] = "logKube.${project.name}"
            extra["alias"] = project.name
        }
        "version catalog" {
            extra["artifactId"] = "logKube.versionCatalog"
        }
        "dokka" {
            extra["isDokkaConfigured"] = true
        }
    }
}