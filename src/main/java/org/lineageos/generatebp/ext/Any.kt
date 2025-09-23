/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

fun <T> T.hashCodeOf(
    vararg selectors: T.() -> Any?,
) = selectors.fold(0) { hash, property -> 31 * hash + property().hashCode() }
