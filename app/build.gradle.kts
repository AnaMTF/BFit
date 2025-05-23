plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "proiect.pdm.bfit"
    compileSdk = 35

    defaultConfig {
        applicationId = "proiect.pdm.bfit"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.activity)
    implementation(libs.play.services.location)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    val room_version = "2.7.1"
// de aici in jos
//    implementation("androidx.room:room-runtime:$room_version")
//
//    // If this project only uses Java source, use the Java annotationProcessor
//    // No additional plugins are necessary
//    //annotationProcessor("androidx.room:room-compiler:$room_version")
//    kapt("androidx.room:room-compiler:$room_version")
//    // optional - Kotlin Extensions and Coroutines support for Room
//    implementation("androidx.room:room-ktx:$room_version")
//
//    // optional - RxJava2 support for Room
//    implementation("androidx.room:room-rxjava2:$room_version")
//
//    // optional - RxJava3 support for Room
//    implementation("androidx.room:room-rxjava3:$room_version")
//
//    // optional - Guava support for Room, including Optional and ListenableFuture
//    implementation("androidx.room:room-guava:$room_version")
//
//    // optional - Test helpers
//    testImplementation("androidx.room:room-testing:$room_version")
//
//    // optional - Paging 3 Integration
//    implementation("androidx.room:room-paging:$room_version")
//
//    // pentru Google Maps
//    implementation ("com.google.android.gms:play-services-maps:18.2.0")
//    implementation ("com.google.android.libraries.places:places:3.4.0")
//    implementation ("com.google.android.gms:play-services-location:21.1.0")
//
//        implementation ("com.github.bumptech.glide:glide:4.15.1")
//        kapt ("com.github.bumptech.glide:compiler:4.15.1")  // dacă folosești Kotlin
//
//
//
//    // Places SDK for Android KTX Library
//    implementation ("com.google.maps.android:places-ktx:3.1.1")
//
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0")) // 1.8+ required
//    implementation("com.google.android.libraries.places:places:4.1.0")  // or 3.5.0+
}