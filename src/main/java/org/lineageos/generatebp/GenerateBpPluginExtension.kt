/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp

import org.gradle.api.provider.Property
import org.lineageos.generatebp.models.Module

interface GenerateBpPluginExtension {
    val targetSdk: Property<Int>
    val minSdk: Property<Int>
    val versionCode: Property<Int>
    val versionName: Property<String>
    val availableInAOSP: Property<(module: Module) -> Boolean>
}
