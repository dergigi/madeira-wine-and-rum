import java.util.Properties

plugins { id("com.android.application") }

val secrets = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun secret(name: String): String? = System.getenv(name) ?: secrets.getProperty(name)

android {
    namespace = "org.dergigi.madeirawineandrum"
    compileSdk = 37
    defaultConfig {
        applicationId = "org.dergigi.madeirawineandrum"
        minSdk = 26
        targetSdk = 37
        versionCode = 10
        versionName = "0.7.1"
    }
    signingConfigs {
        create("release") {
            secret("OEM_STORE_FILE")?.let { storeFile = rootProject.file(it) }
            storePassword = secret("OEM_STORE_PASSWORD")
            keyAlias = secret("OEM_KEY_ALIAS")
            keyPassword = secret("OEM_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependenciesInfo { includeInApk = false; includeInBundle = false }
}

// A release must never silently fall back to an unsigned or debug-signed APK.
tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    doFirst {
        listOf("OEM_STORE_FILE", "OEM_STORE_PASSWORD", "OEM_KEY_ALIAS", "OEM_KEY_PASSWORD").forEach {
            require(!secret(it).isNullOrBlank()) { "Missing release signing setting: $it" }
        }
    }
}
