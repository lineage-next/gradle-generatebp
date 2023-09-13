/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

group = "org.lineageos"
version = "1.0"

plugins {
    `kotlin-dsl`
    `maven-publish`
}

repositories {
    mavenCentral()
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
