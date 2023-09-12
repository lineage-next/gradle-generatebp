/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.artifacts.ResolvedArtifact
import java.io.File
import java.util.zip.ZipFile
import kotlin.reflect.safeCast

/**
 * An artifact.
 * @param file The file
 * @param fileType The type of the artifact
 * @param module The module to which this artifact belongs
 * @param targetSdkVersion The target SDK for this artifact
 * @param minSdkVersion The minimum SDK version for this artifact
 * @param dependencies The dependencies of this artifact, each dependency shouldn't provide a valid
 *                     version
 */
data class Artifact(
    val file: File,
    val fileType: FileType,
    val module: Module,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val dependencies: Set<Module>,
) : Comparable<Artifact> {
    enum class FileType(val extension: String) {
        AAR("aar"),
        JAR("jar");

        companion object {
            fun fromExtension(extension: String) = values().firstOrNull {
                extension == it.extension
            }
        }
    }

    override fun equals(other: Any?) = Artifact::class.safeCast(other)?.let {
        compareTo(it) == 0
    } ?: false

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + module.hashCode()
        result = 31 * result + targetSdkVersion.hashCode()
        result = 31 * result + minSdkVersion.hashCode()
        result = 31 * result + dependencies.hashCode()
        return result
    }

    override fun compareTo(other: Artifact) = compareValuesBy(
        this, other,
        { it.file },
        { it.fileType },
        { it.module },
        { it.targetSdkVersion },
        { it.minSdkVersion },
        { it.dependencies.hashCode() },
    )

    companion object {
        private const val DEFAULT_MIN_SDK_VERSION = 14

        fun fromResolvedArtifact(it: ResolvedArtifact, defaultTargetSdkVersion: Int): Artifact {
            val file = it.file

            val fileType = it.extension?.let { FileType.fromExtension(it) }
                ?: throw Exception("Unknown artifact extension ${it.extension}")

            // Parse dependencies
            val dependencies = file.parentFile.parentFile.walk().filter {
                it.extension == "pom"
            }.map {
                mutableListOf<Module>().apply {
                    val pom = XmlParser().parse(it)
                    val dependencies = (pom["dependencies"] as NodeList).firstOrNull() as Node?

                    dependencies?.children()?.forEach { node ->
                        val dependency = node as Node

                        add(
                            Module(
                                (dependency.get("groupId") as NodeList).text(),
                                (dependency.get("artifactId") as NodeList).text()
                            )
                        )
                    }
                }
            }.flatten().toSet()

            var targetSdkVersion = defaultTargetSdkVersion
            var minSdkVersion = DEFAULT_MIN_SDK_VERSION

            // Parse AndroidManifest.xml for AARs
            if (it.extension == "aar") {
                ZipFile(file).use {
                    for (zipEntry in it.entries().asIterator()) {
                        if (zipEntry.name != "/AndroidManifest.xml") {
                            continue
                        }

                        it.getInputStream(zipEntry)?.use { inputStream ->
                            val androidManifest = XmlParser().parse(inputStream)

                            val usesSdk = (androidManifest["uses-sdk"] as NodeList).first() as Node
                            targetSdkVersion = Int::class.safeCast(
                                usesSdk.get("@targetSdkVersion")
                            ) ?: targetSdkVersion
                            minSdkVersion = Int::class.safeCast(
                                usesSdk.get("@minSdkVersion")
                            ) ?: minSdkVersion
                        }

                        break
                    }
                }
            }

            return Artifact(
                file,
                fileType,
                Module.fromModuleVersionIdentifier(it.moduleVersion.id),
                targetSdkVersion,
                minSdkVersion,
                dependencies
            )
        }
    }
}
