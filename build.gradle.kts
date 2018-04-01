import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension

version = "unspecified"

apply {
    plugin("java")
}

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}