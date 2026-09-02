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
 * Plugin API for signing already-converted `.mdd` files. Signing is entirely OEM/vendor specific
 * (key material, algorithm, HSM/keystore access, etc.), so this API only defines the contract used
 * by the `sign` CLI command to invoke vendor-provided plugins.
 *
 * Implementations need to be registered through a [SigningPluginProvider], discovered via
 * `META-INF/services/converter.plugin.api.SigningPluginProvider`.
 */
interface SigningPlugin {
    /**
     * Unique identifier for a plugin -- should be human-readable and concise.
     * Vendor-specific plugins should be prefixed with `vendor-`, where vendor
     * may be the actual name of the vendor.
     */
    fun getPluginIdentifier(): String

    /**
     * List of signature algorithm identifiers this plugin is able to produce (used for
     * `Signature.algorithmIdentifier` values it may set, and for CLI `--algorithm` filtering).
     */
    fun getSupportedAlgorithms(): List<String>

    /**
     * Called by the `sign` command for every chunk of the file, when chunk-scoped signing was
     * requested.
     *
     * @param api access to common plugin facilities (e.g. logging, plugin-specific options via
     *   [PluginApi.getPluginOption]).
     * @param chunk the chunk this signature is being requested for. `chunk.signaturesList` already
     *   contains any signatures present before this call (e.g. to detect the chunk has already
     *   been signed).
     * @param data the raw (possibly compressed) payload of [chunk], exactly as stored in the mdd
     *   file.
     * @return zero, one, or multiple signatures to add to `chunk.signatures`. An empty list means
     *   this plugin does not want to add a signature (e.g. already signed, or not applicable).
     */
    fun signChunk(
        api: SigningApi,
        chunk: Chunk,
        data: ByteArray,
    ): List<Signature>

    /**
     * Called by the `sign` command once per file, when file-scoped signing was requested.
     *
     * The returned signatures are appended to `MDDFile.chunksSignatureList`, which supports
     * multiple whole-file signatures (e.g. using different algorithms).
     *
     * @param api access to common plugin facilities (e.g. logging, plugin-specific options via
     *   [PluginApi.getPluginOption]).
     * @param existingSignatures the whole-file signature(s) already present before this call.
     * @return zero or more signatures for the whole file.
     */
    fun signFile(
        api: SigningApi,
        existingSignatures: List<Signature>,
    ): List<Signature>
}
