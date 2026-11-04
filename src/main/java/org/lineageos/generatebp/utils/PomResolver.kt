/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.maven.MavenModule
import org.gradle.maven.MavenPomArtifact
import org.lineageos.generatebp.models.ModuleIdentifier
import java.io.File

/**
 * Resolves POM files through Gradle instead of guessing their location in Gradle's cache, since
 * Gradle may have the parsed metadata cached while the POM file itself is missing from disk.
 *
 * @param dependencyHandler The project's [DependencyHandler] used to run the resolution queries
 */
class PomResolver(
    private val dependencyHandler: DependencyHandler,
) {
    private val cache = mutableMapOf<String, File>()

    /**
     * Get the POM file of the given [moduleIdentifier], downloading it if needed.
     */
    fun resolve(moduleIdentifier: ModuleIdentifier): File = cache.getOrPut(
        moduleIdentifier.toString()
    ) {
        val result = dependencyHandler.createArtifactResolutionQuery()
            .forModule(moduleIdentifier.group, moduleIdentifier.name, moduleIdentifier.version)
            .withArtifacts(MavenModule::class.java, MavenPomArtifact::class.java)
            .execute()

        val component = result.resolvedComponents.firstOrNull()
            ?: error("No POM found for $moduleIdentifier")

        component.getArtifacts(MavenPomArtifact::class.java)
            .filterIsInstance<ResolvedArtifactResult>()
            .firstOrNull()
            ?.file
            ?: error("No POM found for $moduleIdentifier")
    }
}
