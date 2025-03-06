plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.0"
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

group = "org.tera201"
version = "1.7.0-223"

val javafxModules = listOf("javafx-controls", "javafx-graphics", "javafx-swing", "javafx-base")
val javaFXVersion = "21";

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    implementation("org.tera201:javafx-uml-graph:0.0.2-SNAPSHOT")
    implementation("org.tera201:code-to-uml:0.1.0-SNAPSHOT")
    implementation("org.tera201:swrminer:0.4.1-SNAPSHOT")
    implementation("org.tera201:javafx-code-modeling-tool:1.2.2-SNAPSHOT")
    implementation("org.tera201:swing-components:1.1.2-SNAPSHOT")
    implementation("com.formdev:flatlaf:3.4.1")
    implementation("com.formdev:flatlaf-extras:3.4.1")
    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    javafxModules.forEach { lib ->
        runtimeOnly("org.openjfx:$lib:$javaFXVersion:linux")
        runtimeOnly("org.openjfx:$lib:$javaFXVersion:win")
    }

    intellijPlatform {
        intellijIdeaCommunity("2022.3")
        plugin("com.intellij.javafx:1.0.4")
        instrumentationTools()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            untilBuild = provider { "233.*" }
            sinceBuild = provider { "223" }
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