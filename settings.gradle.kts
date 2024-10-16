pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "TarsosDSP repository"
            setUrl("https://mvn.0110.be/releases")
        }
//        maven{
//            setUrl("https://repo.clojars.org/")
//        }
    }
}

rootProject.name = "SafetySteward"
include(":app")
include(":mailkit")
include(":nativetemplates")
