plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.0"
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.5.1-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
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
    intellijPlatform {
        intellijIdeaCommunity("2024.3.2.1")
        plugin("com.intellij.javafx:1.0.4")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            untilBuild = provider { null }
        }
    }
}

javafx {
    version = "20"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.swing", "javafx.web", "javafx.media", "javafx.base")
}

tasks {
    runIde {
        jvmArgs("-Xms256m", "-Xmx3048m")
    }
}