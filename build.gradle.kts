plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "org.tera201"
version = "1.4.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
//    maven {
//        url = uri("https://maven.pkg.github.com/tera201/JavaFXUMLCityBuilder")
//        credentials {
//            username = project.properties["username"].toString()
//            password = project.properties["password"].toString()
//        }
//    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    implementation("org.tera201:javafx-uml-graph:0.0.2-SNAPSHOT")
    implementation("org.tera201:code-to-uml:1.1.0-SNAPSHOT")
    implementation("org.tera201:swrminer:0.3.1-SNAPSHOT")
    implementation("org.tera201:javafx-code-modeling-tool:1.2.2-SNAPSHOT")
    implementation("org.tera201:swing-components:1.1.1-SNAPSHOT")
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
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            apiVersion = "1.8"
            languageVersion = "1.8"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.languageVersion = "1.8"
        kotlinOptions.apiVersion = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.languageVersion = "1.8"
        kotlinOptions.apiVersion = "1.8"
    }
    patchPluginXml {
        sinceBuild.set("221.*")
    }
}