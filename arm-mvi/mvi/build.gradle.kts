import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "io.github.iur.arm.mvi.mvi"
    compileSdk = 36
    resourcePrefix = "mvi_"

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=io.github.iur.arm.mvi.common.InternalMavericksApi",
            "-opt-in=io.github.iur.arm.mvi.common.ExperimentalMavericksApi",
        )
    }
}

dependencies {
    api(libs.arm.mvi.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.github.iur"
            artifactId = "arm-mvi-mvi"
            version = project.findProperty("arm.mvi.version") as String

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("ARM MVI Core")
                description.set("Core MVI library for ARM project")
                url.set("https://github.com/your-repo/arm")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("developer")
                        name.set("Iur")
                        email.set("guanzhirui@outlook.com")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "aliyun"
            url =
                uri("https://packages.aliyun.com/62e88d2c1a358b4399afaf04/maven/2260669-release-lzjiju")
            credentials {
                username = "REDACTED_ALIYUN_USERNAME"
                password = "REDACTED_ALIYUN_PASSWORD"
            }
        }
    }
}
