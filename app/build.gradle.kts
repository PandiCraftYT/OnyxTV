plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // Supabase no requiere plugin de google-services a menos que uses Push
}

android {
    namespace = "com.example.onyxapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.onyxapp"
        minSdk = 23
        targetSdk = 35 
        versionCode = 3
        versionName = "1.2"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    splits {
        abi {
            isEnable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("org.videolan.android:libvlc-all:3.6.0-eap5")
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation("androidx.compose.material3:material3")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
