@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

configureLibraryModule()

dependencies {
    implementation(libs.androidx.test.core)
    implementation(libs.truth)
}
