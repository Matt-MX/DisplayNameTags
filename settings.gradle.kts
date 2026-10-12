pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("com.gradle.develocity") version("4.5.1")
}

develocity {
    if (System.getenv("CI") != null) {
        buildScan {
            publishing.onlyIf { true }
            termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
            termsOfUseAgree = "yes"
        }
    }
}