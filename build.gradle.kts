@file:Suppress("UnstableApiUsage")

// https://youtrack.jetbrains.com/issue/KTIJ-19369

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    jacoco
}

allprojects {
    group = project.groupId
    version = project.versionName

    plugins.withId("com.vanniktech.maven.publish.base") {
        group = project.groupId
        version = project.versionName

        configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
            publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.DEFAULT)
            signAllPublications()
            pomFromGradleProperties()
        }
    }
}

configureDokkaMultiModule()
configureSpotless(ktlintVersion = libs.versions.ktlint.get())
configureRootProjectJacoco(jacocoToolVersion = libs.versions.jacoco.get())
configureSonarQube()
configureBinaryCompatibility()
