@file:Suppress("UnstableApiUsage")

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

configureLibraryModule(
    publish = true,
    document = true,
    qualityConfig = QualityConfig(
        jacocoToolVersion = libs.versions.jacoco.get(),
        androidTest = true,
    ),
) {
    defaultConfig {
        // Enable multidex for androidTests.
        multiDexEnabled = true
    }

    sourceSets {
        getByName("test") {
            assets.srcDir("../sdk-testdata/src/main/assets/")
        }
        getByName("androidTest") {
            assets.srcDir("../sdk-testdata/src/main/assets/")
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
    implementation(libs.kotlin.stdlib)
    implementation(libs.playservices.tasks)

    compileOnly(libs.playservices.appset)
    compileOnly(libs.androidx.fragment)

    testImplementation(libs.androidx.fragment)
    testImplementation(projects.sdkTestdata)
    testImplementation(projects.sdkRobolectricutils) {
        exclude(":sdk")
    }

    androidTestImplementation(libs.playservices.identifier)
    androidTestImplementation(libs.playservices.appset)
    androidTestImplementation(projects.sdkTestdata)
    androidTestImplementation(projects.sdkTestutils)
    androidTestImplementation(libs.bundles.test.android)
}
