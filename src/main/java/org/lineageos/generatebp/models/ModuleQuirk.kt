/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

/**
 * How a [ModuleIdentifier] should be treated in the AOSP environment.
 */
enum class ModuleQuirk {
    /**
     * Pretend the module doesn't exist at all. This can be used for BOM dependencies.
     */
    IGNORE,

    /**
     * The module should be discarded, but not its dependencies, which are used by its dependants.
     */
    INHERIT_DEPENDENCIES,
}
