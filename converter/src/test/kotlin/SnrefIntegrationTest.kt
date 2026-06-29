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
import assertk.assertions.contains
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
import dataformat.EcuData
import jakarta.xml.bind.JAXBContext
import schema.odx.ODX
import java.io.File
import java.util.logging.Logger
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Integration tests for SNREF-style TABLE-KEY references and TABLE-ROW-REF in TABLE rowwrappers.
 *
 * Packs the synthetic-odx-snref fixtures into a PDX archive, runs the full converter, and
 * verifies that services using TABLE-SNREF, TABLE-ROW-SNREF, and TABLE-ROW-REF are written
 * to the output without errors.
 */
class SnrefIntegrationTest {
    private lateinit var ecuData: EcuData
    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = createTempDirectory("snref-integration-test").toFile()
        tempDir.deleteOnExit()

        val pdxFile = File(tempDir, "snref.pdx")
        packPdx("/synthetic-odx-snref", pdxFile)

        val mddOutputFile = File(tempDir, "snref.mdd")
        val context: JAXBContext =
            org.eclipse.persistence.jaxb.JAXBContextFactory
                .createContext(arrayOf(ODX::class.java), null)

        val logger = Logger.getLogger("snref-integration-test")
        val converter = FileConverter(logger, context)
        val stats = mutableListOf<ChunkStat>()
        converter.convert(pdxFile, mddOutputFile, ConverterOptions(), stats)

        assertThat(mddOutputFile.exists()).isTrue()
        assertThat(mddOutputFile.length()).isGreaterThan(0)

        ecuData = readMddEcuData(mddOutputFile)
    }

    @Test
    fun `converter succeeds with TABLE-ROW-REF in table rowwrapper`() {
        // The presence of the base variant in the output confirms that TableA (which contains
        // a TABLE-ROW-REF alongside an inline TABLE-ROW) was written without errors.
        val variantNames =
            (0 until ecuData.variantsLength).map {
                ecuData.variants(it)?.diagLayer?.shortName
            }
        assertThat(variantNames).contains("SnrefECU")
    }

    @Test
    fun `service with TABLE-SNREF table key is written to output`() {
        val variant =
            (0 until ecuData.variantsLength)
                .map { ecuData.variants(it) }
                .first { it?.diagLayer?.shortName == "SnrefECU" }!!

        val serviceNames =
            (0 until variant.diagLayer!!.diagServicesLength).map {
                variant.diagLayer!!
                    .diagServices(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(serviceNames).contains("ReadWithTableSnref")
    }

    @Test
    fun `service with TABLE-ROW-SNREF table key is written to output`() {
        val variant =
            (0 until ecuData.variantsLength)
                .map { ecuData.variants(it) }
                .first { it?.diagLayer?.shortName == "SnrefECU" }!!

        val serviceNames =
            (0 until variant.diagLayer!!.diagServicesLength).map {
                variant.diagLayer!!
                    .diagServices(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(serviceNames).contains("ReadWithTableRowSnref")
    }
}

/**
 * Integration test for the lenient-mode multi-entry TABLE-KEY handling.
 *
 * A TABLE-KEY with more than one entry in its rest list is not supported by the MDD file
 * format.  With lenient=true the converter logs a warning and uses only the first entry
 * instead of throwing, allowing conversion to complete.
 */
class LenientTableKeyTest {
    private lateinit var ecuData: EcuData

    @BeforeTest
    fun setUp() {
        val tempDir = createTempDirectory("lenient-tablekey-test").toFile()
        tempDir.deleteOnExit()

        val pdxFile = File(tempDir, "lenient.pdx")
        packPdx("/synthetic-odx-lenient", pdxFile)

        val mddOutputFile = File(tempDir, "lenient.mdd")
        val context: JAXBContext =
            org.eclipse.persistence.jaxb.JAXBContextFactory
                .createContext(arrayOf(ODX::class.java), null)

        val logger = Logger.getLogger("lenient-tablekey-test")
        val converter = FileConverter(logger, context)
        val stats = mutableListOf<ChunkStat>()
        converter.convert(pdxFile, mddOutputFile, ConverterOptions(lenient = true), stats)

        assertThat(mddOutputFile.exists()).isTrue()
        assertThat(mddOutputFile.length()).isGreaterThan(0)

        ecuData = readMddEcuData(mddOutputFile)
    }

    @Test
    fun `converter succeeds when TABLE-KEY has multiple entries in lenient mode`() {
        // If the converter reached this point the multi-entry TABLE-KEY was handled gracefully.
        val variantNames =
            (0 until ecuData.variantsLength).map {
                ecuData.variants(it)?.diagLayer?.shortName
            }
        assertThat(variantNames).contains("LenientECU")
    }

    @Test
    fun `service with multi-entry TABLE-KEY is present in output`() {
        val variant =
            (0 until ecuData.variantsLength)
                .map { ecuData.variants(it) }
                .first { it?.diagLayer?.shortName == "LenientECU" }!!

        val serviceNames =
            (0 until variant.diagLayer!!.diagServicesLength).map {
                variant.diagLayer!!
                    .diagServices(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(serviceNames).contains("ReadWithMultiEntryTableKey")
    }
}
