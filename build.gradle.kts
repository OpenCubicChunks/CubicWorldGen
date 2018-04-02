import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension

version = "unspecified"

val malisisCoreVersion by project

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val deobfCompile by configurations

dependencies {
    deobfCompile("net.malisis:malisiscore:$malisisCoreVersion") {
        isTransitive = false
    }

    compile("com.flowpowered:flow-noise:1.0.1-SNAPSHOT")
}