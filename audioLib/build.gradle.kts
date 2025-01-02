plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("maven-publish")
}

android {
    namespace = "com.blackend.udbhav.audiolib"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.media3)
    implementation(libs.react.native)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

fun getVersionName(): String {
    return "v0.0.1-alpha"
}

fun getGroupId(): String {
    return "com.github.Udbhav003"
}

fun getArtifactId(): String {
    return "AudioLib"
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = groupId
            artifactId = artifactId
            version = version

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}