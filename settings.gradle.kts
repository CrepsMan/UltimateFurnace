pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("gg.meza.stonecraft") version "1.15.1"
    id("dev.kikugie.stonecutter") version "0.10"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            for (it in loaders) version("$version-$it", version)
        }

        mc("1.19.2", "fabric", "forge")
        mc("1.20.0", "fabric", "forge")
        mc("1.20.1", "fabric", "forge")
        mc("1.20.2", "fabric", "forge", "neoforge")
        mc("1.20.3", "fabric", "neoforge")
        mc("1.20.4", "fabric", "forge", "neoforge")
        mc("1.20.5", "fabric", "neoforge")
        mc("1.20.6", "fabric", "forge", "neoforge")
        mc("1.21.0", "fabric", "forge", "neoforge")
        mc("1.21.1", "fabric", "forge", "neoforge")
        mc("1.21.2", "fabric", "neoforge")
        mc("1.21.3", "fabric", "forge", "neoforge")
        mc("1.21.4", "fabric", "forge", "neoforge")
        mc("1.21.5", "fabric", "forge", "neoforge")
        mc("1.21.6", "fabric", "forge", "neoforge")
        mc("1.21.7", "fabric", "forge", "neoforge")
        mc("1.21.8", "fabric", "forge", "neoforge")
        mc("1.21.9", "fabric", "forge", "neoforge")
        mc("1.21.10", "fabric", "forge", "neoforge")
        mc("1.21.11", "fabric", "forge", "neoforge")
        mc("26.1", "fabric", "forge", "neoforge")
        mc("26.1.1", "fabric", "forge", "neoforge")
        mc("26.1.2", "fabric", "forge", "neoforge")
        mc("26.2", "fabric", "forge", "neoforge")
        mc("26.3", "fabric", "forge", "neoforge")

        vcsVersion = "26.3-fabric"
    }
    create(rootProject)
}

rootProject.name = "UltimateFurnace"
