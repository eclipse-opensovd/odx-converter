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

package converter.plugins.default

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThan
import com.google.protobuf.ByteString
import converter.plugin.api.ChunkApi
import converter.plugin.api.ConverterApi
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.util.logging.Logger
import kotlin.test.Test

class CompressionPluginTest {
    private val plugin = CompressionPlugin()

    @Test
    fun `plugin identifier is compression`() {
        assertThat(plugin.getPluginIdentifier()).isEqualTo("compression")
    }

    @Test
    fun `plugin version is 0_1_0`() {
        assertThat(plugin.getPluginVersion()).isEqualTo("0.1.0")
    }

    @Test
    fun `plugin priority is 50`() {
        assertThat(plugin.getPluginPriority()).isEqualTo(50)
    }

    @Test
    fun `processChunk compresses data with LZMA and adds SHA-512 signature`() {
        val testData = "Hello, this is test data for compression!".repeat(10).toByteArray()
        val chunkBuilder = Chunk.newBuilder().setData(ByteString.copyFrom(testData))

        val api = mockk<ConverterApi>()
        every { api.logger } returns Logger.getLogger("test")

        val chunkApi = mockk<ChunkApi>()
        every { chunkApi.chunk } returns chunkBuilder

        plugin.processChunk(api, testData, chunkApi)

        // Verify compression algorithm is set
        assertThat(chunkBuilder.compressionAlgorithm).isEqualTo("lzma")

        // Verify data is actually LZMA compressed by decompressing it
        val compressedData = chunkBuilder.data.toByteArray()
        val decompressed =
            LZMACompressorInputStream(ByteArrayInputStream(compressedData)).use {
                it.readAllBytes()
            }
        assertThat(decompressed.toList()).isEqualTo(testData.toList())

        // Verify SHA-512 signature is added
        assertThat(chunkBuilder.signaturesList).hasSize(1)
        val signature = chunkBuilder.getSignatures(0)
        assertThat(signature.algorithm).isEqualTo("sha512_uncompressed")

        // Verify the hash is correct
        val expectedHash = MessageDigest.getInstance("SHA-512").digest(testData)
        assertThat(signature.signature.toByteArray().toList()).isEqualTo(expectedHash.toList())
    }

    @Test
    fun `processChunk uses chunk data over initialData when both present`() {
        val initialData = "initial data".toByteArray()
        val chunkData = "chunk data".toByteArray()
        val chunkBuilder = Chunk.newBuilder().setData(ByteString.copyFrom(chunkData))

        val api = mockk<ConverterApi>()
        every { api.logger } returns Logger.getLogger("test")

        val chunkApi = mockk<ChunkApi>()
        every { chunkApi.chunk } returns chunkBuilder

        plugin.processChunk(api, initialData, chunkApi)

        // Verify the chunk data was used (not initialData) by decompressing
        val compressedData = chunkBuilder.data.toByteArray()
        val decompressed =
            LZMACompressorInputStream(ByteArrayInputStream(compressedData)).use {
                it.readAllBytes()
            }
        assertThat(decompressed.toList()).isEqualTo(chunkData.toList())
    }

    @Test
    fun `compressed data is smaller than original for repetitive data`() {
        val testData = "AAAA".repeat(1000).toByteArray()
        val chunkBuilder = Chunk.newBuilder().setData(ByteString.copyFrom(testData))

        val api = mockk<ConverterApi>()
        every { api.logger } returns Logger.getLogger("test")

        val chunkApi = mockk<ChunkApi>()
        every { chunkApi.chunk } returns chunkBuilder

        plugin.processChunk(api, testData, chunkApi)

        assertThat(chunkBuilder.data.size()).isLessThan(testData.size)
    }
}
