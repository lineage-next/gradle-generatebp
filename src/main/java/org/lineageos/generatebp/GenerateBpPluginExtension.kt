/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp

import org.gradle.api.provider.Property
import org.lineageos.generatebp.models.Module

interface GenerateBpPluginExtension {
    val targetSdk: Property<Int>
    val availableInAOSP: Property<(module: Module) -> Boolean>
}
