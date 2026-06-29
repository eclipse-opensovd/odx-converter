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
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import schema.odx.BASEVARIANT
import schema.odx.BASEVARIANTS
import schema.odx.DATAOBJECTPROP
import schema.odx.DATAOBJECTPROPS
import schema.odx.DIAGCOMMS
import schema.odx.DIAGDATADICTIONARYSPEC
import schema.odx.DIAGLAYERCONTAINER
import schema.odx.DIAGSERVICE
import schema.odx.ECUSHAREDDATA
import schema.odx.ECUSHAREDDATAS
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
import schema.odx.SINGLEECUJOB
import schema.odx.STRUCTURE
import schema.odx.STRUCTURES
import schema.odx.TABLE
import schema.odx.TABLEROW
import schema.odx.TABLES
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ODXCollectionTest {
    private fun createOdxWithDiagLayerContainer(block: DIAGLAYERCONTAINER.() -> Unit): ODX {
        val odx = ODX()
        val dlc = DIAGLAYERCONTAINER()
        dlc.shortname = "TestContainer"
        block(dlc)
        odx.diaglayercontainer = dlc
        return odx
    }

    @Test
    fun `containerKey returns diaglayercontainer short name`() {
        val odx = createOdxWithDiagLayerContainer {}
        val collection = ODXCollection(odx)
        assertThat(collection.containerKey).isEqualTo("TestContainer")
    }

    @Test
    fun `containerKey returns comparamsubset short name when no diaglayercontainer`() {
        val odx = ODX()
        val subset = schema.odx.COMPARAMSUBSET()
        subset.shortname = "ComParamSubset1"
        subset.id = "cs1"
        odx.comparamsubset = subset
        val collection = ODXCollection(odx)
        assertThat(collection.containerKey).isEqualTo("ComParamSubset1")
    }

    @Test
    fun `containerKey throws when no recognized container`() {
        val odx = ODX()
        val collection = ODXCollection(odx)
        assertFailsWith<IllegalStateException> {
            collection.containerKey
        }
    }

    @Test
    fun `basevariants are indexed by id`() {
        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.basevariants).hasSize(1)
        assertThat(collection.basevariants["bv1"]).isSameInstanceAs(bv)
    }

    @Test
    fun `ecuvariants are indexed by id`() {
        val ev = ECUVARIANT()
        ev.id = "ev1"
        ev.shortname = "EcuVar1"

        val odx =
            createOdxWithDiagLayerContainer {
                ecuvariants = ECUVARIANTS().apply { ecuvariant.add(ev) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.ecuvariants).hasSize(1)
        assertThat(collection.ecuvariants["ev1"]).isSameInstanceAs(ev)
    }

    @Test
    fun `diagServices are collected from all diag layers`() {
        val service = DIAGSERVICE()
        service.id = "ds1"
        service.shortname = "ReadDTC"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.diagcomms = DIAGCOMMS().apply { diagcommproxy.add(service) }

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.diagServices).hasSize(1)
        assertThat(collection.diagServices["ds1"]).isSameInstanceAs(service)
    }

    @Test
    fun `singleEcuJobs are collected from diag layers`() {
        val job = SINGLEECUJOB()
        job.id = "job1"
        job.shortname = "TestJob"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.diagcomms = DIAGCOMMS().apply { diagcommproxy.add(job) }

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.singleEcuJobs).hasSize(1)
        assertThat(collection.singleEcuJobs["job1"]).isSameInstanceAs(job)
    }

    @Test
    fun `requests are collected from diag layers`() {
        val request = REQUEST()
        request.id = "req1"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.requests = REQUESTS().apply { this.request.add(request) }

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.requests).hasSize(1)
        assertThat(collection.requests["req1"]).isSameInstanceAs(request)
    }

    @Test
    fun `dataObjectProps are indexed from diagDataDictionaries`() {
        val dop = DATAOBJECTPROP()
        dop.id = "dop1"
        dop.shortname = "MyDOP"

        val ddd = DIAGDATADICTIONARYSPEC()
        ddd.dataobjectprops = DATAOBJECTPROPS().apply { dataobjectprop.add(dop) }

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.diagdatadictionaryspec = ddd

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.dataObjectProps).hasSize(1)
        assertThat(collection.dataObjectProps["dop1"]).isSameInstanceAs(dop)
    }

    @Test
    fun `resolveDopByShortName finds DOP by short name`() {
        val dop = DATAOBJECTPROP()
        dop.id = "dop1"
        dop.shortname = "MyDOP"

        val ddd = DIAGDATADICTIONARYSPEC()
        ddd.dataobjectprops = DATAOBJECTPROPS().apply { dataobjectprop.add(dop) }

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.diagdatadictionaryspec = ddd

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.resolveDopByShortName("MyDOP")).isSameInstanceAs(dop)
        assertThat(collection.resolveDopByShortName("NonExistent")).isNull()
    }

    @Test
    fun `resolveDiagServiceByShortName finds service by short name`() {
        val service = DIAGSERVICE()
        service.id = "ds1"
        service.shortname = "ReadDTC"

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.diagcomms = DIAGCOMMS().apply { diagcommproxy.add(service) }

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.resolveDiagServiceByShortName("ReadDTC")).isSameInstanceAs(service)
        assertThat(collection.resolveDiagServiceByShortName("NonExistent")).isNull()
    }

    @Test
    fun `structures are indexed from diagDataDictionaries`() {
        val structure = STRUCTURE()
        structure.id = "struct1"
        structure.shortname = "MyStruct"

        val ddd = DIAGDATADICTIONARYSPEC()
        ddd.structures = STRUCTURES().apply { this.structure.add(structure) }

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BaseVar1"
        bv.diagdatadictionaryspec = ddd

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.structures).hasSize(1)
        assertThat(collection.resolveStructureByShortName("MyStruct")).isSameInstanceAs(structure)
    }

    @Test
    fun `protocols are indexed by id`() {
        val protocol = PROTOCOL()
        protocol.id = "proto1"
        protocol.shortname = "UDS"

        val odx =
            createOdxWithDiagLayerContainer {
                protocols = PROTOCOLS().apply { this.protocol.add(protocol) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.protocols).hasSize(1)
        assertThat(collection.protocols["proto1"]).isSameInstanceAs(protocol)
        assertThat(collection.resolveProtocolByShortName("UDS")).isSameInstanceAs(protocol)
    }

    @Test
    fun `empty collection returns empty maps`() {
        val odx = createOdxWithDiagLayerContainer {}
        val collection = ODXCollection(odx)
        assertThat(collection.basevariants).isEmpty()
        assertThat(collection.ecuvariants).isEmpty()
        assertThat(collection.diagServices).isEmpty()
        assertThat(collection.singleEcuJobs).isEmpty()
        assertThat(collection.requests).isEmpty()
        assertThat(collection.dataObjectProps).isEmpty()
        assertThat(collection.protocols).isEmpty()
    }

    @Test
    fun `functionalGroups are indexed by id`() {
        val fg = FUNCTIONALGROUP()
        fg.id = "fg1"
        fg.shortname = "FuncGroup1"

        val odx =
            createOdxWithDiagLayerContainer {
                functionalgroups = FUNCTIONALGROUPS().apply { functionalgroup.add(fg) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.functionalGroups).hasSize(1)
        assertThat(collection.functionalGroups["fg1"]).isSameInstanceAs(fg)
    }

    @Test
    fun `ecuSharedDatas are indexed by id`() {
        val esd = ECUSHAREDDATA()
        esd.id = "esd1"
        esd.shortname = "SharedData1"

        val odx =
            createOdxWithDiagLayerContainer {
                ecushareddatas = ECUSHAREDDATAS().apply { ecushareddata.add(esd) }
            }

        val collection = ODXCollection(odx)
        assertThat(collection.ecuSharedDatas).hasSize(1)
        assertThat(collection.ecuSharedDatas["esd1"]).isSameInstanceAs(esd)
    }

    private fun createOdxWithTableRow(
        tableId: String = "t1",
        tableShortName: String = "TestTable",
        rowId: String = "tr1",
        rowShortName: String = "Row1",
        additionalRowwrapperItems: List<Any> = emptyList(),
    ): Pair<ODXCollection, TABLEROW> {
        val row = TABLEROW()
        row.id = rowId
        row.shortname = rowShortName
        row.key = "1"

        val table = TABLE()
        table.id = tableId
        table.shortname = tableShortName
        table.rowwrapper.add(row)
        additionalRowwrapperItems.forEach { table.rowwrapper.add(it) }

        val ddd = DIAGDATADICTIONARYSPEC()
        ddd.tables = TABLES().apply { this.table.add(table) }

        val bv = BASEVARIANT()
        bv.id = "bv1"
        bv.shortname = "BV1"
        bv.diagdatadictionaryspec = ddd

        val odx =
            createOdxWithDiagLayerContainer {
                basevariants = BASEVARIANTS().apply { basevariant.add(bv) }
            }

        return ODXCollection(odx) to row
    }

    @Test
    fun `tableRows filters out ODXLINK row references`() {
        val odxLink = ODXLINK()
        odxLink.idref = "tr_external"

        val (collection, row) = createOdxWithTableRow(additionalRowwrapperItems = listOf(odxLink))

        // Only the inline TABLEROW should appear; the ODXLINK row-ref is silently skipped.
        assertThat(collection.tableRows).hasSize(1)
        assertThat(collection.tableRows["tr1"]).isSameInstanceAs(row)
    }

    @Test
    fun `tableRowsByShortName indexes rows by short name`() {
        val (collection, row) = createOdxWithTableRow()

        assertThat(collection.tableRowsByShortName["Row1"]).isSameInstanceAs(row)
        assertThat(collection.tableRowsByShortName["NonExistent"]).isNull()
    }

    @Test
    fun `resolveTableRowByShortName finds row by short name`() {
        val (collection, row) = createOdxWithTableRow()

        assertThat(collection.resolveTableRowByShortName("Row1")).isSameInstanceAs(row)
        assertThat(collection.resolveTableRowByShortName("NonExistent")).isNull()
    }
}
