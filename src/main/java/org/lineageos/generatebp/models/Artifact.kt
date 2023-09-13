/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.artifacts.ResolvedArtifact
import org.lineageos.generatebp.utils.Logger
import org.lineageos.generatebp.utils.POM
import org.lineageos.generatebp.utils.ReuseUtils
import java.io.File
import java.util.zip.ZipFile
import kotlin.reflect.safeCast

/**
 * An artifact.
 * @param file The file
 * @param fileType The type of the artifact
 * @param module The module to which this artifact belongs
 * @param licenses The licenses of this artifact
 * @param organizationName The name of the organization that manages this artifact
 * @param developersNames The developers' names of this artifact
 * @param inceptionYear The initial release of the module
 * @param targetSdkVersion The target SDK for this artifact
 * @param minSdkVersion The minimum SDK version for this artifact
 * @param dependencies The dependencies of this artifact, each dependency shouldn't provide a valid
 *                     version
 */
data class Artifact(
    val file: File,
    val fileType: FileType,
    val module: Module,
    val licenses: List<License>,
    val organizationName: String?,
    val developersNames: List<String>,
    val inceptionYear: Int?,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val dependencies: List<Module>,
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
        result = 31 * result + licenses.hashCode()
        result = 31 * result + organizationName.hashCode()
        result = 31 * result + developersNames.hashCode()
        result = 31 * result + inceptionYear.hashCode()
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
        { it.licenses.hashCode() },
        { it.organizationName },
        { it.developersNames.hashCode() },
        { it.inceptionYear },
        { it.targetSdkVersion },
        { it.minSdkVersion },
        { it.dependencies.hashCode() },
    )

    val reuseCopyrightFileContent by lazy {
        require(licenses.isNotEmpty()) {
            "Licenses not found for module ${module.gradleName}"
        }

        if (licenses.size > 1) {
            Logger.info(
                "More than one license found for ${module.gradleName}, picking the first one found"
            )
        }

        val license = licenses.first()

        val copyrights = organizationName?.let {
            listOf(it)
        } ?: developersNames

        ReuseUtils.generateReuseCopyrightContent(license, copyrights, inceptionYear)
    }

    companion object {
        private const val DEFAULT_MIN_SDK_VERSION = 14

        fun fromResolvedArtifact(it: ResolvedArtifact, defaultTargetSdkVersion: Int): Artifact {
            val module = Module.fromModuleVersionIdentifier(it.moduleVersion.id)

            val file = it.file

            val fileType = it.extension?.let { FileType.fromExtension(it) }
                ?: throw Exception(
                    "Unknown artifact extension ${it.extension} for artifact ${file.path}"
                )

            val pom = POM.fromArtifact(file, module)

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
                module,
                pom.licenses,
                pom.organizationName,
                pom.developersNames,
                pom.inceptionYear,
                targetSdkVersion,
                minSdkVersion,
                pom.dependencies
            )
        }
    }
}
