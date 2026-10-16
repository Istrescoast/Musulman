plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "fr.nejma"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.nejma"
        minSdk = 26
        targetSdk = 36
        // Le numéro de build GitHub fait monter la version à chaque compilation.
        val build = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()
        versionCode = build
        versionName = "1.0.$build"
    }

    // Clé fixe gardée dans le dépôt : chaque nouvelle version s'installe par-dessus l'ancienne
    // sans perdre les données.
    signingConfigs {
        create("nejma") {
            storeFile = file("nejma.jks")
            storePassword = "nejma-appli"
            keyAlias = "nejma"
            keyPassword = "nejma-appli"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("nejma")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.webkit:webkit:1.13.0")
}
