plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.pockettracker"
    compileSdk = 35
    defaultConfig { applicationId = "com.pockettracker"; minSdk = 26; targetSdk = 33; versionCode = 1; versionName = "0.1.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation(project(":core")) }
