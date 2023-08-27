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

    // esbuild --bundle korge-sandbox/build/compileSync/js/main/developmentExecutable/kotlin/korge5-korge-sandbox.mjs --outfile=out.js

    val runDeno by creating(Exec::class) {
        dependsOn("jsBrowserDevelopmentExecutableDistribution")
        //dependsOn("jsBrowserDistribution")
        //dependsOn("jsMainClasses")
        group = "deno"
        executable = "deno"
        //this.args = listOf("run", "-A", "--unstable", file("build/compileSync/js/main/developmentExecutable/kotlin/lol.mjs").absolutePath)
        //this.args = listOf("run", "-A", "--unstable", File(buildDir, "dist/js/developmentExecutable/${project.name}.js").absolutePath)
        //this.args = listOf("run", "-A", "--unstable", File(buildDir, "dist/js/developmentExecutable/${project.name}.js").absolutePath)
        //this.args = listOf("run", "-A", "--unstable", File(buildDir, "dist/js/productionExecutable/${project.name}.js").absolutePath)
        this.args = listOf("run", "--inspect", "-A", "--unstable", File(buildDir, "compileSync/js/main/developmentExecutable/kotlin/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath)
        //this.args = listOf("run", "-A", "--unstable", File(buildDir, "compileSync/js/main/productionExecutable/kotlin/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath)
    }

    val jvmMainClasses by getting

    val runJvm by creating(JavaExec::class) {
        dependsOn("jvmMainClasses")
        mainClass.set("MainKt")
        //classpath = java.sourceSets["main"].runtimeClasspath
        //classpath = kotlin.sourceSets["main"].kotlin.classesDirectory

        val mainJvmCompilation = kotlin.targets.getByName("jvm").compilations["main"]
        classpath(mainJvmCompilation.runtimeDependencyFiles)
        classpath(mainJvmCompilation.compileDependencyFiles)
        //if (project.korge.searchResourceProcessorsInMainSourceSet) {
        classpath(mainJvmCompilation.output.allOutputs)
        classpath(mainJvmCompilation.output.classesDirs)
        //}
        //project.kotlin.jvm()
        //classpath(project.files().from(project.getCompilationKorgeProcessedResourcesFolder(mainJvmCompilation)))

        //classpath(kotlin.sourceSets["main"].output.classesDirs, configurations.named("jvmRuntimeClasspath").get())
        //println(jvmMainClasses.outputs.files.toList())
        //println(kotlin.sourceSets["main"].output.classesDirs.toList())
        //println(configurations.named("jvmRuntimeClasspath").get().toList())
        if (!JvmAddOpens.beforeJava9) jvmArgs(*JvmAddOpens.createAddOpensTypedArray())
        //jvmArgs("-XstartOnFirstThread")
    }
    /*
    afterEvaluate {
    }
    afterEvaluate {
        val jvmRun by creating(JavaExec::class) {
            if (!JvmAddOpens.beforeJava9) jvmArgs(*JvmAddOpens.createAddOpensTypedArray())
        }
    }

     */
}