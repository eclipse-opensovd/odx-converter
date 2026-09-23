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
     * @return a list of human-readable error messages; empty if everything succeeded. Currently,
     *   the only possible error is more than one signing plugin producing a whole-file signature
     *   (the mdd format only supports a single whole-file signature).
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
        val api = SigningApiHandler(builder, logger)

        if (scope == "chunk" || scope == "both") {
            builder.chunksBuilderList.forEach { chunkBuilder ->
                // chunk already carries its own (existing) signaturesList, which plugins can inspect
                val chunk = chunkBuilder.build()
                val data = chunk.data.toByteArray()
                plugins.forEach { plugin ->
                    val signatures = plugin.signChunk(api, chunk, data, options)
                    signatures.forEach { chunkBuilder.addSignatures(it) }
                    if (signatures.isNotEmpty()) {
                        onChunkSigned(chunk, plugin, signatures.size)
                    }
                }
            }
        }

        if (scope == "file" || scope == "both") {
            val existingFileSignatures = if (builder.hasChunksSignature()) listOf(builder.chunksSignature) else emptyList()
            val fileSignatures = plugins.flatMap { plugin -> plugin.signFile(api, existingFileSignatures, options) }
            if (fileSignatures.size > 1) {
                errors.add(
                    "Multiple signing plugins produced a whole-file signature, but the current mdd format only " +
                        "supports a single whole-file signature. Use --algorithm to disambiguate.",
                )
            } else {
                fileSignatures.firstOrNull()?.let {
                    builder.chunksSignature = it
                    onFileSigned(it)
                }
            }
        }

        return errors
    }
}
