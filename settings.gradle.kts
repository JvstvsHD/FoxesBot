rootProject.name = "FoxesBot"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven("https://snapshots-repo.kordex.dev")
        maven("https://releases-repo.kordex.dev")
    }
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}