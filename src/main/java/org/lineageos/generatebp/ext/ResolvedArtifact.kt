/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.gradle.api.artifacts.ResolvedArtifact
import org.lineageos.generatebp.models.Artifact
import org.lineageos.generatebp.models.Artifact.FileType
import org.lineageos.generatebp.utils.PomParser
import java.util.zip.ZipFile
import kotlin.reflect.safeCast

fun ResolvedArtifact.toArtifact(defaultTargetSdkVersion: Int): Artifact {
    val moduleIdentifier = moduleVersion.id.toModuleIdentifier()

    val file = file

    val fileType = extension?.let {
        FileType.fromExtension(it)
    } ?: error("Unknown artifact extension $extension for artifact ${file.path}")

    val pom = PomParser.fromArtifact(file, moduleIdentifier)

    var targetSdkVersion = defaultTargetSdkVersion
    var minSdkVersion: Int? = null
    var containsJniLibs = false

    if (extension == "aar") {
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
                    containsJniLibs = true
                }
            }
        }
    }

    return Artifact(
        file = file,
        fileType = fileType,
        moduleIdentifier = moduleIdentifier,
        pom = pom,
        targetSdkVersion = targetSdkVersion,
        minSdkVersion = minSdkVersion,
        containsJniLibs = containsJniLibs,
    )
}
