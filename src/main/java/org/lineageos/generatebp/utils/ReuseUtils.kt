/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

import org.lineageos.generatebp.models.License

object ReuseUtils {
    private const val SPDX_FILE_COPYRIGHT_TEXT_TEMPLATE = "SPDX-FileCopyrightText: %s\n"
    private const val SPDX_LICENSE_IDENTIFIER_TEMPLATE = "SPDX-License-Identifier: %s"

    internal val currentYear = Year.now().value

    fun generateReuseCopyrightContent(
        license: License? = null,
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

        license?.let {
            if (addNewlineBetweenCopyrightAndLicense && copyrights.isNotEmpty()) {
                append("\n")
            }

            append(SPDX_LICENSE_IDENTIFIER_TEMPLATE.format(it.spdxId))
        }

        if (addEndingNewline && isNotEmpty()) {
            append("\n")
        }
    }
}
