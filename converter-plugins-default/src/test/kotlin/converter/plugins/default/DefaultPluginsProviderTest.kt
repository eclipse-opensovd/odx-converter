/*
 * Copyright (c) 2025 The Contributors to Eclipse OpenSOVD (see CONTRIBUTORS)
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

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isInstanceOf
import kotlin.test.Test

class DefaultPluginsProviderTest {
    @Test
    fun `getPlugins returns list with single CompressionPlugin`() {
        val provider = DefaultPluginsProvider()
        val plugins = provider.getPlugins()
        assertThat(plugins).hasSize(1)
        assertThat(plugins[0]).isInstanceOf<CompressionPlugin>()
    }
}
