@file:Suppress("SuspiciousCollectionReassignment")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.accessors.dm.LibrariesForVersions
import org.gradle.accessors.dm.RootProjectAccessor
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Warning
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn


plugins {
    with(versions.plugins) {
        alias(kotlin.multiplatform) apply false
        alias(kotlin.allopen) apply false
        alias(kotlinx.benchmark) apply false
        alias(dokka)
    }
    `version-catalog`
//    `maven-publish`
//    signing
    id("com.vanniktech.maven.publish") version "0.31.0"
}

val logKubeVersion = project.properties["version"] as String
val logKubeGroup = project.properties["group"] as String

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.kotlin.link")
//        maven("https://oss.sonatype.org/content/repositories/snapshots")
        mavenLocal()
    }
}

val jvmTargetVersion : String by properties

val Project.versions: LibrariesForVersions get() = rootProject.extensions.getByName<LibrariesForVersions>("versions")
val Project.projects: RootProjectAccessor get() = rootProject.extensions.getByName<RootProjectAccessor>("projects")
fun PluginAware.apply(pluginDependency: PluginDependency) = apply(plugin = pluginDependency.pluginId)
fun PluginAware.apply(pluginDependency: Provider<PluginDependency>) = apply(plugin = pluginDependency.get().pluginId)
fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)
fun PluginManager.withPlugins(vararg pluginDeps: PluginDependency, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
fun PluginManager.withPlugins(vararg pluginDeps: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach { it.action() }

val Project.artifact: String get() = extra["artifactId"] as String
val Project.alias: String get() = extra["alias"] as String

catalog.versionCatalog {
    version("logKube", logKubeVersion)
}

gradle.projectsEvaluated {
    val bundleProjects = stal.lookUp.projectsThat { has("versionCatalog") }
    val bundleAliases = bundleProjects.map { it.alias }
    catalog.versionCatalog {
        for (p in bundleProjects)
            library(p.alias, logKubeGroup, p.artifact).versionRef("logKube")

        bundle("all", bundleAliases)
    }
}

stal {
    action {
        "kotlin jvm" {
            apply(versions.plugins.kotlin.jvm)
            configure<KotlinJvmProjectExtension> {
                jvmToolchain(jvmTargetVersion.toInt())
                
                compilerOptions {
                    freeCompilerArgs = freeCompilerArgs.get() + listOf(
                        "-Xexpect-actual-classes",
                        "-Xconsistent-data-class-copy-visibility",
                    )
                }

                @Suppress("UNUSED_VARIABLE")
                sourceSets {
                    val test by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
        }
        "kotlin multiplatform" {
            apply(versions.plugins.kotlin.multiplatform)
            configure<KotlinMultiplatformExtension> {
                applyDefaultHierarchyTemplate()
                
                compilerOptions {
                    freeCompilerArgs = freeCompilerArgs.get() + listOf(
                        "-Xexpect-actual-classes",
                        "-Xconsistent-data-class-copy-visibility",
                    )
                }
                
                jvm {
                    testRuns.all {
                        executionTask {
                            useJUnitPlatform()
                        }
                    }
                }
                
                js {
                    browser()
                    nodejs()
                }

                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    browser()
                    nodejs()
                    d8()
                }

//                linuxX64()
//                mingwX64()
//                macosX64()

//                androidTarget()
//                iosX64()
//                iosArm64()
//                iosSimulatorArm64()
//                macosArm64()

                @Suppress("UNUSED_VARIABLE")
                sourceSets {
                    commonTest {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
            afterEvaluate {
                yarn.lockFileDirectory = rootDir.resolve("gradle")
            }
        }
        "kotlin common settings" {
            pluginManager.withPlugins(versions.plugins.kotlin.jvm, versions.plugins.kotlin.multiplatform) {
                configure<KotlinProjectExtension> {
                    jvmToolchain {
                        languageVersion = JavaLanguageVersion.of(project.extra["jvmTargetVersion"] as String)
                        vendor = JvmVendorSpec.matching(project.extra["jvmVendor"] as String)
                    }
                    
                    sourceSets {
                        all {
                            languageSettings {
                                progressiveMode = true
                                enableLanguageFeature("ContextParameters")
                                enableLanguageFeature("ValueClasses")
                                enableLanguageFeature("ContractSyntaxV2")
                                enableLanguageFeature("ExplicitBackingFields")
                                optIn("kotlin.time.ExperimentalTime")
                                optIn("kotlin.contracts.ExperimentalContracts")
                                optIn("kotlin.ExperimentalStdlibApi")
                                optIn("kotlin.ExperimentalSubclassOptIn")
                                optIn("kotlin.ExperimentalUnsignedTypes")
                                optIn("kotlin.uuid.ExperimentalUuidApi")
                            }
                        }
                    }
                }
            }
            pluginManager.withPlugin("org.gradle.java") {
                tasks.withType<Test> {
                    useJUnitPlatform()
                }
            }
        }
        "kotlin library settings" {
            configure<KotlinProjectExtension> {
                explicitApi = Warning
            }
        }
        "dokka" {
            val thisProject = this
            val docsProject = project(":docs")
            
            apply(versions.plugins.dokka)
            dependencies {
                dokkaPlugin(versions.dokka.mathjax)
            }
            
            docsProject.afterEvaluate {
                dependencies {
                    dokka(thisProject)
                }
            }
            
            configure<DokkaExtension> {
                moduleName = project.artifact
                // DOKKA-3885
                dokkaGeneratorIsolation = ClassLoaderIsolation()
            }
        }
        "kotlin multiplatform publication" {
            pluginManager.withPlugin("com.vanniktech.maven.publish") {
                configure<MavenPublishBaseExtension> {
                    configure(
                        KotlinMultiplatform(
                            javadocJar =
                                if (extra["isDokkaConfigured"] == true) JavadocJar.Dokka("dokkaGeneratePublicationHtml")
                                else JavadocJar.Empty(),
                            sourcesJar = true,
                        )
                    )
                }
            }
        }
        "publishing" {
            apply(plugin = "com.vanniktech.maven.publish")
            configure<MavenPublishBaseExtension> {
                publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
                
                signAllPublications()
                
                coordinates(groupId = project.group as String, artifactId = project.artifact, version = project.version as String)
                
                pom {
                    name = "LogKube"
                    description = "Simple universal logging library"
                    url = "https://github.com/lounres/LogKube"

                    licenses {
                        license {
                            name = "Apache License, Version 2.0"
                            url = "https://opensource.org/license/apache-2-0/"
                        }
                    }
                    developers {
                        developer {
                            id = "lounres"
                            name = "Gleb Minaev"
                            email = "minaevgleb@yandex.ru"
                        }
                    }
                    scm {
                        url = "https://github.com/lounres/LogKube"
                    }
                }
            }
        }
    }
}