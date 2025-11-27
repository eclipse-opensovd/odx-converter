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

import converter.plugin.api.ChunkApi
import converter.plugin.api.ConverterApi
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import java.util.logging.Logger

class PluginApiHandler(
    private val mddFileBuilder: MDDFile.Builder,
    private val loggerArg: Logger,
    private val addChunkFun: (Chunk.Builder, PluginApiHandler) -> Unit,
) : ConverterApi {
    override val mddFile: MDDFile.Builder
        get() = mddFileBuilder

    override val logger: Logger
        get() = loggerArg

    override fun addChunk(chunk: Chunk.Builder) {
        addChunkFun(chunk, this)
    }
}

class ChunkApiHandler(
    private val chunkBuilder: Chunk.Builder,
) : ChunkApi {
    var removeChunk = false

    override val chunk: Chunk.Builder
        get() = chunkBuilder

    override fun keepChunk() {
        removeChunk = false
    }

    override fun removeChunk() {
        removeChunk = true
    }
}
