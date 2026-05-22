val gsonVersion = "2.10.1"
// REVISION: Added version variables for the new architectural libraries
val roomVersion = "2.6.1"
val workVersion = "2.9.0"
val mpChartVersion = "3.1.0" // Added MPAndroidChart version variable

// NEW: Added explicit version variables for the Onboarding UI requirements
val viewPagerVersion = "1.0.0"
val materialVersion = "1.11.0"

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.crystelle.electrolife"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.crystelle.electrolife"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)

    // VERIFICATION: Explicitly defining Material Components version for ViewPager2 TabLayout compatibility
    // implementation(libs.material) // Commented out the old dynamic lib reference
    implementation("com.google.android.material:material:$materialVersion")

    implementation(libs.activity)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.gson.v2101)
    implementation(libs.constraintlayout)

    // --- NEW ARCHITECTURAL DEPENDENCIES ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Task 1: Background Monitoring
    // Purpose: Android WorkManager API for scheduling daily budget checks
    implementation("androidx.work:work-runtime:$workVersion")

    // Task 2: Text-Heavy, Data-Poor Representation
    // Purpose: MPAndroidChart library for interactive Donut/Pie charts
    // Heads-up: Make sure maven { url 'https://jitpack.io' } is in your settings.gradle file!
    implementation("com.github.PhilJay:MPAndroidChart:$mpChartVersion")

    // Task 3: Historical Analytics (Month-over-Month)
    // Purpose: Google's Room API for robust SQLite database management
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Task 4: Swipeable Onboarding Integration
    // Purpose: Required for horizontal swiping gestures in the OnboardingActivity
    implementation("androidx.viewpager2:viewpager2:$viewPagerVersion")

    // ---------------------------------------

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}