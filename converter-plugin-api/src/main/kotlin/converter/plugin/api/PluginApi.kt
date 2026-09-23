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
}
