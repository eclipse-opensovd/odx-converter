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
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import dataformat.EcuData
import dataformat.ParamType
import jakarta.xml.bind.JAXBContext
import org.eclipse.opensovd.cda.mdd.MDDFile
import schema.odx.ODX
import java.io.File
import java.util.logging.Logger
import kotlin.io.path.createTempDirectory
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Integration tests that pack synthetic ODX files into a PDX archive,
 * run the converter, and verify the resulting MDD output.
 */
class IntegrationTest {
    private lateinit var ecuData: EcuData
    private lateinit var mddFile: MDDFile
    private lateinit var tempDir: File

    @BeforeTest
    fun setUp() {
        tempDir = createTempDirectory("integration-test").toFile()
        tempDir.deleteOnExit()

        val pdxFile = File(tempDir, "synthetic.pdx")
        packPdx("/synthetic-odx", pdxFile)

        val mddOutputFile = File(tempDir, "synthetic.mdd")
        val context: JAXBContext =
            org.eclipse.persistence.jaxb.JAXBContextFactory
                .createContext(arrayOf(ODX::class.java), null)

        val logger = Logger.getLogger("integration-test")
        val converter = FileConverter(logger, context)
        val stats = mutableListOf<ChunkStat>()
        converter.convert(pdxFile, mddOutputFile, ConverterOptions(), stats)

        assertThat(mddOutputFile.exists()).isTrue()
        assertThat(mddOutputFile.length()).isGreaterThan(0)

        ecuData = readMddEcuData(mddOutputFile)
        mddFile = readMddFile(mddOutputFile)
    }

    // ---- ECU-level assertions ----

    @Test
    fun `ECU name is derived from base variant`() {
        assertThat(ecuData.ecuName).isEqualTo("SynthECU")
    }

    @Test
    fun `MDD file contains metadata`() {
        assertThat(mddFile.metadataMap).isNotNull()
        assertThat(mddFile.metadataMap.containsKey("source")).isTrue()
        assertThat(mddFile.metadataMap["source"]).isEqualTo("synthetic.pdx")
    }

    // ---- Variant assertions ----

    @Test
    fun `MDD contains base variant and ECU variant`() {
        assertThat(ecuData.variantsLength).isGreaterThan(0)

        val variantNames =
            (0 until ecuData.variantsLength).map {
                ecuData.variants(it)?.diagLayer?.shortName
            }
        assertThat(variantNames).contains("SynthECU")
        assertThat(variantNames).contains("SynthVariantA")
    }

    @Test
    fun `base variant has five diag services`() {
        val baseVariant = findVariantByName("SynthECU")
        assertThat(baseVariant).isNotNull()
        assertThat(baseVariant!!.diagLayer!!.diagServicesLength).isEqualTo(5)
    }

    @Test
    fun `base variant services have correct short names`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val serviceNames =
            (0 until baseVariant.diagLayer!!.diagServicesLength).map {
                baseVariant.diagLayer!!
                    .diagServices(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(serviceNames).contains("ReadDataByIdentifier")
        assertThat(serviceNames).contains("ReadDTCInformation")
        assertThat(serviceNames).contains("DiagnosticSessionControl")
        assertThat(serviceNames).contains("ReadWithAllParams")
    }

    @Test
    fun `ECU variant has its own diag service`() {
        val ecuVariant = findVariantByName("SynthVariantA")
        assertThat(ecuVariant).isNotNull()

        val serviceNames =
            (0 until ecuVariant!!.diagLayer!!.diagServicesLength).map {
                ecuVariant.diagLayer!!
                    .diagServices(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(serviceNames).contains("WriteDataByIdentifier")
    }

    @Test
    fun `ECU variant has parent reference`() {
        val ecuVariant = findVariantByName("SynthVariantA")
        assertThat(ecuVariant).isNotNull()
        assertThat(ecuVariant!!.parentRefsLength).isGreaterThan(0)
    }

    @Test
    fun `ECU variant has variant pattern`() {
        val ecuVariant = findVariantByName("SynthVariantA")
        assertThat(ecuVariant).isNotNull()
        assertThat(ecuVariant!!.variantPatternLength).isGreaterThan(0)
    }

    // ---- Diag service detail assertions ----

    @Test
    fun `ReadDataByIdentifier service has request with params`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")
        assertThat(service).isNotNull()

        val request = service!!.request
        assertThat(request).isNotNull()
        assertThat(request!!.paramsLength).isGreaterThan(0)

        // Find the SID param (CODED-CONST)
        val sidParam =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.shortName == "SID" }
        assertThat(sidParam).isNotNull()
        assertThat(sidParam!!.paramType).isEqualTo(ParamType.CODED_CONST)
    }

    @Test
    fun `ReadDataByIdentifier service has positive response with MATCHING-REQUEST-PARAM`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        assertThat(service.posResponsesLength).isGreaterThan(0)

        val posResponse = service.posResponses(0)!!
        val matchingParam =
            (0 until posResponse.paramsLength)
                .map { posResponse.params(it) }
                .firstOrNull { it?.paramType == ParamType.MATCHING_REQUEST_PARAM }
        assertThat(matchingParam).isNotNull()
    }

