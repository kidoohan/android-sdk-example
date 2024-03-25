import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTaskPartial

fun Project.configureDokka() {
    apply<DokkaPlugin>()

    // javadoc, (partial)html 모두에 적용되는 사항
    tasks.withType<AbstractDokkaLeafTask> {
        dokkaSourceSets.configureEach {
            perPackageOption {
                matchingRegex.set(".*internal.*")
                suppress.set(true)
            }

            documentedVisibilities.set(
                setOf(
                    DokkaConfiguration.Visibility.PUBLIC,
                    DokkaConfiguration.Visibility.PROTECTED
                )
            )

            noAndroidSdkLink.set(false)
            jdkVersion.set(8)
            externalDocumentationLink("https://developer.android.com/reference/")
        }

        moduleName.set(project.name)
        failOnWarning.set(true)
        suppressInheritedMembers.set(true)
    }

    // (partial)html 에만 적용되는 사항
    tasks.withType<DokkaTaskPartial>().configureEach {
        outputDirectory.set(buildDir.resolve("dokka"))
    }

    tasks.register<Jar>("dokkaJavadocJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP

        val dokkaJavadoc = tasks.named("dokkaJavadoc")
        from(dokkaJavadoc)
        archiveClassifier.set("javadoc")
        destinationDirectory.set(file("$buildDir/outputs/javadoc/"))
    }
}

fun Project.configureDokkaMultiModule() {
    apply<DokkaPlugin>()
    tasks.withType<DokkaMultiModuleTask>().configureEach {
        outputDirectory.set(file("$rootDir/docs/static/api"))
    }
}
