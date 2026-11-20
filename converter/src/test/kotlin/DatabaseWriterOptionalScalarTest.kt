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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dataformat.Param
import org.eclipse.persistence.jaxb.JAXBContextFactory
import org.junit.jupiter.api.io.TempDir
import schema.odx.ODX
import java.io.File
import java.util.logging.Logger
import kotlin.test.BeforeTest
import kotlin.test.Test

class DatabaseWriterOptionalScalarTest {
    @TempDir
    lateinit var tempDir: File

    private lateinit var params: Map<String, Param>

    @BeforeTest
    fun convertPositions() {
        val pdx = File(tempDir, "positions.pdx")
        packPdx("/synthetic-odx-positions", pdx)
        val mdd = File(tempDir, "positions.mdd")
        val context = JAXBContextFactory.createContext(arrayOf(ODX::class.java), null)
        FileConverter(Logger.getLogger("positions-test"), context)
            .convert(pdx, mdd, ConverterOptions(), mutableListOf())

        val ecu = readMddEcuData(mdd)
        val layer = requireNotNull(ecu.variants(0)?.diagLayer)
        val request = requireNotNull(layer.diagServices(0)?.request)
        params =
            (0 until request.paramsLength)
                .map { requireNotNull(request.params(it)) }
                .associateBy { requireNotNull(it.shortName) }
    }

    @Test
    fun `shared-byte fields retain explicit zero byte positions`() {
        for (name in listOf("FirstFlag", "SecondFlag", "ThirdFlag")) {
            assertThat(params.getValue(name).bytePosition).isEqualTo(0u)
        }
    }

    @Test
    fun `explicit zero and nonzero bit positions are preserved`() {
        assertThat(params.getValue("FirstFlag").bitPosition).isEqualTo(0u)
        assertThat(params.getValue("SecondFlag").bitPosition).isEqualTo(2u)
        assertThat(params.getValue("ThirdFlag").bitPosition).isEqualTo(4u)
        assertThat(params.getValue("PositionedField").bytePosition).isEqualTo(1u)
        assertThat(params.getValue("PositionedField").bitPosition).isNull()
    }

    @Test
    fun `unspecified positions remain absent for implicit positioning`() {
        assertThat(params.getValue("ImplicitField").bytePosition).isNull()
        assertThat(params.getValue("ImplicitField").bitPosition).isNull()
    }
}
