@file:Suppress("SuspiciousCollectionReassignment")
@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

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
    `maven-publish`
    signing
    alias(versions.plugins.nexus.publish.plugin)
}

val logKubeVersion = project.properties["version"] as String
val logKubeGroup = project.properties["group"] as String

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.kotlin.link")
//        maven("https://oss.sonatype.org/content/repositories/snapshots")
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

val Project.artifact: String get() = "${extra["artifactPrefix"]}${project.name}"
val Project.alias: String get() = "${extra["aliasPrefix"]}${project.name}"

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

publishing {
    publications {
        create<MavenPublication>("versionCatalog") {
            artifactId = "logKube.versionCatalog"
            from(components["versionCatalog"])
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
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
                
                jvmToolchain(jvmTargetVersion.toInt())
                
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

//                js(IR) {
//                    browser()
//                    nodejs()
//                }

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
                    val commonTest by getting {
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
                    sourceSets {
                        all {
                            languageSettings {
                                progressiveMode = true
                                enableLanguageFeature("ContextReceivers")
                                enableLanguageFeature("ValueClasses")
                                enableLanguageFeature("ContractSyntaxV2")
                                enableLanguageFeature("ExplicitBackingFields")
                                optIn("kotlin.contracts.ExperimentalContracts")
                                optIn("kotlin.ExperimentalStdlibApi")
                                optIn("kotlin.ExperimentalSubclassOptIn")
                                optIn("kotlin.ExperimentalUnsignedTypes")
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
                moduleName = "${project.extra["artifactPrefix"]}${project.name}"
                // DOKKA-3885
                dokkaGeneratorIsolation = ClassLoaderIsolation()
            }

            task<Jar>("dokkaJar") {
                group = "dokka"
                description = "Assembles Kotlin docs with Dokka into a javadoc JAR"
                archiveClassifier = "javadoc"
                afterEvaluate {
                    val dokkaGeneratePublicationHtml by tasks.getting
                    dependsOn(dokkaGeneratePublicationHtml)
                    from(dokkaGeneratePublicationHtml)
                }
            }
        }
        "publication" {
            pluginManager.withPlugin("org.gradle.maven-publish") {
                afterEvaluate {
                    configure<PublishingExtension> {
                        publications.withType<MavenPublication> {
                            artifactId = "${extra["artifactPrefix"]}$artifactId"
                        }
                    }
                }
            }
        }
        "publishing" {
            apply(plugin = "org.gradle.maven-publish")
            apply(plugin = "org.gradle.signing")
            afterEvaluate {
                configure<PublishingExtension> {
                    publications.withType<MavenPublication> {
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
                tasks.withType<AbstractPublishToMaven>().configureEach {
                    val signingTasks = tasks.withType<Sign>()
                    mustRunAfter(signingTasks)
                }
            }
            configure<SigningExtension> {
                sign(the<PublishingExtension>().publications)
            }
        }
        case { hasAllOf("dokka", "publishing") } implies {
            afterEvaluate {
                configure<PublishingExtension> {
                    publications.withType<MavenPublication> {
                        artifact(tasks.named<Jar>("dokkaJar"))
                    }
                }
            }
        }
    }
}