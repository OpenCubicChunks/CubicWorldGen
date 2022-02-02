import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.format.DateTimeFormatter

buildscript {
    System.setProperty("net.minecraftforge.gradle.check.certs", "false")
}

plugins {
    java
    `maven-publish`
    signing
    idea
    id("net.minecraftforge.gradle").version("5.1.27")
    id("org.spongepowered.mixin").version("0.7-SNAPSHOT")
    id("com.github.johnrengelman.shadow").version("7.1.2")
    id("com.github.hierynomus.license").version("0.16.1")
    id("io.github.opencubicchunks.gradle.mcGitVersion")
    id("io.github.opencubicchunks.gradle.mixingen")
}

// TODO: update to gradle 7.3+, currently blocked by https://youtrack.jetbrains.com/issue/IDEA-276738
val hasCubicChunksBuild = gradle.includedBuilds.any { it.name == "CubicChunks" || it.name == "1.12" }
val cubicChunksBuildProject = gradle.includedBuilds.find { it.name == "CubicChunks" || it.name == "1.12" }
val licenseYear: String by project
val projectName: String by project
val doRelease: String by project
val theForgeVersion: String by project
val malisisCoreVersion: String by project

group = "io.github.opencubicchunks"

base {
    archivesName.set("CubicWorldGen")
}

