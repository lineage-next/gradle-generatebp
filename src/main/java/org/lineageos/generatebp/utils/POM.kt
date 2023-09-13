/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.lineageos.generatebp.models.License
import org.lineageos.generatebp.models.Module
import java.io.File
import java.nio.file.Paths

/**
 * A class representing a POM.
 * @param file the POM itself
 * @param module the module to which this POM belongs to
 */
class POM(
    private val file: File,
    private val module: Module,
) {
    private val artifactsDir = file.parentFile.parentFile.parentFile.parentFile.parentFile

    val inceptionYear: Int?
    val organizationName: String?
    val licenses: List<License>
    val developersNames: List<String>
    val dependencies: List<Module>

    init {
        XmlParser().parse(file).let {
            inceptionYear = parseInceptionYear(it, module)
            organizationName = parseOrganizationName(it, module)
            licenses = parseLicenses(it, module)
            developersNames = parseDevelopersNames(it, module)
            dependencies = parseDependencies(it, module)
        }
    }

    /**
     * Get the inception year specified in the [pomNode]. If not found, it will be recursively
     * searched from the module's parent's POMs.
     * @param pomNode The POM's node to parse
     * @param module The module to which the artifact belongs
     */
    private fun parseInceptionYear(pomNode: Node, module: Module): Int? {
        val inceptionYearNode =
            (pomNode["inceptionYear"] as NodeList).firstOrNull() as Node? ?: run {
                Logger.debug(
                    "Inception year node in POM not found for ${module.gradleName}, trying with parent"
                )
                return parseParent(pomNode, module)?.let {
                    parseInceptionYear(it)
                } ?: run {
                    Logger.debug(
                        "Inception year node in POM not found for ${module.gradleName} and no parent available"
                    )
                    return null
                }
            }

        return inceptionYearNode.text().toInt()
    }

    private fun parseOrganizationName(pomNode: Node, module: Module): String? {
        val organizationNode = (pomNode["organization"] as NodeList).firstOrNull() as Node? ?: run {
            Logger.debug(
                "Organization node in POM not found for ${module.gradleName}, trying with parent"
            )
            return parseParent(pomNode, module)?.let {
                parseOrganizationName(it)
            } ?: run {
                Logger.debug(
                    "Organization node in POM not found for ${module.gradleName} and no parent available"
                )
                return null
            }
        }

        return (organizationNode.get("name") as NodeList?)?.text()
            ?: run {
                Logger.debug("Name not found for ${module.gradleName}'s organization")
                return null
            }
    }

    /**
     * Get the licenses specified in the [pomNode]. If not found, they will be recursively searched
     * from the module's parent's POMs.
     * @param pomNode The POM's node to parse
     * @param module The module to which the artifact belongs
     */
    private fun parseLicenses(pomNode: Node, module: Module): List<License> {
        val licensesNode = (pomNode["licenses"] as NodeList).firstOrNull() as Node? ?: run {
            Logger.debug(
                "Licenses node in POM not found for ${module.gradleName}, trying with parent"
            )
            return parseParent(pomNode, module)?.let {
                parseLicenses(it)
            } ?: run {
                Logger.debug(
                    "Licenses node in POM not found for ${module.gradleName} and no parent available"
                )
                return listOf()
            }
        }

        return licensesNode.children().mapNotNull {
            it as Node?
        }.mapNotNull { licenseNode ->
            (licenseNode.get("url") as NodeList?)?.text()?.let {
                License.fromUrl(it) ?: run {
                    Logger.debug("Unknown license URL $it")
                    null
                }
            } ?: run {
                Logger.debug("License URL not found for ${module.gradleName}")
                null
            }
        }
    }

    /**
     * Get the developers specified in the [pomNode]. If not found, they will be recursively
     * searched from the module's parent's POMs.
     * @param pomNode The POM's node to parse
     * @param module The module to which the artifact belongs
     */
    private fun parseDevelopersNames(pomNode: Node, module: Module): List<String> {
        val developersNode = (pomNode["developers"] as NodeList).firstOrNull() as Node? ?: run {
            Logger.debug(
                "Developers node in POM not found for ${module.gradleName}, trying with parent"
            )
            return parseParent(pomNode, module)?.let {
                parseDevelopersNames(it)
            } ?: run {
                Logger.debug(
                    "Developers node in POM not found for ${module.gradleName} and no parent available"
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
     * @param module The module to which the artifact belongs
     */
    private fun parseDependencies(pomNode: Node, module: Module): List<Module> {
        val dependenciesNode = (pomNode["dependencies"] as NodeList).firstOrNull() as Node?
            ?: run {
                Logger.debug("Dependencies node in POM not found for ${module.gradleName}")
                return listOf()
            }

        return dependenciesNode.children().mapNotNull { node ->
            node as Node?
        }.map { dependencyNode ->
            val groupId = (dependencyNode.get("groupId") as NodeList?)?.text()
                ?: throw Exception("Group ID not found for ${module.gradleName}'s dependency")
            val artifactId = (dependencyNode.get("artifactId") as NodeList?)?.text()
                ?: throw Exception("Artifact ID not found for ${module.gradleName}'s dependency")

            Module(groupId, artifactId)
        }
    }

    /**
     * Given a [module], find its artifact's POM and get the inception year.
     * @param module The module to get the developers from
     */
    private fun parseInceptionYear(module: Module): Int? {
        val pom = findPOMFromArtifactDir(getArtifactDirFromModule(module))

        XmlParser().parse(pom).let {
            return parseInceptionYear(it, module)
        }
    }

    /**
     * Given a [module], find its artifact's POM and get the organization name.
     * @param module The module to get the developers from
     */
    private fun parseOrganizationName(module: Module): String? {
        val pom = findPOMFromArtifactDir(getArtifactDirFromModule(module))

        XmlParser().parse(pom).let {
            return parseOrganizationName(it, module)
        }
    }

    /**
     * Given a [module], find its artifact's POM and get the licenses.
     * @param module The module to get the license from
     */
    private fun parseLicenses(module: Module): List<License> {
        val pom = findPOMFromArtifactDir(getArtifactDirFromModule(module))

        XmlParser().parse(pom).let {
            return parseLicenses(it, module)
        }
    }

    /**
     * Given a [module], find its artifact's POM and get the developers.
     * @param module The module to get the developers from
     */
    private fun parseDevelopersNames(module: Module): List<String> {
        val pom = findPOMFromArtifactDir(getArtifactDirFromModule(module))

        XmlParser().parse(pom).let {
            return parseDevelopersNames(it, module)
        }
    }

    private fun parseParent(pomNode: Node, module: Module): Module? {
        val parentNode = (pomNode["parent"] as NodeList).firstOrNull() as Node?
            ?: run {
                Logger.debug("Parent node in POM not found for ${module.gradleName}")
                return null
            }

        val groupId = (parentNode.get("groupId") as NodeList?)?.text()
            ?: throw Exception("Group ID not found for ${module.gradleName}'s parent")
        val artifactId = (parentNode.get("artifactId") as NodeList?)?.text()
            ?: throw Exception("Artifact ID not found for ${module.gradleName}'s parent")
        val version = (parentNode.get("version") as NodeList?)?.text()
            ?: throw Exception("Version not found for ${module.gradleName}'s parent")

        return Module(groupId, artifactId, version)
    }

    /**
     * Get the path to the [module]'s artifact.
     */
    private fun getArtifactDirFromModule(module: Module) =
        Paths.get(artifactsDir.path, module.group, module.name, module.version).toFile()

    companion object {
        /**
         * Obtain a [POM] object given an artifact's file.
         * @param artifact The artifact
         * @param module The module to which the artifact belongs
         */
        fun fromArtifact(artifact: File, module: Module): POM {
            val pomFile = findPOMFromArtifactFile(artifact)

            return POM(pomFile, module)
        }

        /**
         * Find a POM given an artifact directory.
         * @param artifactDir The artifact directory
         */
        private fun findPOMFromArtifactDir(artifactDir: File): File {
            require(artifactDir.isDirectory) { "${artifactDir.absolutePath} is not a directory" }

            val poms = artifactDir.walk().filter {
                it.extension == "pom"
            }.toSet()

            require(poms.isNotEmpty()) { "No POM found for artifact ${artifactDir.path}" }

            if (poms.size > 1) {
                Logger.debug("Multiple POMs found for ${artifactDir.path}, using the first one")
            }

            return poms.first()
        }

        /**
         * Find a POM given an artifact file.
         * @param artifact The artifact file, used to find the artifact's main dir
         */
        private fun findPOMFromArtifactFile(artifact: File) =
            findPOMFromArtifactDir(artifact.parentFile.parentFile)
    }
}
