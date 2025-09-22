/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

object Logger {
    const val DEBUG = false

    fun log(
        message: String,
        tag: String? = null,
        throwable: Throwable? = null,
    ) = println(
        buildString {
            tag?.let {
                append("[$it] ")
            }

            append(message)

            throwable?.let {
                append("\n")
                append(it.stackTraceToString())
            }
        }
    )

    @Suppress("KotlinConstantConditions")
    inline fun <reified T> T.debug(
        message: String,
        throwable: Throwable? = null,
    ) = when (DEBUG) {
        true -> info(
            message,
            throwable,
        )

        false -> Unit
    }

    inline fun <reified T> T.info(
        message: String,
        throwable: Throwable? = null,
    ) = log(
        message,
        T::class.simpleName,
        throwable,
    )
}
