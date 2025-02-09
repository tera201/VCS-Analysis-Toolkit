plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.5.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    implementation("org.tera201:javafx-uml-graph:0.0.2-SNAPSHOT")
    implementation("org.tera201:code-to-uml:0.1.0-SNAPSHOT")
    implementation("org.tera201:swrminer:0.4.1-SNAPSHOT")
    implementation("org.tera201:javafx-code-modeling-tool:1.2.2-SNAPSHOT")
    implementation("org.tera201:swing-components:1.1.2-SNAPSHOT")
    implementation("com.formdev:flatlaf:3.4.1")
    implementation("com.formdev:flatlaf-extras:3.4.1")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
}

javafx {
    version = "20"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.swing", "javafx.web", "javafx.media", "javafx.base")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2024.1")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.javafx:1.0.4"))
}

tasks {
    runIde {
        jvmArgs("-Xms256m", "-Xmx3048m")
    }

    patchPluginXml {
        sinceBuild.set("221.*")
    }
}