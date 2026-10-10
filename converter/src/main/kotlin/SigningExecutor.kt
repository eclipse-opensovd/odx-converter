/*
 * Copyright (c) 2026 The Contributors to Eclipse OpenSOVD (see CONTRIBUTORS)
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

import converter.plugin.api.SigningPlugin
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import org.eclipse.opensovd.cda.mdd.Signature
import java.util.logging.Logger

/**
 * Shared logic to apply a set of [SigningPlugin]s to an in-progress [MDDFile.Builder], used both
 * by the `convert` command (automatic signing right after conversion) and the `sign` command
 * (signing of an already-converted `.mdd` file).
 */
object SigningExecutor {
    /**
     * Applies [plugins] to [builder] according to [scope] (`"chunk"`, `"file"`, or `"both"`).
     *
     * @return a list of human-readable error messages; empty if everything succeeded.
     */
    fun apply(
        builder: MDDFile.Builder,
        plugins: List<SigningPlugin>,
        scope: String,
        options: Map<String, String>,
        logger: Logger,
        onChunkSigned: (Chunk, SigningPlugin, Int) -> Unit = { _, _, _ -> },
        onFileSigned: (Signature) -> Unit = {},
    ): List<String> {
        val errors = mutableListOf<String>()
        val api = SigningApiHandler(builder, logger, options)

        if (scope == "chunk" || scope == "both") {
            builder.chunksBuilderList.forEach { chunkBuilder ->
                // chunk already carries its own (existing) signaturesList, which plugins can inspect
                val chunk = chunkBuilder.build()
                val data = chunk.data.toByteArray()
                plugins.forEach { plugin ->
                    val signatures = plugin.signChunk(api, chunk, data)
                    signatures.forEach { chunkBuilder.addSignatures(it) }
                    if (signatures.isNotEmpty()) {
                        onChunkSigned(chunk, plugin, signatures.size)
                    }
                }
            }
        }

        if (scope == "file" || scope == "both") {
            val existingFileSignatures = builder.chunksSignatureList.toList()
            val fileSignatures = plugins.flatMap { plugin -> plugin.signFile(api, existingFileSignatures) }
            fileSignatures.forEach {
                builder.addChunksSignature(it)
                onFileSigned(it)
            }
        }

        return errors
    }
}
