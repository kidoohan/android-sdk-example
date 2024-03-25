import org.gradle.api.Project // ktlint-disable filename
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubePlugin

fun Project.configureSonarQube() {
    apply<SonarQubePlugin>()
    configure<SonarQubeExtension> {
        properties {
            property("sonar.sourceEncoding", "UTF-8")
            property("sonar.projectVersion", project.versionName)
            property("sonar.sources", "src/main/java")
            property("sonar.tests", "src/test/java, src/androidTest/java")
            property(
                "sonar.exclusions",
                arrayOf(
                    "sdk-sample/src/main/**/*",
                    "sdk-testdata/**/*",
                    "sdk-testutils/**/*",
                    "sdk-robolectricutils/**/*",
                    "**/mraid.js",
                ).joinToString(separator = ", "),
            )
            property(
                "sonar.junit.reportPaths",
                arrayOf(
                    "build/test-results/testDebugUnitTest",
                    "build/outputs/androidTest-results/connected",
                ).joinToString(separator = ", "),
            )
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                file("${rootProject.buildDir}/reports/jacoco/jacocoFullReport/jacocoFullReport.xml"),
            )
        }
    }
}
