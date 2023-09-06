import org.apache.tools.ant.taskdefs.condition.Os
import java.awt.*
import java.awt.image.BufferedImage
import java.io.*
import java.util.Base64

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
                api(project(":korge"))
            }
        }
    }
}

fun findExecutableOnPath(name: String): File? {
    val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)

    for (dirname in System.getenv("PATH").split(File.pathSeparator)) {
        val potentialNames = if (isWindows) listOf("$name.exe", "$name.cmd", "$name.bat") else listOf(name)
        for (name in potentialNames) {
            val file = File(dirname, name)
            if (file.isFile && file.canExecute()) {
                return file.absoluteFile
            }
        }
    }
    return null
}

enum class GameCategory(val apple: String) {
    ACTION("action-games"),
    ADVENTURE("adventure-games"),
    ARCADE("arcade-games"),
    BOARD("board-games"),
    CARD("card-games"),
    CASINO("casino-games"),
    DICE("dice-games"),
    EDUCATIONAL("educational-games"),
    FAMILY("family-games"),
    KIDS("kids-games"),
    MUSIC("music-games"),
    PUZZLE("puzzle-games"),
    RACING("racing-games"),
    ROLE_PLAYING("role-playing-games"),
    SIMULATION("simulation-games"),
    SPORTS("sports-games"),
    STRATEGY("strategy-games"),
    TRIVIA("trivia-games"),
    WORD("word-games"),
    ;
}

fun createInfoPlist(exeBaseName: String, name: String = exeBaseName, id: String = exeBaseName, description: String = exeBaseName, copyright: String = "Copyright", version: String = "0.0.1", gameCategory: GameCategory = GameCategory.ACTION): String = """
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0"><dict>
        <key>CFBundleDisplayName</key><string>${name}</string>
        <key>CFBundleExecutable</key><string>${exeBaseName}</string>
        <key>CFBundleIconFile</key><string>${exeBaseName}.icns</string>
        <key>CFBundleIdentifier</key><string>${id}</string>
        <key>CFBundleName</key><string>${name}</string>
        <key>CFBundleGetInfoString</key><string>${description}</string>
        <key>NSHumanReadableCopyright</key><string>${copyright}</string>
        <key>CFBundleVersion</key><string>${version}</string>
        <key>CFBundleShortVersionString</key><string>${version}</string>
        <key>LSApplicationCategoryType</key><string>${gameCategory.apple}</string>
        <key>CFBundleInfoDictionaryVersion</key><string>6.0</string>
        <key>CFBundlePackageType</key><string>APPL</string>
        <key>CFBundleSignature</key><string>????</string>
        <key>LSMinimumSystemVersion</key><string>10.9.0</string>
        <key>NSHighResolutionCapable</key><true/>
    </dict></plist>
""".trimIndent()

fun createIcns(image: ByteArray): ByteArray {
    fun java.awt.Image.toBufferedImage(): java.awt.image.BufferedImage {
        if (this is java.awt.image.BufferedImage && this.type == BufferedImage.TYPE_INT_ARGB) return this
        val bimage = BufferedImage(
            this.getWidth(null),
            this.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        )
        val bGr = bimage.createGraphics()
        bGr.drawImage(this, 0, 0, null)
        bGr.dispose()
        return bimage
    }

    fun java.awt.image.BufferedImage.encodePNG(): ByteArray =
        ByteArrayOutputStream().also { javax.imageio.ImageIO.write(this, "png", it) }.toByteArray()

    val bimage = javax.imageio.ImageIO.read(image.inputStream())
    val scaledImage = bimage.getScaledInstance(512, 512, Image.SCALE_SMOOTH).toBufferedImage()
    val ic09Bytes = scaledImage.encodePNG()
    return ByteArrayOutputStream().also { baos ->
        DataOutputStream(baos).apply {
            writeBytes("icns")
            writeInt(8 + 8 + ic09Bytes.size)
            writeBytes("ic09")
            writeInt(ic09Bytes.size)
            write(ic09Bytes)
            flush()
        }
    }.toByteArray()
}

