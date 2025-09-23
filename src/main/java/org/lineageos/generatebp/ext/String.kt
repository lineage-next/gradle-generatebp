/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

fun spaces(n: Int) = " ".repeat(n)

fun String.indentWithSpaces(n: Int) = prependIndent(spaces(n))

fun Iterable<String>.indentWithSpaces(n: Int) = map {
    it.indentWithSpaces(n)
}

fun Sequence<String>.indentWithSpaces(n: Int) = map {
    it.indentWithSpaces(n)
}
