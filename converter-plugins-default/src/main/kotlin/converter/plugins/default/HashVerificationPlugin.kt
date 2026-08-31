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
package converter.plugins.default

import converter.plugin.api.VerificationApi
import converter.plugin.api.VerificationPlugin
import converter.plugin.api.VerificationResult
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.Signature
import java.security.MessageDigest

/**
 * Default verification plugin, verifies the `sha512_uncompressed` chunk hashes added by
 * [CompressionPlugin]. This is not a cryptographic signature (no key material or trust involved),
 * it only detects accidental corruption/mismatch between the stored (possibly compressed) payload
 * and its recorded hash of the original uncompressed data.
 */
class HashVerificationPlugin : VerificationPlugin {
    override fun getPluginIdentifier(): String = "compression"

    override fun getSupportedAlgorithms(): List<String> = listOf(ALGORITHM)

    override fun verifyChunkSignature(
        api: VerificationApi,
        chunk: Chunk,
        signature: Signature,
        data: ByteArray,
    ): VerificationResult {
        if (signature.algorithm != ALGORITHM) {
            return VerificationResult.UNSUPPORTED_ALGORITHM
        }

        val uncompressed =
            try {
                decompress(chunk, data)
            } catch (e: Exception) {
                api.logger.warning(
                    "Failed to decompress chunk '${chunk.name}' (${chunk.type}) while verifying " +
                        "'$ALGORITHM' signature: ${e.message}",
                )
                return VerificationResult.INVALID
            }
        val digest = MessageDigest.getInstance("SHA-512").digest(uncompressed)
        return if (digest.contentEquals(signature.signature.toByteArray())) {
            VerificationResult.VALID
        } else {
            VerificationResult.INVALID
        }
    }

    override fun verifyFileSignature(
        api: VerificationApi,
        signature: Signature,
    ): VerificationResult =
        // CompressionPlugin never produces a whole-file signature, only chunk-scoped hashes.
        VerificationResult.UNSUPPORTED_ALGORITHM

    private fun decompress(
        chunk: Chunk,
        raw: ByteArray,
    ): ByteArray {
        if (!chunk.hasCompressionAlgorithm()) {
            return raw
        }
        return when (chunk.compressionAlgorithm) {
            "lzma" -> LZMACompressorInputStream(raw.inputStream()).use { it.readAllBytes() }
            else -> error("Unsupported compression algorithm '${chunk.compressionAlgorithm}' for chunk '${chunk.name}'")
        }
    }

    companion object {
        private const val ALGORITHM = "sha512_uncompressed"
    }
}
