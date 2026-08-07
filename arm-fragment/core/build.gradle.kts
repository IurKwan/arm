plugins {
    alias(libs.plugins.android.library)
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    namespace = "io.github.iur.arm.fragment.core"
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
}

dependencies {
    implementation(libs.timber)

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
            artifactId = "arm-fragment-core"
            version = "0.0.2"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("ARM Fragment Core")
                description.set("Core library for ARM fragment management")
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
