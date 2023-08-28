plugins {
    kotlin("multiplatform") version "1.9.10"
    //application apply false
    `maven-publish`
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    }

    group = "org.korge"
    version = "0.0.1-SNAPSHOT"
}

kotlin { jvm() }

subprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    //apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    //apply<KotlinMultiplatformPlugin>()
    //apply(ApplicationPlugin::class)

    java {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    kotlin {
        jvm {
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
                kotlinOptions.suppressWarnings = true
                //kotlinOptions.freeCompilerArgs = listOf("-Xuse-k2")
                kotlinOptions.freeCompilerArgs = listOf()
            }
            //withJava()
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
        js(IR) {
            binaries.executable()
            //useCommonJs()
            //nodejs()
            useEsModules()
            browser {
            }
            compilations.all {
                kotlinOptions.suppressWarnings = true
                //kotlinOptions.freeCompilerArgs = listOf()
            }
        }
        wasm {
            this.useEsModules()
            browser {
            }
            compilations.all {
                kotlinOptions.suppressWarnings = true
            //    kotlinOptions.freeCompilerArgs = listOf()
            }
        }
        sourceSets {
            val commonMain by getting {
                kotlin.setSrcDirs(listOf("src/common"))
                resources.setSrcDirs(listOf("src/resources"))
                dependencies {
                    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2-wasm0")
                    api("org.jetbrains.kotlinx:atomicfu:0.21.0-wasm0")

                    //if (this.project.path == ":korge") {
                    //    implementation(project(":korge-core"))
                    //}
                }
            }
            val commonTest by getting {
                kotlin.setSrcDirs(listOf("test/common"))
                resources.setSrcDirs(listOf("test/resources"))

                dependencies {
                    api(kotlin("test"))
                    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.2-wasm0")
                }
            }
            val jvmMain by getting {
                kotlin.setSrcDirs(listOf("src/jvm"))
                resources.setSrcDirs(listOf<String>())

                dependencies {
                    api("net.java.dev.jna:jna:5.13.0")
                    api("net.java.dev.jna:jna-platform:5.13.0")
                }
            }
            val jvmTest by getting {
                kotlin.setSrcDirs(listOf("test/jvm"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                    api(kotlin("test"))
                }
            }
            val jsMain by getting {
                kotlin.setSrcDirs(listOf("src/js"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                }
            }
            val jsTest by getting {
                kotlin.setSrcDirs(listOf("test/js"))
                resources.setSrcDirs(listOf<String>())
            }
            val wasmMain by getting {
                kotlin.setSrcDirs(listOf("src/wasm"))
                resources.setSrcDirs(listOf<String>())
                dependencies {
                }
            }
            val wasmTest by getting {
                kotlin.setSrcDirs(listOf("test/wasm"))
                resources.setSrcDirs(listOf<String>())
            }
        }
    }

    //if (path == ":korge") {
    //    //println("config=${configurations.toList()}")
    //    dependencies {
    //        add("commonMainImplementation", project(":korge-core"))
    //        //add("commonTestImplementation", project(path = ":korge-core", configuration = "commonTestImplementation"))
    //        //testImplementation(project(path = ":another-project", configuration = "testArtifacts"))
    //    }
    //}
}
