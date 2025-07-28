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

@Serializable
data class ConverterOptions(
    val lenient: Boolean = false,
    val includeJobFiles: Boolean = false,
    val partialJobFiles: List<PartialFilePattern> = emptyList(),
)

@Serializable
data class PartialFilePattern(
    val jobFilePattern: String,
    val includePattern: String,
)
