import java.io.File
import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.registering

val localProps = localProperties(rootDir)
val jamendoClientId =
    localProps.getProperty("jamendoClientId")?.takeIf { it.isNotBlank() }
        ?: throw GradleException("Specify jamendoClientId in local.properties")
val authBaseUrl = localProps.getProperty("authBaseUrl")?.takeIf { it.isNotBlank() }
    ?: throw GradleException("Specify authBaseUrl in local.properties")
val networkSecurityDomain =
    localProps.getProperty("networkSecurityDomain")?.takeIf { it.isNotBlank() }
        ?: throw GradleException("Specify networkSecurityDomain in local.properties")
val generatedNetworkSecurityResDir = layout.buildDirectory.dir("generated/networkSecurity/res")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.musicplay"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.musicplay"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "JAMENDO_CLIENT_ID",
            "\"$jamendoClientId\""
        )
        buildConfigField(
            "String",
            "AUTH_BASE_URL",
            "\"$authBaseUrl\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    sourceSets["main"].res.srcDir(generatedNetworkSecurityResDir)
}

val generateNetworkSecurityConfig by tasks.registering(GenerateNetworkSecurityConfigTask::class) {
    domain.set(networkSecurityDomain)
    outputDir.set(generatedNetworkSecurityResDir.map { it.dir("xml") })
}

tasks.named("preBuild") {
    dependsOn(generateNetworkSecurityConfig)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

abstract class GenerateNetworkSecurityConfigTask : DefaultTask() {
    @get:Input
    abstract val domain: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val xmlDir = outputDir.get().asFile
        xmlDir.mkdirs()
        val configFile = File(xmlDir, "network_security_config.xml")
        configFile.writeText(
            """
                <?xml version="1.0" encoding="utf-8"?>
                <network-security-config>
                    <domain-config cleartextTrafficPermitted="true">
                        <domain includeSubdomains="false">${domain.get()}</domain>
                    </domain-config>
                </network-security-config>
            """.trimIndent()
        )
    }
}

fun localProperties(rootDir: File): Properties {
    val properties = Properties()
    val localPropertiesFile = File(rootDir, "local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            properties.load(stream)
        }
    }
    return properties
}
