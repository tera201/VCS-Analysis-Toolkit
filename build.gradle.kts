plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/tera201/JavaFXUMLCityBuilder")
        credentials {
            username = project.properties["username"].toString()
            password = project.properties["password"].toString()
        }
        }
    maven {
        url = uri("https://maven.pkg.github.com/tera201/JavaFXUMLGraph")
        credentials {
            username = project.properties["username"].toString()
            password = project.properties["password"].toString()
        }

    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "lib", "include" to listOf("*.jar"))))
    implementation("org.example:javafx-uml-city-builder:latest.integration")
    implementation("org.example:javafx-uml-graph-idea:latest.integration")
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2021.2")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.javafx:1.0.3"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
