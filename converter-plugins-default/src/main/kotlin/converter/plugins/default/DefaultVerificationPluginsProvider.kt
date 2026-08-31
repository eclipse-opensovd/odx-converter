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

import converter.plugin.api.VerificationPlugin
import converter.plugin.api.VerificationPluginProvider

/**
 * Provides the default verification plugins
 */
class DefaultVerificationPluginsProvider : VerificationPluginProvider {
    override fun getPlugins(): List<VerificationPlugin> =
        listOf(
            HashVerificationPlugin(),
        )
}
