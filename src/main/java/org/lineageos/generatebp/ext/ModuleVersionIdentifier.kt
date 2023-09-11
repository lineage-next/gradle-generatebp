/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

import org.gradle.api.artifacts.ModuleVersionIdentifier

val ModuleVersionIdentifier.gradleModuleName
    get() = "${group}:${name}:${version}"

val ModuleVersionIdentifier.aospModulePath
    get() = "${group.replace(".", "/")}/${name}/${version}"
