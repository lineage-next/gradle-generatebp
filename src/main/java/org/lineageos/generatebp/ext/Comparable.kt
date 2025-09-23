/*
 * SPDX-FileCopyrightText: 2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

inline fun <reified T> Comparable<T>.equalsComparable(other: Any?) = (other as? T)?.let {
    compareTo(other) == 0
} ?: false
