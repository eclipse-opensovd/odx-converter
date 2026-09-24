/*
 * Copyright (c) 2025 The contributors to Eclipse OpenSOVD (see CONTRIBUTORS)
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

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ConverterOptions(
    val lenient: Boolean = false,
    val includeJobFiles: Boolean = false,
    val partialJobFiles: List<PartialFilePattern> = emptyList(),
    val withAudiences: List<String> = emptyList(),
    val skipSigning: Boolean = false,
    // Plugin options may contain sensitive values (e.g. tokens/credentials passed to plugins) and
    // must never be persisted into the output MDD file's metadata. @Transient excludes this field
    // from JSON (de)serialization entirely, while still keeping it available in-memory for plugins.
    @Transient
    val pluginOptions: Map<String, String> = emptyMap(),
)

@Serializable
data class PartialFilePattern(
    val jobFilePattern: String,
    val includePattern: String,
)

@Serializable
data class PartialJobFilePattern(
    val jobFileName: String,
    val partialFilePattern: PartialFilePattern,
)
