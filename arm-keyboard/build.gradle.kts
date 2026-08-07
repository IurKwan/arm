plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

android {
    namespace = "io.github.iur.arm.keyboard"
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
}

afterEvaluate {
    tasks.register<Jar>("androidSourcesJar") {
        archiveClassifier.set("sources")

        // Android 已统一 Java/Kotlin 源集，这里包含 Java + Kotlin 文件夹
        from(android.sourceSets["main"].java.srcDirs)

        // 额外包含 manifest 与 res（可选）
        from("src/main/aidl")
        from("src/main/manifest")
    }

    artifacts {
        add("archives", tasks.named("androidSourcesJar"))
    }
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
            artifactId = "arm-keyboard"
            version = "1.0.7"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("ARM Fragment TTS")
                description.set("Fragment fragmentation library for ARM project")
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
                username = providers.systemProperty("aliyunUsername")
                    .orElse(providers.environmentVariable("ALIYUN_MAVEN_USERNAME"))
                    .getOrElse("")
                password = providers.systemProperty("aliyunPassword")
                    .orElse(providers.environmentVariable("ALIYUN_MAVEN_PASSWORD"))
                    .getOrElse("")
            }
        }
    }
}
