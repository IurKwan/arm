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
                username = providers.gradleProperty("aliyunUsername")
                    .orElse(providers.environmentVariable("ALIYUN_MAVEN_USERNAME"))
                    .getOrElse("")
                password = providers.gradleProperty("aliyunPassword")
                    .orElse(providers.environmentVariable("ALIYUN_MAVEN_PASSWORD"))
                    .getOrElse("")
            }
            setUrl("https://packages.aliyun.com/62e88d2c1a358b4399afaf04/maven/2260669-release-lzjiju")
        }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven(url = "https://jitpack.io")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven {
            credentials {
                username = providers.gradleProperty("aliyunUsername")
                    .orElse(providers.environmentVariable("ALIYUN_MAVEN_USERNAME"))
                    .getOrElse("")
                password = providers.gradleProperty("aliyunPassword")
                    .orElse(providers.environmentVariable("ALIYUN_MAVEN_PASSWORD"))
                    .getOrElse("")
            }
            setUrl("https://packages.aliyun.com/62e88d2c1a358b4399afaf04/maven/2260669-release-lzjiju")
        }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven(url = "https://jitpack.io")
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
include(":arm-modbus")
