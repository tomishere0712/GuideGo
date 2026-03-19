import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.guidego"
    compileSdk = 36

//    val localProperties = Properties()
//    val localPropertiesFile = rootProject.file("local.properties")
//    if (localPropertiesFile.exists()) {
//        localProperties.load(localPropertiesFile.inputStream())
//    }
//    val baseUrl = localProperties.getProperty("BASE_URL") ?: "http://10.0.2.2:5000/api/"

    defaultConfig {
        applicationId = "com.example.guidego"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val baseUrl = localProperties.getProperty("BASE_URL") ?: "http://10.0.2.2:5000/api/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY") ?: ""
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.swiperefreshlayout)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Image Loading
    implementation(libs.glide)

    // UI
    implementation(libs.circleimageview)
    implementation(libs.play.services.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Google map
    implementation("com.google.android.gms:play-services-location:21.0.1")
}