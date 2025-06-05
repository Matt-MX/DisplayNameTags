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
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://maven.evokegames.gg/snapshots")

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

    // Downloaded during runtime
    compileOnly(libs.caffeine)

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
        val mcVersion = libs.versions.paper.get().split("-")[0]
        minecraftVersion(mcVersion)

        downloadPlugins {
            hangar("ViaVersion", "5.3.2")
            hangar("ViaBackwards", "5.3.2")
            modrinth("packetevents","2HJtPM2W")

            // For testing groups in config.yml
            modrinth("luckperms", "v5.4.145-bukkit")
        }

        jvmArgs("-Dcom.mojang.eula.agree=true")
    }

    runPaper.folia.registerTask()
}

java {
    //withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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