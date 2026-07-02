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

import schema.odx.ADDITIONALAUDIENCE
import schema.odx.BASEVARIANT
import schema.odx.COMPARAM
import schema.odx.COMPLEXCOMPARAM
import schema.odx.DIAGSERVICE
import schema.odx.ECUSHAREDDATA
import schema.odx.ECUVARIANT
import schema.odx.ECUVARIANTPATTERN
import schema.odx.FUNCTCLASS
import schema.odx.FUNCTIONALGROUP
import schema.odx.INPUTPARAM
import schema.odx.LIBRARY
import schema.odx.MATCHINGBASEVARIANTPARAMETER
import schema.odx.MATCHINGPARAMETER
import schema.odx.NEGOUTPUTPARAM
import schema.odx.OUTPUTPARAM
import schema.odx.PHYSICALDIMENSION
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.SINGLEECUJOB
import schema.odx.STATE
import schema.odx.STATECHART
import schema.odx.STATETRANSITION
import schema.odx.TABLE
import schema.odx.TABLEROW
import schema.odx.UNITGROUP
import schema.odx.VALUE
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ResolutionErrorTest {
    @Test
    fun `message contains all context fields`() {
        val message =
            resolutionMessage(
                ResolutionContext(
                    expected = "REQUEST",
                    refKind = "ODXLINK",
                    refValue = "EV_Foo.REQ_Bar",
                    docref = "BV_Foo",
                    scopeSearched = "BV_Foo",
                    sourceFile = "bv_foo.odx-d",
                    logicalPath = "EcuVariant_X / Service_Y",
                    candidates = listOf("EV_Foo.REQ_Baz", "EV_Foo.REQ_Other"),
                ),
            )

        assertContains(message, "Failed to resolve REQUEST via ODXLINK 'EV_Foo.REQ_Bar'")
        assertContains(message, "in:")
        assertContains(message, "EcuVariant_X > Service_Y")
        assertContains(message, "(file: bv_foo.odx-d)")
        assertContains(message, "searched:")
        assertContains(message, "BV_Foo (type: REQUEST, 2 entries)")
        assertContains(message, "docref:   BV_Foo")
    }

    @Test
    fun `message shows entry count from candidates`() {
        val message =
            resolutionMessage(
                ResolutionContext(
                    expected = "STRUCTURE",
                    refKind = "SNREF",
                    refValue = "Anything",
                    scopeSearched = "DL_Foo",
                    candidates = emptyList(),
                ),
            )

        assertContains(message, "DL_Foo (type: STRUCTURE, 0 entries)")
    }

    @Test
    fun `unknown source and scope are rendered explicitly`() {
        val message =
            resolutionMessage(
                ResolutionContext(
                    expected = "PROTOCOL",
                    refKind = "SNREF",
                    refValue = "P",
                    candidates = listOf("Q"),
                ),
            )

        assertContains(message, "<unknown file>")
        assertContains(message, "<unknown scope>")
    }

    @Test
    fun `docref line is omitted when docref is null`() {
        val message =
            resolutionMessage(
                ResolutionContext(
                    expected = "DOP",
                    refKind = "ODXLINK",
                    refValue = "SomeDop",
                    scopeSearched = "DL_Foo",
                    sourceFile = "foo.odx-d",
                ),
            )

        assertFalse(message.contains("docref:"))
    }

    @Test
    fun `logical path uses breadcrumb arrows`() {
        val message =
            resolutionMessage(
                ResolutionContext(
                    expected = "PARAM",
                    refKind = "SNREF",
                    refValue = "MyParam",
                    logicalPath = "Variant_A / Service_B / Request_C",
                    sourceFile = "variant_a.odx-d",
                    scopeSearched = "DL_VariantA",
                    candidates = listOf("OtherParam"),
                ),
            )

        assertContains(message, "Variant_A > Service_B > Request_C (file: variant_a.odx-d)")
    }

    @Test
    fun `formatElementPath renders DIAGLAYER hierarchy types`() {
        val ecuVariant = ECUVARIANT().apply { shortname = "EV_Test" }
        val baseVariant = BASEVARIANT().apply { shortname = "BV_Test" }
        val functionalGroup = FUNCTIONALGROUP().apply { shortname = "FG_Test" }
        val protocol = PROTOCOL().apply { shortname = "Prot_Test" }
        val ecuSharedData = ECUSHAREDDATA().apply { shortname = "ESD_Test" }

        assertEquals("ECU-VARIANT (EV_Test)", formatElementPath(listOf(ecuVariant)))
        assertEquals("BASE-VARIANT (BV_Test)", formatElementPath(listOf(baseVariant)))
        assertEquals("FUNCTIONAL-GROUP (FG_Test)", formatElementPath(listOf(functionalGroup)))
        assertEquals("PROTOCOL (Prot_Test)", formatElementPath(listOf(protocol)))
        assertEquals("ECU-SHARED-DATA (ESD_Test)", formatElementPath(listOf(ecuSharedData)))
    }

    @Test
    fun `formatElementPath renders DIAGCOMM hierarchy types`() {
        val diagService = DIAGSERVICE().apply { shortname = "DS_Test" }
        val singleEcuJob = SINGLEECUJOB().apply { shortname = "SEJ_Test" }

        assertEquals("DIAG-SERVICE (DS_Test)", formatElementPath(listOf(diagService)))
        assertEquals("SINGLE-ECU-JOB (SEJ_Test)", formatElementPath(listOf(singleEcuJob)))
    }

    @Test
    fun `formatElementPath renders table and param types`() {
        val table = TABLE().apply { shortname = "Tab_Test" }
        val tableRow = TABLEROW().apply { shortname = "Row_Test" }
        val param = VALUE().apply { shortname = "Param_Test" }

        assertEquals("TABLE (Tab_Test)", formatElementPath(listOf(table)))
        assertEquals("TABLE-ROW (Row_Test)", formatElementPath(listOf(tableRow)))
        assertEquals("PARAM (Param_Test)", formatElementPath(listOf(param)))
    }

    @Test
    fun `formatElementPath renders job param types`() {
        val input = INPUTPARAM().apply { shortname = "In_Test" }
        val output = OUTPUTPARAM().apply { shortname = "Out_Test" }
        val negOutput = NEGOUTPUTPARAM().apply { shortname = "Neg_Test" }

        assertEquals("INPUT-PARAM (In_Test)", formatElementPath(listOf(input)))
        assertEquals("OUTPUT-PARAM (Out_Test)", formatElementPath(listOf(output)))
        assertEquals("NEG-OUTPUT-PARAM (Neg_Test)", formatElementPath(listOf(negOutput)))
    }

    @Test
    fun `formatElementPath renders variant pattern types without short-name`() {
        val pattern = ECUVARIANTPATTERN()
        val matching = MATCHINGPARAMETER()
        val matchingBase = MATCHINGBASEVARIANTPARAMETER()

        assertEquals("VARIANT-PATTERN", formatElementPath(listOf(pattern)))
        assertEquals("MATCHING-PARAM", formatElementPath(listOf(matching)))
        assertEquals("MATCHING-BASE-PARAM", formatElementPath(listOf(matchingBase)))
    }

    @Test
    fun `formatElementPath renders named entity types`() {
        val functClass = FUNCTCLASS().apply { shortname = "FC_Test" }
        val library = LIBRARY().apply { shortname = "Lib_Test" }
        val audience = ADDITIONALAUDIENCE().apply { shortname = "Aud_Test" }
        val unitGroup = UNITGROUP().apply { shortname = "UG_Test" }
        val comparam = COMPARAM().apply { shortname = "CP_Test" }
        val complexComparam = COMPLEXCOMPARAM().apply { shortname = "CCP_Test" }
        val state = STATE().apply { shortname = "S_Test" }
        val physDim = PHYSICALDIMENSION().apply { shortname = "PD_Test" }
        val protStack = PROTSTACK().apply { shortname = "PS_Test" }
        val stateChart = STATECHART().apply { shortname = "SC_Test" }
        val stateTransition = STATETRANSITION().apply { shortname = "ST_Test" }

        assertEquals("FUNCT-CLASS (FC_Test)", formatElementPath(listOf(functClass)))
        assertEquals("LIBRARY (Lib_Test)", formatElementPath(listOf(library)))
        assertEquals("ADDITIONAL-AUDIENCE (Aud_Test)", formatElementPath(listOf(audience)))
        assertEquals("UNIT-GROUP (UG_Test)", formatElementPath(listOf(unitGroup)))
        assertEquals("COMPARAM (CP_Test)", formatElementPath(listOf(comparam)))
        assertEquals("COMPLEX-COMPARAM (CCP_Test)", formatElementPath(listOf(complexComparam)))
        assertEquals("STATE (S_Test)", formatElementPath(listOf(state)))
        assertEquals("PHYSICAL-DIMENSION (PD_Test)", formatElementPath(listOf(physDim)))
        assertEquals("PROT-STACK (PS_Test)", formatElementPath(listOf(protStack)))
        assertEquals("STATE-CHART (SC_Test)", formatElementPath(listOf(stateChart)))
        assertEquals("STATE-TRANSITION (ST_Test)", formatElementPath(listOf(stateTransition)))
    }

    @Test
    fun `formatElementPath renders multi-element path with separator`() {
        val ecuVariant = ECUVARIANT().apply { shortname = "EV_X" }
        val diagService = DIAGSERVICE().apply { shortname = "Svc_Y" }
        val param = VALUE().apply { shortname = "P_Z" }

        assertEquals(
            "ECU-VARIANT (EV_X) / DIAG-SERVICE (Svc_Y) / PARAM (P_Z)",
            formatElementPath(listOf(ecuVariant, diagService, param)),
        )
    }

    @Test
    fun `formatElementPath falls back to class simple name for unknown types`() {
        val unknown = Object()
        assertEquals("Object", formatElementPath(listOf(unknown)))
    }
}
