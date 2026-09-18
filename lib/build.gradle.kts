plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish") // Correct Kotlin DSL syntax for applying plugins
}

android {
    namespace = "com.macwap.rdxrasel"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
//noinspection UseTomlInstead

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.core.ktx)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.browser:browser:1.8.0")

    // Region: 3rd party libraries
    implementation("io.coil-kt:coil-svg:2.7.0")
    implementation("io.coil-kt:coil:2.7.0")
    implementation("androidx.webkit:webkit:1.13.0")
    // End region
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Use properties if they are set (e.g. by JitPack), otherwise use defaults
                groupId = project.group.toString().takeIf { it.isNotEmpty() && it != "unspecified" } ?: "com.macwap.function"
                artifactId = "function"
                version = project.version.toString().takeIf { it.isNotEmpty() && it != "unspecified" } ?: "1.0.3"
                from(components["release"])
            }
        }
    }
}
