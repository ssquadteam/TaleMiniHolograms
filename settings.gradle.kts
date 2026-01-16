pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "TaleMiniHolograms"

// Composite build for local TaleLib development
includeBuild("../TaleLib")
