import org.jetbrains.kotlin.gradle.plugin.extraProperties
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin

plugins {
    kotlin("multiplatform") version "1.9.10"
    application
}

allprojects {
    repositories {
        mavenCentral()
    }
}

kotlin { jvm() }

subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    //apply(plugin = "kotlin")
    apply(plugin = "application")
    //apply<KotlinMultiplatformPlugin>()
    //apply(ApplicationPlugin::class)

    java {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    this.kotlin {
        jvm {
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            //withJava()
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
        js(IR) {
            binaries.executable()
            //useCommonJs()
            //useCommonJs()
            useEsModules()
            browser {
            }
            //nodejs()
        }
        wasm() {
            this.useEsModules()
            browser {
            }
        }
        sourceSets {
            val commonMain by getting {
                kotlin.setSrcDirs(listOf("src"))
                resources.setSrcDirs(listOf("resources"))
            }
            val commonTest by getting {
                kotlin.setSrcDirs(listOf("test"))
                resources.setSrcDirs(listOf("testresources"))

                dependencies {
                    implementation(kotlin("test"))
                }
            }
            val jvmMain by getting {
                kotlin.setSrcDirs(listOf("srcJvm"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                }
            }
            val jvmTest by getting {
                kotlin.setSrcDirs(listOf("testJvm"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            val jsMain by getting {
                kotlin.setSrcDirs(listOf("srcJs"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                }
            }
            val jsTest by getting {
                kotlin.setSrcDirs(listOf("testJs"))
                resources.setSrcDirs(listOf<String>())
            }
            val wasmMain by getting {
                kotlin.setSrcDirs(listOf("srcWasm"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                }
            }
            val wasmTest by getting {
                kotlin.setSrcDirs(listOf("testWasm"))
                resources.setSrcDirs(listOf<String>())
            }
        }
    }

    tasks {
        val jsBrowserDevelopmentExecutableDistribution by getting(Task::class)
        //println(jsBrowserDevelopmentExecutableDistribution)
        //println(jsBrowserDevelopmentExecutableDistribution::class)
        //println(jsBrowserDevelopmentExecutableDistribution.outputs.files.toList())
        val jsFile = File(jsBrowserDevelopmentExecutableDistribution.outputs.files.first(), "lol.js")
        val runDeno by creating(Exec::class) {
            dependsOn("jsBrowserDevelopmentExecutableDistribution")
            group = "deno"
            executable = "deno"
            //this.args = listOf("run", "-A", "--unstable", file("build/compileSync/js/main/developmentExecutable/kotlin/lol.mjs").absolutePath)
            this.args = listOf("run", "-A", "--unstable", jsFile.absolutePath)
        }
    }
}
