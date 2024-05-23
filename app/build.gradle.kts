plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.seestop"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.seestop"
        minSdk = 24
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
}

dependencies {

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:android-maps-utils:0.5")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.code.gson:gson:2.8.8")
    implementation("com.mapbox.maps:android:11.4.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}