/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.artifacts.ResolvedArtifact
import org.lineageos.generatebp.utils.Logger.info
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
 * @param hasJNIs Whether the artifact includes JNIs, only valid for [Artifact.FileType.AAR]
 */
data class Artifact(
    val file: File,
    val fileType: FileType,
    private val module: Module,
    val licenses: List<License>,
    val organizationName: String?,
    val developersNames: List<String>,
    val inceptionYear: Int?,
    val targetSdkVersion: Int,
    val minSdkVersion: Int?,
    val dependencies: List<Module>,
    val hasJNIs: Boolean,
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

    init {
        require(fileType == FileType.AAR || !hasJNIs) {
            "Non-AARs cannot bundle JNIs"
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
        result = 31 * result + hasJNIs.hashCode()
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
        { it.hasJNIs },
    )

    val reuseCopyrightFileContent by lazy {
        if (licenses.isEmpty()) {
            info("No license found for module ${module.gradleName}")
        }

        val copyrights = organizationName?.let {
            listOf(it)
        } ?: developersNames

        if (copyrights.isEmpty()) {
            info("No copyright found for ${module.gradleName}")
        }

        ReuseUtils.generateReuseCopyrightContent(licenses, copyrights, inceptionYear)
    }

    companion object {
        fun fromResolvedArtifact(it: ResolvedArtifact, defaultTargetSdkVersion: Int): Artifact {
            val module = Module.fromModuleVersionIdentifier(it.moduleVersion.id)

            val file = it.file

            val fileType = it.extension?.let { FileType.fromExtension(it) }
                ?: throw Exception(
                    "Unknown artifact extension ${it.extension} for artifact ${file.path}"
                )

            val pom = POM.fromArtifact(file, module)

            var targetSdkVersion = defaultTargetSdkVersion
            var minSdkVersion: Int? = null
            var hasJNIs = false

            if (it.extension == "aar") {
                ZipFile(file).use {
                    for (zipEntry in it.entries().asIterator()) {
                        if (zipEntry.name == "AndroidManifest.xml") {
                            // Parse AndroidManifest.xml for AARs
                            it.getInputStream(zipEntry)?.use { inputStream ->
                                val androidManifest = XmlParser().parse(inputStream)

                                val usesSdk =
                                    (androidManifest["uses-sdk"] as NodeList).first() as Node
                                targetSdkVersion = Int::class.safeCast(
                                    usesSdk.get("@targetSdkVersion")
                                ) ?: targetSdkVersion
                                minSdkVersion = Int::class.safeCast(
                                    usesSdk.get("@minSdkVersion")
                                ) ?: minSdkVersion
                            }
                        } else if (zipEntry.name.startsWith("jni/")) {
                            hasJNIs = true
                        }
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
                pom.dependencies,
                hasJNIs,
            )
        }
    }
}
