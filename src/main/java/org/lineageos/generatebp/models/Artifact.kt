/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import org.lineageos.generatebp.ext.equalsComparable
import org.lineageos.generatebp.ext.hashCodeOf
import java.io.File

/**
 * An artifact.
 *
 * @param file The file
 * @param fileType The type of the artifact
 * @param moduleIdentifier The module to which this artifact belongs
 * @param pom The POM of this artifact
 * @param targetSdkVersion The target SDK for this artifact
 * @param minSdkVersion The minimum SDK version for this artifact
 * @param containsJniLibs Whether the artifact includes JNIs, only valid for [Artifact.FileType.AAR]
 */
class Artifact(
    val file: File,
    val fileType: FileType,
    val moduleIdentifier: ModuleIdentifier,
    val pom: Pom,
    val targetSdkVersion: Int,
    val minSdkVersion: Int?,
    val containsJniLibs: Boolean,
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
        require(fileType == FileType.AAR || !containsJniLibs) {
            "Non-AARs cannot bundle JNIs"
        }
    }

    override fun compareTo(other: Artifact) = compareValuesBy(
        this, other,
        Artifact::file,
        Artifact::moduleIdentifier,
    )

    override fun equals(other: Any?) = equalsComparable(other)

    override fun hashCode() = hashCodeOf(
        Artifact::file,
        Artifact::moduleIdentifier,
    )

    override fun toString(): String = file.absolutePath
}
