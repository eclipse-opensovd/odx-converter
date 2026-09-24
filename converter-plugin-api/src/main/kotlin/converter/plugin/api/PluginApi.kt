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

import java.util.logging.Logger

/**
 * Base API shared by all plugin types ([ConverterPlugin], [SigningPlugin], [VerificationPlugin],
 * ...). Provides access to common facilities every plugin may need, regardless of what it does.
 */
interface PluginApi {
    /**
     * Logger to be used when the information should be logged into a file
     */
    val logger: Logger

    /**
     * Retrieve a plugin-specific option value, set via the CLI's generic plugin-option
     * mechanism (e.g. the `--plugin-option` flag, available on the `convert`, `sign`, and
     * `verify` commands).
     *
     * Plugins are responsible for using their own plugin identifier as part of the key,
     * conventionally in the form `<plugin-id>.<key>` (e.g. `"compression.compress"`), to avoid
     * clashing with other plugins' options.
     *
     * @return the raw string value if set, or `null` if the option was not provided. Plugins are
     * responsible for parsing/validating the value themselves (e.g. via `toBooleanStrictOrNull()`).
     */
    fun getPluginOption(key: String): String?
}
