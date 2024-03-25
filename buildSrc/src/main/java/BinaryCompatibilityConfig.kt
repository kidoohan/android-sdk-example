import kotlinx.validation.ApiValidationExtension // ktlint-disable filename
import kotlinx.validation.BinaryCompatibilityValidatorPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

fun Project.configureBinaryCompatibility() {
    apply<BinaryCompatibilityValidatorPlugin>()
    configure<ApiValidationExtension> {
        ignoredPackages.add("com.example.sdk.internal")
        ignoredProjects.addAll(
            listOf(
                "sdk-robolectricutils",
                "sdk-sample",
                "sdk-testutils"
            )
        )
        ignoredClasses.addAll(
            listOf(
                "com.example.sdk.BuildConfig"
            )
        )
    }
}
