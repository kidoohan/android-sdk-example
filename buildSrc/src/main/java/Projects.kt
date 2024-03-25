import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.io.File

data class QualityConfig(
    val jacocoToolVersion: String,
    val androidTest: Boolean = false
)

fun Project.configureBomModule() {
    apply(plugin = "com.vanniktech.maven.publish.base")
    extensions.configure<PublishingExtension> {
        publications.create<MavenPublication>("release") {
            from(components["javaPlatform"])
            // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/326
            val id = project.property("POM_ARTIFACT_ID").toString()
            artifactId = artifactId.replace(project.name, id)
        }
    }
}

fun Project.configureLibraryModule(
    publish: Boolean = false,
    document: Boolean = false,
    qualityConfig: QualityConfig? = null,
    block: LibraryExtension.() -> Unit = {}
) = configureBaseModule<LibraryExtension> {
    qualityConfig?.run {
        configureSubProjectJacoco(jacocoToolVersion, androidTest)
        testCoverage {
            jacocoVersion = qualityConfig.jacocoToolVersion
        }
    }

    if (publish) {
        if (document) {
            configureDokka()
        }
        apply(plugin = "com.vanniktech.maven.publish.base")
        publishing {
            singleVariant("release")
        }
        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications.create<MavenPublication>("release") {
                    from(components["release"])
                    // https://github.com/vanniktech/gradle-maven-publish-plugin/issues/326
                    val id = project.property("POM_ARTIFACT_ID").toString()
                    artifactId = artifactId.replace(project.name, id)
                }
            }
        }
    }

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    testOptions.unitTests {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true

        all {
            it.jvmArgs("-Xmx4096m", "-noverify")
        }
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles.add(File(projectDir, "proguard-rules.pro"))
        }
    }

    block()
}

fun Project.configureAppModule(
    qualityConfig: QualityConfig? = null,
    block: BaseAppModuleExtension.() -> Unit = {}
) = configureBaseModule<BaseAppModuleExtension> {
    qualityConfig?.run {
        configureSubProjectJacoco(jacocoToolVersion, androidTest)
        testCoverage {
            jacocoVersion = qualityConfig.jacocoToolVersion
        }
    }

    defaultConfig {
        versionCode = 1
        multiDexEnabled = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    block()
}

private inline fun <reified T : BaseExtension> Project.configureBaseModule(
    crossinline block: T.() -> Unit = {}
) = extensions.configure<T>("android") {
    compileSdkVersion(project.compileSdk)
    defaultConfig {
        versionName = project.versionName
        minSdk = project.minSdk
        targetSdk = project.targetSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "VERSION_NAME", "\"${project.versionName}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }

    testOptions.animationsDisabled = true

    block()
}

private fun BaseExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}