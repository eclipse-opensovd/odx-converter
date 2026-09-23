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

/**
 * Result of a signature verification, as performed by a [VerificationPlugin].
 */
enum class VerificationResult {
    /** The signature was successfully verified against the data/certificates. */
    VALID,

    /** The signature was checked, and does NOT match the data/certificates. */
    INVALID,

    /** No plugin was able to handle the signature's algorithm. */
    UNSUPPORTED_ALGORITHM,
}
