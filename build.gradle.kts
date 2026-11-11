plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

val id = property("id").toString()
val pluginName = property("plugin_name").toString()
val pluginMain = "${property("group_name")}.$id.${property("plugin_main_class_name")}"
val pluginAuthor = property("plugin_author").toString()
val includeCommitHash = property("include_commit_hash").toString().toBoolean()
val groupName = property("group_name").toString()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://jitpack.io") {
        content { includeGroup("com.github.NEZNAMY") }
    }
}

dependencies {
    compileOnly(libs.paper)
    compileOnly(libs.packetevents)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.tab)
    compileOnly(libs.skinsrestorer)
    compileOnly(libs.floodgate)
    compileOnly(libs.fastutil)

    implementation(libs.bstats)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    withSourcesJar()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

sourceSets["main"].resources.srcDir("src/resources/")

val commitHash: Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "--short=7", "HEAD")
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim().ifEmpty { "unknown" } }

val pluginVersion: Provider<String> = commitHash.map { hash ->
    if (includeCommitHash) "${project.version}-commit-$hash"
    else project.version.toString()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
    }

    processResources {
        val props = mapOf(
            "name" to pluginName,
            "main" to pluginMain,
            "author" to pluginAuthor,
            "version" to pluginVersion.get(),
        )
        inputs.properties(props)
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveFileName = "${rootProject.name}-${project.version}.jar"
        archiveClassifier = ""
        mergeServiceFiles()
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        relocate("org.bstats", "com.mattmx.nametags.shaded.bstats")
    }

    assemble {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    runServer {
        minecraftVersion(libs.versions.paper.get().substringBefore("-"))
        downloadPlugins {
            modrinth("packetevents", "${libs.versions.packetevents.get()}+spigot")
            modrinth("luckperms", "v5.5.17-bukkit")
        }
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}

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
            groupId = groupName
            artifactId = id
            version = project.version.toString()
        }
    }
}
