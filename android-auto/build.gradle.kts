plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(23)
        missingDimensionStrategy("settings_visibility", "internal")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    api("com.mapbox.navigation:android:2.0.0-rc.4")
    api("com.mapbox.search:mapbox-search-android:1.0.0-beta.19")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.car.app:app:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
}

dependencies {
    testImplementation("org.robolectric:robolectric:4.3.1")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3")
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("io.mockk:mockk-android:1.10.0")
}
