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
import assertk.assertions.isFalse
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import java.util.logging.Logger
import kotlin.test.Test

class ChunkApiHandlerTest {
    @Test
    fun `removeChunk is false by default`() {
        val handler = ChunkApiHandler(Chunk.newBuilder())
        assertThat(handler.removeChunk).isFalse()
    }

    @Test
    fun `removeChunk sets removeChunk to true`() {
        val handler = ChunkApiHandler(Chunk.newBuilder())
        handler.removeChunk()
        assertThat(handler.removeChunk).isTrue()
    }

    @Test
    fun `keepChunk resets removeChunk to false`() {
        val handler = ChunkApiHandler(Chunk.newBuilder())
        handler.removeChunk()
        assertThat(handler.removeChunk).isTrue()
        handler.keepChunk()
        assertThat(handler.removeChunk).isFalse()
    }

    @Test
    fun `chunk property returns the provided builder`() {
        val chunkBuilder = Chunk.newBuilder()
        val handler = ChunkApiHandler(chunkBuilder)
        assertThat(handler.chunk).isSameInstanceAs(chunkBuilder)
    }
}

class PluginApiHandlerTest {
    @Test
    fun `mddFile returns the provided builder`() {
        val mddBuilder = MDDFile.newBuilder()
        val logger = Logger.getLogger("test")
        val handler = PluginApiHandler(mddBuilder, logger) { _, _ -> }
        assertThat(handler.mddFile).isSameInstanceAs(mddBuilder)
    }

    @Test
    fun `logger returns the provided logger`() {
        val mddBuilder = MDDFile.newBuilder()
        val logger = Logger.getLogger("test")
        val handler = PluginApiHandler(mddBuilder, logger) { _, _ -> }
        assertThat(handler.logger).isSameInstanceAs(logger)
    }

    @Test
    fun `addChunk delegates to the provided function`() {
        val mddBuilder = MDDFile.newBuilder()
        val logger = Logger.getLogger("test")
        var capturedChunk: Chunk.Builder? = null
        var capturedHandler: PluginApiHandler? = null
        val handler =
            PluginApiHandler(mddBuilder, logger) { chunk, api ->
                capturedChunk = chunk
                capturedHandler = api
            }
        val chunkBuilder = Chunk.newBuilder()
        handler.addChunk(chunkBuilder)
        assertThat(capturedChunk).isSameInstanceAs(chunkBuilder)
        assertThat(capturedHandler).isSameInstanceAs(handler)
    }
}
