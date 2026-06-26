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
import assertk.assertions.doesNotContainKey
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import schema.odx.BASEVARIANTREF
import schema.odx.CASE
import schema.odx.CODEDCONST
import schema.odx.COMPARAMREF
import schema.odx.DIAGSERVICE
import schema.odx.ECUVARIANT
import schema.odx.ENDOFPDUFIELD
import schema.odx.MATCHINGBASEVARIANTPARAMETER
import schema.odx.MATCHINGPARAMETER
import schema.odx.ODXLINK
import schema.odx.PRECONDITIONSTATEREF
import schema.odx.SINGLEECUJOB
import schema.odx.STATETRANSITIONREF
import schema.odx.TABLEDIAGCOMMCONNECTOR
import schema.odx.TABLEROW
import schema.odx.VALUE
import kotlin.test.Test

class ODXLinkCollectorTest {
    @Test
    fun `ODXLINK is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val link = ODXLINK()
        collector.afterUnmarshal(link, null)
        assertThat(collector.linkToFile[link]).isEqualTo("file1.odx")
    }

    @Test
    fun `PARENTREF is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val ref = BASEVARIANTREF()
        collector.afterUnmarshal(ref, null)
        assertThat(collector.linkToFile[ref]).isEqualTo("file1.odx")
    }

    @Test
    fun `COMPARAMREF is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file2.odx"
        val ref = COMPARAMREF()
        collector.afterUnmarshal(ref, null)
        assertThat(collector.linkToFile[ref]).isEqualTo("file2.odx")
    }

    @Test
    fun `PRECONDITIONSTATEREF is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val ref = PRECONDITIONSTATEREF()
        collector.afterUnmarshal(ref, null)
        assertThat(collector.linkToFile[ref]).isEqualTo("file1.odx")
    }

    @Test
    fun `STATETRANSITIONREF is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val ref = STATETRANSITIONREF()
        collector.afterUnmarshal(ref, null)
        assertThat(collector.linkToFile[ref]).isEqualTo("file1.odx")
    }

    @Test
    fun `PARAM subtypes are tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val value = VALUE()
        val codedConst = CODEDCONST()
        collector.afterUnmarshal(value, null)
        collector.afterUnmarshal(codedConst, null)
        assertThat(collector.linkToFile[value]).isEqualTo("file1.odx")
        assertThat(collector.linkToFile[codedConst]).isEqualTo("file1.odx")
    }

    @Test
    fun `FIELD subtype is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val field = ENDOFPDUFIELD()
        collector.afterUnmarshal(field, null)
        assertThat(collector.linkToFile[field]).isEqualTo("file1.odx")
    }

    @Test
    fun `TABLEDIAGCOMMCONNECTOR is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val connector = TABLEDIAGCOMMCONNECTOR()
        collector.afterUnmarshal(connector, null)
        assertThat(collector.linkToFile[connector]).isEqualTo("file1.odx")
    }

    @Test
    fun `CASE is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val case = CASE()
        collector.afterUnmarshal(case, null)
        assertThat(collector.linkToFile[case]).isEqualTo("file1.odx")
    }

    @Test
    fun `TABLEROW is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val row = TABLEROW()
        collector.afterUnmarshal(row, null)
        assertThat(collector.linkToFile[row]).isEqualTo("file1.odx")
    }

    @Test
    fun `MATCHINGBASEVARIANTPARAMETER is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val param = MATCHINGBASEVARIANTPARAMETER()
        collector.afterUnmarshal(param, null)
        assertThat(collector.linkToFile[param]).isEqualTo("file1.odx")
    }

    @Test
    fun `MATCHINGPARAMETER is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val param = MATCHINGPARAMETER()
        collector.afterUnmarshal(param, null)
        assertThat(collector.linkToFile[param]).isEqualTo("file1.odx")
    }

    @Test
    fun `DIAGCOMM subtypes are tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val service = DIAGSERVICE()
        val job = SINGLEECUJOB()
        collector.afterUnmarshal(service, null)
        collector.afterUnmarshal(job, null)
        assertThat(collector.linkToFile[service]).isEqualTo("file1.odx")
        assertThat(collector.linkToFile[job]).isEqualTo("file1.odx")
    }

    @Test
    fun `ECUVARIANT is tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val variant = ECUVARIANT()
        collector.afterUnmarshal(variant, null)
        assertThat(collector.linkToFile[variant]).isEqualTo("file1.odx")
    }

    @Test
    fun `unrelated types are not tracked`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val unrelated = "just a string"
        collector.afterUnmarshal(unrelated, null)
        assertThat(collector.linkToFile).doesNotContainKey(unrelated)
    }

    @Test
    fun `currentFile is associated with objects`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val link1 = ODXLINK()
        collector.afterUnmarshal(link1, null)

        collector.currentFile = "file2.odx"
        val link2 = ODXLINK()
        collector.afterUnmarshal(link2, null)

        assertThat(collector.linkToFile[link1]).isEqualTo("file1.odx")
        assertThat(collector.linkToFile[link2]).isEqualTo("file2.odx")
    }

    @Test
    fun `identity-based map distinguishes same-value objects`() {
        val collector = ODXLinkCollector()
        collector.currentFile = "file1.odx"
        val link1 = ODXLINK()
        collector.afterUnmarshal(link1, null)

        collector.currentFile = "file2.odx"
        val link2 = ODXLINK()
        collector.afterUnmarshal(link2, null)

        assertThat(collector.linkToFile[link1]).isEqualTo("file1.odx")
        assertThat(collector.linkToFile[link2]).isEqualTo("file2.odx")
        assertThat(collector.linkToFile).hasSize(2)
    }
}
