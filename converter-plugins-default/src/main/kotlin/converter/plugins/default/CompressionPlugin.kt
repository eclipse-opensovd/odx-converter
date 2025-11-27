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

import com.google.protobuf.ByteString
import converter.plugin.api.ChunkApi
import converter.plugin.api.ConverterApi
import converter.plugin.api.ConverterPlugin
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream
import org.eclipse.opensovd.cda.mdd.Signature
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.logging.Logger

/**
 * Default compression plugin, compresses chunks with LZMA, and adds a sha512 hash of the initial (uncompressed) data.
 */
class CompressionPlugin : ConverterPlugin {
    override fun getPluginIdentifier(): String =
        "compression"

    override fun getPluginVersion(): String =
        "0.1.0"

    override fun getPluginDescription(): String =
        "Default plugin to compress chunks with lzma"

    override fun getPluginPriority(): Int =
        50

    override fun beforeProcessing(api: ConverterApi) {
        // No implementation, since plugin doesn't require pre-processing
    }

    override fun processChunk(
        api: ConverterApi,
        initialData: ByteArray,
        chunkApi: ChunkApi
    ) {
        val data = chunkApi.chunk.data?.toByteArray() ?: initialData

        // Compress chunk
        api.logger.finest("Compressing chunk with LZMA")
        val compressed = ByteArrayOutputStream()
        LZMACompressorOutputStream(compressed).use { outputStream ->
            outputStream.write(data)
        }

        chunkApi.chunk.setData(ByteString.copyFrom(compressed.toByteArray()))
        chunkApi.chunk.compressionAlgorithm = "lzma"

        // Add hash of uncompressed data as signature
        api.logger.finest("Calculating SHA-512 for chunk")
        val md = MessageDigest.getInstance("SHA-512")
        val uncompressedDigest = md.digest(data)
        val signature = Signature.newBuilder()
            .setAlgorithm("sha512_uncompressed")
            .setSignature(ByteString.copyFrom(uncompressedDigest))
        chunkApi.chunk.addSignatures(signature)
    }

    override fun afterProcessing(api: ConverterApi) {
        // No implementation, since plugin doesn't require post-processing
    }
}
