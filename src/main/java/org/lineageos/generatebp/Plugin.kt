/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.create
import org.lineageos.generatebp.models.Module

interface GenerateBpPluginExtension {
    val targetSdk: Property<Int>
    val availableInAOSP: Property<(module: Module) -> Boolean>
}

class GenerateBpPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<GenerateBpPluginExtension>("generateBp")
        project.task("generateBp") {
            doLast {
                GenerateBp(
                    project, extension.targetSdk.get(), extension.availableInAOSP.get()
                )()
            }
        }
    }
}
