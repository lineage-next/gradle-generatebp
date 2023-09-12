/*
 * SPDX-FileCopyrightText: 2023 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.generatebp.models

enum class License(
    val spdxId: String,
    val urlRegex: Regex,
) {
    APACHE_2_0(
        "Apache-2.0",
        Regex(".*www.apache.org/licenses/LICENSE-2.0\\..*")
    ),
    GPL_3_0(
        "GPL-3.0",
        Regex(".*www.gnu.org/licenses/gpl-3.0\\..*")
    );

    companion object {
        fun fromUrl(url: String) = values().firstOrNull {
            it.urlRegex.matches(url)
        }
    }
}
