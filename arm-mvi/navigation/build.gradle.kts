import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=io.github.iur.arm.mvi.common.InternalMavericksApi",
        )
    }
}

android {
    namespace = "io.github.iur.arm.mvi.navigation"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
//    api(project(":arm-mvi:mvi"))
    api(libs.arm.mvi.mvi)

    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)

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
//            withSourcesJar()
//            withJavadocJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.github.iur"
            artifactId = "arm-mvi-navigation"
            version = project.findProperty("arm.mvi.version") as String

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("ARM MVI Navigation")
                description.set("Navigation integration for ARM MVI library")
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
