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
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import org.eclipse.opensovd.cda.mdd.Chunk
import schema.odx.LIBRARY
import schema.odx.PROGCODE
import schema.odx.PROGCODES
import schema.odx.SINGLEECUJOB
import java.io.ByteArrayInputStream
import java.util.logging.Logger
import kotlin.test.Test

class ChunkBuilderTest {
    private val logger = Logger.getLogger("test")

    @Test
    fun `createJobsChunks returns empty when includeJobFiles is false`() {
        val builder = ChunkBuilder()
        val odx = mockk<ODXCollectionGroup>()
        val options = ConverterOptions(includeJobFiles = false)

        val result = builder.createJobsChunks(logger, emptyMap(), odx, options)
        assertThat(result).isEmpty()
    }

    @Test
    fun `createJobsChunks returns chunks for job code files`() {
        val builder = ChunkBuilder()
        val options = ConverterOptions(includeJobFiles = true)

        val progCode = PROGCODE()
        progCode.codefile = "job.jar"
        val progCodes = PROGCODES()
        progCodes.progcode.add(progCode)

        val job = SINGLEECUJOB()
        job.id = "job1"
        job.shortname = "TestJob"
        job.progcodes = progCodes

        val odx = mockk<ODXCollectionGroup>()
        every { odx.singleEcuJobs } returns listOf(job)
        every { odx.libraries } returns emptyList()

        val fileContent = "binary data".toByteArray()
        val inputData =
            mapOf(
                "job.jar" to
                    ZipEntryInfos(
                        size = fileContent.size.toLong(),
                        inputStream = { ByteArrayInputStream(fileContent) },
                    ),
            )

        val result = builder.createJobsChunks(logger, inputData, odx, options)
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("job.jar")
        assertThat(result[0].type).isEqualTo(Chunk.DataType.CODE_FILE)
        assertThat(result[0].data.toByteArray().toList()).isEqualTo(fileContent.toList())
    }

    @Test
    fun `createJobsChunks includes library code files`() {
        val builder = ChunkBuilder()
        val options = ConverterOptions(includeJobFiles = true)

        val library = LIBRARY()
        library.id = "lib1"
        library.shortname = "TestLib"
        library.codefile = "lib.jar"
        library.syntax = "JAVA"

        val odx = mockk<ODXCollectionGroup>()
        every { odx.singleEcuJobs } returns emptyList()
        every { odx.libraries } returns listOf(library)

        val fileContent = "lib data".toByteArray()
        val inputData =
            mapOf(
                "lib.jar" to
                    ZipEntryInfos(
                        size = fileContent.size.toLong(),
                        inputStream = { ByteArrayInputStream(fileContent) },
                    ),
            )

        val result = builder.createJobsChunks(logger, inputData, odx, options)
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("lib.jar")
    }

    @Test
    fun `createJobsChunks deduplicates files referenced multiple times`() {
        val builder = ChunkBuilder()
        val options = ConverterOptions(includeJobFiles = true)

        val progCode1 = PROGCODE().apply { codefile = "shared.jar" }
        val progCode2 = PROGCODE().apply { codefile = "shared.jar" }
        val job1 =
            SINGLEECUJOB().apply {
                id = "job1"
                shortname = "Job1"
                progcodes = PROGCODES().apply { progcode.add(progCode1) }
            }
        val job2 =
            SINGLEECUJOB().apply {
                id = "job2"
                shortname = "Job2"
                progcodes = PROGCODES().apply { progcode.add(progCode2) }
            }

        val odx = mockk<ODXCollectionGroup>()
        every { odx.singleEcuJobs } returns listOf(job1, job2)
        every { odx.libraries } returns emptyList()

        val fileContent = "shared data".toByteArray()
        val inputData =
            mapOf(
                "shared.jar" to
                    ZipEntryInfos(
                        size = fileContent.size.toLong(),
                        inputStream = { ByteArrayInputStream(fileContent) },
                    ),
            )

        val result = builder.createJobsChunks(logger, inputData, odx, options)
        assertThat(result).hasSize(1)
    }

    @Test
    fun `createPartialChunks returns empty when partialJobFiles is empty`() {
        val builder = ChunkBuilder()
        val odx = mockk<ODXCollectionGroup>()
        val options = ConverterOptions(partialJobFiles = emptyList())

        val result = builder.createPartialChunks(logger, emptyMap(), odx, options)
        assertThat(result).isEmpty()
    }
}
