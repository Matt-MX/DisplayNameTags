import org.gradle.api.attributes.java.TargetJvmVersion
import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    id("java")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)

    `maven-publish`
}

val id = findProperty("id").toString()
val pluginName = findProperty("plugin_name")

repositories {
    maven("https://maven.pvphub.me/releases")
    maven("https://repo.viaversion.com")
    maven("https://repo.codemc.org/repository/maven-public/") {
        name = "codemc"
    }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://maven.pvphub.me/tofaa")

    mavenLocal()
    mavenCentral()
    // Always make sure to put JitPack at the end of the list for performance reasons
    maven("https://jitpack.io")
}

dependencies {
    // Provided
    compileOnly(libs.paper)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.tab)
    compileOnly(libs.packetevents)
    compileOnly(libs.skinsrestorer)

    // Shaded
    implementation(libs.entitylib)
    implementation(libs.bstats)

    testImplementation(libs.junit.jupiter)
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveFileName = "${rootProject.name}-${version}.jar"
        archiveClassifier = null

        mergeServiceFiles()
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }

        relocate("me.tofaa.entitylib", "com.mattmx.nametags.shaded.entitylib")
        relocate("org.bstats", "com.mattmx.nametags.shaded.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }

    withType<ProcessResources> {
        val props = mapOf(
            "name" to pluginName,
            "main" to "${findProperty("group_name")}.${id}.${findProperty("plugin_main_class_name")}",
            "author" to findProperty("plugin_author"),
            "version" to if (findProperty("include_commit_hash")
                    .toString().toBoolean()
            ) "${rootProject.version}-commit-${getCurrentCommitHash()}" else rootProject.version.toString(),
            "loader" to findProperty("loader")
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("*plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    runServer {
        // Override with -PrunMcVersion=26.2 to test against a different release.
        minecraftVersion(
            providers.gradleProperty("runMcVersion").orNull
                ?: libs.versions.paper.get().substringBefore(".build.")
        )

        downloadPlugins {
            hangar("ViaVersion", "5.12.0")
            hangar("ViaBackwards", "5.12.0")
            // No PacketEvents RELEASE supports 26.3 yet, and injecting 2.13.0 here crashes the
            // server on startup. Put a 26.3-capable build in run/plugins/ instead.
            // modrinth("packetevents", "h0ncTpUP")

            // For testing groups in config.yml
            modrinth("luckperms", "v5.5.71-bukkit")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    runPaper.folia.registerTask {
        minecraftVersion(
            providers.gradleProperty("runFoliaMcVersion").orNull ?: libs.versions.paper.get().substringBefore(".build.")
        )

        downloadPlugins {
            modrinth("luckperms", "v5.5.71-bukkit")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}

java {
    //withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

listOf(configurations.compileClasspath, configurations.testCompileClasspath).forEach { config ->
    config.configure {
        attributes {
            attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
        }
    }
}

sourceSets["main"].resources.srcDir("src/resources/")

publishing {
    repositories {
        maven {
            name = "pvphub-releases"
            url = uri("https://maven.pvphub.me/releases")
            credentials {
                username = System.getenv("PVPHUB_MAVEN_USERNAME")
                password = System.getenv("PVPHUB_MAVEN_SECRET")
            }
        }
    }
    publications {
        create<MavenPublication>(id) {
            from(components["java"])
            groupId = group.toString()
            artifactId = id
            version = rootProject.version.toString()
        }
    }
}

fun getCurrentCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "HEAD").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val commitHash = reader.readLine()
    reader.close()
    process.waitFor()
    if (process.exitValue() == 0) {
        return commitHash?.substring(0, 7) ?: ""
    } else {
        return "unknown"
    }
}