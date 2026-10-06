plugins { id("com.android.application") }
android {
    namespace = "com.example.server"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.server"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = true }
}
dependencies { implementation(project(":contract")) }
