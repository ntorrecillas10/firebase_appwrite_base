plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    //necesario
    id("com.google.gms.google-services")
    id ("kotlin-kapt")
}

android {
    namespace = "com.nacho.firebase_appwrite_base"
    compileSdk = 35

    buildFeatures {
        //necesario
        viewBinding=true
        dataBinding=true
    }

    defaultConfig {
        applicationId = "com.nacho.firebase_appwrite_base"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.text.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //appwrite
    implementation("io.appwrite:sdk-for-kotlin:5.0.1")

    //firebase
    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-core:20.0.0")
    implementation ("com.google.firebase:firebase-storage:21.0.1")
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.google.firebase:firebase-analytics:22.1.2")
    implementation ("com.google.android.gms:play-services-auth:21.3.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation (libs.firebase.analytics.ktx)
    implementation(libs.google.firebase.analytics)
    implementation (libs.firebase.storage.ktx)
    //glide , para cargar una imagen desde un enlace
    implementation(libs.github.glide) // Use the latest version
    annotationProcessor(libs.compiler)

}