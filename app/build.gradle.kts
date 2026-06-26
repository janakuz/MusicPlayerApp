import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}


val apikeyPropertiesFile = rootProject.file("secrets.properties")
val apikeyProperties = Properties()
apikeyProperties.load(FileInputStream(apikeyPropertiesFile))

android {
    namespace = "com.example.musicapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.musicapp"
        minSdk = 33
        targetSdk = 36
        versionCode = 4
        versionName = "1.2"

        android.buildFeatures.buildConfig = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DISCOGS_KEY", apikeyProperties.getProperty("DISCOGS_KEY"))
        buildConfigField("String", "DISCOGS_SECRET", apikeyProperties.getProperty("DISCOGS_SECRET"))
        buildConfigField("String", "LASTFM_KEY", apikeyProperties.getProperty("LASTFM_KEY"))
        buildConfigField("String", "LASTFM_SECRET", apikeyProperties.getProperty("LASTFM_SECRET"))
        buildConfigField("String", "ESSENTIA_KEY", apikeyProperties.getProperty("ESSENTIA_KEY"))
        buildConfigField("String", "USER_AGENT", apikeyProperties.getProperty("USER_AGENT"))


    }

    buildFeatures{
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui:1.8.0")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.navigation:navigation-compose:2.7.4")
    implementation("androidx.media3:media3-session:1.7.1")
    implementation("androidx.media3:media3-exoplayer:1.7.1")
    implementation("androidx.media3:media3-transformer:1.7.1")
    implementation("sh.calvin.reorderable:reorderable:2.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Retrofit with Scalar Converter
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    //   implementation(libs.androidx.navigation.compose.jvmstubs)
    implementation("androidx.room:room-runtime:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-common-jvm:2.7.2")
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp("androidx.room:room-compiler:${rootProject.extra["room_version"]}")
    implementation("androidx.room:room-ktx:${rootProject.extra["room_version"]}")
// Hilt
    implementation("com.google.dagger:hilt-android:2.56.2")
 //   ksp("com.google.dagger:hilt-android-compiler:2.56.2")
    kapt("com.google.dagger:hilt-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}