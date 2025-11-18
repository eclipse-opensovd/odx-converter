/*
 * Copyright (c) 2025 The Contributors to Eclipse OpenSOVD (see CONTRIBUTORS)
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.File
import java.util.jar.JarFile

object ManifestReader {
    val version: String
        get() = attributes?.getValue("Implementation-Version") ?: "development"

    val buildDate: String
        get() = attributes?.getValue("Implementation-BuildDate") ?: "unknown"

    val commitHash: String
        get() = attributes?.getValue("Implementation-Commit") ?: "unknown"

    private val attributes = javaClass.protectionDomain?.codeSource?.location?.let { loc ->
        val f = File(loc.toURI())
        if (f.isFile) {
            JarFile(f).use { jf ->
                jf.manifest?.mainAttributes
            }
        } else {
            null
        }
    }
}