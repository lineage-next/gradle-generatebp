/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

object Logger {
    private const val DEBUG = false

    fun info(message: String) = println(message)

    fun debug(message: String) {
        if (DEBUG) {
            info(message)
        }
    }
}
