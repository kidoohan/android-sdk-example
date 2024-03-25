plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

@Suppress(
    "MISSING_DEPENDENCY_CLASS",
    "UNRESOLVED_REFERENCE_WRONG_RECEIVER",
)
dependencies {
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.gradlePlugin.spotless)
    implementation(libs.gradlePlugin.sonarqube)
    implementation(libs.gradlePlugin.mavenPublish)
    implementation(libs.gradlePlugin.dokka)
    implementation(libs.gradlePlugin.binaryCompatibility)
}
