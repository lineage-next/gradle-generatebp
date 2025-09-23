/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.lineageos.generatebp.models.ModuleIdentifier

/**
 * Convert to a [ModuleIdentifier].
 */
fun ModuleVersionIdentifier.toModuleIdentifier() = ModuleIdentifier(
    group,
    name,
    version,
)
