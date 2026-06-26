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
import assertk.assertions.isTrue
import io.mockk.every
import io.mockk.mockk
import schema.odx.ADDITIONALAUDIENCE
import schema.odx.AUDIENCE
import schema.odx.DIAGSERVICE
import schema.odx.ENABLEDAUDIENCEREFS
import schema.odx.ODXLINK
import kotlin.test.Test

class DatabaseWriterFilterTest {
    /**
     * DatabaseWriter's filterByConverterOptions and isIncludedWithOption are
     * instance methods that need an ODXCollectionGroup for audience resolution.
     * We test the filtering logic by constructing a minimal DatabaseWriter with mocks.
     */

    private fun createDiagServiceWithAudience(vararg audienceShortNames: String): DIAGSERVICE {
        val service = DIAGSERVICE()
        service.id = "ds_${audienceShortNames.joinToString("_")}"
        service.shortname = "Service_${audienceShortNames.joinToString("_")}"

        if (audienceShortNames.isNotEmpty()) {
            val audience = AUDIENCE()
            val enabledRefs = ENABLEDAUDIENCEREFS()
            audienceShortNames.forEach { name ->
                val ref = ODXLINK()
                ref.idref = "aud_$name"
                enabledRefs.enabledaudienceref.add(ref)
            }
            audience.enabledaudiencerefs = enabledRefs
            service.audience = audience
        }

        return service
    }

    private fun createWriter(withAudiences: List<String>): DatabaseWriter {
        val options = ConverterOptions(withAudiences = withAudiences)
        val logger =
            java.util.logging.Logger
                .getLogger("test")

        // Create mock ODXCollectionGroup that resolves audiences
        val odx = mockk<ODXCollectionGroup>()
        every { odx.basevariants } returns emptyList()
        every { odx.ecuvariants } returns emptyList()
        every { odx.functionalGroups } returns emptyList()
        every { odx.dtcs } returns emptyList()
        every { odx.resolveAdditionalAudience(any()) } answers {
            val link = firstArg<ODXLINK>()
            val audience = ADDITIONALAUDIENCE()
            audience.shortname = link.idref.removePrefix("aud_")
            audience
        }

        return DatabaseWriter(logger, odx, options)
    }

    @Test
    fun `filterByConverterOptions includes all when withAudiences is empty`() {
        val writer = createWriter(withAudiences = emptyList())
        val services =
            listOf(
                createDiagServiceWithAudience("AfterSales"),
                createDiagServiceWithAudience("Development"),
            )

        val filtered = with(writer) { services.filterByConverterOptions(ConverterOptions()) }
        assertThat(filtered).hasSize(2)
    }

    @Test
    fun `filterByConverterOptions includes services without audience`() {
        val writer = createWriter(withAudiences = listOf("AfterSales"))
        val serviceWithout = DIAGSERVICE()
        serviceWithout.id = "ds_no_audience"
        serviceWithout.shortname = "NoAudience"

        val options = ConverterOptions(withAudiences = listOf("AfterSales"))
        val filtered = with(writer) { listOf(serviceWithout).filterByConverterOptions(options) }
        assertThat(filtered).hasSize(1)
    }

    @Test
    fun `filterByConverterOptions filters by matching audience`() {
        val writer = createWriter(withAudiences = listOf("AfterSales"))
        val matching = createDiagServiceWithAudience("AfterSales")
        val nonMatching = createDiagServiceWithAudience("Development")

        val options = ConverterOptions(withAudiences = listOf("AfterSales"))
        val filtered = with(writer) { listOf(matching, nonMatching).filterByConverterOptions(options) }
        assertThat(filtered).hasSize(1)
        assertThat(filtered[0].shortname.contains("AfterSales")).isTrue()
    }

    @Test
    fun `isIncludedWithOption is case-insensitive`() {
        val writer = createWriter(withAudiences = listOf("aftersales"))
        val service = createDiagServiceWithAudience("AfterSales")

        val options = ConverterOptions(withAudiences = listOf("aftersales"))
        val included = with(writer) { service.isIncludedWithOption(options) }
        assertThat(included).isTrue()
    }

    @Test
    fun `filterByConverterOptions returns empty list when no audiences match`() {
        val writer = createWriter(withAudiences = listOf("Manufacturing"))
        val service = createDiagServiceWithAudience("AfterSales")

        val options = ConverterOptions(withAudiences = listOf("Manufacturing"))
        val filtered = with(writer) { listOf(service).filterByConverterOptions(options) }
        assertThat(filtered).isEmpty()
    }

    @Test
    fun `filterByConverterOptions handles multiple withAudiences`() {
        val writer = createWriter(withAudiences = listOf("AfterSales", "Development"))
        val service1 = createDiagServiceWithAudience("AfterSales")
        val service2 = createDiagServiceWithAudience("Development")
        val service3 = createDiagServiceWithAudience("Manufacturing")

        val options = ConverterOptions(withAudiences = listOf("AfterSales", "Development"))
        val filtered = with(writer) { listOf(service1, service2, service3).filterByConverterOptions(options) }
        assertThat(filtered).hasSize(2)
    }
}
