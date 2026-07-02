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

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import schema.odx.BASEVARIANT
import schema.odx.BASEVARIANTREF
import schema.odx.BASEVARIANTS
import schema.odx.DIAGCOMMS
import schema.odx.DIAGDATADICTIONARYSPEC
import schema.odx.DIAGLAYERCONTAINER
import schema.odx.DIAGSERVICE
import schema.odx.ECUVARIANT
import schema.odx.ECUVARIANTS
import schema.odx.FUNCTIONALGROUP
import schema.odx.FUNCTIONALGROUPS
import schema.odx.ODX
import schema.odx.ODXLINK
import schema.odx.PROTOCOL
import schema.odx.PROTOCOLS
import schema.odx.REQUEST
import schema.odx.REQUESTS
import schema.odx.TABLE
import schema.odx.TABLEROW
import schema.odx.TABLES
import java.util.IdentityHashMap
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.Test

class ODXCollectionGroupTest {
    private val logger = Logger.getLogger("test")

    private fun createOdxWithBaseVariant(
        containerShortName: String,
        bvId: String,
        bvShortName: String,
        block: BASEVARIANT.() -> Unit = {},
    ): ODX {
        val odx = ODX()
        val dlc = DIAGLAYERCONTAINER()
        dlc.shortname = containerShortName
        val bv = BASEVARIANT()
        bv.id = bvId
        bv.shortname = bvShortName
        block(bv)
        dlc.basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
        odx.diaglayercontainer = dlc
        return odx
    }