fun createMacExecutable(appPath: File, exeFile: File) {
    File(appPath, "Contents/MacOS").mkdirs()
    File(appPath, "Contents/Resources").mkdirs()
    File(appPath, "Contents/Resources/program.icns").writeBytes(createIcns(JITTO_PNG_BASE64))
    File(appPath, "Contents/Info.plist").writeText(createInfoPlist(exeFile.name))
    File(appPath, "Contents/MacOS/${exeFile.name}").also {
        it.writeBytes(exeFile.readBytes())
        it.setExecutable(true)
    }
}

afterEvaluate {
    afterEvaluate {
        tasks {
            val jvmRun by getting(JavaExec::class) {
                if (!JvmAddOpens.beforeJava9) jvmArgs(*JvmAddOpens.createAddOpensTypedArray())
            }
        }
    }
}

val JITTO_PNG_BASE64 = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAgAAAAIACAMAAADDpiTIAAADAFBMVEVHcEz////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////9/f3////////+/f7////////////////////////////////////////////////////////18frazOy/pt2kgc6MX8F6RbdxO7NnLK1dH6hTEaNNCJ9HAJxIAp1RDqJaGqZiJapsNLBzPbSDU72Zcsi0l9fQvebq4fT////Lt+Oeect3QrZXFqVpL6+4nNnl2/L//v/28/vIs+KGV76zldb49fzCq9+ohtHn3vLNuuVQDKGwktXu6ParitJKBJ7dz+7t5vWcdsr///+RZsTz7vmWbcfTwuiHWb/Yyer69/2UasZkKKzXx+nx6/mAT7uhfc28odv///+Yb8dfIanj1/Dw8PCnp6cAAAD8+/06OjoAAADS0tIAAAAAAAB9S7lwcHAAAAAAAAAAAABOCqCsjNLZyury8vIYGBgAAADFr+CgoKAAAAAAAACLXsA0NDQAAAAAAADMzMwBAQEAAADf0+5UVFQAAAAAAAC1mdfZ2dkFBQUAAACOYsJZWVkAAABFRUUAAAC9vb0AAAAAAACqqqoAAAAAAAD5+fkAAACtjdN1dXUAAAAAAAAAAAAqKioAAACEhIT///////////8AAACIiIgAAAAAAAAgICAAAABfX18AAAAAAACOjo4AAADBwcHFruA9PT1kZGQAAADw6vfCo+qSWNh8ONBoGclZAsRYAMN3L86OUte+nOjt4/mKXMCug+Kpe+Dh4eEAAAChbt1dCMWYYdoAAAALCwvbx/JjEcfSuu+1juUjIyOkc96DQtPIquxvJMurfuF7SLiwkdTo6Oi5ubmysrIAAABpaWmYmJheE7pAQEBMAKdWAL5PALCvr6/v7+8RERG0tLSTk5N2dnZbW1vp6el7e3vb29t9fX3ExMRa4vB8AAAA/nRSTlMAEThcfJiwvsvZ5fL79enf0MW3oYtqSCMDDD5v7v+GVCAB42Yzgf+fKverB9NNdxZjk3UwG///////////////////////////////Q/////////////////////////////////87///////////////////a////08YF/9Yg//FX////nQr//////9T//+k////5VP//av//hwH///8C//+j/4//dWD//U7/Mf//4hXA/3z/VqXXt/9lGv+u/0fc/yn/////D////////////////////zj///+T//////////////////+3/8z///////////////////+A1l8179cAACfISURBVHgB7MGFtaRQEAXAHsfdoZFx9/xj2wT2O/Y4t4oAAAAAAAAAAAAAAAAA+m40nkxn84UkK6qmabphWrbjen4QRjRsECepK2X8EU2e50VZEQzRKPBsjb9h6a7CimBIqvVG2vIPGLsiJhiIfW7yzx2OxYlAeOX0zL+lukFFILDq4vDfmNcbgaBOd5n/7vAICQQUPxWux/b4IhBMPFW5Rs6aQCBRanDNjiWBKJIz1++wORGI4H3kZphFRdB30VPnxjglQb/tJW6S7lcE/VX5B26YcyPoq9E/9u47MIrr3Pv4iJ4gum2ErR+wLgtCrtcsAtSeVbdkoRUaSVaXVpKRBQuS0EW61/smhIwtN66DC1eJlVjpvRec3lwwZem5EQnujtN7/PbmwjUzO8+UndnGzufvxO172DnnzMyZNTDEsy5n/YaNuXn5BYXkLSouKS0rr7ilssoNI+bfKjgS07QFBtpXb6rxkbrC2g2b60ToWp4hOBKP64p0aKtvqCj1ko7ixtvqoGORcxlIPNlLoKmpubyQDMpraYWmK68WHIklbRE0uNs2tJMpuR2d0JB1neBIJGuvBc/TlU/m+Ru7wUu/UUgcjtVXgtXTW0QR2tgmgjXDJSQIx+2Z4NRt8ZMFpc1ucG7KEBKC47IsMPru8JNFtf3gLHFGQGL0T4c692Yf2WBrjzMCEtm0LKjbVkr2aO8KQN1Ml+CIs9WZUFW/neyzYwDqrhEc8bX2SqjqLiE7eVvcUHWp4IintGuhRuzwk81qeqBqhRA/juzZUDM4RPbzVUNN+u1CvDhcM6FmXT5FxU43VMxfKcSJ41KoaSikKGkMQMXCbCEuHDenQ8Wwl6KmxgMVM4V4cCz7Z4QTWyiaajsTZiLoWIJw7l0UXSN1CDd3peCIuelq/TdQtBWPIty/ZAiOGFs6D+EqKPryexDuX+8UHDHlmo1wXRQLeX0I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")

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

    val denoPath = findExecutableOnPath("deno")?.absolutePath ?: "deno"

    val runDeno by creating(Exec::class) {
        dependsOn(compileSyncWithResources)
        group = "run"
        commandLine(denoPath, "run", "--inspect", "-A", "--unstable", File(buildDir, "jsDevelopment/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath)
    }

    // For JS testing
    // ./gradlew -t buildDenoDebug
    val buildDenoDebugModules by creating(Copy::class) {
        dependsOn("jsDevelopmentExecutableCompileSync")
        dependsOn(buildDistCopy)
        from(File(buildDir, "compileSync/js/main/developmentExecutable/kotlin"))
        into(File(buildDir, "dist"))
        doLast {
            File(buildDir, "dist/program.mjs").also {
                it.writeText("#!/usr/bin/env -S deno run -A --unstable\nimport('./korge5-korge-sandbox.mjs')")
                it.setExecutable(true)
            }
        }
    }

    val buildDenoDebug by creating(Exec::class) {
        dependsOn("jsDevelopmentExecutableCompileSync")
        dependsOn(buildDistCopy)
        group = "dist"
        executable = "esbuild"
        val releaseOutput = File(buildDir, "compileSync/js/main/developmentExecutable/kotlin/korge5-${project.name.trim(':').replace(':', '-')}.mjs").absolutePath
        val outFile = File(buildDir, "dist/program.mjs").absolutePath
        args("--bundle", releaseOutput, "--format=esm", "--log-level=error", "--outfile=$outFile", "--banner:js=#!/usr/bin/env -S deno run -A --unstable")
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
        val outFile = File(buildDir, "dist/program.mjs").absolutePath
        args("--bundle", releaseOutput, "--format=esm", "--log-level=warning", "--outfile=$outFile", "--minify", "--banner:js=#!/usr/bin/env -S deno run -A --unstable")
        workingDir(rootProject.rootDir)
        doLast {
            File(outFile).setExecutable(true)
        }
    }

    val buildDenoMacApp by creating(Task::class) {
        dependsOn(buildDenoRelease)
        doLast {
            exec {
                commandLine(denoPath, "compile", "-A", "--unstable", File(buildDir, "dist/program.mjs").absolutePath)
                workingDir(File(buildDir, "dist"))
            }
            createMacExecutable(File(buildDir, "dist/program.app"), File(buildDir, "dist/program"))
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
            //val buildTask = "buildDenoDebug"
            val buildTask = "buildDenoDebugModules"
            exec {
                commandLine("../gradlew", buildTask)
                //./gradlew buildDenoDebug
                //live-server korge-sandbox/build/dist & ./gradlew -t buildDenoDebug
            }
            Thread {
                exec {
                    commandLine("live-server", "build/dist")
                }
            }.start()
            exec {
                commandLine("../gradlew", "-t", buildTask)
                //./gradlew buildDenoDebug
                //live-server korge-sandbox/build/dist & ./gradlew -t buildDenoDebug
            }
        }
    }
}
