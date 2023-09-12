kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":korge-foundation"))
                implementation("org.ow2.asm:asm:9.5")
                implementation("org.ow2.asm:asm-util:9.5")
            }
        }
    }
}
