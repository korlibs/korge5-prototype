import org.apache.tools.ant.taskdefs.condition.Os

apply(plugin = "application")

object JvmAddOpens {
    val isWindows get() = Os.isFamily(Os.FAMILY_WINDOWS)
    val isMacos get() = Os.isFamily(Os.FAMILY_MAC)
    val isLinux get() = Os.isFamily(Os.FAMILY_UNIX) && !isMacos
    val isArm get() = listOf("arm", "arm64", "aarch64").any { Os.isArch(it) }
    val inCI: Boolean get() = !System.getenv("CI").isNullOrBlank() || !System.getProperty("CI").isNullOrBlank()

    val beforeJava9 = System.getProperty("java.version").startsWith("1.")

    fun createAddOpensTypedArray(): Array<String> = createAddOpens().toTypedArray()

    @OptIn(ExperimentalStdlibApi::class)
    fun createAddOpens(): List<String> = buildList<String> {
        add("--add-opens=java.desktop/sun.java2d.opengl=ALL-UNNAMED")
        add("--add-opens=java.desktop/java.awt=ALL-UNNAMED")
        add("--add-opens=java.desktop/sun.awt=ALL-UNNAMED")
        if (isMacos) {
            add("--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED")
            add("--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
            add("--add-opens=java.desktop/com.apple.eawt=ALL-UNNAMED")
            add("--add-opens=java.desktop/com.apple.eawt.event=ALL-UNNAMED")
        }
        if (isLinux) add("--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED")
    }
}


kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":korge"))
            }
        }
    }
}


tasks {
    val jsMainClasses by getting(Task::class)
    //val jsBrowserDevelopmentExecutableDistribution by getting(Task::class)
    //println(jsBrowserDevelopmentExecutableDistribution)
    //println(jsBrowserDevelopmentExecutableDistribution::class)
    //println(jsBrowserDevelopmentExecutableDistribution.outputs.files.toList())
    //val jsFile = File(jsMainClasses.outputs.files.first(), "${project.name}.js")

    val mainJvmCompilation = kotlin.targets.getByName("jvm").compilations["main"]

    val buildDistCopy by creating(Copy::class) {
        dependsOn("jsProcessResources")
        from(File(buildDir, "processedResources/js/main"))
        into(File(buildDir, "dist"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    val compileSyncWithResources by creating(Copy::class) {
        dependsOn("jsProcessResources")
        dependsOn("jsDevelopmentExecutableCompileSync")
        from(File(buildDir, "processedResources/js/main"))
        from(File(buildDir, "compileSync/js/main/developmentExecutable/kotlin"))
        into(File(buildDir, "jsDevelopment"))
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    //val jsDevelopmentExecutableCompileSync by getting(IncrementalSyncTask::class) {
    //    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    //}

    val runDeno by creating(Exec::class) {
        dependsOn(compileSyncWithResources)
        group = "run"
        executable = "deno"
        args("run", "--inspect", "-A", "--unstable", File(buildDir, "jsDevelopment/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath)
    }

    // For JS testing
    // ./gradlew -t buildDenoDebug
    val buildDenoDebug by creating(Exec::class) {
        dependsOn("jsDevelopmentExecutableCompileSync")
        dependsOn(buildDistCopy)
        group = "dist"
        executable = "esbuild"
        val releaseOutput = File(buildDir, "compileSync/js/main/developmentExecutable/kotlin/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath
        val outFile = File(buildDir, "dist/out.js").absolutePath
        args("--bundle", releaseOutput, "--log-level=error", "--outfile=$outFile", "--minify", "--banner:js=#!/usr/bin/env -S deno run -A --unstable")
        workingDir(rootProject.rootDir)
        doLast {
            File(outFile).setExecutable(true)
        }
    }

    val buildDenoRelease by creating(Exec::class) {
        dependsOn("jsProductionExecutableCompileSync")
        dependsOn(buildDistCopy)
        group = "dist"
        executable = "esbuild"
        val releaseOutput = File(buildDir, "compileSync/js/main/productionExecutable/kotlin/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath
        val outFile = File(buildDir, "dist/out.js").absolutePath
        args("--bundle", releaseOutput, "--log-level=warning", "--outfile=$outFile", "--minify", "--banner:js=#!/usr/bin/env -S deno run -A --unstable")
        workingDir(rootProject.rootDir)
        doLast {
            File(outFile).setExecutable(true)
        }
    }

    val jvmMainClasses by getting

    val runJvm by creating(JavaExec::class) {
        dependsOn("jvmMainClasses")
        mainClass.set("MainKt")
        group = "run"

        classpath(mainJvmCompilation.runtimeDependencyFiles)
        classpath(mainJvmCompilation.compileDependencyFiles)
        classpath(mainJvmCompilation.output.allOutputs)
        classpath(mainJvmCompilation.output.classesDirs)
        if (!JvmAddOpens.beforeJava9) jvmArgs(*JvmAddOpens.createAddOpensTypedArray())
        //jvmArgs("-XstartOnFirstThread")
    }

    val runJsHotreload by creating {
        group = "run"
        doFirst {
            exec {
                commandLine("../gradlew", "buildDenoDebug")
                //./gradlew buildDenoDebug
                //live-server korge-sandbox/build/dist & ./gradlew -t buildDenoDebug
            }
            Thread {
                exec {
                    commandLine("live-server", "build/dist")
                }
            }.start()
            exec {
                commandLine("../gradlew", "-t", "buildDenoDebug")
                //./gradlew buildDenoDebug
                //live-server korge-sandbox/build/dist & ./gradlew -t buildDenoDebug
            }
        }
    }
}