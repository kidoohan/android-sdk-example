import com.diffplug.gradle.spotless.SpotlessExtension // ktlint-disable filename
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

fun Project.configureSpotless(ktlintVersion: String) {
    apply<SpotlessPlugin>()
    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude(".idea/**", "buildSrc/.gradle/**")
            ktlint(ktlintVersion)
        }
        java {
            target("**/*.java")
            targetExclude(".idea/**", "buildSrc/.gradle/**")
            googleJavaFormat().aosp()
            removeUnusedImports()
        }
        kotlinGradle {
            target("*.gradle.kts", "gradle/*.gradle.kts", "buildSrc/*.gradle.kts")
            ktlint(ktlintVersion)
        }
        format("markdown") {
            target(
                fileTree(
                    mapOf(
                        "dir" to ".",
                        "include" to listOf("**/*.md"),
                        "exclude" to listOf(".gradle/**", ".gradle-cache/**", "build/**")
                    )
                )
            )

            indentWithSpaces()
            endWithNewline()
        }
        format("misc") {
            target(
                fileTree(
                    mapOf(
                        "dir" to ".",
                        "include" to listOf("**/.gitignore", "**/*.yaml", "**/*.yml", "**/*.sh"),
                        "exclude" to listOf("**/*.md", ".gradle/**", ".gradle-cache/**", "build/**")
                    )
                )
            )
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
    }
}
