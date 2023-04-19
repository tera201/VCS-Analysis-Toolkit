plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
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
    implementation("org.example:javafx-uml-graph-idea:1.1-SNAPSHOT")
    implementation("org.example:cpp-to-uml:1.1-SNAPSHOT")
    implementation("org.example:swrminer:1.1-SNAPSHOT")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
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

    patchPluginXml {
        sinceBuild.set("203")
        untilBuild.set("231.*")
    }

//    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//    }
//
//    publishPlugin {
//        token.set(System.getenv("PUBLISH_TOKEN"))
//    }
}
