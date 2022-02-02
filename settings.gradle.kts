pluginManagement {
    repositories {
        maven {
            setUrl("https://maven.minecraftforge.net/")
        }
        gradlePluginPortal()
        mavenCentral()
        maven {
            setUrl("https://repo.spongepowered.org/repository/maven-public/")
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "net.minecraftforge.gradle") {
                useModule("${requested.id}:ForgeGradle:${requested.version}")
            }
            if (requested.id.id == "org.spongepowered.mixin") {
                useModule("org.spongepowered:mixingradle:${requested.version}")
            }
        }
    }
}
rootProject.name = "CubicWorldGen"

// include CC as subproject if it exists
if (file("../1.12").exists()) {
    println("Including build at ../1.12/ in composite build")
    includeBuild("../1.12") {
        dependencySubstitution {
            substitute(module("io.github.opencubicchunks:cubicchunks")).using(project(":"))
        }
    }
} else if (file("../CubicChunks").exists()) {
    println("Including build at ../CubicChunks/ in composite build")
    includeBuild("../CubicChunks") {
        dependencySubstitution {
            substitute(module("io.github.opencubicchunks:cubicchunks")).using(project(":"))
        }
    }
}