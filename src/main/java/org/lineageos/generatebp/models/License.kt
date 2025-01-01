/*
 * SPDX-FileCopyrightText: 2023-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

enum class License(
    val spdxId: String,
    vararg val urlRegex: Regex,
) {
    APACHE_2_0(
        "Apache-2.0",
        Regex(".*www.apache.org/licenses/LICENSE-2.0\\..*"),
        Regex("http(s)?://api.github.com/licenses/apache-2.0"),
    ),
    BSD_2_CLAUSE(
        "BSD-2-Clause",
        Regex("http(s)?://(www\\.)?opensource.org/licenses/bsd-2-clause"),
        Regex("http(s)?://api.github.com/licenses/bsd-2-clause"),
    ),
    BSD_3_CLAUSE(
        "BSD-3-Clause",
        Regex("http(s)?://(www\\.)?opensource.org/licenses/bsd-(license|3-clause)"),
        Regex("http(s)?://api.github.com/licenses/bsd-3-clause"),
    ),
    GPL_3_0(
        "GPL-3.0",
        Regex(".*www.gnu.org/licenses/gpl-3.0\\..*"),
        Regex("http(s)?://api.github.com/licenses/gpl-3.0"),
    ),
    LGPL_3_0(
        "LGPL-3.0",
        Regex(".*www.gnu.org/licenses/lgpl-3.0\\..*"),
        Regex("http(s)?://api.github.com/licenses/lgpl-3.0"),
    ),
    MIT(
        "MIT",
        Regex("http(s)?://opensource.org/licenses/MIT"),
        Regex("http(s)?://api.github.com/licenses/mit"),
    );

    companion object {
        fun fromUrl(url: String) = values().firstOrNull {
            it.urlRegex.any { regex ->
                regex.matches(url)
            }
        }
    }
}
