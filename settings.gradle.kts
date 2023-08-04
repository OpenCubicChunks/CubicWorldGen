pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "NeoForged"
            setUrl("https://maven.neoforged.net/releases")
        }
        maven {
            setUrl("https://repo.spongepowered.org/repository/maven-public/")
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("0.5.0")
}
rootProject.name = "CubicWorldGen"

// include CC as subproject if it exists
if (file("../1.12").exists()) {
    println("Including build at ../1.12/ in composite build")
    includeBuild("../1.12") {
        name = "CubicChunks"
        dependencySubstitution {
            substitute(module("io.github.opencubicchunks:cubicchunks")).using(project(":"))
        }
    }
} else if (file("../CubicChunks").exists()) {
    println("Including build at ../CubicChunks/ in composite build")
    includeBuild("../CubicChunks") {
        name = "CubicChunks"
        dependencySubstitution {
            substitute(module("io.github.opencubicchunks:cubicchunks")).using(project(":"))
        }
    }
}