plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "net.invictusmanagement.invictuskiosk"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.invictusmanagement.invictuskiosk"
        minSdk = 26
        targetSdk = 35
        versionCode = 16
        versionName = "1.16"

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
        debug {
            isMinifyEnabled = false
        }
    }

    flavorDimensions += "version"

    productFlavors {
        create("live") {
            dimension = "version"

            buildConfigField("String", "_baseUrl", "\"https://kiosk.invictusmanagement.net/api/v1/\"")
            buildConfigField("String", "_mobileBaseUrl", "\"https://mobile.invictusmanagement.net/api/v1/\"")
            buildConfigField("String", "_chatHubBaseUrl", "\"https://mobilechat.invictusmanagement.net/chathub\"")
            buildConfigField("String", "_chatMobileHubBaseUrl", "\"https://mobile.invictusmanagement.net/chathub\"")
            resValue("string", "app_name", "Invictus kiosk")
        }

        create("local") {
            dimension = "version"

            buildConfigField("String", "_baseUrl", "\"https://kioskdev.invictusmanagement.net/api/v1/\"")
            buildConfigField("String", "_mobileBaseUrl", "\"https://mobiledev.invictusmanagement.net/api/v1/\"")
            buildConfigField("String", "_chatHubBaseUrl", "\"https://mobilechatdev.invictusmanagement.net/chathub\"")
            buildConfigField("String", "_chatMobileHubBaseUrl", "\"https://mobiledev.invictusmanagement.net/chathub\"")
            resValue("string", "app_name", "Invictus Kiosk")
        }

        create("localhost") {
            dimension = "version"

            buildConfigField("String", "_baseUrl", "\"https://kioskdev.invictusmanagement.net/api/v1/\"")
            buildConfigField("String", "_mobileBaseUrl", "\"https://mobiledev.invictusmanagement.net/api/v1/\"")
            buildConfigField("String", "_chatHubBaseUrl", "\"https://mobilechatdev.invictusmanagement.net/chathub\"")
            buildConfigField("String", "_chatMobileHubBaseUrl", "\"https://mobiledev.invictusmanagement.net/chathub\"")
            resValue("string", "app_name", "Invictus Kiosk")
        }

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(":relaymanager"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.play.services.vision.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose dependencies
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.accompanist.flowlayout)

    // Navigation dependencies
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Dagger Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.media3.datasource)

    // CameraX + ML Kit
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.barcode.scanning)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Twilio
    implementation(libs.video.android)

    // Coil
    implementation(libs.coil.compose)

    // SignalR
    implementation(libs.signalr)

    //room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
}
