/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

import org.lineageos.generatebp.ext.equalsComparable
import org.lineageos.generatebp.ext.hashCodeOf
import java.io.File

/**
 * A class representing a POM.
 *
 * @param file The POM itself
 * @param moduleIdentifier The module to which this POM belongs to
 * @param inceptionYear The initial release of the POM's artifact
 * @param organizationName The name of the organization that manages the POM's artifact
 * @param licenses The licenses of the POM's artifact
 * @param developersNames The developers' names of the POM's artifact
 * @param dependencies The dependencies of the module
 */
class Pom(
    val file: File,
    val moduleIdentifier: ModuleIdentifier,
    val inceptionYear: Int?,
    val organizationName: String?,
    val licenses: List<License>,
    val developersNames: List<String>,
    val dependencies: List<ModuleIdentifier>,
) : Comparable<Pom> {
    override fun compareTo(other: Pom) = compareValuesBy(
        this, other,
        Pom::file,
        Pom::moduleIdentifier,
    )

    override fun equals(other: Any?) = equalsComparable(other)

    override fun hashCode() = hashCodeOf(
        Pom::file,
        Pom::moduleIdentifier,
    )

    override fun toString(): String = file.absolutePath
}
