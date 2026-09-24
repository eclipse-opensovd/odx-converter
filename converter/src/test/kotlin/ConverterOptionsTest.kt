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

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class ConverterOptionsTest {
    @Test
    fun `default values are correct`() {
        val options = ConverterOptions()
        assertThat(options.lenient).isFalse()
        assertThat(options.includeJobFiles).isFalse()
        assertThat(options.partialJobFiles).isEmpty()
        assertThat(options.withAudiences).isEmpty()
        assertThat(options.pluginOptions).isEmpty()
    }

    @Test
    fun `serialization roundtrip with defaults`() {
        val options = ConverterOptions()
        val json = Json.encodeToString(options)
        val decoded = Json.decodeFromString<ConverterOptions>(json)
        assertThat(decoded).isEqualTo(options)
    }

    @Test
    fun `serialization roundtrip with custom values`() {
        val options =
            ConverterOptions(
                lenient = true,
                includeJobFiles = true,
                partialJobFiles =
                    listOf(
                        PartialFilePattern(jobFilePattern = ".*\\.jar", includePattern = ".*\\.class"),
                    ),
                withAudiences = listOf("AfterSales", "Development"),
                pluginOptions = mapOf("compression.compress" to "false"),
            )
        val json = Json.encodeToString(options)
        val decoded = Json.decodeFromString<ConverterOptions>(json)
        // pluginOptions is @Transient and must never survive (de)serialization.
        assertThat(decoded).isEqualTo(options.copy(pluginOptions = emptyMap()))
    }

    @Test
    fun `pluginOptions are excluded from serialization`() {
        val options = ConverterOptions(pluginOptions = mapOf("foo.secret-token" to "supersecret"))
        val json = Json.encodeToString(options)
        assertThat(json).doesNotContain("supersecret")
        assertThat(json).doesNotContain("pluginOptions")

        val decoded = Json.decodeFromString<ConverterOptions>(json)
        assertThat(decoded.pluginOptions).isEmpty()
    }

    @Test
    fun `deserialization with missing optional fields uses defaults`() {
        val json = """{}"""
        val decoded = Json.decodeFromString<ConverterOptions>(json)
        assertThat(decoded).isEqualTo(ConverterOptions())
    }

    @Test
    fun `PartialFilePattern serialization roundtrip`() {
        val pattern = PartialFilePattern(jobFilePattern = "test.*", includePattern = ".*\\.py")
        val json = Json.encodeToString(pattern)
        val decoded = Json.decodeFromString<PartialFilePattern>(json)
        assertThat(decoded).isEqualTo(pattern)
    }

    @Test
    fun `PartialJobFilePattern serialization roundtrip`() {
        val pattern =
            PartialJobFilePattern(
                jobFileName = "test.jar",
                partialFilePattern = PartialFilePattern(jobFilePattern = "test.*", includePattern = ".*\\.py"),
            )
        val json = Json.encodeToString(pattern)
        val decoded = Json.decodeFromString<PartialJobFilePattern>(json)
        assertThat(decoded).isEqualTo(pattern)
    }
}
