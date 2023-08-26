kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":korge-core"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
            }
        }
    }
}