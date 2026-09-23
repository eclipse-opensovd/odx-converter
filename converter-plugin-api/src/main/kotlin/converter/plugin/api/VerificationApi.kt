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

import org.eclipse.opensovd.cda.mdd.MDDFile

/**
 * API passed to [VerificationPlugin]s. In addition to the common [PluginApi] facilities (e.g.
 * logging), gives read-only access to the full mdd file being verified, so a plugin can inspect
 * parts of the file beyond the individual chunk/signature it was explicitly called for (e.g.
 * `ecuName`, `revision`, other chunks, metadata).
 */
interface VerificationApi : PluginApi {
    /**
     * Allows read-only access to the mdd-file being verified.
     */
    val mddFile: MDDFile
}
