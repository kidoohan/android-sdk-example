@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

configureLibraryModule()

dependencies {
    implementation(projects.sdk)

    api(projects.sdkTestutils)
    api(libs.bundles.test.jvm)
}
