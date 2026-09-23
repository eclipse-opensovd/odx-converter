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
     * `Signature.algorithm` values it may set, and for CLI `--algorithm` filtering).
     */
    fun getSupportedAlgorithms(): List<String>

    /**
     * Called by the `sign` command for every chunk of the file, when chunk-scoped signing was
     * requested.
     *
     * @param api access to common plugin facilities (e.g. logging).
     * @param chunk the chunk this signature is being requested for. `chunk.signaturesList` already
     *   contains any signatures present before this call (e.g. to detect the chunk has already
     *   been signed).
     * @param data the raw (possibly compressed) payload of [chunk], exactly as stored in the mdd
     *   file.
     * @param options free-form key/value options passed through from the CLI (e.g. key/keystore
     *   location, key identifiers, PIN, etc.) - entirely defined by the plugin.
     * @return zero, one, or multiple signatures to add to `chunk.signatures`. An empty list means
     *   this plugin does not want to add a signature (e.g. already signed, or not applicable).
     */
    fun signChunk(
        api: SigningApi,
        chunk: Chunk,
        data: ByteArray,
        options: Map<String, String>,
    ): List<Signature>

    /**
     * Called by the `sign` command once per file, when file-scoped signing was requested.
     *
     * Note: the current mdd format only supports a single whole-file signature
     * (`MDDFile.chunksSignature`). This method still returns a [List] for forward compatibility
     * with a planned format change to support multiple file-level signatures - the `sign`
     * orchestrator currently enforces that the combined result of all invoked plugins contains at
     * most one signature.
     *
     * @param api access to common plugin facilities (e.g. logging).
     * @param existingSignatures the whole-file signature(s) already present before this call (0 or
     *   1 element with the current mdd format).
     * @param options free-form key/value options passed through from the CLI.
     * @return zero or more signatures for the whole file.
     */
    fun signFile(
        api: SigningApi,
        existingSignatures: List<Signature>,
        options: Map<String, String>,
    ): List<Signature>
}
