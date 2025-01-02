/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

import org.lineageos.generatebp.models.License

object ReuseUtils {
    private const val SPDX_FILE_COPYRIGHT_TEXT_TEMPLATE = "SPDX-FileCopyrightText: %s\n"
    private const val SPDX_LICENSE_IDENTIFIER_HEADER = "SPDX-License-Identifier: "

    private const val SPDX_LICENSE_IDENTIFIER_JOIN = " AND "

    fun generateReuseCopyrightContent(
        licenses: List<License> = listOf(),
        copyrights: List<String> = listOf(),
        initialYear: Int? = null,
        addNewlineBetweenCopyrightAndLicense: Boolean = true,
        addEndingNewline: Boolean = true,
    ) = buildString {
        if (copyrights.isNotEmpty()) {
            copyrights.forEach { developerName ->
                append(
                    SPDX_FILE_COPYRIGHT_TEXT_TEMPLATE.format(
                        initialYear?.let {
                            "$it $developerName"
                        } ?: developerName
                    )
                )
            }
        }

        licenses.distinct().takeIf { it.isNotEmpty() }?.let {
            if (addNewlineBetweenCopyrightAndLicense && copyrights.isNotEmpty()) {
                append("\n")
            }

            append(SPDX_LICENSE_IDENTIFIER_HEADER)

            it.joinTo(this, SPDX_LICENSE_IDENTIFIER_JOIN) { license -> license.spdxId }
        }

        if (addEndingNewline && isNotEmpty()) {
            append("\n")
        }
    }
}
