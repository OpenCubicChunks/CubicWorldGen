import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.user.ReobfMappingType
import net.minecraftforge.gradle.user.patcherUser.forge.ForgeExtension
import nl.javadude.gradle.plugins.license.LicensePlugin
import org.gradle.api.internal.HasConvention
import org.spongepowered.asm.gradle.plugins.MixinGradlePlugin

// Gradle repositories and dependencies
buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            setUrl("http://files.minecraftforge.net/maven")
        }
        maven {
            setUrl("http://repo.spongepowered.org/maven")
        }
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }

    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.6-SNAPSHOT")
        classpath("com.github.jengelman.gradle.plugins:shadow:2.0.4")
        classpath("gradle.plugin.nl.javadude.gradle.plugins:license-gradle-plugin:0.14.0")
        classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
    }
}

plugins {
    base
    java
    maven
    `maven-publish`
    idea
    id("io.github.opencubicchunks.gradle.fg2fixed")
    id("io.github.opencubicchunks.gradle.mixingen")
    id("io.github.opencubicchunks.gradle.remapper")
    id("io.github.opencubicchunks.gradle.mcGitVersion")
}

apply {
    plugin<ShadowPlugin>()
    plugin<MixinGradlePlugin>()
    plugin<LicensePlugin>()
}

mcGitVersion {
    isSnapshot = true
}

// TODO: Reduce duplication of buildscript code between CC projects?
group = "io.github.opencubicchunks"

if (gradle.includedBuilds.any { it.name == "CubicChunks" }) {
    tasks["clean"].dependsOn(gradle.includedBuild("CubicChunksAPI").task(":clean"))
    tasks["clean"].dependsOn(gradle.includedBuild("CubicChunks").task(":clean"))
}

val theForgeVersion: String by project
val versionSuffix: String  by project
val theMappingsVersion: String by project

val licenseYear: String by project
val projectName: String by project

val malisisCoreVersion: String by project
val malisisCoreMinVersion: String by project

val release: String by project

val sourceSets = the<JavaPluginConvention>().sourceSets
val mainSourceSet = sourceSets["main"]

val minecraft = the<ForgeExtension>()

val shadowJar: ShadowJar by tasks
val build by tasks


