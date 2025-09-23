/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

/**
 * A Gradle module.
 *
 * @param dependencies The dependencies of the module
 * @param artifact The artifact of this module
 * @param treatAsFirstLevelDependency AAR with only JAR parents. Whether this module is a dependency
 *   of modules without a AAR artifact and this module includes a AAR artifact. This is needed to
 *   include non-code assets for this module since a JAR target can only include Java libraries
 */
class Module(
    group: String,
    name: String,
    version: String,
    val dependencies: Set<ModuleIdentifier>,
    val artifact: Artifact?,
    val treatAsFirstLevelDependency: Boolean,
) : ModuleIdentifier(group, name, version)
