import java.io.StringReader
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.secrets.gradle.plugin)
}

if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.google.services.get().pluginId)
}

val unicodeLocalProperties = listOf("FIREBASE_TEST_ROLES")

fun decodeProperties(bytes: ByteArray): Properties {
    val text = try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (exception: CharacterCodingException) {
        String(bytes, Charsets.ISO_8859_1)
    }
    return Properties().apply { load(StringReader(text.removePrefix("\uFEFF"))) }
}

fun localProperty(key: String): String? =
    listOf("local.properties", "local.defaults.properties").firstNotNullOfOrNull { name ->
        providers.fileContents(rootProject.layout.projectDirectory.file(name)).asBytes.orNull
            ?.let(::decodeProperties)
            ?.getProperty(key)
            ?.trim()
            ?.removeSurrounding("\"")
    }

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n") + "\""

android {
    namespace = "com.uniruta.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.uniruta.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        unicodeLocalProperties.forEach { key ->
            buildConfigField("String", key, (localProperty(key) ?: "NONE").asBuildConfigString())
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

secrets {
    propertiesFileName = "local.properties"
    defaultPropertiesFileName = "local.defaults.properties"
    ignoreList = (ignoreList + unicodeLocalProperties.map { "^$it$" }).toMutableList()
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.maps.compose)
    implementation(libs.play.services.location)
    implementation(libs.play.services.code.scanner)
    implementation(libs.zxing.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
