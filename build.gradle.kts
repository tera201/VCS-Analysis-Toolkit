plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.0"
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.7.2"

val javafxModules = listOf("javafx-controls", "javafx-graphics", "javafx-swing", "javafx-base")
val javaFXVersion = "21";

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.tera201:javafx-uml-graph")
    implementation("org.tera201:code-to-uml")
    implementation("org.tera201:swrminer")
    implementation("org.tera201:javafx-code-modeling-tool")
    implementation("org.tera201:swing-components")
    implementation("com.formdev:flatlaf:3.4.1")
    implementation("com.formdev:flatlaf-extras:3.4.1")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    javafxModules.forEach { lib ->
        runtimeOnly("org.openjfx:$lib:$javaFXVersion:linux")
        runtimeOnly("org.openjfx:$lib:$javaFXVersion:win")
    }

    intellijPlatform {
        intellijIdeaCommunity("2024.3.2.1")
        plugin("com.intellij.javafx:1.0.4")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            untilBuild = provider { "243.*" }
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