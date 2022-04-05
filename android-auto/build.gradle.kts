plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 23
        missingDimensionStrategy("settings_visibility", "internal")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    api("androidx.car.app:app:1.1.0")
    api("com.mapbox.navigation:android:2.4.0-rc.1")
    api("com.mapbox.search:mapbox-search-android:1.0.0-beta.26")
    api("com.mapbox.search:mapbox-search-android-ui:1.0.0-beta.26")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}

dependencies {
    testImplementation("org.robolectric:robolectric:4.7.3")
    testImplementation("androidx.test.ext:junit:1.1.3")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")
    testImplementation("androidx.lifecycle:lifecycle-runtime-testing:2.4.1")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:core:1.4.0")
    androidTestImplementation("io.mockk:mockk-android:1.12.2")
}
