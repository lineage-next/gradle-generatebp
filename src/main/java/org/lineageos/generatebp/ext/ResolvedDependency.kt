/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

import org.gradle.api.artifacts.ResolvedDependency
import org.lineageos.generatebp.models.Module
import org.lineageos.generatebp.models.ModuleIdentifier
import org.lineageos.generatebp.utils.Logger.debug

/**
 * Get the list of the transitive dependencies of this [ResolvedDependency], with this
 * [ResolvedDependency] included.
 */
fun ResolvedDependency.getRecursiveDependencies(): Set<ResolvedDependency> = buildSet {
    add(this@getRecursiveDependencies)

    children.forEach {
        addAll(it.getRecursiveDependencies())
    }
}

fun ResolvedDependency.toModuleIdentifier() = ModuleIdentifier(
    moduleGroup,
    moduleName,
    moduleVersion,
)

/**
 * Create a [Module] from a [ResolvedDependency].
 */
fun ResolvedDependency.toModule(
    targetSdk: Int,
): Module = Module(
    moduleGroup,
    moduleName,
    moduleVersion,
    dependencies = children.map {
        it.toModuleIdentifier()
    }.toSet(),
    artifact = moduleArtifacts.also {
        if (it.size > 1) {
            debug("Multiple artifacts found, using first one: $it")
        }
    }.firstOrNull()?.toArtifact(targetSdk),
    treatAsFirstLevelDependency = parents.all { parents ->
        parents.moduleArtifacts.all { it.extension == "jar" }
    } && moduleArtifacts.any {
        it.extension == "aar"
    },
)
