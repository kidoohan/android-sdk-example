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
    namespace = "com.example.sdk.sample"
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
    buildFeatures.viewBinding = true
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.multidex)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.2")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    implementation(libs.playservices.identifier)
    implementation(libs.playservices.appset)

    implementation(projects.sdk)
    implementation(libs.androidx.fragment.testing)
    implementation(libs.androidx.test.espresso.idlingResource)

    implementation(projects.sdkTestdata)
    implementation(projects.sdkTestutils)

    androidTestImplementation(libs.bundles.test.android)
}
