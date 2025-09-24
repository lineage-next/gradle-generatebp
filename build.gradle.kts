/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

group = "org.lineageos"
version = "1.29"

plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("generateBp") {
            id = "org.lineageos.generatebp"
            implementationClass = "org.lineageos.generatebp.GenerateBpPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            pom {
                name.set(rootProject.name)
                url.set("https://github.com/lineage-next/gradle-generatebp")

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        name.set("The LineageOS Project")
                        url.set("https://lineageos.org")
                    }
                }
            }
        }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
