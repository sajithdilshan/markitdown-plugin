import java.util.Properties

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    id("org.jetbrains.intellij.platform") version "2.13.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

/** Machine-local defaults (gitignored); see https://plugins.jetbrains.com/docs/intellij/plugin-signing.html */
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.isFile) f.inputStream().use(::load)
}

fun signingCertificateChainFile(): String? =
    System.getenv("PLUGIN_SIGNING_CERT_CHAIN_FILE")
        ?: localProperties.getProperty("pluginSigning.certificateChainFile")
        ?: findProperty("pluginSigning.certificateChainFile") as String?

fun signingPrivateKeyFile(): String? =
    System.getenv("PLUGIN_SIGNING_PRIVATE_KEY_FILE")
        ?: localProperties.getProperty("pluginSigning.privateKeyFile")
        ?: findProperty("pluginSigning.privateKeyFile") as String?

fun signingPrivateKeyPassword(): String? =
    System.getenv("PLUGIN_SIGNING_PRIVATE_KEY_PASSWORD")
        ?: localProperties.getProperty("pluginSigning.privateKeyPassword")
        ?: findProperty("pluginSigning.privateKeyPassword") as String?

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
    }
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        val chain = signingCertificateChainFile()
        val key = signingPrivateKeyFile()
        if (chain != null && key != null) {
            certificateChainFile.set(file(chain))
            privateKeyFile.set(file(key))
            signingPrivateKeyPassword()?.let { password.set(it) }
        }
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
        // Route by version suffix: "1.0.0" -> default (stable); "1.1.0-eap.2" -> "eap"; "1.1.0-beta" -> "beta".
        val versionValue = providers.gradleProperty("pluginVersion").get()
        val channel = versionValue.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }
        channels = listOf(channel)
    }
}

/**
 * Rebuilds the CodeMirror editor bundle (src/main/resources/markit/markit-editor.js) from src/main/web.
 * Not wired into the default build: the bundle is committed so builds don't require Node.
 */
tasks.register<Exec>("buildWebEditor") {
    group = "build"
    description = "Installs npm dependencies and bundles the web editor with esbuild."
    workingDir = file("src/main/web")
    commandLine("sh", "-c", "npm ci && npm run build")
}
