/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

import org.gradle.api.artifacts.ResolvedDependency

val ResolvedDependency.recursiveDependencies: Set<ResolvedDependency>
    get() = mutableSetOf(this).apply {
        addAll(
            children.map {
                it.recursiveDependencies
            }.flatten()
        )
    }.toSet()
