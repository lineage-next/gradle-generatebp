/*
 * SPDX-FileCopyrightText: 2023-2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.utils

import org.lineageos.generatebp.models.License
import java.io.File
import java.time.Year

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
        addCurrentYear: Boolean = true,
    ) = buildString {
        val copyrightYearString = initialYear?.let { year ->
            if (year == currentYear || !addCurrentYear) {
                "$year"
            } else {
                "${year}-${currentYear}"
            }
        } ?: when (addCurrentYear) {
            true -> "$currentYear"
            false -> null
        }

        if (copyrights.isNotEmpty()) {
            copyrights.forEach { developerName ->
                append(
                    SPDX_FILE_COPYRIGHT_TEXT_TEMPLATE.format(
                        copyrightYearString?.let {
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

    fun readInitialCopyrightYear(path: String) = runCatching {
        File(path).readLines().firstOrNull {
            it.contains("SPDX-FileCopyrightText:")
        }?.let {
            // Extract initial year from copyright text
            Regex("\\d+").find(it)?.value?.toInt()
        }
    }.getOrNull()

    fun readCurrentCopyrightYear(path: String) = runCatching {
        File(path).readLines().firstOrNull {
            it.contains("SPDX-FileCopyrightText:")
        }?.let {
            // Extract current year from copyright text
            Regex("(?:\\d+-)?(\\d+)").find(it)?.groupValues?.last()?.toInt()
        }
    }.getOrNull()
}
