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

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.11.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.browser:browser:1.8.0")

    // Region: 3rd party libraries
    implementation("io.coil-kt:coil-svg:1.3.0")
    implementation("io.coil-kt:coil:1.3.0")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("io.supercharge:shimmerlayout:2.1.0")
    // End region
}
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                groupId = "com.macwap.function" // Group ID
                artifactId = "function" // Artifact ID
                version = "1.0.3" // Version
                from(components["release"]) // Use the release component for publishing
            }
        }
    }
}
