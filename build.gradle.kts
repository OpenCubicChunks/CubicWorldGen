import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.ReobfTaskFactory
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import org.spongepowered.asm.gradle.plugins.MixinExtension

version = "unspecified"

val malisisCoreVersion by project
val sourceSets = the<JavaPluginConvention>().sourceSets

repositories {
    mavenCentral()
}

configure<MixinExtension> {
    add(sourceSets["main"], "cubicgen.refmap.json")
}
configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val deobfCompile by configurations
val compile by configurations

val shade by configurations.creating
compile.extendsFrom(shade)

val shadeJarAtrifact by configurations.creating

dependencies {
    deobfCompile("net.malisis:malisiscore:$malisisCoreVersion") {
        isTransitive = false
    }

    shade("com.flowpowered:flow-noise:1.0.1-SNAPSHOT")
}

fun Jar.setupManifest() {
    manifest {
        attributes["FMLAT"] = "cubicchunks_cubicgen_at.cfg"
        attributes["FMLCorePlugin"] = "io.github.opencubicchunks.cubicchunks.cubicgen.asm.CubicGenCoreMod"
        attributes["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        attributes["TweakOrder"] = "0"
        attributes["ForceLoadAsMod"] = "true"
        attributes["Maven-Version"] = "${project.group}:${project.base.archivesBaseName}:${project.version}:core"
    }
}
tasks {
    val shadeJar by tasks.creating(ShadowJar::class) {
        from(sourceSets["main"].output)
        configurations = listOf(shade)
        setupManifest()
        classifier = "all"
    }
    configure<NamedDomainObjectContainer<ReobfTaskFactory.ReobfTaskWrapper>> {
        create("shadeJar").apply {
            mappingType = ReobfMappingType.SEARGE
        }
    }
    "assemble"().dependsOn("reobfShadeJar")
}
artifacts {
    withGroovyBuilder {
        "shadeJarAtrifact"(tasks["shadeJar"])
    }
}