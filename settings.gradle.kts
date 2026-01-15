pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            credentials {
                username = "REDACTED_ALIYUN_USERNAME"
                password = "REDACTED_ALIYUN_PASSWORD"
            }
            setUrl("https://packages.aliyun.com/62e88d2c1a358b4399afaf04/maven/2260669-release-lzjiju")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            credentials {
                username = "REDACTED_ALIYUN_USERNAME"
                password = "REDACTED_ALIYUN_PASSWORD"
            }
            setUrl("https://packages.aliyun.com/62e88d2c1a358b4399afaf04/maven/2260669-release-lzjiju")
        }
    }
}

rootProject.name = "arm"
include(":app")
include(":arm-mvi:common")
include(":arm-mvi:compose")
include(":arm-mvi:mvi")
include(":arm-mvi:hilt")
include(":arm-mvi:navigation")
include(":arm-mvi:rxjava")
include(":arm-fragment:core")
include(":arm-fragment:fragmentation")
include(":arm-tts")
include(":arm-keyboard")
