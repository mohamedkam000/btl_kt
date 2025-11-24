plugins {
    id("com.android.application")
}

android {
    namespace = "com.btl.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.btl.app"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        androidResources {
            localeFilters += setOf("en")
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("bt.p12")
            storePassword = "1234"
            keyAlias = "US"
            keyPassword = "1234"
            storeType = "pkcs12"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
}