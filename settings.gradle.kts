pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven ("https://jitpack.io")
    }
}
rootProject.name = "Music Player"
include ("app")
include(":core:resource")
include(":material-theme")
