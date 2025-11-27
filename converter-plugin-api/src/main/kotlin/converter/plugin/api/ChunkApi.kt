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

package converter.plugin.api

import org.eclipse.opensovd.cda.mdd.Chunk

/**
 * API to interact with chunk data.
 *
 * To remove a currently processed chunk from being added to the result, call
 * `removeChunk`, to revert this, call `keepChunk`.
 */
interface ChunkApi {
    /**
     * The data of the chunk to be added
     */
    val chunk: Chunk.Builder

    /**
     * Keeps this chunk, which is also the default
     */
    fun keepChunk()

    /**
     * Removes this chunk in the serialized result
     */
    fun removeChunk()
}
