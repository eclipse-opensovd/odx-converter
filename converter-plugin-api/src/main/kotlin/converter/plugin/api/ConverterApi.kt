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
import org.eclipse.opensovd.cda.mdd.MDDFile

/**
 * API for the converter, allows access to the contents of the mdd-file
 */
interface ConverterApi : PluginApi {
    /**
     * Allows access mdd-file through its builder
     */
    val mddFile: MDDFile.Builder

    /**
     * Adds a chunk that will be processed by all plugins (including the one that added it)
     */
    fun addChunk(chunk: Chunk.Builder)
}
