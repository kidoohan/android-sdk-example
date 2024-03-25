@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

configureAppModule(
    qualityConfig = QualityConfig(
        jacocoToolVersion = libs.versions.jacoco.get(),
        androidTest = true,
    ),
) {
    defaultConfig {
        applicationId = "com.example.sdk.sample"
        testApplicationId = "com.example.sdk.sample.test"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            assets.srcDir("../sdk-testdata/src/main/assets/")
        }
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    viewBinding.isEnabled = true
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.multidex)

    implementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.test.espresso.idlingResource)

    androidTestImplementation(libs.bundles.test.android)
}
