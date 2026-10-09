plugins {
    id("com.android.application")
}

android {
    testOptions { unitTests.isIncludeAndroidResources = true }
    namespace = "de.joeldankert.revierverwaltung"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.joeldankert.revierverwaltung"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.16.1")
}
