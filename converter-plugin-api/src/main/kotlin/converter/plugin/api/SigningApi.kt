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
 * API passed to [SigningPlugin]s. In addition to the common [PluginApi] facilities (e.g.
 * logging), gives access to the full mdd file being signed, through its builder, so a plugin can
 * inspect (or, if needed, modify) parts of the file beyond the individual chunk/signatures it was
 * explicitly called for (e.g. `ecuName`, `revision`, other chunks, metadata).
 */
interface SigningApi : PluginApi {
    /**
     * Allows access to the mdd-file being signed, through its builder.
     */
    val mddFile: MDDFile.Builder
}
