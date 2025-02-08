rootProject.name = "IdeaPlugin"

includeBuild("code-to-uml") {
    dependencySubstitution {
        substitute(module("org.tera201:code-to-uml"))
            .using(project(":"))
    }
}