mcGitVersion {
    isSnapshot = true
    setCommitVersion("tags/v0.0", "0.0")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

minecraft {
    mappings("stable", "39-1.12")

    val coremods = if (hasCubicChunksBuild)
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

    runs {
        create("client") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            jvmArgs(args)
        }

        create("server") {
            workingDirectory(project.file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            jvmArgs(args)
        }
    }
}

sourceSets {
    create("optifine_dummy")
    // TODO: make this unnecessary, it's an awful hack
    create("api") {
        if (!System.getProperty("idea.sync.active", "false").toBoolean()) {
            java {
                srcDir("CubicChunksAPI/src/main/java")
            }
            resources {
                srcDir("CubicChunksAPI/src/main/resources")
            }
            compileClasspath = sourceSets.main.get().compileClasspath
        }
    }
}

val shade: Configuration by configurations.creating

configurations {
    implementation {
        extendsFrom(shade)
    }
    testImplementation {
        extendsFrom(getByName("minecraft"))
    }
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://oss.sonatype.org/content/repositories/public/")
    }
    // Note: sponge repository needs to be the second one because flow-noise is both in sponge and sonatype repository
    // but sponge has older one, and we need the newer one from sonatype
    // currently gradle seems to resolve dependencies from repositories in the order they are defined here
    maven {
        setUrl("https://repo.spongepowered.org/maven")
    }
    maven {
        setUrl("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}


dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = theForgeVersion)

    // provided by cubicchunks implementation
    implementation("org.spongepowered:mixin:0.8.1-SNAPSHOT") {
        isTransitive = false
    }

    implementation(fg.deobf("curse.maven:hackForMixinFMLAgent_deobfedDeps_-223896:2680892"))

    shade("com.flowpowered:flow-noise:1.0.1-SNAPSHOT")
    shade("blue.endless:jankson:1.2.1")

    testImplementation("junit:junit:4.11")
    testImplementation("org.hamcrest:hamcrest-junit:2.0.0.0")
    testImplementation("it.ozimov:java7-hamcrest-matchers:0.7.0")
    testImplementation("org.mockito:mockito-core:2.1.0-RC.2")
    testImplementation("org.spongepowered:launchwrappertestsuite:1.0-SNAPSHOT")
    compileOnly("io.github.opencubicchunks:cubicchunks-api:1.12.2-0.0-SNAPSHOT")

    if (hasCubicChunksBuild) {
        testImplementation("io.github.opencubicchunks:cubicchunks-api:1.12.2-0.0-SNAPSHOT")
        runtimeOnly("io.github.opencubicchunks:cubicchunks:1.12.2-0.0-SNAPSHOT")
    } else {
        testImplementation(fg.deobf("io.github.opencubicchunks:cubicchunks-api:1.12.2-0.0-SNAPSHOT"))
    }
    if (!System.getProperty("idea.sync.active", "false").toBoolean()) {
        annotationProcessor("org.spongepowered:mixin:0.8.4:processor")
    }
}

idea {
    module.apply {
        inheritOutputDirs = true
    }
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}

mixin {
    val forgeMinorVersion = theForgeVersion.split(Regex("-")).getOrNull(1)?.split(Regex("\\."))?.getOrNull(1)
            ?: throw IllegalStateException("Couldn't parse forge version")
    token("MC_FORGE", forgeMinorVersion)
}

sourceSets.main {
    ext["refMap"] = "cubicgen.refmap.json"
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

tasks {
    test {
        systemProperty("lwts.tweaker", "cubicchunks.tweaker.MixinTweakerServer")
        jvmArgs("-Dmixin.debug.verbose=true", //verbose mixin output for easier debugging of mixins
                "-Dmixin.checks.interfaces=true", //check if all interface methods are overriden in mixin
                "-Dmixin.env.remapRefMap=true")
        testLogging {
            showStandardStreams = true
        }
    }

    compileJava {
        options.isDeprecation = true
        options.compilerArgs.add("-Xlint:unchecked")
    }

    fun substituteVersion(jar: Jar) {
        val fs = FileSystems.newFileSystem(jar.archiveFile.get().asFile.toPath(), jar.javaClass.classLoader)
        var str = String(Files.readAllBytes(fs.getPath("mcmod.info")), StandardCharsets.UTF_8)
        str = str.replace("%%VERSION%%", project.version.toString())
        Files.write(fs.getPath("mcmod.info"), str.toByteArray(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING)
        fs.close()
    }

    fun Jar.setupManifest() {
        manifest.attributes(
                "Specification-Title" to project.name,
                "Specification-Version" to project.version,
                "Specification-Vendor" to "OpenCubicChunks",
                "Implementation-Title" to "${project.group}.${project.name.toLowerCase().replace(' ', '_')}",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "OpenCubicChunks",
                "Implementation-Timestamp" to DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                "FMLCorePlugin" to "io.github.opencubicchunks.cubicchunks.cubicgen.asm.coremod.CubicGenCoreMod",
                "Maven-Version" to "${project.group}:${project.base.archivesBaseName}:${project.version.toString()}:core",
                "FMLCorePluginContainsFMLMod" to "true" // workaround for mixin double-loading the mod on new forge versions
        )
    }

    fun configureShadowJar(task: ShadowJar, classifier: String) {
        task.configurations = listOf(shade)
        task.exclude("META-INF/MUMFREY*")
        task.from(sourceSets["main"].output)
        task.from(sourceSets["api"].output)
        task.exclude("log4j2.xml")
        task.relocate("blue.endless.jankson.", "io.github.opencubicchunks.cubicchunks.cubicgen.blue.endless.jankson.")
        task.archiveClassifier.set(classifier)

        task.setupManifest()
    }

    jar {
        finalizedBy("reobfJar")
        setupManifest()
        doLast {
            substituteVersion(this as Jar)
        }
    }

    shadowJar {
        configureShadowJar(this, "all")
        doLast {
            substituteVersion(this as Jar)
        }
    }

    val sourcesJar by creating(Jar::class) {
        classifier = "sources"
        from(sourceSets["main"].java.srcDirs)
    }

    val devShadowJar by creating(ShadowJar::class) {
        configureShadowJar(this, "dev")
    }

    reobf {
        create("shadowJar")
    }

    shadowJar {
        finalizedBy("reobfShadowJar")
    }

    build {
        dependsOn(shadowJar, devShadowJar, sourcesJar)
    }

    if (hasCubicChunksBuild) {
        clean {
            dependsOn(cubicChunksBuildProject!!.task(":clean"))
        }
    }
}

configurations {
    create("mainArchives")
    create("apiArchives")
}

// tasks must be before artifacts, don't change the order
artifacts {
    archives(tasks["shadowJar"])
    archives(tasks["sourcesJar"])
    archives(tasks["devShadowJar"])
}

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
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"

            setUrl(if (local) localUrl else if (doRelease.toBoolean()) releasesRepoUrl else snapshotsRepoUrl)
            if (!local) {
                credentials {
                    username = user
                    password = pass
                }
            }
        }
    }
    publications {
        create("mod", MavenPublication::class) {
            version = project.ext["mavenProjectVersion"]!!.toString()
            artifactId = base.archivesBaseName.toLowerCase()
            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
            }
            artifact(tasks["shadowJar"]) {
                classifier = ""
            }
            artifact(tasks["devShadowJar"]) {
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
    tasks["publishModPublicationToMavenRepository"].dependsOn("sourcesJar", "shadowJar", "devShadowJar")
}

signing {
    isRequired = false
    // isRequired = gradle.taskGraph.hasTask("uploadArchives")
    sign(configurations.archives.get())
}

license {
    ext["project"] = projectName
    ext["year"] = licenseYear
    exclude("**/*.info")
    exclude("**/package-info.java")
    exclude("**/*.json")
    exclude("**/*.xml")
    exclude("assets/*")
    exclude("io/github/opencubicchunks/cubicchunks/cubicgen/XxHash.java")
    header = file("HEADER.txt")
    ignoreFailures = false
    strictCheck = true
    mapping(mapOf("java" to "SLASHSTAR_STYLE"))
}