    @Test
    fun `ReadDataByIdentifier service has negative response with NRC-CONST`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        assertThat(service.negResponsesLength).isGreaterThan(0)

        val negResponse = service.negResponses(0)!!
        val nrcParam =
            (0 until negResponse.paramsLength)
                .map { negResponse.params(it) }
                .firstOrNull { it?.paramType == ParamType.NRC_CONST }
        assertThat(nrcParam).isNotNull()
    }

    @Test
    fun `ReadDataByIdentifier service has audience with enabled refs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        val audience = service.diagComm!!.audience
        assertThat(audience).isNotNull()
        assertThat(audience!!.isSupplier).isTrue()
        assertThat(audience.isDevelopment).isTrue()
        assertThat(audience.isManufacturing).isFalse()
        assertThat(audience.enabledAudiencesLength).isGreaterThan(0)
    }

    @Test
    fun `ReadDTCInformation service has disabled audience refs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDTCInformation")!!
        val audience = service.diagComm!!.audience
        assertThat(audience).isNotNull()
        assertThat(audience!!.disabledAudiencesLength).isGreaterThan(0)
    }

    @Test
    fun `ReadDataByIdentifier service has SDGs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        val sdgs = service.diagComm!!.sdgs
        assertThat(sdgs).isNotNull()
        assertThat(sdgs!!.sdgsLength).isGreaterThan(0)
    }

    @Test
    fun `ReadDataByIdentifier service has funct class refs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        assertThat(service.diagComm!!.functClassLength).isGreaterThan(0)
    }

    @Test
    fun `ReadDataByIdentifier service has pre-condition state refs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        assertThat(service.diagComm!!.preConditionStateRefsLength).isGreaterThan(0)
    }

    @Test
    fun `ReadDataByIdentifier service has state transition refs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadDataByIdentifier")!!
        assertThat(service.diagComm!!.stateTransitionRefsLength).isGreaterThan(0)
    }

    // ---- All param types in ReadWithAllParams ----

    @Test
    fun `ReadWithAllParams request has VALUE param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.shortName == "ValueBySNRef" }
        assertThat(param).isNotNull()
        assertThat(param!!.paramType).isEqualTo(ParamType.VALUE)
    }

    @Test
    fun `ReadWithAllParams request has PHYS-CONST param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.PHYS_CONST }
        assertThat(param).isNotNull()
        assertThat(param!!.shortName).isEqualTo("PhysConstParam")
    }

    @Test
    fun `ReadWithAllParams request has RESERVED param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.RESERVED }
        assertThat(param).isNotNull()
    }

    @Test
    fun `ReadWithAllParams request has DYNAMIC param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.DYNAMIC }
        assertThat(param).isNotNull()
    }

    @Test
    fun `ReadWithAllParams request has LENGTH-KEY param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.LENGTH_KEY }
        assertThat(param).isNotNull()
    }

    @Test
    fun `ReadWithAllParams request has TABLE-KEY param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.TABLE_KEY }
        assertThat(param).isNotNull()
    }

    @Test
    fun `ReadWithAllParams request has TABLE-STRUCT param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.TABLE_STRUCT }
        assertThat(param).isNotNull()
    }

    @Test
    fun `ReadWithAllParams request has SYSTEM param`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val service = findServiceByName(baseVariant, "ReadWithAllParams")!!
        val request = service.request!!
        val param =
            (0 until request.paramsLength)
                .map { request.params(it) }
                .firstOrNull { it?.paramType == ParamType.SYSTEM }
        assertThat(param).isNotNull()
    }

    // ---- DiagLayer-level assertions ----

    @Test
    fun `base variant has state charts`() {
        val baseVariant = findVariantByName("SynthECU")!!
        assertThat(baseVariant.diagLayer!!.stateChartsLength).isGreaterThan(0)
        val stateChart = baseVariant.diagLayer!!.stateCharts(0)!!
        assertThat(stateChart.shortName).isEqualTo("SessionStateChart")
        assertThat(stateChart.statesLength).isEqualTo(2)
        assertThat(stateChart.stateTransitionsLength).isEqualTo(2)
    }

    @Test
    fun `base variant has additional audiences`() {
        val baseVariant = findVariantByName("SynthECU")!!
        assertThat(baseVariant.diagLayer!!.additionalAudiencesLength).isGreaterThan(0)
    }

    @Test
    fun `base variant has funct classes`() {
        val baseVariant = findVariantByName("SynthECU")!!
        assertThat(baseVariant.diagLayer!!.functClassesLength).isEqualTo(2)
    }

    @Test
    fun `base variant has SDGs`() {
        val baseVariant = findVariantByName("SynthECU")!!
        val sdgs = baseVariant.diagLayer!!.sdgs
        assertThat(sdgs).isNotNull()
        assertThat(sdgs!!.sdgsLength).isGreaterThan(0)
    }

    // ---- DTCs at ECU level ----

    @Test
    fun `MDD contains DTCs`() {
        assertThat(ecuData.dtcsLength).isGreaterThan(0)
        val dtcNames = (0 until ecuData.dtcsLength).map { ecuData.dtcs(it)?.shortName }
        assertThat(dtcNames).contains("P0100")
        assertThat(dtcNames).contains("P0200")
    }

    @Test
    fun `DTC P0100 has correct properties`() {
        val dtc =
            (0 until ecuData.dtcsLength)
                .map { ecuData.dtcs(it) }
                .firstOrNull { it?.shortName == "P0100" }
        assertThat(dtc).isNotNull()
        assertThat(dtc!!.troubleCode).isEqualTo(256u)
        assertThat(dtc.displayTroubleCode).isEqualTo("P0100")
    }

    // ---- ECU variant SINGLE-ECU-JOB assertions ----

    @Test
    fun `ECU variant has SINGLE-ECU-JOB`() {
        val ecuVariant = findVariantByName("SynthVariantA")!!
        val jobNames =
            (0 until ecuVariant.diagLayer!!.singleEcuJobsLength).map {
                ecuVariant.diagLayer!!
                    .singleEcuJobs(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(jobNames).contains("ResetECU")
    }

    @Test
    fun `SINGLE-ECU-JOB has prog codes`() {
        val ecuVariant = findVariantByName("SynthVariantA")!!
        val job =
            (0 until ecuVariant.diagLayer!!.singleEcuJobsLength)
                .map { ecuVariant.diagLayer!!.singleEcuJobs(it) }
                .firstOrNull { it?.diagComm?.shortName == "ResetECU" }
        assertThat(job).isNotNull()
        assertThat(job!!.progCodesLength).isGreaterThan(0)
    }

    @Test
    fun `SINGLE-ECU-JOB has input and output params`() {
        val ecuVariant = findVariantByName("SynthVariantA")!!
        val job =
            (0 until ecuVariant.diagLayer!!.singleEcuJobsLength)
                .map { ecuVariant.diagLayer!!.singleEcuJobs(it) }
                .firstOrNull { it?.diagComm?.shortName == "ResetECU" }
        assertThat(job).isNotNull()
        assertThat(job!!.inputParamsLength).isGreaterThan(0)
        assertThat(job.outputParamsLength).isGreaterThan(0)
        assertThat(job.negOutputParamsLength).isGreaterThan(0)
    }

    // ---- Functional group assertions ----

    @Test
    fun `MDD contains functional group`() {
        assertThat(ecuData.functionalGroupsLength).isGreaterThan(0)

        val fgNames =
            (0 until ecuData.functionalGroupsLength).map {
                ecuData.functionalGroups(it)?.diagLayer?.shortName
            }
        assertThat(fgNames).contains("SharedDiagnostics")
    }

    @Test
    fun `functional group has TesterPresent service`() {
        val fg =
            (0 until ecuData.functionalGroupsLength)
                .map { ecuData.functionalGroups(it) }
                .firstOrNull { it?.diagLayer?.shortName == "SharedDiagnostics" }
        assertThat(fg).isNotNull()
        assertThat(fg!!.diagLayer!!.diagServicesLength).isGreaterThan(0)

        val serviceNames =
            (0 until fg.diagLayer!!.diagServicesLength).map {
                fg.diagLayer!!
                    .diagServices(it)
                    ?.diagComm
                    ?.shortName
            }
        assertThat(serviceNames).contains("TesterPresent")
    }

    @Test
    fun `functional group has parent reference to base variant`() {
        val fg =
            (0 until ecuData.functionalGroupsLength)
                .map { ecuData.functionalGroups(it) }
                .firstOrNull { it?.diagLayer?.shortName == "SharedDiagnostics" }
        assertThat(fg).isNotNull()
        assertThat(fg!!.parentRefsLength).isGreaterThan(0)
    }

    // ---- COMPARAM chain assertions ----

    @Test
    fun `base variant has comParamRefs`() {
        val bv = findVariantByName("SynthECU")
        assertThat(bv).isNotNull()
        assertThat(bv!!.diagLayer!!.comParamRefsLength).isEqualTo(2)
    }

    @Test
    fun `comParamRef has simple value and protocol reference`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(0)
        assertThat(ref).isNotNull()
        assertThat(ref!!.simpleValue).isNotNull()
        assertThat(ref.simpleValue!!.value).isEqualTo("3000")
        assertThat(ref.protocol).isNotNull()
        assertThat(ref.protocol!!.diagLayer!!.shortName).isEqualTo("UDS_on_CAN")
        assertThat(ref.protStack).isNotNull()
        assertThat(ref.protStack!!.shortName).isEqualTo("UDS_CAN_Stack")
    }

    @Test
    fun `comParamRef resolves to regular comParam`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(0)!!
        assertThat(ref.comParam).isNotNull()
        assertThat(ref.comParam!!.shortName).isEqualTo("CP_Timeout")
        assertThat(ref.comParam!!.paramClass).isEqualTo("TIMING")
    }

    @Test
    fun `comParamRef with complex value resolves to complex comParam`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(1)
        assertThat(ref).isNotNull()
        assertThat(ref!!.complexValue).isNotNull()
        assertThat(ref.comParam).isNotNull()
        assertThat(ref.comParam!!.shortName).isEqualTo("CCP_TimingParams")
    }

    @Test
    fun `protocol has comParamSpec with protStack`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(0)!!
        val protocol = ref.protocol!!
        assertThat(protocol.comParamSpec).isNotNull()
        assertThat(protocol.comParamSpec!!.protStacksLength).isEqualTo(1)
        val protStack = protocol.comParamSpec!!.protStacks(0)!!
        assertThat(protStack.shortName).isEqualTo("UDS_CAN_Stack")
        assertThat(protStack.pduProtocolType).isEqualTo("UDS")
        assertThat(protStack.physicalLinkType).isEqualTo("CAN")
    }

    @Test
    fun `protStack has comParamSubSet with unitSpec`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(0)!!
        val protStack = ref.protocol!!.comParamSpec!!.protStacks(0)!!
        assertThat(protStack.comparamSubsetRefsLength).isEqualTo(1)
        val subSet = protStack.comparamSubsetRefs(0)!!
        assertThat(subSet.comParamsLength).isGreaterThan(0)
        assertThat(subSet.complexComParamsLength).isGreaterThan(0)
        assertThat(subSet.unitSpec).isNotNull()
    }

    @Test
    fun `unitSpec has units, unit groups, and physical dimensions`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(0)!!
        val unitSpec =
            ref.protocol!!
                .comParamSpec!!
                .protStacks(0)!!
                .comparamSubsetRefs(0)!!
                .unitSpec!!
        assertThat(unitSpec.unitsLength).isGreaterThan(0)
        assertThat(unitSpec.unitGroupsLength).isGreaterThan(0)
        assertThat(unitSpec.physicalDimensionsLength).isGreaterThan(0)
    }

    @Test
    fun `unit has display name and physical dimension reference`() {
        val bv = findVariantByName("SynthECU")!!
        val ref = bv.diagLayer!!.comParamRefs(0)!!
        val unitSpec =
            ref.protocol!!
                .comParamSpec!!
                .protStacks(0)!!
                .comparamSubsetRefs(0)!!
                .unitSpec!!
        val msUnit =
            (0 until unitSpec.unitsLength)
                .map { unitSpec.units(it) }
                .firstOrNull { it?.shortName == "Milliseconds" }
        assertThat(msUnit).isNotNull()
        assertThat(msUnit!!.displayName).isEqualTo("ms")
        assertThat(msUnit.physicalDimension).isNotNull()
        assertThat(msUnit.physicalDimension!!.shortName).isEqualTo("Time")
    }

    // ---- Helper functions ----

    private fun findVariantByName(name: String) =
        (0 until ecuData.variantsLength)
            .map { ecuData.variants(it) }
            .firstOrNull { it?.diagLayer?.shortName == name }

    private fun findServiceByName(
        variant: dataformat.Variant,
        name: String,
    ) = (0 until variant.diagLayer!!.diagServicesLength)
        .map { variant.diagLayer!!.diagServices(it) }
        .firstOrNull { it?.diagComm?.shortName == name }
}

/**
 * Separate test class that runs the converter with withAudiences filtering enabled.
 */
class AudienceFilteringTest {
    private lateinit var ecuData: EcuData

    @BeforeTest
    fun setUp() {
        val tempDir = createTempDirectory("audience-filter-test").toFile()
        tempDir.deleteOnExit()

        val pdxFile = File(tempDir, "synthetic.pdx")
        packPdx("/synthetic-odx", pdxFile)

        val mddOutputFile = File(tempDir, "synthetic.mdd")
        val context: JAXBContext =
            org.eclipse.persistence.jaxb.JAXBContextFactory
                .createContext(arrayOf(ODX::class.java), null)

        val logger = Logger.getLogger("audience-filter-test")
        val converter = FileConverter(logger, context)
        val stats = mutableListOf<ChunkStat>()
        converter.convert(pdxFile, mddOutputFile, ConverterOptions(withAudiences = listOf("SupplierAudience")), stats)

        ecuData = readMddEcuData(mddOutputFile)
    }

    @Test
    fun `services with matching audience are included`() {
        val variant =
            (0 until ecuData.variantsLength)
                .map { ecuData.variants(it) }
                .first { it?.diagLayer?.shortName == "SynthECU" }!!

        val serviceNames =
            (0 until variant.diagLayer!!.diagServicesLength)
                .map {
                    variant.diagLayer!!
                        .diagServices(it)!!
                        .diagComm!!
                        .shortName
                }

        // ReadDataByIdentifier has ENABLED-AUDIENCE-REF to aa_supplier (SupplierAudience) → included
        assertThat(serviceNames).contains("ReadDataByIdentifier")
    }

    @Test
    fun `services without audience restrictions are included`() {
        val variant =
            (0 until ecuData.variantsLength)
                .map { ecuData.variants(it) }
                .first { it?.diagLayer?.shortName == "SynthECU" }!!

        val serviceNames =
            (0 until variant.diagLayer!!.diagServicesLength)
                .map {
                    variant.diagLayer!!
                        .diagServices(it)!!
                        .diagComm!!
                        .shortName
                }

        // Services without AUDIENCE or without ENABLED-AUDIENCE-REFS should be included
        assertThat(serviceNames).contains("ReadComplexDops")
    }
}
