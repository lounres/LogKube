plugins {
    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(versions.kotlinx.datetime)
                implementation(versions.kotlinx.serialization.core)
            }
        }
    }
}