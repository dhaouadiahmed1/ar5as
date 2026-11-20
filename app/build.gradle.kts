plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "app.ar5as"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.ar5as"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "0.3.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Product photos are stored in the repo as base64 TEXT (app/images-b64/*.b64)
// because the GitHub App snapshot push used by Muse only accepts UTF-8 text
// files — binary JPGs cannot be pushed that way. This task decodes them back
// to JPGs in src/main/res/drawable/ before resource merging, so the photos
// are present in every build (CI and local). python3 is available on the
// GitHub ubuntu runners used by .github/workflows/android.yml.
tasks.register<Exec>("decodeImages") {
    description = "Decode base64 product photos into src/main/res/drawable/"
    group = "build"
    workingDir = rootDir
    // If python3 is missing this fails with Gradle's own clear error
    // ("Cannot run program \"python3\"") — python3 is preinstalled on the
    // GitHub ubuntu runners used by .github/workflows/android.yml.
    commandLine(
        "python3", "scripts/decode_images.py"
    )
}

tasks.named("preBuild") {
    dependsOn("decodeImages")
}
