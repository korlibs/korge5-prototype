kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2-wasm0")
                api("org.jetbrains.kotlinx:atomicfu:0.21.0-wasm0")

                //if (this.project.path == ":korge") {
                //    implementation(project(":korge-core"))
                //}
            }
        }
    }
}
