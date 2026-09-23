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
 * Provider interface to return a list of verification plugins, plugins need to provide the
 * implementation in a `META-INF/services/converter.plugin.api.VerificationPluginProvider` file.
 *
 * The `verify` command uses the ServiceLoader mechanism to get these Provider-Implementations, and
 * retrieve the plugins.
 */
interface VerificationPluginProvider {
    fun getPlugins(): List<VerificationPlugin>
}
