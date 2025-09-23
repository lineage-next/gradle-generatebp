/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import org.lineageos.generatebp.ext.equalsComparable
import org.lineageos.generatebp.ext.hashCodeOf

/**
 * A Gradle module identifier.
 *
 * @param group The group name
 * @param name The name of the module
 * @param version The version of the module. This field is ignored for comparison
 */
open class ModuleIdentifier(
    val group: String,
    val name: String,
    val version: String,
) : Comparable<ModuleIdentifier> {
    /**
     * Get the name of this module as a Gradle dependency.
     */
    val gradleName = "$group:$name:$version"

    /**
     * Get the relative path to the module in an AOSP build system.
     */
    val aospModulePath = "${group.replace(".", "/")}/${name}/${version}"

    override fun compareTo(other: ModuleIdentifier) = compareValuesBy(
        this, other,
        ModuleIdentifier::group,
        ModuleIdentifier::name,
    )

    override fun equals(other: Any?) = equalsComparable(other)

    override fun hashCode() = hashCodeOf(
        ModuleIdentifier::group,
        ModuleIdentifier::name,
    )

    override fun toString() = gradleName
}
