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

package converter.plugin.api

import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.Signature

/**
 * Plugin API for verifying signatures present in an already-converted (and signed) `.mdd` file.
 * Verification is entirely OEM/vendor specific (key/certificate material, algorithm, trust chain
 * validation, etc.), so this API only defines the contract used by the `verify` CLI command to
 * invoke vendor-provided plugins.
 *
 * Implementations need to be registered through a [VerificationPluginProvider], discovered via
 * `META-INF/services/converter.plugin.api.VerificationPluginProvider`.
 */
interface VerificationPlugin {
    /**
     * Unique identifier for a plugin -- should be human-readable and concise.
     * Vendor-specific plugins should be prefixed with `vendor-`, where vendor
     * may be the actual name of the vendor.
     */
    fun getPluginIdentifier(): String

    /**
     * List of signature algorithm identifiers this plugin is able to verify.
     */
    fun getSupportedAlgorithms(): List<String>

    /**
     * Verifies a single chunk-scoped [signature].
     *
     * @param api access to common plugin facilities (e.g. logging).
     * @param chunk the chunk [signature] belongs to. `chunk.signaturesList` contains all
     *   signatures present on this chunk (including [signature] itself).
     * @param signature the signature to verify.
     * @param data the raw (possibly compressed) payload of [chunk], exactly as stored in the mdd
     *   file, that the signature was computed over.
     */
    fun verifyChunkSignature(
        api: VerificationApi,
        chunk: Chunk,
        signature: Signature,
        data: ByteArray,
    ): VerificationResult

    /**
     * Verifies the whole-file [signature] (`MDDFile.chunksSignature`).
     *
     * @param api access to common plugin facilities (e.g. logging).
     * @param signature the whole-file signature to verify.
     */
    fun verifyFileSignature(
        api: VerificationApi,
        signature: Signature,
    ): VerificationResult
}