base {
    archivesBaseName = "CubicWorldGen"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

minecraft {
    version = theForgeVersion
    runDir = "run"
    mappings = theMappingsVersion

    replace("@@MALISIS_VERSION@@", malisisCoreMinVersion)
    replaceIn("io/github/opencubicchunks/cubicchunks/cubicgen/CustomCubicMod.java")

    val coremods = if (gradle.includedBuilds.any { it.name == "CubicChunks" })
        "-Dfml.coreMods.load=io.github.opencubicchunks.cubicchunks.cubicgen.asm.coremod.CubicGenCoreMod,io.github.opencubicchunks.cubicchunks.core.asm.coremod.CubicChunksCoreMod"
        else "-Dfml.coreMods.load=io.github.opencubicchunks.cubicchunks.cubicgen.asm.coremod.CubicGenCoreMod" //the core mod class, needed for mixins
    val args = listOf(
            coremods,
            "-Dmixin.env.compatLevel=JAVA_8", //needed to use java 8 when using mixins
            "-Dmixin.debug.verbose=true", //verbose mixin output for easier debugging of mixins
            "-Dmixin.debug.export=true", //export classes from mixin to runDirectory/.mixin.out
            "-Dcubicchunks.debug=true", //various debug options of cubic chunks mod. Adds items that are not normally there!
            "-XX:-OmitStackTraceInFastThrow", //without this sometimes you end up with exception with empty stacktrace
            "-Dmixin.checks.interfaces=true", //check if all interface methods are overriden in mixin
            "-Dfml.noGrab=false", //change to disable Minecraft taking control over mouse
            "-ea", //enable assertions
            "-da:io.netty..." //disable netty assertions because they sometimes fail
    )

    clientJvmArgs.addAll(args)
    serverJvmArgs.addAll(args)
}

license {
    val ext = (this as HasConvention).convention.extraProperties
    ext["project"] = projectName
    ext["year"] = licenseYear
    exclude("**/*.info")
    exclude("**/package-info.java")
    exclude("**/*.json")
    exclude("**/*.xml")
    exclude("assets/*")
    header = file("HEADER.txt")
    ignoreFailures = false
    strictCheck = true
    mapping(mapOf("java" to "SLASHSTAR_STYLE"))
}

mixin {
    add(sourceSets["main"], "cubicgen.refmap.json")
}

mixinGen {
    filePattern = "%s.mixins.json"
    defaultRefmap = "cubicgen.refmap.json"
    defaultCompatibilityLevel = "JAVA_8"
    defaultMinVersion = "0.7.10"

    config("cubicgen") {
        required = true
        packageName = "io.github.opencubicchunks.cubicchunks.cubicgen.asm.mixin"
        conformVisibility = true
        injectorsDefaultRequire = 1
        configurationPlugin = "io.github.opencubicchunks.cubicchunks.cubicgen.asm.CubicGenMixinConfig"
    }
}

repositories {
    mavenCentral()
    maven { setUrl("https://oss.sonatype.org/content/repositories/public/") }
    // Note: sponge repository needs to be the second one because flow-noise is both in sponge and sonatype repository
    // but sponge has older one, and we need the newer one from sonatype
    // currently gradle seems to resolve dependencies from repositories in the order they are defined here
    maven { setUrl("http://repo.spongepowered.org/maven") }
    maven { setUrl("https://minecraft.curseforge.com/api/maven/") }
    maven { setUrl("https://repo.elytradev.com") } // jankson snapshot
}

val deobfCompile by configurations
val compile by configurations
val testCompile by configurations
val forgeGradleGradleStart by configurations
val forgeGradleMcDeps by configurations
val runtime by configurations
val implementation by configurations

val shade by configurations.creating
compile.extendsFrom(shade)
// needed for tests to work
testCompile.extendsFrom(forgeGradleGradleStart)
testCompile.extendsFrom(forgeGradleMcDeps)

dependencies {

    //deobfCompile("io.github.opencubicchunks:cubicchunks:1.12.2-0.0-SNAPSHOT")
    // provided by cubicchunks implementation
    compile("org.spongepowered:mixin:0.7.10-SNAPSHOT") {
        isTransitive = false
    }

    deobfCompile("malisiscore:malisiscore:1.12.2:$malisisCoreVersion") {
        isTransitive = false
    }

    shade("com.flowpowered:flow-noise:1.0.1-SNAPSHOT")
    shade("blue.endless:jankson:1.2.0-beta.2-61")

    testCompile("junit:junit:4.11")
    testCompile("org.hamcrest:hamcrest-junit:2.0.0.0")
    testCompile("it.ozimov:java7-hamcrest-matchers:0.7.0")
    testCompile("org.mockito:mockito-core:2.1.0-RC.2")
    testCompile("org.spongepowered:launchwrappertestsuite:1.0-SNAPSHOT")

    if (gradle.includedBuilds.any { it.name == "CubicChunks" }) {
        implementation("io.github.opencubicchunks:cubicchunks-api:1.12.2-0.0-SNAPSHOT")
        runtime("io.github.opencubicchunks:cubicchunks:1.12.2-0.0-SNAPSHOT")
    } else {
        deobfCompile("io.github.opencubicchunks:cubicchunks-api:1.12.2-0.0-SNAPSHOT")
    }
}

fun Jar.setupManifest() {
    manifest {
        attributes["FMLCorePluginContainsFMLMod"] = "true"
        attributes["FMLCorePlugin"] = "io.github.opencubicchunks.cubicchunks.cubicgen.asm.coremod.CubicGenCoreMod"
        attributes["ForceLoadAsMod"] = "true"
        attributes["Maven-Version"] = "${project.group}:${project.base.archivesBaseName}:${project.version.toString()}:core"
    }
}

fun configureShadowJar(task: ShadowJar, classifier: String) {
    task.configurations = listOf(shade)
    task.exclude("META-INF/MUMFREY*")
    task.from(sourceSets["main"].output)
    task.from(sourceSets["api"].output)
    task.exclude("log4j2.xml")
    task.relocate("blue.endless.jankson.", "io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.")
    task.classifier = classifier

    task.setupManifest()
}

shadowJar.apply { configureShadowJar(this, "all") }

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].java.srcDirs)
}
val devShadowJar by tasks.creating(ShadowJar::class) {
    configureShadowJar(this, "dev")
}

