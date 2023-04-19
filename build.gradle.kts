plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://maven.pkg.github.com/tera201/JavaFXUMLCityBuilder")
        credentials {
            username = project.properties["username"].toString()
            password = project.properties["password"].toString()
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    implementation("org.example:javafx-uml-city-builder:latest.integration")
    implementation("org.example:javafx-uml-graph-idea:1.1-SNAPSHOT")
    implementation("org.example:cpp-to-uml:1.2-SNAPSHOT")
    implementation("org.example:swrminer:1.1-SNAPSHOT")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2023.1")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.javafx:1.0.4"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
            apiVersion = "1.8"
            languageVersion = "1.8"
        }
    }

    patchPluginXml {
        sinceBuild.set("203")
        untilBuild.set("231.*")
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.languageVersion = "1.8"
        kotlinOptions.apiVersion = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.languageVersion = "1.8"
        kotlinOptions.apiVersion = "1.8"
    }
}
