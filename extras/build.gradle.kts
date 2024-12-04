import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    id("java")
    alias(libs.plugins.runPaper)
    alias(libs.plugins.paperweight) apply true
    alias(libs.plugins.shadow) apply true

    `maven-publish`
}

runPaper.folia.registerTask()

val id = findProperty("id").toString() + "-extras"
val pluginName = findProperty("plugin_name").toString() + "Extras"
val pluginEntryPoint = findProperty("plugin_main_class_name").toString() + "Extras"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.pvphub.me/releases")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://maven.evokegames.gg/snapshots")
    maven("https://repo.viaversion.com")
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paperApi.get())

    compileOnly(rootProject)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.tab.api)
    compileOnly(libs.packet.events)
    implementation(libs.entity.lib)
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}

tasks {

    withType<ProcessResources> {
        val props = mapOf(
            "name" to pluginName,
            "main" to "${findProperty("group_name")}.${findProperty("id")}.extras.${pluginEntryPoint}",
            "author" to findProperty("plugin_author"),
            "version" to if (findProperty("include_commit_hash")
                    .toString().toBoolean()
            ) "${rootProject.version}-commit-${getCurrentCommitHash()}" else rootProject.version.toString()
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        mergeServiceFiles()
    }

    test {
        useJUnitPlatform()
    }

    assemble {
        dependsOn(reobfJar)
    }

    runServer {
        val mcVersion = libs.versions.paperApi.get().split("-")[0]
        minecraftVersion(mcVersion)

        downloadPlugins {
            hangar("ViaVersion", "5.0.1")
            hangar("ViaBackwards", "5.0.1")

            // For testing groups in config.yml
            url("https://download.luckperms.net/1556/bukkit/loader/LuckPerms-Bukkit-5.4.141.jar")
        }
    }
}

java {
//    withJavadocJar()
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
        throw IllegalStateException("Failed to retrieve the commit hash.")
    }
}