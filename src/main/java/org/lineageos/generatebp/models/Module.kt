/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleVersionIdentifier
import kotlin.reflect.safeCast

/**
 * A Gradle module.
 * @param group The group name
 * @param name The name of the module
 * @param version The version of the module, set to "any" by default. This field is ignored for
 *                comparison and it's only used for [aospModulePath]
 */
data class Module(
    val group: String,
    val name: String,
    val version: String = VERSION_ANY
) : Comparable<Module> {
    override fun equals(other: Any?) = Module::class.safeCast(other)?.let {
        compareTo(it) == 0
    } ?: false

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun compareTo(other: Module) = compareValuesBy(
        this, other,
        { it.group },
        { it.name },
    )

    val gradleName = "$group:$name:$version"

    val aospModulePath = "${group.replace(".", "/")}/${name}/${version}"

    companion object {
        private const val VERSION_ANY = "any"

        fun fromModuleVersionIdentifier(it: ModuleVersionIdentifier) =
            Module(it.group, it.name, it.version)

        fun fromDependency(it: Dependency) =
            Module(it.group!!, it.name, it.version ?: VERSION_ANY)
    }
}