reobf {
    create("shadowJar").apply {
        mappingType = ReobfMappingType.SEARGE
    }
}
build.dependsOn("reobfShadowJar", devShadowJar)

publishing {
    repositories {
        maven {
            val user = (project.properties["sonatypeUsername"] ?: System.getenv("sonatypeUsername")) as String?
            val pass = (project.properties["sonatypePassword"] ?: System.getenv("sonatypePassword")) as String?
            val local = user == null || pass == null
            if (local) {
                logger.warn("Username or password not set, publishing to local repository in build/mvnrepo/")
            }
            val localUrl = "$buildDir/mvnrepo"
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl =  "https://oss.sonatype.org/content/repositories/snapshots"

            setUrl(if (local) localUrl else if (release.toBoolean()) releasesRepoUrl else snapshotsRepoUrl)
            if (!local) {
                credentials {
                    username = user
                    password = pass
                }
            }
        }
    }
    (publications) {
        "mod"(MavenPublication::class) {
            version = project.ext["mavenProjectVersion"]!!.toString()
            artifactId = base.archivesBaseName.toLowerCase()
            from(components["java"])
            artifacts.clear()
            artifact(sourcesJar) {
                classifier = "sources"
            }
            artifact(shadowJar) {
                classifier = ""
            }
            artifact(devShadowJar) {
                classifier = "dev"
            }
            pom {
                name.set(projectName)
                description.set("CubicChunks customizable world generator")
                packaging = "jar"
                url.set("https://github.com/OpenCubicChunks/CubicWorldGen")
                scm {
                    connection.set("scm:git:git://github.com/OpenCubicChunks/CubicWorldGen.git")
                    developerConnection.set("scm:git:ssh://git@github.com:OpenCubicChunks/CubicWorldGen.git")
                    url.set("https://github.com/OpenCubicChunks/CubicWorldGen")
                }

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("http://www.tldrlegal.com/license/mit-license")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("Barteks2x")
                        name.set("Barteks2x")
                    }
                    // TODO: add more developers
                }

                issueManagement {
                    system.set("github")
                    url.set("https://github.com/OpenCubicChunks/CubicWorldGen/issues")
                }
            }
        }
    }
}
afterEvaluate {
    tasks["publishModPublicationToMavenRepository"].dependsOn("reobfShadowJar")
}
artifacts {
    withGroovyBuilder {
        "archives"(shadowJar, sourcesJar, devShadowJar)
    }
}
tasks {
    "test"(Test::class) {
        systemProperty("lwts.tweaker", "io.github.opencubicchunks.cubicchunks.tweaker.MixinTweakerServer")
        jvmArgs("-Dmixin.debug.verbose=true", //verbose mixin output for easier debugging of mixins
                "-Dmixin.checks.interfaces=true", //check if all interface methods are overriden in mixin
                "-Dmixin.env.remapRefMap=true")
        testLogging {
            showStandardStreams = true
        }
    }

    "processResources"(ProcessResources::class) {
        // this will ensure that this task is redone when the versions change.
        inputs.property("version", project.version.toString())
        inputs.property("mcversion", minecraft.version)

        // replace stuff in mcmod.info, nothing else
        from(mainSourceSet.resources.srcDirs) {
            include("mcmod.info")

            // replace version and mcversion
            expand(mapOf("version" to project.version.toString(), "mcversion" to minecraft.version))
        }

        // copy everything else, thats not the mcmod.info
        from(mainSourceSet.resources.srcDirs) {
            exclude("mcmod.info")
        }
    }
}

fun extractForgeMinorVersion(): String {
    // version format: MC_VERSION-MAJOR.MINOR.?.BUILD
    return theForgeVersion.split(Regex("-")).getOrNull(1)?.split(Regex("\\."))?.getOrNull(1)
            ?: throw RuntimeException("Invalid forge version format: $theForgeVersion")
}
