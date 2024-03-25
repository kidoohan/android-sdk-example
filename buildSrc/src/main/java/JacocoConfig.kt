import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.withType
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

fun Project.configureSubProjectJacoco(
    jacocoToolVersion: String,
    androidTest: Boolean = false
) {
    apply<JacocoPlugin>()
    configure<JacocoPluginExtension> {
        toolVersion = jacocoToolVersion
    }
    tasks.create(name = "jacocoTestReport", type = JacocoReport::class) {
        val testTasks = mutableSetOf("testDebugUnitTest")
        if (androidTest) {
            testTasks.add("connectedDebugAndroidTest")
        }

        dependsOn(testTasks)
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        sourceDirectories.setFrom("$projectDir/src/main/java")
        classDirectories.setFrom(
            fileTree("$buildDir/intermediates/javac/debug"),
            fileTree("$buildDir/tmp/kotlin-classes/debug")
        )
        executionData.setFrom(
            fileTree(buildDir) {
                include(
                    listOf(
                        // Unit test coverage report location
                        "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                        // Instrumentation coverage report location
                        "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
                    )
                )
            }
        )
    }

    tasks.withType<Test> {
        configure<JacocoTaskExtension> {
            // Required for jacoco to work with Robolectric tests
            // See https://newbedev.com/jacoco-doesn-t-work-with-robolectric-tests
            isIncludeNoLocationClasses = true

            // Required for jacoco to work
            // See https://github.com/gradle/gradle/issues/5184#issuecomment-391982009
            excludes = listOf("jdk.internal.*")
        }
    }
}

fun Project.configureRootProjectJacoco(jacocoToolVersion: String) {
    if (this != rootProject) {
        return
    }

    apply<JacocoPlugin>()
    configure<JacocoPluginExtension> {
        toolVersion = jacocoToolVersion
    }
    tasks.create(name = "jacocoFullReport", type = JacocoReport::class) {
        val jacocoReportTasks = subprojects.map {
            it.getTasksByName("jacocoTestReport", false)
        }.flatten().filterIsInstance<JacocoReport>().onEach {
            dependsOn(it)
        }
        group = LifecycleBasePlugin.VERIFICATION_GROUP

        reports {
            xml.required.set(true)
            html.required.set(true)
        }

        sourceDirectories.setFrom(jacocoReportTasks.mapNotNull { it.sourceDirectories })
        classDirectories.setFrom(jacocoReportTasks.mapNotNull { it.classDirectories })
        executionData.setFrom(jacocoReportTasks.mapNotNull { it.executionData })
    }
}