    @Test
    fun `ecuName is derived from first base variant`() {
        val odx = createOdxWithBaseVariant("Container1", "bv1", "MyECU")
        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )
        assertThat(group.ecuName).isEqualTo("MyECU")
    }

    @Test
    fun `ecuName is functional_groups when only functional groups exist`() {
        val odx = ODX()
        val dlc = DIAGLAYERCONTAINER()
        dlc.shortname = "Container1"
        val fg = FUNCTIONALGROUP()
        fg.id = "fg1"
        fg.shortname = "FuncGroup1"
        dlc.functionalgroups = FUNCTIONALGROUPS().apply { functionalgroup.add(fg) }
        odx.diaglayercontainer = dlc

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )
        assertThat(group.ecuName).isEqualTo("functional_groups")
    }

    @Test
    fun `collections are created per ODX file`() {
        val odx1 = createOdxWithBaseVariant("Container1", "bv1", "ECU1")
        val odx2 = ODX()
        val dlc2 = DIAGLAYERCONTAINER()
        dlc2.shortname = "Container2"
        val ev = ECUVARIANT()
        ev.id = "ev1"
        ev.shortname = "EcuVar1"
        dlc2.ecuvariants = ECUVARIANTS().apply { ecuvariant.add(ev) }
        odx2.diaglayercontainer = dlc2

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx1, "file2.odx" to odx2),
                rawSize = 200,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )
        assertThat(group.collections).hasSize(2)
        assertThat(group.collections["Container1"]).isNotNull()
        assertThat(group.collections["Container2"]).isNotNull()
    }

    @Test
    fun `basevariants merges across files`() {
        val odx1 = createOdxWithBaseVariant("Container1", "bv1", "ECU1")
        val odx2 = createOdxWithBaseVariant("Container2", "bv2", "ECU2")

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx1, "file2.odx" to odx2),
                rawSize = 200,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )
        assertThat(group.basevariants).hasSize(2)
    }

    @Test
    fun `resolveRequest resolves via linkOwnership`() {
        val request = REQUEST()
        request.id = "req1"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "ECU1"
        bv.requests = REQUESTS().apply { this.request.add(request) }

        val odx = ODX()
        val dlc = DIAGLAYERCONTAINER()
        dlc.shortname = "Container1"
        dlc.basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
        odx.diaglayercontainer = dlc

        val link = ODXLINK()
        link.idref = "req1"

        val linkOwnership = IdentityHashMap<Any, String>()
        linkOwnership[link] = "file1.odx"

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = linkOwnership,
            )

        val resolved = group.resolveRequest(link)
        assertThat(resolved).isSameInstanceAs(request)
    }

    @Test
    fun `resolveRequest resolves via docref`() {
        val request = REQUEST()
        request.id = "req1"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "ECU1"
        bv.requests = REQUESTS().apply { this.request.add(request) }

        val odx = ODX()
        val dlc = DIAGLAYERCONTAINER()
        dlc.shortname = "Container1"
        dlc.basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
        odx.diaglayercontainer = dlc

        val link = ODXLINK()
        link.idref = "req1"
        link.docref = "Container1"

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        val resolved = group.resolveRequest(link)
        assertThat(resolved).isSameInstanceAs(request)
    }

    @Test
    fun `resolveRequest returns null for non-existent id`() {
        val odx = createOdxWithBaseVariant("Container1", "bv1", "ECU1")

        val link = ODXLINK()
        link.idref = "nonexistent"
        link.docref = "Container1"

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(lenient = true),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.resolveRequest(link)).isNull()
    }

    @Test
    fun `cross-file resolution works with docref`() {
        // File 1 has a request
        val request = REQUEST()
        request.id = "req1"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "ECU1"
        bv.requests = REQUESTS().apply { this.request.add(request) }

        val odx1 = ODX()
        val dlc1 = DIAGLAYERCONTAINER()
        dlc1.shortname = "Container1"
        dlc1.basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
        odx1.diaglayercontainer = dlc1

        // File 2 has a link with docref pointing to Container1
        val odx2 = ODX()
        val dlc2 = DIAGLAYERCONTAINER()
        dlc2.shortname = "Container2"
        dlc2.ecuvariants =
            ECUVARIANTS().apply {
                ecuvariant.add(
                    ECUVARIANT().apply {
                        id = "ev1"
                        shortname = "Var1"
                    },
                )
            }
        odx2.diaglayercontainer = dlc2

        val link = ODXLINK()
        link.idref = "req1"
        link.docref = "Container1"

        val linkOwnership = IdentityHashMap<Any, String>()
        linkOwnership[link] = "file2.odx"

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx1, "file2.odx" to odx2),
                rawSize = 200,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = linkOwnership,
            )

        assertThat(group.resolveRequest(link)).isSameInstanceAs(request)
    }

    @Test
    fun `collectionFor returns correct collection for tracked object`() {
        val odx = createOdxWithBaseVariant("Container1", "bv1", "ECU1")
        val link = ODXLINK()

        val linkOwnership = IdentityHashMap<Any, String>()
        linkOwnership[link] = "file1.odx"

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = linkOwnership,
            )

        val collection = group.collectionFor(link)
        assertThat(collection).isNotNull()
        assertThat(collection!!.containerKey).isEqualTo("Container1")
    }

    @Test
    fun `collectionFor returns null for untracked object`() {
        val odx = createOdxWithBaseVariant("Container1", "bv1", "ECU1")

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.collectionFor("untracked")).isNull()
    }

    @Test
    fun `diagServices merges across all collections`() {
        val service1 = DIAGSERVICE()
        service1.id = "ds1"
        service1.shortname = "Service1"

        val service2 = DIAGSERVICE()
        service2.id = "ds2"
        service2.shortname = "Service2"

        val bv1 =
            BASEVARIANT().apply {
                id = "bv1"
                shortname = "ECU1"
                diagcomms = DIAGCOMMS().apply { diagcommproxy.add(service1) }
            }
        val bv2 =
            BASEVARIANT().apply {
                id = "bv2"
                shortname = "ECU2"
                diagcomms = DIAGCOMMS().apply { diagcommproxy.add(service2) }
            }

        val odx1 =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container1"
                        basevariants = BASEVARIANTS().apply { basevariant.add(bv1) }
                    }
            }
        val odx2 =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container2"
                        basevariants = BASEVARIANTS().apply { basevariant.add(bv2) }
                    }
            }

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx1, "file2.odx" to odx2),
                rawSize = 200,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.diagServices).hasSize(2)
    }

    @Test
    fun `resolveProtocolByShortName resolves across collections`() {
        val protocol = PROTOCOL()
        protocol.id = "proto1"
        protocol.shortname = "UDS"

        val odx =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container1"
                        protocols = PROTOCOLS().apply { this.protocol.add(protocol) }
                    }
            }

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.resolveProtocolByShortName("UDS")).isSameInstanceAs(protocol)
        assertThat(group.resolveProtocolByShortName("NonExistent")).isNull()
    }

    @Test
    fun `resolveRequest resolves when docref matches a layer short name`() {
        // Container short name is "Container1" but the BV short name is "MyVariant".
        // A link with docref="MyVariant" should resolve via layerNameToCollection fallback.
        val request = REQUEST()
        request.id = "req1"

        val bv =
            BASEVARIANT().apply {
                id = "bv1"
                shortname = "MyVariant"
                requests = REQUESTS().apply { this.request.add(request) }
            }

        val odx =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container1"
                        basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
                    }
            }

        val link =
            ODXLINK().apply {
                idref = "req1"
                docref = "MyVariant" // layer short name, not container name
            }

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.resolveRequest(link)).isSameInstanceAs(request)
    }

    @Test
    fun `resolveTableRow resolves via docref`() {
        val row =
            TABLEROW().apply {
                id = "tr1"
                shortname = "Row1"
                key = "1"
            }

        val table =
            TABLE().apply {
                id = "t1"
                shortname = "TestTable"
                rowwrapper.add(row)
            }

        val ddd = DIAGDATADICTIONARYSPEC().apply { tables = TABLES().apply { this.table.add(table) } }

        val bv =
            BASEVARIANT().apply {
                id = "bv1"
                shortname = "ECU1"
                diagdatadictionaryspec = ddd
            }

        val odx =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container1"
                        basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
                    }
            }

        val link =
            ODXLINK().apply {
                idref = "tr1"
                docref = "Container1"
            }

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.resolveTableRow(link)).isSameInstanceAs(row)
    }

    @Test
    fun `resolveParent resolves base variant when docref matches layer short name`() {
        val bv =
            BASEVARIANT().apply {
                id = "bv1"
                shortname = "MyVariant"
            }

        val odx =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container1"
                        basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
                    }
            }

        val ref =
            BASEVARIANTREF().apply {
                idref = "bv1"
                docref = "MyVariant" // layer short name, not container name
            }

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.resolveParent(ref)).isSameInstanceAs(bv)
    }

    @Test
    fun `resolveParent returns null when docref is unknown`() {
        val odx = createOdxWithBaseVariant("Container1", "bv1", "ECU1")

        val ref =
            BASEVARIANTREF().apply {
                idref = "bv1"
                docref = "UnknownContainer"
            }

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx),
                rawSize = 100,
                options = ConverterOptions(lenient = true),
                logger = logger,
                linkOwnership = IdentityHashMap(),
            )

        assertThat(group.resolveParent(ref)).isNull()
    }

    @Test
    fun `layerNameToCollection logs warning for duplicate layer short names`() {
        // Two BVs in different containers share the same short name.
        val odx1 =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container1"
                        basevariants =
                            BASEVARIANTS().apply {
                                basevariant.add(
                                    BASEVARIANT().apply {
                                        id = "bv1"
                                        shortname = "SharedLayerName"
                                    },
                                )
                            }
                    }
            }
        val odx2 =
            ODX().apply {
                diaglayercontainer =
                    DIAGLAYERCONTAINER().apply {
                        shortname = "Container2"
                        basevariants =
                            BASEVARIANTS().apply {
                                basevariant.add(
                                    BASEVARIANT().apply {
                                        id = "bv2"
                                        shortname = "SharedLayerName" // duplicate!
                                    },
                                )
                            }
                    }
            }

        val warnings = mutableListOf<String>()
        val capturingHandler =
            object : Handler() {
                override fun publish(record: LogRecord) {
                    warnings.add(record.message)
                }

                override fun flush() {}

                override fun close() {}
            }
        val testLogger = Logger.getLogger("duplicate-layer-test")
        testLogger.addHandler(capturingHandler)

        val group =
            ODXCollectionGroup(
                data = mapOf("file1.odx" to odx1, "file2.odx" to odx2),
                rawSize = 200,
                options = ConverterOptions(lenient = true),
                logger = testLogger,
                linkOwnership = IdentityHashMap(),
            )

        // Trigger layerNameToCollection initialisation by resolving via the duplicate layer name.
        val link =
            ODXLINK().apply {
                idref = "nonexistent"
                docref = "SharedLayerName"
            }
        group.resolveRequest(link)

        assertThat(warnings.any { it.contains("Duplicate layer short-name") }).isTrue()
    }
}
