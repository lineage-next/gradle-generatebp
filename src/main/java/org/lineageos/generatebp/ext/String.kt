/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.ext

fun spaces(n: Int): String {
    var ret = ""
    for (i in n downTo 1) {
        ret += ' '
    }
    return ret
}

fun String.indentWithSpaces(n: Int) = prependIndent(spaces(n))

fun List<String>.indentWithSpaces(n: Int) = map {
    it.indentWithSpaces(n)
}
