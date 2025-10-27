plugins {
    id("java")
    alias(libs.plugins.platform)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.javafxplugin)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.9.0"

val javafxModules = listOf("javafx-controls", "javafx-graphics", "javafx-swing", "javafx-base")
val javaFXVersion = libs.versions.javafx.get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.tera201:javafx-uml-graph")
    implementation("org.tera201:code-to-model")
    implementation("org.tera201:vcs-manager")
    implementation("org.tera201:javafx-code-modeling-tool")
    implementation("org.tera201:swing-components")
    implementation(libs.bundles.flatlaf)
    implementation(libs.sqlite)
    javafxModules.forEach { lib ->
        runtimeOnly("org.openjfx:$lib:$javaFXVersion:linux")
        runtimeOnly("org.openjfx:$lib:$javaFXVersion:win")
    }

    intellijPlatform {
        intellijIdeaCommunity("2025.1")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = provider { "241" }
        }
    }
}

javafx {
    version = javaFXVersion
    modules = javafxModules.map { it.replace("-", ".") }
}

tasks {
    runIde {
        jvmArgs("-Xms256m", "-Xmx3048m")
    }
}