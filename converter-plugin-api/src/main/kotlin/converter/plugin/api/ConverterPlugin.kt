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

/**
 * Plugin API that needs to be implemented by a plugin
 */
interface ConverterPlugin {
    /**
     * Unique identifier for a plugin -- should be human-readable and concise.
     * Vendor-specific plugins should be prefixed with `vendor-`, where vendor
     * may be the actual name of the vendor.
     */
    fun getPluginIdentifier(): String

    /**
     * Version of the plugin, it should follow semantic versioning.
     */
    fun getPluginVersion(): String

    /**
     * Short description of the plugin
     */
    fun getPluginDescription(): String

    /**
     * Priority of the plugin, priority is used ascending, lower priority plugins are processed first
     */
    fun getPluginPriority(): Int

    /**
     * Called before any chunks are created
     */
    fun beforeProcessing(api: ConverterApi)

    /**
     * Called for every chunk that has been created within the odx-converter itself
     */
    fun processChunk(api: ConverterApi, initialData: ByteArray, chunkApi: ChunkApi)

    /**
     * Called after default chunks have been created, before the output file is created
     */
    fun afterProcessing(api: ConverterApi)
}