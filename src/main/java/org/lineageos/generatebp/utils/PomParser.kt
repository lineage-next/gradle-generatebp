/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.lineageos.generatebp.models.License
import org.lineageos.generatebp.models.ModuleIdentifier
import org.lineageos.generatebp.models.Pom
import org.lineageos.generatebp.utils.Logger.debug
import org.lineageos.generatebp.utils.Logger.info
import java.io.File
import java.nio.file.Paths

/**
 * A class representing a POM.
 *
 * @param file the POM itself
 * @param moduleIdentifier The [ModuleIdentifier] to which this POM belongs to
 */
class PomParser(
    private val file: File,
    private val moduleIdentifier: ModuleIdentifier,
) {
    private val artifactsDir = file.parentFile.parentFile.parentFile.parentFile.parentFile

    val inceptionYear: Int?
    val organizationName: String?
    val licenses: List<License>
    val developersNames: List<String>
    val dependencies: List<ModuleIdentifier>

    init {
        XmlParser().parse(file).let {
            inceptionYear = parseInceptionYear(it, moduleIdentifier)
            organizationName = parseOrganizationName(it, moduleIdentifier)
            licenses = parseLicenses(it, moduleIdentifier)
            developersNames = parseDevelopersNames(it, moduleIdentifier)
            dependencies = parseDependencies(it, moduleIdentifier)
        }
    }

    /**
     * Get the inception year specified in the [pomNode]. If not found, it will be recursively
     * searched from the module's parent's POMs.
     * @param pomNode The POM's node to parse
     * @param moduleIdentifier The module to which the artifact belongs
     */
    private fun parseInceptionYear(
        pomNode: Node,
        moduleIdentifier: ModuleIdentifier,
    ): Int? {
        val inceptionYearNode =
            (pomNode["inceptionYear"] as NodeList).firstOrNull() as Node? ?: run {
                debug(
                    "Inception year node in POM not found for $moduleIdentifier, trying with parent"
                )
                return parseParent(pomNode, moduleIdentifier)?.let {
                    parseInceptionYear(it)
                } ?: run {
                    debug(
                        "Inception year node in POM not found for $moduleIdentifier and no parent available"
                    )
                    return null
                }
            }

        return inceptionYearNode.text().toInt()
    }

    private fun parseOrganizationName(
        pomNode: Node,
        moduleIdentifier: ModuleIdentifier,
    ): String? {
        val organizationNode = (pomNode["organization"] as NodeList).firstOrNull() as Node? ?: run {
            debug(
                "Organization node in POM not found for $moduleIdentifier, trying with parent"
            )
            return parseParent(pomNode, moduleIdentifier)?.let {
                parseOrganizationName(it)
            } ?: run {
                debug(
                    "Organization node in POM not found for $moduleIdentifier and no parent available"
                )
                return null
            }
        }

        return (organizationNode.get("name") as NodeList?)?.text()
            ?: run {
                debug("Name not found for $moduleIdentifier's organization")
                return null
            }
    }

    /**
     * Get the licenses specified in the [pomNode]. If not found, they will be recursively searched
     * from the module's parent's POMs.
     * @param pomNode The POM's node to parse
     * @param moduleIdentifier The module to which the artifact belongs
     */
    private fun parseLicenses(
        pomNode: Node,
        moduleIdentifier: ModuleIdentifier,
    ): List<License> {
        val licensesNode = (pomNode["licenses"] as NodeList).firstOrNull() as Node? ?: run {
            debug(
                "Licenses node in POM not found for $moduleIdentifier, trying with parent"
            )
            return parseParent(pomNode, moduleIdentifier)?.let {
                parseLicenses(it)
            } ?: run {
                debug(
                    "Licenses node in POM not found for $moduleIdentifier and no parent available"
                )
                return listOf()
            }
        }

        return licensesNode.children().mapNotNull {
            it as Node?
        }.mapNotNull { licenseNode ->
            (licenseNode.get("url") as NodeList?)?.text()?.let {
                License.fromUrl(it) ?: run {
                    info("Unknown license URL $it")
                    null
                }
            } ?: run {
                debug("License URL not found for $moduleIdentifier")
                null
            }
        }
    }

    /**
     * Get the developers specified in the [pomNode]. If not found, they will be recursively
     * searched from the module's parent's POMs.
     * @param pomNode The POM's node to parse
     * @param moduleIdentifier The module to which the artifact belongs
     */
    private fun parseDevelopersNames(
        pomNode: Node,
        moduleIdentifier: ModuleIdentifier,
    ): List<String> {
        val developersNode = (pomNode["developers"] as NodeList).firstOrNull() as Node? ?: run {
            debug(
                "Developers node in POM not found for $moduleIdentifier, trying with parent"
            )
            return parseParent(pomNode, moduleIdentifier)?.let {
                parseDevelopersNames(it)
            } ?: run {
                debug(
                    "Developers node in POM not found for $moduleIdentifier and no parent available"
                )
                return listOf()
            }
        }

        val developersNodeChildren = developersNode.children()

        return developersNodeChildren.map {
            it as Node
        }.mapNotNull {
            (it.get("name") as NodeList?)?.text()
        }
    }

    /**
     * Get the list of dependencies specified in the [pomNode].
     * @param pomNode The POM's node to parse
     * @param moduleIdentifier The module to which the artifact belongs
     */
    private fun parseDependencies(
        pomNode: Node,
        moduleIdentifier: ModuleIdentifier,
    ): List<ModuleIdentifier> {
        val dependenciesNode = (pomNode["dependencies"] as NodeList).firstOrNull() as Node?
            ?: run {
                debug("Dependencies node in POM not found for $moduleIdentifier")
                return listOf()
            }

        return dependenciesNode.children().mapNotNull { node ->
            node as Node?
        }.map { dependencyNode ->
            val groupId = (dependencyNode.get("groupId") as NodeList?)?.text()
                ?: error("Group ID not found for $moduleIdentifier's dependency")
            val artifactId = (dependencyNode.get("artifactId") as NodeList?)?.text()
                ?: error("Artifact ID not found for $moduleIdentifier's dependency")
            val version = (dependencyNode.get("version") as NodeList?)?.text()
                ?: error("Version not found for $moduleIdentifier's dependency")

            ModuleIdentifier(groupId, artifactId, version)
        }
    }

    /**
     * Given a [moduleIdentifier], find its artifact's POM and get the inception year.
     * @param moduleIdentifier The module to get the developers from
     */
    private fun parseInceptionYear(moduleIdentifier: ModuleIdentifier): Int? {
        val pom = findPomFromArtifactDir(getArtifactDirFromModule(moduleIdentifier))

        XmlParser().parse(pom).let {
            return parseInceptionYear(it, moduleIdentifier)
        }
    }

    /**
     * Given a [moduleIdentifier], find its artifact's POM and get the organization name.
     * @param moduleIdentifier The module to get the developers from
     */
    private fun parseOrganizationName(moduleIdentifier: ModuleIdentifier): String? {
        val pom = findPomFromArtifactDir(getArtifactDirFromModule(moduleIdentifier))

        XmlParser().parse(pom).let {
            return parseOrganizationName(it, moduleIdentifier)
        }
    }

    /**
     * Given a [moduleIdentifier], find its artifact's POM and get the licenses.
     * @param moduleIdentifier The module to get the license from
     */
    private fun parseLicenses(moduleIdentifier: ModuleIdentifier): List<License> {
        val pom = findPomFromArtifactDir(getArtifactDirFromModule(moduleIdentifier))

        XmlParser().parse(pom).let {
            return parseLicenses(it, moduleIdentifier)
        }
    }

    /**
     * Given a [moduleIdentifier], find its artifact's POM and get the developers.
     * @param moduleIdentifier The module to get the developers from
     */
    private fun parseDevelopersNames(
        moduleIdentifier: ModuleIdentifier
    ): List<String> {
        val pom = findPomFromArtifactDir(getArtifactDirFromModule(moduleIdentifier))

        XmlParser().parse(pom).let {
            return parseDevelopersNames(it, moduleIdentifier)
        }
    }

    private fun parseParent(
        pomNode: Node,
        moduleIdentifier: ModuleIdentifier,
    ): ModuleIdentifier? {
        val parentNode = (pomNode["parent"] as NodeList).firstOrNull() as Node?
            ?: run {
                debug("Parent node in POM not found for $moduleIdentifier")
                return null
            }

        val groupId = (parentNode.get("groupId") as NodeList?)?.text()
            ?: error("Group ID not found for $moduleIdentifier's parent")
        val artifactId = (parentNode.get("artifactId") as NodeList?)?.text()
            ?: error("Artifact ID not found for $moduleIdentifier's parent")
        val version = (parentNode.get("version") as NodeList?)?.text()
            ?: error("Version not found for $moduleIdentifier's parent")

        return ModuleIdentifier(groupId, artifactId, version)
    }

    /**
     * Get the path to the [moduleIdentifier]'s artifact.
     */
    private fun getArtifactDirFromModule(
        moduleIdentifier: ModuleIdentifier,
    ) = Paths.get(
        artifactsDir.path,
        moduleIdentifier.group,
        moduleIdentifier.name,
        moduleIdentifier.version,
    ).toFile()

    fun build(): Pom {
        return Pom(
            file = file,
            moduleIdentifier = moduleIdentifier,
            inceptionYear = inceptionYear,
            organizationName = organizationName,
            licenses = licenses,
            developersNames = developersNames,
            dependencies = dependencies,
        )
    }

    companion object {
        /**
         * Obtain a [Pom] object given an artifact's file.
         *
         * @param artifact The artifact
         * @param moduleIdentifier The module identifier to which the artifact belongs
         */
        fun fromArtifact(
            artifact: File,
            moduleIdentifier: ModuleIdentifier,
        ): Pom {
            val pomFile = findPOMFromArtifactFile(artifact)

            return PomParser(pomFile, moduleIdentifier).build()
        }

        /**
         * Find a POM given an artifact directory.
         * @param artifactDir The artifact directory
         */
        private fun findPomFromArtifactDir(artifactDir: File): File {
            require(artifactDir.isDirectory) { "${artifactDir.absolutePath} is not a directory" }

            val poms = artifactDir.walk().filter {
                it.extension == "pom"
            }.toSet()

            require(poms.isNotEmpty()) { "No POM found for artifact ${artifactDir.path}" }

            if (poms.size > 1) {
                debug("Multiple POMs found for ${artifactDir.path}, using the first one")
            }

            return poms.first()
        }

        /**
         * Find a POM given an artifact file.
         * @param artifact The artifact file, used to find the artifact's main dir
         */
        private fun findPOMFromArtifactFile(
            artifact: File,
        ) = findPomFromArtifactDir(artifact.parentFile.parentFile)
    }
}
