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

@file:OptIn(ExperimentalUnsignedTypes::class)

import com.google.flatbuffers.FlatBufferBuilder
import dataformat.AdditionalAudience
import dataformat.Audience
import dataformat.Case
import dataformat.CodedConst
import dataformat.ComParam
import dataformat.ComParamRef
import dataformat.ComParamSpec
import dataformat.ComParamSpecificData
import dataformat.ComParamSubSet
import dataformat.ComParamType
import dataformat.ComplexComParam
import dataformat.ComplexValue
import dataformat.CompuDefaultValue
import dataformat.CompuInternalToPhys
import dataformat.CompuMethod
import dataformat.CompuPhysToInternal
import dataformat.CompuRationalCoEffs
import dataformat.CompuScale
import dataformat.CompuValues
import dataformat.DOP
import dataformat.DTC
import dataformat.DTCDOP
import dataformat.DefaultCase
import dataformat.DetermineNumberOfItems
import dataformat.DiagCodedType
import dataformat.DiagComm
import dataformat.DiagLayer
import dataformat.DiagService
import dataformat.Dynamic
import dataformat.DynamicLengthField
import dataformat.EcuData
import dataformat.EcuSharedData
import dataformat.EndOfPduField
import dataformat.EnvData
import dataformat.EnvDataDesc
import dataformat.Field
import dataformat.FunctClass
import dataformat.FunctionalGroup
import dataformat.JobParam
import dataformat.LeadingLengthInfoType
import dataformat.LengthKeyRef
import dataformat.Library
import dataformat.Limit
import dataformat.LongName
import dataformat.MUXDOP
import dataformat.MatchingParameter
import dataformat.MatchingRequestParam
import dataformat.MinMaxLengthType
import dataformat.NormalDOP
import dataformat.NrcConst
import dataformat.Param
import dataformat.ParamLengthInfoType
import dataformat.ParamSpecificData
import dataformat.ParentRef
import dataformat.ParentRefType
import dataformat.PhysConst
import dataformat.PhysicalDimension
import dataformat.PhysicalType
import dataformat.PreConditionStateRef
import dataformat.ProgCode
import dataformat.ProtStack
import dataformat.Protocol
import dataformat.RegularComParam
import dataformat.Request
import dataformat.Reserved
import dataformat.Response
import dataformat.ResponseType
import dataformat.SD
import dataformat.SDG
import dataformat.SDGCaption
import dataformat.SDGS
import dataformat.SDOrSDG
import dataformat.SDxorSDG
import dataformat.SimpleOrComplexValueEntry
import dataformat.SimpleValue
import dataformat.SingleEcuJob
import dataformat.SpecificDOPData
import dataformat.SpecificDataType
import dataformat.StandardLengthType
import dataformat.StateChart
import dataformat.StateTransition
import dataformat.StateTransitionRef
import dataformat.StaticField
import dataformat.Structure
import dataformat.SwitchKey
import dataformat.TableDop
import dataformat.TableEntry
import dataformat.TableKey
import dataformat.TableKeyReference
import dataformat.TableRow
import dataformat.TableStruct
import dataformat.Text
import dataformat.UnitGroup
import dataformat.UnitSpec
import dataformat.Value
import dataformat.Variant
import dataformat.VariantPattern
import schema.odx.ADDITIONALAUDIENCE
import schema.odx.AUDIENCE
import schema.odx.BASEVARIANT
import schema.odx.CASE
import schema.odx.CODEDCONST
import schema.odx.COMPARAM
import schema.odx.COMPARAMREF
import schema.odx.COMPARAMSPEC
import schema.odx.COMPARAMSUBSET
import schema.odx.COMPLEXCOMPARAM
import schema.odx.COMPLEXVALUE
import schema.odx.COMPUCONST
import schema.odx.COMPUDEFAULTVALUE
import schema.odx.COMPUINTERNALTOPHYS
import schema.odx.COMPUINVERSEVALUE
import schema.odx.COMPUMETHOD
import schema.odx.COMPUPHYSTOINTERNAL
import schema.odx.COMPURATIONALCOEFFS
import schema.odx.COMPUSCALE
import schema.odx.DATAOBJECTPROP
import schema.odx.DEFAULTCASE
import schema.odx.DETERMINENUMBEROFITEMS
import schema.odx.DIAGCODEDTYPE
import schema.odx.DIAGCOMM
import schema.odx.DIAGLAYER
import schema.odx.DIAGSERVICE
import schema.odx.DOPBASE
import schema.odx.DYNAMIC
import schema.odx.DYNAMICLENGTHFIELD
import schema.odx.ECUSHAREDDATA
import schema.odx.ECUVARIANT
import schema.odx.ECUVARIANTPATTERN
import schema.odx.ENDOFPDUFIELD
import schema.odx.ENVDATA
import schema.odx.ENVDATADESC
import schema.odx.FIELD
import schema.odx.FUNCTCLASS
import schema.odx.FUNCTIONALGROUP
import schema.odx.GLOBALNEGRESPONSE
import schema.odx.HIERARCHYELEMENT
import schema.odx.INPUTPARAM
import schema.odx.LEADINGLENGTHINFOTYPE
import schema.odx.LENGTHKEY
import schema.odx.LIBRARY
import schema.odx.LIMIT
import schema.odx.LONGNAME
import schema.odx.MATCHINGBASEVARIANTPARAMETER
import schema.odx.MATCHINGPARAMETER
import schema.odx.MATCHINGREQUESTPARAM
import schema.odx.MINMAXLENGTHTYPE
import schema.odx.NEGOUTPUTPARAM
import schema.odx.NEGRESPONSE
import schema.odx.NRCCONST
import schema.odx.ODXLINK
import schema.odx.OUTPUTPARAM
import schema.odx.PARAM
import schema.odx.PARAMLENGTHINFOTYPE
import schema.odx.PARENTREF
import schema.odx.PHYSCONST
import schema.odx.PHYSICALDIMENSION
import schema.odx.PHYSICALTYPE
import schema.odx.POSRESPONSE
import schema.odx.PRECONDITIONSTATEREF
import schema.odx.PROGCODE
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.REQUEST
import schema.odx.RESERVED
import schema.odx.RESPONSE
import schema.odx.SIMPLEVALUE
import schema.odx.SINGLEECUJOB
import schema.odx.STANDARDLENGTHTYPE
import schema.odx.STATE
import schema.odx.STATECHART
import schema.odx.STATETRANSITION
import schema.odx.STATETRANSITIONREF
import schema.odx.STATICFIELD
import schema.odx.STRUCTURE
import schema.odx.SWITCHKEY
import schema.odx.SYSTEM
import schema.odx.TABLE
import schema.odx.TABLEENTRY
import schema.odx.TABLEKEY
import schema.odx.TABLEROW
import schema.odx.TABLESTRUCT
import schema.odx.TEXT
import schema.odx.UNIT
import schema.odx.UNITGROUP
import schema.odx.UNITSPEC
import schema.odx.VALUE
import java.text.NumberFormat
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger
import kotlin.collections.toIntArray
import kotlin.collections.toUIntArray

class DatabaseWriter(
    private val logger: Logger,
    private val odx: ODXCollection,
    private val options: ConverterOptions
) {
    private val builder = FlatBufferBuilder()

    private val createdObjects: MutableSet<Any?> = mutableSetOf()

    private val sdMap: MutableMap<schema.odx.SD, Int> = mutableMapOf()
    private val sdgMap: MutableMap<schema.odx.SDG, Int> = mutableMapOf()
    private val sdgsMap: MutableMap<schema.odx.SDGS, Int> = mutableMapOf()
    private val additionalAudienceMap: MutableMap<ADDITIONALAUDIENCE, Int> = mutableMapOf()
    private val audiencesMap: MutableMap<AUDIENCE, Int> = mutableMapOf()
    private val diagCodedTypes: MutableMap<DIAGCODEDTYPE, Int> = mutableMapOf()
    private val unitMap: MutableMap<UNIT, Int> = mutableMapOf()
    private val dtcs: MutableMap<schema.odx.DTC, Int> = mutableMapOf()
    private val dopMap: MutableMap<DOPBASE, Int> = mutableMapOf()
    private val tableMap: MutableMap<TABLE, Int> = mutableMapOf()
    private val tableRowMap: MutableMap<TABLEROW, Int> = mutableMapOf()
    private val paramMap: MutableMap<PARAM, Int> = mutableMapOf()
    private val responseMap: MutableMap<RESPONSE, Int> = mutableMapOf()
    private val requestMap: MutableMap<REQUEST, Int> = mutableMapOf()
    private val functClasses: MutableMap<FUNCTCLASS, Int> = mutableMapOf()
    private val stateMap: MutableMap<STATE, Int> = mutableMapOf()
    private val stateTransitionMap: MutableMap<STATETRANSITION, Int> = mutableMapOf()
    private val stateTransitionRefMap: MutableMap<STATETRANSITIONREF, Int> = mutableMapOf()
    private val stateChartMap: MutableMap<STATECHART, Int> = mutableMapOf()
    private val preConditionStateRefsMap: MutableMap<PRECONDITIONSTATEREF, Int> = mutableMapOf()
    private val comParamMap: MutableMap<COMPARAM, Int> = mutableMapOf()
    private val complexComParamMap: MutableMap<COMPLEXCOMPARAM, Int> = mutableMapOf()
    private val comParamSubSetMap: MutableMap<COMPARAMSUBSET, Int> = mutableMapOf()
    private val protStackMap: MutableMap<PROTSTACK, Int> = mutableMapOf()
    private val protocolMap: MutableMap<PROTOCOL, Int> = mutableMapOf()
    private val diagServicesMap: MutableMap<DIAGSERVICE, Int> = mutableMapOf()
    private val singleEcuJobsMap: MutableMap<SINGLEECUJOB, Int> = mutableMapOf()
    private val baseVariantMap: MutableMap<BASEVARIANT, Int> = mutableMapOf()
    private val ecuVariantMap: MutableMap<ECUVARIANT, Int> = mutableMapOf()
    private val functionalGroupMap: MutableMap<FUNCTIONALGROUP, Int> = mutableMapOf()
    private val ecuSharedDataMap: MutableMap<ECUSHAREDDATA, Int> = mutableMapOf()
    private val librariesMap: MutableMap<LIBRARY, Int> = mutableMapOf()
    private val dedupStrMap: MutableMap<String, Int> = mutableMapOf()

    private val strOccurrences: MutableMap<String, AtomicInteger> = mutableMapOf()

    init {
        // TODO compuscales?
        val commonStrs =
            (0..10).mapNotNull { it.toString() } + "true" + "false" + "TRUE" + "FALSE" + "NONE" + "on" + "off" + "%"
        val stringsToDeduplicate = odx.sds.mapNotNull { it.value } +
                odx.sds.mapNotNull { it.ti } +
                odx.sds.mapNotNull { it.si } +
                odx.sdgs.mapNotNull { it.si } +
                odx.sdgs.mapNotNull { it.sdgcaption?.shortname }
        odx.params.mapNotNull { it.shortname } +
                odx.params.mapNotNull { it.semantic } +
                odx.dtcs.mapNotNull { it.value.shortname } +
                odx.dtcs.mapNotNull { it.value.text?.value } +
                odx.dtcs.mapNotNull { it.value.text?.ti } +
                odx.states.mapNotNull { it.value.shortname } +
                odx.envDataDescs.mapNotNull { it.value.shortname } +
                commonStrs


        stringsToDeduplicate.toSet().forEach {
            dedupStrMap[it] = it.offset()
        }

        val depBuilder = DopSerializationOrder(odx)
        val dopOrder = depBuilder.serializationOrder()

        sdMap.fillMissing(odx.sds) { it.offset() }
        sdgMap.fillMissing(odx.sdgs) { it.offset() }
        sdgsMap.fillMissing(odx.sdgss) { it.offset() }
        librariesMap.fillMissing(odx.libraries.values) { it.offset() }
        additionalAudienceMap.fillMissing(odx.additionalAudiences.values) { it.offset() }
        audiencesMap.fillMissing(odx.audiences) { it.offset() }
        diagCodedTypes.fillMissing(odx.diagCodedTypes) { it.offset() }
        unitMap.fillMissing(odx.units.values) { it.offset() }
        dtcs.fillMissing(odx.dtcs.values) { it.offset() }
        functClasses.fillMissing(odx.functClasses.values) { it.offset() }
        stateMap.fillMissing(odx.states.values) { it.offset() }
        stateTransitionMap.fillMissing(odx.stateTransitions.values) { it.offset() }
        stateChartMap.fillMissing(odx.stateCharts.values) { it.offset() }
        stateTransitionRefMap.fillMissing(odx.stateTransitionsRefs) { it.offset() }
        preConditionStateRefsMap.fillMissing(odx.preConditionstateRefs) { it.offset() }
        dopMap.fillMissing(dopOrder.map { it.dop }) { it.offset() }
        tableRowMap.fillMissing(odx.tableRows.values) { it.offset() }
        tableMap.fillMissing(odx.tables.values) { it.offset() }
        paramMap.fillMissing(odx.params) { it.offset() }
        requestMap.fillMissing(odx.requests.values) { it.offset() }
        responseMap.fillMissing(odx.responses) { it.offset() }
        comParamMap.fillMissing(odx.comparams.values) { it.offset() }
        complexComParamMap.fillMissing(odx.complexComparams.values) { it.offset() }
        comParamSubSetMap.fillMissing(odx.comParamSubSets.values) { it.offset() }
        protStackMap.fillMissing(odx.protStacks.values) { it.offset() }
        protocolMap.fillMissing(odx.protocols.values) { it.offset() }
        singleEcuJobsMap.fillMissing(odx.singleEcuJobs.values) { it.offset() }
        diagServicesMap.fillMissing(odx.diagServices.values) { it.offset() }
        baseVariantMap.fillMissing(odx.basevariants.values) { it.offset() }
        ecuVariantMap.fillMissing(odx.ecuvariants.values) { it.offset() }
        functionalGroupMap.fillMissing(odx.functionalGroups.values) { it.offset() }

        calculateStrDedupPotential(strOccurrences)
//        println(strOccurrences.filter { it.value.get() > 10 }.map { it.key + ": " + it.value }.joinToString("\n"))

        /*
        ecuSharedDataMap.fillMissing(odx.ecuSharedDatas.values) { it.offset() }

         */

        /*
        val unused = mutableMapOf<Int, Any>().also { it.putAll(predetermineObjectIds) }
        usedObjectIds.keys.forEach { unused.remove(it) }
        logger.info("${odx.ecuName} referenced ${predetermineObjectIds.size} / total ${usedObjectIds.keys.size} / unused ${unused.size}")
        if (unused.isNotEmpty()) {
            // Unused entries indicate that a reference was created but never put into the protobuf structure
            logger.warning(
                "Warning: Unused references - this usually means, that something was referenced, but that something was not saved to the output file: ${
                    unused.values.joinToString(
                        ", "
                    )
                }"
            )
        } */
    }

    private fun calculateStrDedupPotential(strOccurrences: MutableMap<String, AtomicInteger>) {
        var wastedBytesPotential = 0
        strOccurrences.forEach {
            if (it.value.get() > 1) {
                wastedBytesPotential += (it.value.get() - 1) * it.key.length
            }
        }

        println("String dedup potential: " + NumberFormat.getInstance().format(wastedBytesPotential))
    }

    fun createEcuData(): ByteArray {
        val version = "2025-04-30".offset()
        val ecuName = odx.ecuName.offset()
        val odxRevision = odx.odxRevision.offset()

        val libraries = EcuData.createLibrariesVector(builder, librariesMap.values.toIntArray())
        val units = EcuData.createUnitsVector(builder, unitMap.values.toIntArray())
        val additionalAudiences =
            EcuData.createAdditionalAudiencesVector(builder, additionalAudienceMap.values.toIntArray())
        val audiences = EcuData.createAudiencesVector(builder, additionalAudienceMap.values.toIntArray())
        val dtcs = EcuData.createDtcsVector(builder, dtcs.values.toIntArray())
        val sds = EcuData.createSdsVector(builder, sdMap.values.toIntArray())
        val sdgs = EcuData.createSdgsVector(builder, sdgMap.values.toIntArray())
        val sdgss = EcuData.createSdgssVector(builder, sdgsMap.values.toIntArray())
        val functClasses = EcuData.createFunctClassesVector(builder, functClasses.values.toIntArray())
        val states = EcuData.createStatesVector(builder, stateMap.values.toIntArray())
        val stateTransitions = EcuData.createStateTransitionsVector(builder, stateTransitionMap.values.toIntArray())
        val stateCharts = EcuData.createStateChartsVector(builder, stateChartMap.values.toIntArray())
        val stateTransitionRefs =
            EcuData.createStateTransitionRefsVector(builder, stateTransitionRefMap.values.toIntArray())
        val preConditionStateRefs =
            EcuData.createPreConditionStateRefsVector(builder, preConditionStateRefsMap.values.toIntArray())
        val diagCodedTypes = EcuData.createDiagCodedTypesVector(builder, diagCodedTypes.values.toIntArray())
        val dops = EcuData.createDopsVector(builder, dopMap.values.toIntArray())
        val tableRows = EcuData.createTableRowsVector(builder, tableRowMap.values.toIntArray())
        val tables = EcuData.createTablesVector(builder, tableMap.values.toIntArray())
        val params = EcuData.createParamsVector(builder, paramMap.values.toIntArray())
        val requests = EcuData.createRequestsVector(builder, requestMap.values.toIntArray())
        val responses = EcuData.createResponsesVector(builder, responseMap.values.toIntArray())
        val comParams = EcuData.createComParamsVector(
            builder,
            comParamMap.values.toIntArray() + complexComParamMap.values.toIntArray()
        )
        val comParamSubSets = EcuData.createComParamSubSetsVector(builder, comParamSubSetMap.values.toIntArray())
        val protStacks = EcuData.createProtStacksVector(builder, protStackMap.values.toIntArray())
        val protocols = EcuData.createProtocolsVector(builder, protocolMap.values.toIntArray())
        val singleEcuJobs = EcuData.createSingleEcuJobsVector(builder, singleEcuJobsMap.values.toIntArray())
        val diagServices = EcuData.createDiagServicesVector(builder, diagServicesMap.values.toIntArray())
        val variants = EcuData.createVariantsVector(
            builder,
            baseVariantMap.values.toIntArray() + ecuVariantMap.values.toIntArray()
        )
        val functionalGroups = EcuData.createFunctionalGroupsVector(builder, functionalGroupMap.values.toIntArray())

        EcuData.startEcuData(builder)
        EcuData.addVersion(builder, version)
        EcuData.addEcuName(builder, ecuName)
        EcuData.addRevision(builder, odxRevision)

        EcuData.addLibraries(builder, libraries)
        EcuData.addUnits(builder, units)
        EcuData.addAdditionalAudiences(builder, additionalAudiences)
        EcuData.addAudiences(builder, audiences)
        EcuData.addDtcs(builder, dtcs)
        EcuData.addSds(builder, sds)
        EcuData.addSdgs(builder, sdgs)
        EcuData.addSdgss(builder, sdgss)
        EcuData.addFunctClasses(builder, functClasses)
        EcuData.addStates(builder, states)
        EcuData.addStateTransitions(builder, stateTransitions)
        EcuData.addStateCharts(builder, stateCharts)
        EcuData.addStateTransitionRefs(builder, stateTransitionRefs)
        EcuData.addPreConditionStateRefs(builder, preConditionStateRefs)
        EcuData.addDiagCodedTypes(builder, diagCodedTypes)
        EcuData.addDops(builder, dops)
        EcuData.addTableRows(builder, tableRows)
        EcuData.addTables(builder, tables)
        EcuData.addParams(builder, params)
        EcuData.addRequests(builder, requests)
        EcuData.addResponses(builder, responses)
        EcuData.addComParams(builder, comParams)
        EcuData.addComParamSubSets(builder, comParamSubSets)
        EcuData.addProtStacks(builder, protStacks)
        EcuData.addProtocols(builder, protocols)
        EcuData.addSingleEcuJobs(builder, singleEcuJobs)
        EcuData.addDiagServices(builder, diagServices)
        EcuData.addVariants(builder, variants)
        EcuData.addFunctionalGroups(builder, functionalGroups)

        val ecuData = EcuData.endEcuData(builder)

        /*
        builder.addAllEcuSharedDatas(ecuSharedDataMap.values)
        */
        builder.finish(ecuData)
        return builder.sizedByteArray()
    }

    private fun DOPBASE.offset(mustBeCached: Boolean = false): Int {
        return dopMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val sdgs = this.sdgs?.offset()

            val specificData = when (this) {
                is DATAOBJECTPROP -> this.toNormalDop()
                is ENDOFPDUFIELD -> this.toEndOfPduField()
                is STATICFIELD -> this.toStaticField()
                is ENVDATA -> this.toEnvData()
                is ENVDATADESC -> this.toEnvDataDesc()
                is schema.odx.DTCDOP -> this.toDTCDOP()
                is STRUCTURE -> this.toStructure()
                is schema.odx.MUX -> this.toMUXDOP()
                is DYNAMICLENGTHFIELD -> this.toDynamicLengthField()
                else -> throw IllegalStateException("Unknown type of data for DOP: $this")
            }

            val specificDataType = when (this) {
                is DATAOBJECTPROP -> SpecificDOPData.NormalDOP
                is ENDOFPDUFIELD -> SpecificDOPData.EndOfPduField
                is STATICFIELD -> SpecificDOPData.StaticField
                is ENVDATA -> SpecificDOPData.EnvData
                is ENVDATADESC -> SpecificDOPData.EnvDataDesc
                is schema.odx.DTCDOP -> SpecificDOPData.DTCDOP
                is STRUCTURE -> SpecificDOPData.Structure
                is schema.odx.MUX -> SpecificDOPData.MUXDOP
                is DYNAMICLENGTHFIELD -> SpecificDOPData.DynamicLengthField
                else -> throw IllegalStateException("Unknown type of data for DOP: $this")
            }


            DOP.startDOP(builder)
            DOP.addShortName(builder, shortName)
            DOP.addSpecificData(builder, specificData)
            DOP.addSpecificDataType(builder, specificDataType)

            sdgs?.let { DOP.addSdgs(builder, it) }

            DOP.endDOP(builder)
        }
    }

    private fun DIAGSERVICE.offset(mustBeCached: Boolean = false): Int {
        return diagServicesMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val diagComm = (this as DIAGCOMM).offsetInternal()
            val request = odx.requests[this.requestref.idref]?.offset(true)
                ?: throw IllegalStateException("Couldn't find requestref ${this.requestref.idref}")
            val posResponses = this.posresponserefs?.posresponseref?.map {
                val pr = odx.posResponses[it.idref] ?: throw IllegalStateException("Couldn't find response ${it.idref}")
                pr.offset(true)
            }?.toIntArray()?.let {
                DiagService.createPosResponsesVector(builder, it)
            }
            val negResponses = this.negresponserefs?.negresponseref?.map {
                val nr = odx.negResponses[it.idref] ?: throw IllegalStateException("Couldn't find response ${it.idref}")
                nr.offset(true)
            }?.toIntArray()?.let {
                DiagService.createNegResponsesVector(builder, it)
            }
            val comParamRefs = this.comparamrefs?.comparamref?.map {
                it.offset()
            }?.toIntArray()?.let {
                DiagService.createComParamRefsVector(builder, it)
            }

            DiagService.startDiagService(builder)
            DiagService.addDiagComm(builder, diagComm)
            DiagService.addRequest(builder, request)
            posResponses?.let { DiagService.addPosResponses(builder, it) }
            negResponses?.let { DiagService.addNegResponses(builder, it) }
            comParamRefs?.let { DiagService.addComParamRefs(builder, it) }
            this.addressing?.let { DiagService.addAddressing(builder, it.toProtoBufEnum()) }
            this.transmissionmode?.let { DiagService.addTransmissionMode(builder, it.toProtoBufEnum()) }
            DiagService.addIsCyclic(builder, this.isISCYCLIC)
            DiagService.addIsMultiple(builder, this.isISMULTIPLE)
            DiagService.endDiagService(builder)
        }
    }

    private fun TABLE.offset(mustBeCached: Boolean = false): Int {
        return tableMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val semantic = this.semantic?.offset()
            val longName = this.longname?.offset()
            val keyLabel = this.keylabel?.offset()
            val keyDop = this.keydopref?.idref?.let {
                val dop =
                    odx.combinedDataObjectProps[it] ?: throw IllegalStateException("Couldn't find dop ${it}")
                dop.offset(true)
            }
            val structLabel = this.structlabel?.offset()
            val sdgs = this.sdgs?.offset(true)

            val rows = this.rowwrapper.map { row ->
                if (row is TABLEROW) {
                    row.offset(true)
                } else {
                    throw IllegalStateException("Unsupported row type ${row.javaClass.simpleName}")
                }
            }.toIntArray().let {
                TableDop.createRowsVector(builder, it)
            }

            TableDop.startTableDop(builder)
            TableDop.addShortName(builder, shortName)
            semantic?.let { TableDop.addSemantic(builder, it) }
            longName?.let { TableDop.addLongName(builder, it) }
            keyLabel?.let { TableDop.addKeyLabel(builder, it) }
            keyDop?.let { TableDop.addKeyDop(builder, it) }
            structLabel?.let { TableDop.addStructLabel(builder, it) }

            TableDop.addRows(builder, rows)

            // TODO Diag comms
//            table.addAllDiagCommConnector(
//                this.tablediagcommconnectors?.tablediagcommconnector?.map { it.offset() } ?: emptyList()
//            )

            sdgs?.let { TableDop.addSdgs(builder, it) }

            TableDop.endTableDop(builder)
        }
    }

//    private fun TABLEDIAGCOMMCONNECTOR.toProtoBuf(): TableDiagCommConnector {
//        val tdc = TableDiagCommConnector.newBuilder()
//
//        tdc.semantic = this.semantic
//
//        if (this.diagcommref != null) {
//            val diagService = this.diagcommref?.idref?.let { odx.diagServices[it] }
//            val ecuJob = this.diagcommref?.idref?.let { odx.singleEcuJobs[it] }
//            if (diagService == null && ecuJob == null) {
//                throw IllegalStateException("Couldn't resolve ${this.diagcommref.idref}")
//            }
//        } else if (this.diagcommsnref != null) {
//            throw IllegalStateException("Unsupported short name ref ${this.diagcommsnref}")
//        }
//
//        return tdc.build()
//    }

    private fun schema.odx.SD.offset(mustBeCached: Boolean = false): Int {
        return sdMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val value = this.value?.offset()
            val si = this.si?.offset()
            val ti = this.ti?.offset()

            SD.startSD(builder)

            value?.let { SD.addValue(builder, it) }
            si?.let { SD.addSi(builder, it) }
            ti?.let { SD.addTi(builder, it) }

            SD.endSD(builder)
        }
    }

    private fun schema.odx.SDG.offset(mustBeCached: Boolean = false): Int {
        return sdgMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val si = this.si?.offset()

            val caption = this.sdgcaption?.shortname?.let {
                val shortName = it.offset()

                SDGCaption.startSDGCaption(builder)
                SDGCaption.addShortName(builder, shortName)
                SDGCaption.endSDGCaption(builder)
            }

            val sdg = SDG.createSdsVector(builder, this.sdgOrSD.map {
                val sdOrSdg = when (it) {
                    is schema.odx.SD -> it.offset()
                    is schema.odx.SDG -> it.offset()
                    else -> throw IllegalArgumentException("Unknown sdg type: $it")
                }
                SDOrSDG.startSDOrSDG(builder)
                SDOrSDG.addSdOrSdg(builder, sdOrSdg)
                when (it) {
                    is schema.odx.SD -> SDOrSDG.addSdOrSdgType(builder, SDxorSDG.SD)
                    is schema.odx.SDG -> SDOrSDG.addSdOrSdgType(builder, SDxorSDG.SDG)
                    else -> error("This path should never be reached -- unknown object type in SDOrSDG list $it")
                }
                SDOrSDG.endSDOrSDG(builder)
            }.toIntArray())

            SDG.startSDG(builder)
            si?.let { SDG.addSi(builder, it) }
            caption?.let { SDG.addCaption(builder, it) }
            sdg.let { SDG.addSds(builder, it) }

            SDG.endSDG(builder)
        }
    }

    private fun schema.odx.SDGS.offset(mustBeCached: Boolean = false): Int {
        return sdgsMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val sdgs = SDGS.createSdgsVector(builder, this.sdg.map { it.offset() }.toIntArray())

            SDGS.startSDGS(builder)
            SDGS.addSdgs(builder, sdgs)
            SDGS.endSDGS(builder)
        }
    }

    private fun REQUEST.offset(mustBeCached: Boolean = false): Int {
        return requestMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val sdgs = this.sdgs?.offset(true)
            val params = this.params?.param?.map {
                it.offset(true)
            }?.toIntArray()?.let {
                Request.createParamsVector(builder, it)
            }

            Request.startRequest(builder)
            sdgs?.let { Request.addSdgs(builder, it) }
            params?.let { Request.addParams(builder, it) }
            Request.endRequest(builder)
        }
    }

    private fun RESPONSE.offset(mustBeCached: Boolean = false): Int {
        return responseMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val sdgs = this.sdgs?.offset()
            val params = this.params?.param?.map {
                it.offset(true)
            }?.toIntArray()?.let {
                Request.createParamsVector(builder, it)
            }

            Response.startResponse(builder)
            when (this) {
                is POSRESPONSE -> Response.addResponseType(builder, ResponseType.POS_RESPONSE)
                is NEGRESPONSE -> Response.addResponseType(builder, ResponseType.NEG_RESPONSE)
                is GLOBALNEGRESPONSE -> Response.addResponseType(builder, ResponseType.GLOBAL_NEG_RESPONSE)
                else -> throw IllegalStateException("Unknown response type ${this::class.java.simpleName}")
            }
            sdgs?.let {
                Response.addSdgs(builder, it)
            }
            params?.let {
                Response.addParams(builder, it)
            }
            Response.endResponse(builder)
        }
    }

    private fun PARAM.offset(mustBeCached: Boolean = false): Int {
        return paramMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            try {
                val shortName = this.shortname.offset()
                val semantic = this.semantic?.offset()
                val sdgs = this.sdgs?.offset()

                val specificData = when (this) {
                    is VALUE -> {
                        this.dopsnref?.shortname?.let {
                            TODO("dop shortname ref in VALUE not supported ${this.dopsnref.shortname}")
                        }
                        val dop = this.dopref?.let {
                            val dop = odx.combinedDataObjectProps[it.idref]
                                ?: throw IllegalStateException("Couldn't find ${it.idref}")
                            dop.offset(true)
                        }
                        val physicalDefaultValue = this.physicaldefaultvalue?.offset()

                        Value.startValue(builder)
                        dop?.let { Value.addDop(builder, it) }
                        physicalDefaultValue?.let { Value.addPhysicalDefaultValue(builder, it) }
                        Value.endValue(builder)

                    }

                    is CODEDCONST -> {
                        val diagCodedType = this.diagcodedtype.offset(true)
                        val codedValue = this.codedvalue?.offset()

                        CodedConst.startCodedConst(builder)
                        CodedConst.addDiagCodedType(builder, diagCodedType)
                        codedValue?.let { CodedConst.addDiagCodedType(builder, it) }
                        CodedConst.endCodedConst(builder)
                    }

                    is DYNAMIC -> {
                        Dynamic.startDynamic(builder)
                        Dynamic.endDynamic(builder)
                    }

                    is LENGTHKEY -> {
                        if (this.dopsnref?.shortname != null) {
                            TODO("DOP short name not supported ${this.dopsnref.shortname}")
                        }
                        val dop = this.dopref?.let {
                            val dop = odx.combinedDataObjectProps[it.idref]
                                ?: throw IllegalStateException("Couldn't find ${it.idref}")
                            dop.offset(true)
                        }

                        LengthKeyRef.startLengthKeyRef(builder)
                        dop?.let { LengthKeyRef.addDop(builder, it) }
                        LengthKeyRef.endLengthKeyRef(builder)
                    }

                    is MATCHINGREQUESTPARAM -> {
                        MatchingRequestParam.startMatchingRequestParam(builder)
                        MatchingRequestParam.addRequestBytePos(builder, this.requestbytepos)
                        MatchingRequestParam.addByteLength(builder, this.bytelength.toUInt())
                        MatchingRequestParam.endMatchingRequestParam(builder)
                    }

                    is NRCCONST -> {
                        val diagCodedType = this.diagcodedtype?.offset(true)
                        val codedValues = this.codedvalues?.codedvalue?.map { it.offset() }?.toIntArray()?.let {
                            NrcConst.createCodedValuesVector(builder, it)
                        }

                        NrcConst.startNrcConst(builder)
                        diagCodedType?.let { NrcConst.addDiagCodedType(builder, it) }
                        codedValues?.let { NrcConst.addCodedValues(builder, it) }
                        NrcConst.endNrcConst(builder)
                    }

                    is PHYSCONST -> {
                        val physConstValue = this.physconstantvalue?.offset()
                        val dop = this.dopref?.let {
                            val dop = odx.combinedDataObjectProps[it.idref]
                                ?: throw IllegalStateException("couldn't find dop ${it.idref}")
                            dop.offset(true)
                        }
                        this.dopsnref?.shortname?.let {
                            TODO("DOP short name not supported ${this.dopsnref.shortname}")
                        }

                        PhysConst.startPhysConst(builder)
                        physConstValue?.let { PhysConst.addPhysConstantValue(builder, it) }
                        dop?.let { PhysConst.addDop(builder, it) }
                        PhysConst.endPhysConst(builder)
                    }

                    is RESERVED -> {
                        Reserved.startReserved(builder)
                        Reserved.addBitLength(builder, this.bitlength.toUInt())
                        Reserved.endReserved(builder)
                    }

                    is SYSTEM -> {
                        this.dopsnref?.shortname?.let {
                            TODO("DOP short name ref ${this.dopsnref.shortname}")
                        }

                        val sysParam = this.sysparam.offset()
                        val dop = this.dopref?.let {
                            val dop = odx.combinedDataObjectProps[it.idref]
                                ?: throw IllegalStateException("Couldn't find DOP ${it.idref}")
                            dop.offset(true)
                        }

                        dataformat.System.startSystem(builder)

                        dataformat.System.addSysParam(builder, sysParam)
                        dop?.let { dataformat.System.addSysParam(builder, it) }

                        dataformat.System.endSystem(builder)
                    }

                    is TABLEKEY -> {
                        val entry =
                            this.rest.firstOrNull()?.value
                                ?: throw IllegalStateException("TABLE-KEY ${this.id} has no entries")
                        if (this.rest.size > 1) {
                            throw IllegalStateException("TABLE-KEY ${this.id} has more than one entry")
                        }
                        var tableKeyReference: Int
                        var tableKeyReferenceType: UByte
                        if (entry is ODXLINK) {
                            val table = odx.tables[entry.idref]
                            if (table == null) {
                                val row = odx.tableRows[entry.idref]
                                    ?: throw IllegalStateException("ODXLINK ${this.id} is neither TABLE nor TABLE-KEY")
                                tableKeyReference = row.offset(true)
                                tableKeyReferenceType = TableKeyReference.TableRow
                            } else {
                                tableKeyReference = 0 // TODO table.offset()
                                tableKeyReferenceType = TableKeyReference.TableDop
                            }
                        } else {
                            throw IllegalStateException("Unknown type for TABLE-KEY/TABLEROW ${this.id} entry ${entry.javaClass.simpleName}")
                        }

                        TableKey.startTableKey(builder)
                        TableKey.addTableKeyReference(builder, tableKeyReference)
                        TableKey.addTableKeyReferenceType(builder, tableKeyReferenceType)
                        TableKey.endTableKey(builder)
                    }

                    is TABLEENTRY -> {
                        val param = (this as PARAM).offset()
                        val target = this.target?.toProtoBufEnum()
                        val tableRow = this.tablerowref.idref?.let {
                            val row = odx.tableRows[it] ?: throw IllegalStateException("Couldn't find TABLE-ROW $it")
                            row.offset()
                        }

                        TableEntry.startTableEntry(builder)
                        TableEntry.addParam(builder, param)
                        target?.let { TableEntry.addTarget(builder, it) }
                        tableRow?.let { TableEntry.addTableRow(builder, it) }
                        TableEntry.endTableEntry(builder)
                    }

                    is TABLESTRUCT -> {
                        this.tablekeysnref?.let {
                            TODO("TABLE-KEY-SNREF not supported ${this.shortname}")
                        }
                        val tableKey = odx.tableKeys[this.tablekeyref.idref]?.offset(true)
                            ?: throw IllegalStateException("Couldn't find TABLE-KEY ${this.tablekeyref.idref}")

                        TableStruct.startTableStruct(builder)
                        tableKey.let { TableStruct.addTableKey(builder, it) }
                        TableStruct.endTableStruct(builder)
                    }

                    else -> throw IllegalStateException("Unknown object type ${this.javaClass.simpleName}")
                }
                val specificDataType = when (this) {
                    is VALUE -> ParamSpecificData.Value
                    is CODEDCONST -> ParamSpecificData.CodedConst
                    is DYNAMIC -> ParamSpecificData.Dynamic
                    is LENGTHKEY -> ParamSpecificData.LengthKeyRef
                    is MATCHINGREQUESTPARAM -> ParamSpecificData.MatchingRequestParam
                    is NRCCONST -> ParamSpecificData.NrcConst
                    is PHYSCONST -> ParamSpecificData.PhysConst
                    is RESERVED -> ParamSpecificData.Reserved
                    is SYSTEM -> ParamSpecificData.System
                    is TABLEKEY -> ParamSpecificData.TableKey
                    is TABLEENTRY -> ParamSpecificData.TableEntry
                    is TABLESTRUCT -> ParamSpecificData.TableStruct
                    else -> throw IllegalStateException("Unknown object type ${this.javaClass.simpleName}")
                }

                Param.startParam(builder)
                Param.addParamType(builder, this.toParamTypeEnum())
                Param.addShortName(builder, shortName)
                semantic?.let { Param.addSemantic(builder, it) }

                sdgs?.let {
                    Param.addSdgs(builder, it)
                }

                this.byteposition?.let { Param.addBytePosition(builder, it.toUInt()) }
                this.bitposition?.let { Param.addBitPosition(builder, it.toUInt()) }

                Param.addSpecificData(builder, specificData)
                Param.addSpecificDataType(builder, specificDataType)

                Param.endParam(builder)
            } catch (e: Exception) {
                throw IllegalStateException("Error in Param ${this.shortname}", e)
            }
        }
    }

    private fun FUNCTCLASS.offset(mustBeCached: Boolean = false): Int {
        return functClasses.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortname = this.shortname.offset()

            FunctClass.startFunctClass(builder)
            FunctClass.addShortName(builder, shortname)
            FunctClass.endFunctClass(builder)
        }
    }

    private fun STANDARDLENGTHTYPE.offsetType(): Int {
        val bitmask = this.bitmask?.offset()

        StandardLengthType.startStandardLengthType(builder)
        bitmask?.let {
            StandardLengthType.addBitMask(builder, it)
        }
        StandardLengthType.addCondensed(builder, this.isCONDENSED)
        StandardLengthType.addBitLength(builder, this.bitlength.toUInt())

        return StandardLengthType.endStandardLengthType(builder)
    }

    private fun MINMAXLENGTHTYPE.offsetType(): Int {
        MinMaxLengthType.startMinMaxLengthType(builder)
        MinMaxLengthType.addMinLength(builder, this.minlength.toUInt())
        this.maxlength?.let {
            MinMaxLengthType.addMaxLength(builder, this.maxlength.toUInt())
        }
        this.termination?.let {
            MinMaxLengthType.addTermination(builder, this.termination.toProtoBufEnum())
        }
        return MinMaxLengthType.endMinMaxLengthType(builder)
    }

    private fun LEADINGLENGTHINFOTYPE.offsetType(): Int {
        LeadingLengthInfoType.startLeadingLengthInfoType(builder)
        LeadingLengthInfoType.addBitLength(builder, this.bitlength.toUInt())
        return LeadingLengthInfoType.endLeadingLengthInfoType(builder)
    }

    private fun PARAMLENGTHINFOTYPE.offsetType(): Int {
        val lengthKey = odx.lengthKeys[this.lengthkeyref.idref]?.offset()
            ?: throw IllegalStateException("Unknown length key reference ${this.lengthkeyref.idref}")

        ParamLengthInfoType.startParamLengthInfoType(builder)
// TODO
//        ParamLengthInfoType.addLengthKey(builder, lengthKey)
        return ParamLengthInfoType.endParamLengthInfoType(builder)
    }

    private fun DATAOBJECTPROP.toNormalDop(): Int {
        val diagCodedType = this.diagcodedtype?.offset()
        val unit = this.unitref?.let {
            val unit = odx.units[it.idref] ?: throw IllegalStateException("Couldn't find unit ${it.idref}")
            unit.offset(true)
        }
        val physicalType = this.physicaltype?.offset()
        val compuMethod = this.compumethod?.offset()

        NormalDOP.startNormalDOP(builder)
        diagCodedType?.let { NormalDOP.addDiagCodedType(builder, it) }
        unit?.let { NormalDOP.addUnitRef(builder, it) }
        physicalType?.let { NormalDOP.addPhysicalType(builder, it) }
        compuMethod?.let { NormalDOP.addCompuMethod(builder, it) }

        return NormalDOP.endNormalDOP(builder)
    }

    private fun DIAGCODEDTYPE.offset(mustBeCached: Boolean = false): Int {
        return diagCodedTypes.getCachedOffset(this, mustBeCached = mustBeCached) {
            val baseTypeEncoding = this.basetypeencoding?.offset()

            val specificData = when (this) {
                is STANDARDLENGTHTYPE -> this.offsetType()
                is MINMAXLENGTHTYPE -> this.offsetType()
                is LEADINGLENGTHINFOTYPE -> this.offsetType()
                is PARAMLENGTHINFOTYPE -> this.offsetType()
                else -> {
                    throw IllegalStateException("Unsupported diag coded type ${this::class.java.simpleName}")
                }
            }
            val specificType = when (this) {
                is STANDARDLENGTHTYPE -> SpecificDataType.StandardLengthType
                is MINMAXLENGTHTYPE -> SpecificDataType.MinMaxLengthType
                is LEADINGLENGTHINFOTYPE -> SpecificDataType.LeadingLengthInfoType
                is PARAMLENGTHINFOTYPE -> SpecificDataType.ParamLengthInfoType
                else -> {
                    throw IllegalStateException("Unsupported diag coded type ${this::class.java.simpleName}")
                }
            }
            DiagCodedType.startDiagCodedType(builder)

            DiagCodedType.addType(builder, this.toTypeEnum())
            DiagCodedType.addBaseDataType(builder, this.basedatatype.toDiagCodedTypeEnum())
            baseTypeEncoding?.let {
                DiagCodedType.addBaseTypeEncoding(builder, it)
            }
            DiagCodedType.addIsHighLowByteOrder(builder, this.isISHIGHLOWBYTEORDER)
            DiagCodedType.addSpecificData(builder, specificData)
            DiagCodedType.addSpecificDataType(builder, specificType)

            DiagCodedType.endDiagCodedType(builder)
        }
    }

    private fun UNIT.offset(mustBeCached: Boolean = false): Int {
        return unitMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val displayName = this.displayname.offset()
            val physicaldimension = this.physicaldimensionref?.let { ref ->
                val physDimension = odx.physDimensions[ref.idref]
                    ?: throw IllegalStateException("Couldn't find physical dimension ${ref.idref}")
                physDimension.offset()
            }

            dataformat.Unit.startUnit(builder)

            dataformat.Unit.addShortName(builder, shortName)
            dataformat.Unit.addDisplayName(builder, displayName)
            this.factorsitounit?.let {
                dataformat.Unit.addFactorsitounit(builder, it)
            }
            this.offsetsitounit?.let {
                dataformat.Unit.addOffsetitounit(builder, it)
            }
            physicaldimension?.let {
                dataformat.Unit.addPhysicalDimension(builder, it)
            }

            dataformat.Unit.endUnit(builder)
        }
    }

    private fun ENDOFPDUFIELD.toEndOfPduField(): Int {

        val field = this.toField()

        EndOfPduField.startEndOfPduField(builder)

        this.maxnumberofitems?.let { EndOfPduField.addMaxNumberOfItems(builder, it.toUInt()) }
        this.minnumberofitems?.let { EndOfPduField.addMinNumberOfItems(builder, it.toUInt()) }
        EndOfPduField.addField(builder, field)

        return EndOfPduField.endEndOfPduField(builder)
    }

    private fun STATICFIELD.toStaticField(): Int {
        val field = this.toField()

        StaticField.startStaticField(builder)

        StaticField.addFixedNumberOfItems(builder, this.fixednumberofitems.toUInt())
        StaticField.addItemByteSize(builder, this.itembytesize.toUInt())
        StaticField.addField(builder, field)

        return StaticField.endStaticField(builder)
    }

    private fun FIELD.toField(): Int {
        val basicStructure = this.basicstructureref?.let {
            val dop =
                odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find dop ${it.idref}")
            dop.offset(true)
        }
        this.basicstructuresnref?.let {
            throw IllegalStateException("Short name reference for basic structure ref not supported")
        }
        val envDataRef = this.envdatadescref?.let {
            val dop =
                odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find dop ${it.idref}")
            dop.offset(true)
        }
        this.envdatadescsnref?.let {
            throw IllegalStateException("Short name reference for envdata desc not supported")
        }

        Field.startField(builder)

        basicStructure?.let { Field.addBasicStructure(builder, it) }
        envDataRef?.let { Field.addEnvDataDesc(builder, it) }
        Field.addIsVisible(builder, this.isISVISIBLE)

        return Field.endField(builder)
    }

    private fun ENVDATA.toEnvData(): Int {
        val dtcValues = this.dtcvalues?.dtcvalue?.map { it.value.toUInt() }?.toUIntArray()?.let {
            EnvData.createDtcValuesVector(builder, it)
        }
//        val params = this.params?.param?.map { it.offset() }?.toIntArray()?.let {
//            EnvData.createParamsVector(builder, it)
//        }

        EnvData.startEnvData(builder)
        dtcValues?.let { EnvData.addDtcValues(builder, dtcValues) }
// TODO
//        params?.let { EnvData.addParams(builder, params) }
        return EnvData.endEnvData(builder)
    }

    private fun ENVDATADESC.toEnvDataDesc(): Int {
        val envDatas = this.envdatarefs?.envdataref?.map {
            val envData =
                odx.envDatas[it.idref] ?: throw IllegalStateException("Couldn't find env data ${it.idref}")
            envData.offset(true)
        }?.toIntArray()?.let {
            EnvDataDesc.createEnvDatasVector(builder, it)
        }

        val paramShortName = this.paramsnref?.shortname?.offset()
        val paramShortNamePath = this.paramsnpathref?.shortnamepath?.offset()

        EnvDataDesc.startEnvDataDesc(builder)

        envDatas?.let { EnvDataDesc.addEnvDatas(builder, it) }

        paramShortName?.let { EnvDataDesc.addParamShortName(builder, it) }
        paramShortNamePath?.let { EnvDataDesc.addParamPathShortName(builder, it) }

        return EnvData.endEnvData(builder)
    }

    private fun PHYSICALTYPE.offset(): Int {
        PhysicalType.startPhysicalType(builder)

        PhysicalType.addBaseDataType(builder, this.basedatatype.toProtoBufEnum())
        this.precision?.let { PhysicalType.addPrecision(builder, it.toUInt()) }
        this.displayradix?.let { PhysicalType.addDisplayRadix(builder, it.toProtoBufEnum()) }

        return PhysicalType.endPhysicalType(builder)
    }

    private fun schema.odx.DTC.offset(mustBeCached: Boolean = false): Int {
        return dtcs.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val displayTroubleCode = this.displaytroublecode?.offset()
            val text = this.text?.offset()
            val sdgs = this.sdgs?.offset()

            DTC.startDTC(builder)

            DTC.addShortName(builder, shortName)
            DTC.addTroubleCode(builder, this.troublecode.toUInt())

            displayTroubleCode?.let {
                DTC.addDisplayTroubleCode(builder, it)
            }
            text?.let {
                DTC.addText(builder, it)
            }
            this.level?.let {
                DTC.addLevel(builder, it.toUInt())
            }
            sdgs?.let {
                DTC.addSdgs(builder, it)
            }
            DTC.addIsTemporary(builder, this.isISTEMPORARY)

            DTC.endDTC(builder)
        }
    }

    private fun LONGNAME.offset(): Int {
        val tiOffset = this.ti?.offset()
        val valueOffset = this.value?.offset()

        LongName.startLongName(builder)

        tiOffset?.let { LongName.addTi(builder, tiOffset) }
        valueOffset?.let { LongName.addValue(builder, valueOffset) }

        return LongName.endLongName(builder)
    }

    private fun TEXT.offset(): Int {
        val ti = this.ti?.offset()
        val value = this.value?.offset()

        Text.startText(builder)

        ti?.let { Text.addTi(builder, it) }
        value?.let { Text.addValue(builder, it) }

        return Text.endText(builder)
    }

    private fun COMPUMETHOD.offset(): Int {
        val internalToPhys = this.compuinternaltophys?.offset()
        val physToInternal = this.compuphystointernal?.offset()

        CompuMethod.startCompuMethod(builder)

        this.category?.let { CompuMethod.addCategory(builder, it.toProtoBufEnum()) }
        internalToPhys?.let { CompuMethod.addInternalToPhys(builder, it) }
        physToInternal?.let { CompuMethod.addPhysToInternal(builder, it) }
        return CompuMethod.endCompuMethod(builder)
    }

    private fun LIBRARY.offset(mustBeCached: Boolean = false): Int {
        return librariesMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val codeFile = this.codefile.offset()
            val encryption = this.encryption?.offset()
            val syntax = this.syntax.offset()
            val entrypoint = this.entrypoint?.offset()

            Library.startLibrary(builder)
            Library.addShortName(builder, shortName)
            longName?.let {
                Library.addLongName(builder, longName)
            }
            Library.addCodeFile(builder, codeFile)
            encryption?.let {
                Library.addEncryption(builder, it)
            }
            Library.addSyntax(builder, syntax)
            entrypoint?.let {
                Library.addEntryPoint(builder, it)
            }
            Library.endLibrary(builder)
        }
    }

    private fun PROGCODE.offset(): Int {
        val codeFile = this.codefile?.offset()
        val encryption = this.entrypoint?.offset()
        val syntax = this.syntax?.offset()
        val revision = this.revision?.offset()
        val entrypoint = this.entrypoint?.offset()
        val libraries = this.libraryrefs?.libraryref?.map { ref ->
            val library =
                odx.libraries[ref.idref] ?: throw IllegalStateException("Couldn't find LIBRARY ${ref.idref}")
            library.offset()
        }?.toIntArray()?.let {
            ProgCode.createLibraryVector(builder, it)
        }

        ProgCode.startProgCode(builder)
        codeFile?.let { ProgCode.addCodeFile(builder, it) }
        encryption?.let { ProgCode.addEncryption(builder, it) }
        syntax?.let { ProgCode.addSyntax(builder, it) }
        revision?.let { ProgCode.addRevision(builder, it) }
        entrypoint?.let { ProgCode.addEncryption(builder, it) }
        libraries?.let { ProgCode.addLibrary(builder, it) }

        return ProgCode.endProgCode(builder)
    }

    private fun COMPUPHYSTOINTERNAL.offset(): Int {
        val progcode = this.progcode?.offset()
        val compuscales = this.compuscales?.compuscale?.map { it.offset() }?.toIntArray()?.let {
            CompuPhysToInternal.createCompuScalesVector(builder, it)
        }
        val compudefaultvalue = this.compudefaultvalue?.offset()

        CompuPhysToInternal.startCompuPhysToInternal(builder)
        progcode?.let { CompuPhysToInternal.addProgCode(builder, it) }
        compuscales?.let { CompuPhysToInternal.addCompuScales(builder, it) }
        compudefaultvalue?.let { CompuPhysToInternal.addCompuDefaultValue(builder, it) }

        return CompuPhysToInternal.endCompuPhysToInternal(builder)
    }

    private fun COMPUINTERNALTOPHYS.offset(): Int {
        val progcode = this.progcode?.offset()
        val compuscales = this.compuscales?.compuscale?.map { it.offset() }?.toIntArray()?.let {
            CompuInternalToPhys.createCompuScalesVector(builder, it)
        }
        val compudefaultvalue = this.compudefaultvalue?.offset()

        CompuInternalToPhys.startCompuInternalToPhys(builder)

        progcode?.let { CompuInternalToPhys.addProgCode(builder, it) }
        compuscales?.let { CompuInternalToPhys.addCompuScales(builder, it) }
        compudefaultvalue?.let { CompuInternalToPhys.addCompuDefaultValue(builder, it) }

        return CompuInternalToPhys.endCompuInternalToPhys(builder)
    }

    private fun COMPUSCALE.offset(): Int {
        val shortLabel = this.shortlabel?.offset()
        val lowerLimit = this.lowerlimit?.offset()
        val upperLimit = this.upperlimit?.offset()
        val compuInverseValue = this.compuinversevalue?.offset()
        val compuConst = this.compuconst?.offset()
        val rationalCoEffs = this.compurationalcoeffs?.offset()

        CompuScale.startCompuScale(builder)
        shortLabel?.let { CompuScale.addShortLabel(builder, it) }
        lowerLimit?.let { CompuScale.addLowerLimit(builder, it) }
        upperLimit?.let { CompuScale.addUpperLimit(builder, it) }
        compuInverseValue?.let { CompuScale.addInverseValues(builder, it) }
        compuConst?.let { CompuScale.addConsts(builder, it) }
        rationalCoEffs?.let { CompuScale.addRationalCoEffs(builder, it) }
        return CompuScale.endCompuScale(builder)
    }

    private fun LIMIT.offset(): Int {
        val value = this.value?.offset()

        Limit.startLimit(builder)
        value?.let { Limit.addValue(builder, value) }
        this.intervaltype?.let { Limit.addIntervalType(builder, it.toProtoBufEnum()) }
        return Limit.endLimit(builder)
    }

    private fun COMPUINVERSEVALUE.offset(): Int {
        val vtValue = this.vt?.value?.offset()
        val vtTi = this.vt?.ti?.offset()

        CompuValues.startCompuValues(builder)
        this.v?.value?.let {
            CompuValues.addV(builder, it)
        }
        vtValue?.let {
            CompuValues.addVt(builder, it)
        }
        vtTi?.let {
            CompuValues.addVtTi(builder, it)
        }
        return CompuValues.endCompuValues(builder)
    }

    private fun COMPUCONST.offset(): Int {
        val vtValue = this.vt?.value?.offset()
        val vtTi = this.vt?.ti?.offset()

        CompuValues.startCompuValues(builder)
        this.v?.value?.let {
            CompuValues.addV(builder, it)
        }
        vtValue?.let {
            CompuValues.addVt(builder, it)
        }
        vtTi?.let {
            CompuValues.addVtTi(builder, it)
        }
        return CompuValues.endCompuValues(builder)
    }

    private fun COMPUDEFAULTVALUE.offset(): Int {
        val vtTi = this.vt?.ti?.offset()
        val vtValue = this.vt?.value?.offset()
        val values = if (vtValue != null || vtTi != null || this.v?.value != null) {
            CompuValues.startCompuValues(builder)
            this.v?.value?.let {
                CompuValues.addV(builder, it)
            }
            vtValue?.let {
                CompuValues.addVt(builder, it)
            }
            vtTi?.let {
                CompuValues.addVtTi(builder, it)
            }
            CompuValues.endCompuValues(builder)
        } else null

        val invVtTi = this.compuinversevalue?.vt?.ti?.offset()
        val invVtValue = this.compuinversevalue?.vt?.value?.offset()
        val inverseValues = if (invVtTi != null || invVtValue != null || this.compuinversevalue?.vt?.value != null) {
            CompuValues.startCompuValues(builder)
            this.v?.value?.let {
                CompuValues.addV(builder, it)
            }
            vtValue?.let {
                CompuValues.addVt(builder, it)
            }
            vtTi?.let {
                CompuValues.addVtTi(builder, it)
            }
            CompuValues.endCompuValues(builder)
        } else null

        CompuDefaultValue.startCompuDefaultValue(builder)
        values?.let {
            CompuDefaultValue.addValues(builder, it)
        }
        inverseValues?.let {
            CompuDefaultValue.addInverseValues(builder, it)
        }
        return CompuDefaultValue.endCompuDefaultValue(builder)
    }

    private fun COMPURATIONALCOEFFS.offset(): Int {
        val numerator = this.compunumerator?.v?.mapNotNull { it.value }?.let {
            CompuRationalCoEffs.createNumeratorVector(builder, it.toDoubleArray())
        }
        val denominator = this.compudenominator?.v?.mapNotNull { it.value }?.let {
            CompuRationalCoEffs.createDenominatorVector(builder, it.toDoubleArray())
        }
        CompuRationalCoEffs.startCompuRationalCoEffs(builder)
        numerator?.let { CompuRationalCoEffs.addNumerator(builder, it) }
        denominator?.let { CompuRationalCoEffs.addDenominator(builder, it) }
        return CompuRationalCoEffs.endCompuRationalCoEffs(builder)
    }

    private fun schema.odx.DTCDOP.toDTCDOP(): Int {
        val diagCodedType = this.diagcodedtype.offset()
        val physicalType = this.physicaltype.offset()
        val compuMethod = this.compumethod.offset()

        val dtcs = this.dtcs.dtcproxy?.map {
            if (it is schema.odx.DTC) {
                it.offset()
            } else if (it is ODXLINK) {
                val dop = odx.dtcs[it.idref] ?: throw IllegalStateException("Couldn't find DTC ${it.idref}")
                dop.offset()
            } else {
                throw IllegalStateException("Unsupported DTC type ${it::class.java.simpleName}")
            }
        }?.toIntArray()?.let {
            DTCDOP.createDtcsVector(builder, it)
        }

        DTCDOP.startDTCDOP(builder)

        DTCDOP.addDiagCodedType(builder, diagCodedType)
        DTCDOP.addPhysicalType(builder, physicalType)
        DTCDOP.addCompuMethod(builder, compuMethod)
        dtcs?.let { DTCDOP.addDtcs(builder, it) }
        DTCDOP.addIsVisible(builder, this.isISVISIBLE)

        return DTCDOP.endDTCDOP(builder)
    }

    private fun schema.odx.MUX.toMUXDOP(): Int {
        val switchKey = this.switchkey.offset()
        val defaultCase = this.defaultcase?.offset()
        val cases = this.cases?.case?.map { it.offset() }?.toIntArray()?.let { MUXDOP.createCasesVector(builder, it) }

        MUXDOP.startMUXDOP(builder)
        MUXDOP.addBytePosition(builder, this.byteposition.toUInt())
        MUXDOP.addSwitchKey(builder, switchKey)
        defaultCase?.let { MUXDOP.addDefaultCase(builder, it) }
        cases?.let { MUXDOP.addCases(builder, it) }
        MUXDOP.addIsVisible(builder, this.isISVISIBLE)
        return MUXDOP.endMUXDOP(builder)
    }

    private fun DYNAMICLENGTHFIELD.toDynamicLengthField(): Int {
        val field = (this as FIELD).toField()
        val determineNumberOfItems = this.determinenumberofitems.offset()

        DynamicLengthField.startDynamicLengthField(builder)

        DynamicLengthField.addOffset(builder, this.offset.toUInt())
        DynamicLengthField.addField(builder, field)
        DynamicLengthField.addDetermineNumberOfItems(builder, determineNumberOfItems)

        return DynamicLengthField.endDynamicLengthField(builder)
    }

    private fun STRUCTURE.toStructure(): Int {
        // TODO - reference by id
//        val params = this.params?.param?.map {
//            it.offset()
//        }?.toIntArray()?.let {
//            Structure.createParamsVector(builder, it)
//        }

        Structure.startStructure(builder)

        this.bytesize?.let { Structure.addByteSize(builder, it.toUInt()) }
//        params?.let { Structure.addParams(builder, it) }
        Structure.addIsVisible(builder, this.isISVISIBLE)

        return Structure.endStructure(builder)
    }

    private fun DETERMINENUMBEROFITEMS.offset(): Int {

        val dop = this.dataobjectpropref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find ${it.idref}")
            dop.offset(true)
        }

        DetermineNumberOfItems.startDetermineNumberOfItems(builder)

        DetermineNumberOfItems.addBytePosition(builder, this.byteposition.toUInt())
        this.bitposition?.let { DetermineNumberOfItems.addBitPosition(builder, it.toUInt()) }
        dop?.let { DetermineNumberOfItems.addDop(builder, it) }

        return DetermineNumberOfItems.endDetermineNumberOfItems(builder)
    }

    private fun ADDITIONALAUDIENCE.offset(mustBeCached: Boolean = false): Int {
        return additionalAudienceMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()

            AdditionalAudience.startAdditionalAudience(builder)

            AdditionalAudience.addShortName(builder, shortName)
            longName?.let {
                AdditionalAudience.addLongName(builder, it)
            }

            AdditionalAudience.endAdditionalAudience(builder)
        }
    }

    private fun AUDIENCE.offset(mustBeCached: Boolean = false): Int {
        return audiencesMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val enabledAudiences = this.enabledaudiencerefs?.enabledaudienceref?.let { aa ->
                Audience.createEnabledAudiencesVector(builder, aa.map {
                    val aud = odx.additionalAudiences[it.idref]
                        ?: throw IllegalStateException("Can't find additional audience ${it.idref}")
                    aud.offset(true)
                }.toIntArray())
            }

            val disabledAudiences = this.disabledaudiencerefs?.disabledaudienceref?.let { aa ->
                Audience.createDisabledAudiencesVector(builder, aa.map {
                    val aud = odx.additionalAudiences[it.idref]
                        ?: throw IllegalStateException("Can't find additional audience ${it.idref}")
                    aud.offset(true)
                }.toIntArray())
            }

            Audience.startAudience(builder)
            enabledAudiences?.let {
                Audience.addEnabledAudiences(
                    builder,
                    it
                )
            }

            disabledAudiences?.let {
                Audience.addDisabledAudiences(builder, it)
            }

            Audience.addIsSupplier(builder, this.isISSUPPLIER)
            Audience.addIsDevelopment(builder, this.isISDEVELOPMENT)
            Audience.addIsManufacturing(builder, this.isISMANUFACTURING)
            Audience.addIsAfterSales(builder, this.isISAFTERSALES)
            Audience.addIsAfterMarket(builder, this.isISAFTERMARKET)

            Audience.endAudience(builder)
        }
    }

    private fun PRECONDITIONSTATEREF.offset(mustBeCached: Boolean = false): Int {
        return preConditionStateRefsMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val state =
                odx.states[this.idref]?.offset(true) ?: throw IllegalStateException("Couldn't find STATE ${this.idref}")
            val value = this.value?.offset()
            val inParamIfSnRef = this.inparamifsnref?.shortname?.offset()
            val inParamIfSnPathRef = this.inparamifsnpathref?.shortnamepath?.offset()

            PreConditionStateRef.startPreConditionStateRef(builder)
            PreConditionStateRef.addState(builder, state)
            value?.let { PreConditionStateRef.addValue(builder, it) }
            inParamIfSnRef?.let { PreConditionStateRef.addInParamIfShortName(builder, it) }
            inParamIfSnPathRef?.let { PreConditionStateRef.addInParamPathShortName(builder, it) }
            PreConditionStateRef.endPreConditionStateRef(builder)
        }
    }

    private fun STATETRANSITIONREF.offset(mustBeCached: Boolean = false): Int {
        return stateTransitionRefMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val value = this.value?.offset()
            val stateTransition = this.idref?.let {
                val stateTransition = odx.stateTransitions[this.idref]
                    ?: throw IllegalStateException("Couldn't find STATETRANSITION ${this.idref}")
                stateTransition.offset(true)
            }

            StateTransitionRef.startStateTransitionRef(builder)

            value?.let { StateTransitionRef.addValue(builder, it) }
            stateTransition?.let { StateTransitionRef.addStateTransition(builder, it) }

            StateTransitionRef.endStateTransitionRef(builder)
        }
    }

    private fun INPUTPARAM.offset(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val physicalDefaultValue = this.physicaldefaultvalue?.offset()
        val semantic = this.semantic?.offset()
        val dop = this.dopbaseref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Can't find DOP ${it.idref}")
            dop.offset(true)
        }

        JobParam.startJobParam(builder)
        JobParam.addShortName(builder, shortName)
        longName?.let { JobParam.addLongName(builder, it) }
        physicalDefaultValue?.let { JobParam.addPhysicalDefaultValue(builder, it) }
        semantic?.let { JobParam.addSemantic(builder, it) }
        dop?.let { JobParam.addDopBase(builder, it) }
        return JobParam.endJobParam(builder)
    }

    private fun OUTPUTPARAM.offset(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val semantic = this.semantic?.offset()
        val dop = this.dopbaseref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Can't find DOP ${it.idref}")
            dop.offset(true)
        }

        JobParam.startJobParam(builder)
        JobParam.addShortName(builder, shortName)
        longName?.let { JobParam.addLongName(builder, it) }
        semantic?.let { JobParam.addSemantic(builder, it) }
        dop?.let { JobParam.addDopBase(builder, it) }
        return JobParam.endJobParam(builder)
    }

    private fun NEGOUTPUTPARAM.offset(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val dop = this.dopbaseref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Can't find DOP ${it.idref}")
            dop.offset(true)
        }

        JobParam.startJobParam(builder)
        JobParam.addShortName(builder, shortName)
        longName?.let { JobParam.addLongName(builder, it) }
        dop?.let { JobParam.addDopBase(builder, it) }
        return JobParam.endJobParam(builder)
    }

    private fun DIAGCOMM.offsetInternal(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val diagClass = this.diagnosticclass?.toProtoBufEnum()
        val functClasses = this.functclassrefs?.functclassref?.map {
            val functClass =
                odx.functClasses[it.idref] ?: throw IllegalStateException("Couldn't find funct class ${it.idref}")
            functClass.offset(true)
        }?.toIntArray()?.let {
            DiagComm.createFunctClassVector(builder, it)
        }
        val semantic = this.semantic?.offset()
        val preconditionStateRefs = this.preconditionstaterefs?.preconditionstateref?.map {
            it.offset(true)
        }?.toIntArray()?.let {
            DiagComm.createPreConditionStateRefsVector(builder, it)
        }
        val stateTransitionRefs = this.statetransitionrefs?.statetransitionref?.map {
            it.offset(true)
        }?.toIntArray()?.let {
            DiagComm.createStateTransitionRefsVector(builder, it)
        }
        val protocolRefs = this.protocolsnrefs?.protocolsnref?.map {
            val protocol = odx.protocols.values.firstOrNull { p -> p.shortname == it.shortname }
                ?: throw IllegalStateException("Couldn't find protocol ${it.shortname}")
            protocol.offset(true)
        }?.toIntArray()?.let {
            DiagComm.createProtocolsVector(builder, it)
        }
        val audience = this.audience?.offset(true)
        val sdgs = this.sdgs?.offset(true)

        DiagComm.startDiagComm(builder)
        DiagComm.addShortName(builder, shortName)
        longName?.let { DiagComm.addShortName(builder, it) }
        diagClass?.let { DiagComm.addDiagClassType(builder, it) }
        functClasses?.let { DiagComm.addFunctClass(builder, it) }
        semantic?.let { DiagComm.addSemantic(builder, it) }
        preconditionStateRefs?.let { DiagComm.addPreConditionStateRefs(builder, it) }
        stateTransitionRefs?.let { DiagComm.addStateTransitionRefs(builder, it) }
        protocolRefs?.let { DiagComm.addProtocols(builder, it) }
        audience?.let { DiagComm.addAudience(builder, it) }
        sdgs?.let { DiagComm.addSdgs(builder, it) }
        DiagComm.addIsFinal(builder, this.isISFINAL)
        DiagComm.addIsMandatory(builder, this.isISMANDATORY)
        DiagComm.addIsExecutable(builder, this.isISEXECUTABLE)
        return DiagComm.endDiagComm(builder)
    }

    private fun SINGLEECUJOB.offset(mustBeCached: Boolean = false): Int {
        return singleEcuJobsMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val diagComm = (this as DIAGCOMM).offsetInternal()
            val progCodes = this.progcodes?.progcode?.map {
                it.offset()
            }?.toIntArray()?.let {
                SingleEcuJob.createProgCodesVector(builder, it)
            }
            val inputParams = this.inputparams?.inputparam?.map {
                it.offset()
            }?.toIntArray()?.let {
                SingleEcuJob.createInputParamsVector(builder, it)
            }
            val outputParams = this.outputparams?.outputparam?.map {
                it.offset()
            }?.toIntArray()?.let {
                SingleEcuJob.createOutputParamsVector(builder, it)
            }
            val negOutputParams = this.negoutputparams?.negoutputparam?.map {
                it.offset()
            }?.toIntArray()?.let {
                SingleEcuJob.createNegOutputParamsVector(builder, it)
            }

            SingleEcuJob.startSingleEcuJob(builder)
            SingleEcuJob.addDiagComm(builder, diagComm)
            progCodes?.let { SingleEcuJob.addProgCodes(builder, it) }
            inputParams?.let { SingleEcuJob.addInputParams(builder, it) }
            outputParams?.let { SingleEcuJob.addOutputParams(builder, it) }
            negOutputParams?.let { SingleEcuJob.addNegOutputParams(builder, it) }
            SingleEcuJob.endSingleEcuJob(builder)
        }
    }

    private fun DIAGLAYER.offsetInternal(comparamRefs: Int?): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val sdgs = this.sdgs?.offset(true)
        val functClasses = this.functclasss?.functclass?.map {
            it.offset(true)
        }?.toIntArray()?.let {
            DiagLayer.createFunctClassesVector(builder, it)
        }
        val additionalAudiences = this.additionalaudiences?.additionalaudience?.map {
            it.offset(true)
        }?.toIntArray()?.let {
            DiagLayer.createAdditionalAudiencesVector(builder, it)
        }
        val resolvedLinks: List<DIAGCOMM> = this.diagcomms?.diagcommproxy?.filterIsInstance<ODXLINK>()?.map {
            odx.diagServices[it.idref] ?: odx.singleEcuJobs[it.idref]
            ?: throw IllegalStateException("Couldn't find reference ${it.idref}")
        } ?: emptyList()

        val diagServices = resolvedLinks.filterIsInstance<DIAGSERVICE>().map {
            it.offset(true)
        }.toIntArray().let {
            DiagLayer.createDiagServicesVector(builder, it)
        }
        val singleEcuJobs = resolvedLinks.filterIsInstance<SINGLEECUJOB>().map {
            it.offset(true)
        }.toIntArray().let {
            DiagLayer.createSingleEcuJobsVector(builder, it)
        }

        DiagLayer.startDiagLayer(builder)
        DiagLayer.addShortName(builder, shortName)
        longName?.let { DiagLayer.addLongName(builder, it) }
        sdgs?.let { DiagLayer.addSdgs(builder, it) }
        DiagLayer.addDiagServices(builder, diagServices)
        DiagLayer.addSingleEcuJobs(builder, singleEcuJobs)
        functClasses?.let { DiagLayer.addFunctClasses(builder, it) }
        additionalAudiences?.let { DiagLayer.addAdditionalAudiences(builder, it) }
        comparamRefs?.let { DiagLayer.addComParamRefs(builder, it) }
        return DiagLayer.endDiagLayer(builder)
    }

    private fun DIAGLAYER.offset(): Int {
        return this.offsetInternal(null)
    }

    private fun HIERARCHYELEMENT.offset(): Int {
        // comparam refs are for hierarchielements
        val comParamRefs = this.comparamrefs?.comparamref?.map {
            it.offset()
        }?.toIntArray()?.let {
            DiagLayer.createComParamRefsVector(builder, it)
        }


        val diagLayer = (this as DIAGLAYER).offsetInternal(comParamRefs)
        return diagLayer
    }

    private fun BASEVARIANT.offset(mustBeCached: Boolean): Int {
        return baseVariantMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val diagLayer = this.offset(true)
            val pattern = this.basevariantpattern?.matchingbasevariantparameters?.matchingbasevariantparameter?.map {
                it.offset()
            }?.toIntArray()?.let {
                Variant.createVariantPatternVector(builder, it)
            }
            Variant.startVariant(builder)
            Variant.addDiagLayer(builder, diagLayer)
            Variant.addIsBaseVariant(builder, true)
            pattern?.let { Variant.addVariantPattern(builder, it) }
            // TODO parentrefs
            Variant.endVariant(builder)
        }

//            this.parentrefs?.parentref?.let { parentRefs ->
//                variant.addAllParentRefs(parentRefs.map { it.offset() })
//            }
    }

    private fun ECUVARIANT.offset(mustBeCached: Boolean = false): Int {
        return ecuVariantMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val diagLayer = (this as HIERARCHYELEMENT).offset()
            val pattern = this.ecuvariantpatterns?.ecuvariantpattern?.map {
                it.offset()
            }?.toIntArray()?.let {
                Variant.createVariantPatternVector(builder, it)
            }
            // TODO parentrefs
            Variant.startVariant(builder)
            Variant.addDiagLayer(builder, diagLayer)
            Variant.addIsBaseVariant(builder, false)
            pattern?.let { Variant.addVariantPattern(builder, it) }
            Variant.endVariant(builder)
        }
        //this.parentrefs?.parentref?.let { parentRefs ->
        //    variant.addAllParentRefs(parentRefs.map { it.offset() })
        //}

    }


    private fun ECUSHAREDDATA.offset(mustBeCached: Boolean = false): Int {
        return ecuSharedDataMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            if (this.diagvariables?.diagvariableproxy?.isNotEmpty() == true) {
                logger.warning("DiagVariables from ${this.id} are not supported yet")
                if (!options.lenient) {
                    throw NotImplementedError("DiagVariables from ${this.id} are not supported yet")
                }
            }
            if (this.variablegroups?.variablegroup?.isNotEmpty() == true) {
                logger.warning("VariableGroups from ${this.id} are not supported yet")
                if (!options.lenient) {
                    throw NotImplementedError("VariableGroups from ${this.id} are not supported yet")
                }
            }

            val diagLayer = (this as DIAGLAYER).offset()

            EcuSharedData.startEcuSharedData(builder)
            EcuSharedData.addDiagLayer(builder, diagLayer)
            EcuSharedData.endEcuSharedData(builder)
        }
    }

    fun PARENTREF.offset(): Int {
        val resolved = odx.basevariants[this.idref] ?: odx.ecuvariants[this.idref] ?: odx.protocols[this.idref]
            ?: odx.functionalGroups[this.idref] ?: odx.tables[this.idref] ?: odx.ecuSharedDatas[this.idref]
        val resolvedOffs = when (resolved) {
            is BASEVARIANT -> resolved.offset(true)
            is ECUVARIANT -> resolved.offset(true)
            is PROTOCOL -> resolved.offset(true)
            is TABLE -> resolved.offset(true)
            is FUNCTIONALGROUP -> resolved.offset(true)
            is ECUSHAREDDATA -> resolved.offset(true)
            else -> throw UnsupportedOperationException("Unsupported idref type: ${this.idref} / ${this.doctype?.value()} ($resolved)")
        }
        val resolvedOffsType = when (resolved) {
            is BASEVARIANT -> ParentRefType.Variant
            is ECUVARIANT -> ParentRefType.Variant
            is PROTOCOL -> ParentRefType.Protocol
            is TABLE -> ParentRefType.TableDop
            is FUNCTIONALGROUP -> ParentRefType.FunctionalGroup
            is ECUSHAREDDATA -> ParentRefType.EcuSharedData
            else -> throw UnsupportedOperationException("Unsupported idref type: ${this.idref} / ${this.doctype?.value()} ($resolved)")
        }
        val notInheritedDiagCommShortNames = this.notinheriteddiagcomms?.notinheriteddiagcomm?.map {
            it.diagcommsnref.shortname.offset()
        }?.toIntArray()?.let {
            ParentRef.createNotInheritedDiagCommShortNamesVector(builder, it)
        }
        val notInheritedDopsShortNames = this.notinheriteddops?.notinheriteddop?.map {
            it.dopbasesnref.shortname.offset()
        }?.toIntArray()?.let {
            ParentRef.createNotInheritedDopsShortNamesVector(builder, it)
        }
        val notInheritedTablesShortNames = this.notinheritedtables?.notinheritedtable?.map {
            it.tablesnref.shortname.offset()
        }?.toIntArray()?.let {
            ParentRef.createNotInheritedTablesShortNamesVector(builder, it)
        }
        val notInheritedVariablesShortNames = this.notinheritedvariables?.notinheritedvariable?.map {
            it.diagvariablesnref.shortname.offset()
        }?.toIntArray()?.let {
            ParentRef.createNotInheritedVariablesShortNamesVector(builder, it)
        }
        val notInheritedGlobalNegResponseShortNames = this.notinheritedglobalnegresponses?.notinheritedglobalnegresponse?.map {
            it.globalnegresponsesnref.shortname.offset()
        }?.toIntArray()?.let {
            ParentRef.createNotInheritedGlobalNegResponsesShortNamesVector(builder, it)
        }

        ParentRef.startParentRef(builder)
        ParentRef.addRef(builder, resolvedOffs)
        ParentRef.addRefType(builder, resolvedOffsType)
        notInheritedDiagCommShortNames?.let { ParentRef.addNotInheritedDiagCommShortNames(builder, it) }
        notInheritedDopsShortNames?.let { ParentRef.addNotInheritedDopsShortNames(builder, it) }
        notInheritedTablesShortNames?.let { ParentRef.addNotInheritedTablesShortNames(builder, it) }
        notInheritedVariablesShortNames?.let { ParentRef.addNotInheritedVariablesShortNames(builder, it) }
        notInheritedGlobalNegResponseShortNames?.let { ParentRef.addNotInheritedGlobalNegResponsesShortNames(builder, it) }
        return ParentRef.endParentRef(builder)
    }


    private fun FUNCTIONALGROUP.offset(mustBeCached: Boolean = false): Int {
        return functionalGroupMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val diagLayer = (this as HIERARCHYELEMENT).offset()

//        this.parentrefs?.parentref?.let { parentRefs ->
//            functionalGroup.addAllParentRefs(parentRefs.map { it.offset() })
//        }
            FunctionalGroup.startFunctionalGroup(builder)
            FunctionalGroup.addDiagLayer(builder, diagLayer)
            FunctionalGroup.endFunctionalGroup(builder)
        }
    }

    private fun MATCHINGBASEVARIANTPARAMETER.offset(): Int {
        if (this.outparamifsnref != null) {
            throw IllegalStateException("Unsupported outparam if sn ref")
        }

        if (this.outparamifsnpathref != null) {
            throw IllegalStateException("Unsupported outparam if sn path ref")
        }

        val expectedValue = this.expectedvalue.offset()
        lateinit var diagService: DIAGSERVICE
        val diagServiceOffset = this.diagcommsnref.shortname.let { shortname ->
            diagService = odx.diagServices.values.firstOrNull { it.shortname == shortname }
                ?: throw IllegalStateException("Couldn't find diag service ${shortname}")
            diagService.offset(true)
        }

        MatchingParameter.startMatchingParameter(builder)
        MatchingParameter.addExpectedValue(builder, expectedValue)
        MatchingParameter.addDiagService(builder, diagServiceOffset)
        MatchingParameter.addUsePhysicalAddressing(builder, this.isUSEPHYSICALADDRESSING)
        return MatchingParameter.endMatchingParameter(builder)
    }

    private fun MATCHINGPARAMETER.offset(): Int {
        val expectedValue = this.expectedvalue?.offset()
        lateinit var diagService: DIAGSERVICE
        val diagServiceOffset = this.diagcommsnref.shortname.let { shortname ->
            diagService = odx.diagServices.values.firstOrNull { it.shortname == shortname }
                ?: throw IllegalStateException("Couldn't find diag service ${shortname}")
            diagService.offset(true)
        }
        val outParam = this.outparamifsnref?.shortname?.let { expectedShortName ->
            diagService.posresponserefs?.posresponseref?.flatMap { pr ->
                val posResponse =
                    odx.posResponses[pr.idref] ?: throw IllegalStateException("Couldn't find pos response ${pr.idref}")
                posResponse.params?.param ?: emptyList()
            }?.firstOrNull { params ->
                params.shortname == expectedShortName
            }?.offset(true) ?: throw IllegalStateException("Couldn't find param for shortName $expectedShortName")
        }

        this.outparamifsnpathref?.let {
            throw IllegalStateException("Unsupported outparam if sn path ref")
        }


        MatchingParameter.startMatchingParameter(builder)
        diagServiceOffset?.let { MatchingParameter.addDiagService(builder, it) }
        expectedValue?.let { MatchingParameter.addExpectedValue(builder, it) }
        outParam?.let { MatchingParameter.addOutParam(builder, it) }
        return MatchingParameter.endMatchingParameter(builder)
    }

    private fun ECUVARIANTPATTERN.offset(): Int {
        val matchingParameter = this.matchingparameters?.matchingparameter?.map {
            it.offset()
        }?.toIntArray()?.let {
            Variant.createVariantPatternVector(builder, it)
        }
        VariantPattern.startVariantPattern(builder)
        matchingParameter?.let { VariantPattern.addMatchingParameter(builder, matchingParameter) }
        return VariantPattern.endVariantPattern(builder)
    }

    private fun COMPARAMSUBSET.offset(mustBeCached: Boolean = false): Int {
        return comParamSubSetMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val comParams = this.comparams?.comparam?.map {
                it.offset(true)
            }?.toIntArray()?.let {
                ComParamSubSet.createComParamsVector(builder, it)
            }
            val complexComParams = this.complexcomparams?.complexcomparam?.map {
                it.offset(true)
            }?.toIntArray()?.let {
                ComParamSubSet.createComplexComParamsVector(builder, it)
            }
            val dops = this.dataobjectprops?.dataobjectprop?.map {
                val dop = odx.dataObjectProps[it.id] ?: throw IllegalStateException("Can't find DOP ${it.id}")
                dop.offset(true)
            }?.toIntArray()?.let {
                ComParamSubSet.createDataObjectPropsVector(builder, it)
            }
            val unitSpec = this.unitspec?.offset()

            ComParamSubSet.startComParamSubSet(builder)
            comParams?.let { ComParamSubSet.addComParams(builder, it) }
            complexComParams?.let { ComParamSubSet.addComplexComParams(builder, it) }
            dops?.let { ComParamSubSet.addDataObjectProps(builder, it) }
            unitSpec?.let { ComParamSubSet.addUnitSpec(builder, it) }
            ComParamSubSet.endComParamSubSet(builder)
        }
    }

    private fun UNITGROUP.offset(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val units = this.unitrefs?.unitref?.map {
            val unit = odx.units[it.idref] ?: throw IllegalStateException("Couldn't find unit $it")
            unit.offset(true)
        }?.toIntArray()?.let {
            UnitGroup.createUnitrefsVector(builder, it)
        }

        UnitGroup.startUnitGroup(builder)
        UnitGroup.addShortName(builder, shortName)
        longName?.let { UnitGroup.addLongName(builder, it) }
        units?.let { UnitGroup.addUnitrefs(builder, it) }
        return UnitGroup.endUnitGroup(builder)
    }

    private fun UNITSPEC.offset(): Int {
        val unitGroups = this.unitgroups?.unitgroup?.map { it.offset() }?.toIntArray()?.let {
            UnitSpec.createUnitGroupsVector(builder, it)
        }
        val physicalDimensions = this.physicaldimensions?.physicaldimension?.map { it.offset() }?.toIntArray()?.let {
            UnitSpec.createPhysicalDimensionsVector(builder, it)
        }
        val units = this.units?.unit?.map {
            val unit = odx.units[it.id] ?: throw IllegalStateException("Unit ${it.id} not found")
            unit.offset(true)
        }?.toIntArray()?.let {
            UnitSpec.createUnitsVector(builder, it)
        }
        val sdgs = this.sdgs?.let { it.offset(true) }

        UnitSpec.startUnitSpec(builder)
        unitGroups?.let { UnitSpec.addUnitGroups(builder, it) }
        physicalDimensions?.let { UnitSpec.addPhysicalDimensions(builder, it) }
        units?.let { UnitSpec.addUnits(builder, it) }
        sdgs?.let { UnitSpec.addSdgs(builder, it) }
        return UnitSpec.endUnitSpec(builder)
    }

    private fun COMPARAM.offset(mustBeCached: Boolean = false): Int {
        return comParamMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val paramClass = this.paramclass?.offset()
            val comParamType = this.cptype?.toProtoBufEnum()
            val comParamUsage = this.cpusage?.toProtoBufEnum()
            val displayLevel = this.displaylevel?.toUInt()

            val regularComParam = this.let {
                val physicalDefaultValue = this.physicaldefaultvalue?.offset()
                val dop = this.dataobjectpropref?.let {
                    val dop = odx.combinedDataObjectProps[it.idref]
                        ?: throw IllegalStateException("Couldn't find ${it.idref}")
                    dop.offset(true)
                }

                RegularComParam.startRegularComParam(builder)
                physicalDefaultValue?.let { RegularComParam.addPhysicalDefaultValue(builder, it) }
                dop?.let { RegularComParam.addDop(builder, it) }
                RegularComParam.endRegularComParam(builder)
            }

            ComParam.startComParam(builder)
            ComParam.addComParamType(builder, ComParamType.REGULAR)
            ComParam.addShortName(builder, shortName)
            longName?.let { ComParam.addLongName(builder, it) }
            paramClass?.let { ComParam.addParamClass(builder, it) }
            comParamType?.let { ComParam.addCpType(builder, it) }
            comParamUsage?.let { ComParam.addCpUsage(builder, it) }
            displayLevel?.let { ComParam.addDisplayLevel(builder, it) }
            ComParam.addSpecificData(builder, regularComParam)
            ComParam.addSpecificDataType(builder, ComParamSpecificData.RegularComParam)
            ComParam.endComParam(builder)
        }
    }

    private fun SIMPLEVALUE.offset(): Int {
        val value = this.value?.offset()
        SimpleValue.startSimpleValue(builder)
        value?.let { SimpleValue.addValue(builder, it) }
        return SimpleValue.endSimpleValue(builder)
    }

    private fun COMPLEXVALUE.offset(): Int {
        val entries = this.simplevalueOrCOMPLEXVALUE?.map {
            when (it) {
                is SIMPLEVALUE -> it.offset()
                is COMPLEXVALUE -> it.offset()
                else -> throw IllegalStateException("Unknown object type ${this.javaClass.simpleName}")
            }
        }?.toIntArray()?.let {
            ComplexValue.createEntriesVector(builder, it)
        }
        val entriesTypes = this.simplevalueOrCOMPLEXVALUE?.map {
            when (it) {
                is SIMPLEVALUE -> SimpleOrComplexValueEntry.SimpleValue
                is COMPLEXVALUE -> SimpleOrComplexValueEntry.ComplexValue
                else -> throw IllegalStateException("Unknown object type ${this.javaClass.simpleName}")
            }
        }?.toUByteArray()?.let {
            ComplexValue.createEntriesTypeVector(builder, it)
        }
        ComplexValue.startComplexValue(builder)
        entries?.let {
            ComplexValue.addEntries(builder, it)
            ComplexValue.addEntriesType(builder, entriesTypes ?: error("Inconsistent data"))
        }
        return ComplexValue.endComplexValue(builder)
    }

    private fun COMPLEXCOMPARAM.offset(mustBeCached: Boolean = false): Int {
        return complexComParamMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val paramClass = this.paramclass?.offset()
            val comParamType = this.cptype?.toProtoBufEnum()
            val comParamUsage = this.cpusage?.toProtoBufEnum()
            val displayLevel = this.displaylevel?.toUInt()
            val complexComParam = let {
                val comParams = this.comparamOrCOMPLEXCOMPARAM?.map {
                    when (it) {
                        is COMPARAM -> it.offset(true)
                        is COMPLEXCOMPARAM -> it.offset(true) // TODO this will fail if one is used "too early"
                        else -> throw IllegalStateException("Unknown com param type ${it.id}")
                    }
                }?.toIntArray()?.let {
                    ComplexComParam.createComParamsVector(builder, it)
                }

                val complexPhysicalDefaultValues = this.complexphysicaldefaultvalue?.complexvalues?.complexvalue?.map {
                    it.offset()
                }?.toIntArray()?.let {
                    ComplexComParam.createComplexPhysicalDefaultValuesVector(builder, it)
                }

                ComplexComParam.startComplexComParam(builder)
                comParams?.let { ComplexComParam.addComParams(builder, it) }
                ComplexComParam.addAllowMultipleValues(builder, this.isALLOWMULTIPLEVALUES)
                complexPhysicalDefaultValues?.let { ComplexComParam.addComplexPhysicalDefaultValues(builder, it) }
                ComplexComParam.endComplexComParam(builder)
            }

            ComParam.startComParam(builder)
            ComParam.addComParamType(builder, ComParamType.COMPLEX)
            ComParam.addShortName(builder, shortName)
            longName?.let { ComParam.addLongName(builder, it) }
            paramClass?.let { ComParam.addParamClass(builder, it) }
            comParamType?.let { ComParam.addCpType(builder, it) }
            comParamUsage?.let { ComParam.addCpUsage(builder, it) }
            displayLevel?.let { ComParam.addDisplayLevel(builder, it) }
            ComParam.addSpecificData(builder, complexComParam)
            ComParam.addSpecificDataType(builder, ComParamSpecificData.ComplexComParam)
            ComParam.endComParam(builder)
        }
    }

    private fun STATE.offset(mustBeCached: Boolean = false): Int {
        return stateMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()

            dataformat.State.startState(builder)
            dataformat.State.addShortName(builder, shortName)
            longName?.let { dataformat.State.addLongName(builder, it) }

            dataformat.State.endState(builder)
        }
    }

    private fun PHYSICALDIMENSION.offset(): Int {
        val shortName = this.shortname.offset()
        val longname = this.longname?.offset()

        PhysicalDimension.startPhysicalDimension(builder)

        PhysicalDimension.addShortName(builder, shortName)
        longname?.let { PhysicalDimension.addLongName(builder, longname) }
        this.currentexp?.let { PhysicalDimension.addCurrentExp(builder, it) }
        this.lengthexp?.let { PhysicalDimension.addLengthExp(builder, it) }
        this.massexp?.let { PhysicalDimension.addMassExp(builder, it) }
        this.molaramountexp?.let { PhysicalDimension.addMolarAmountExp(builder, it) }
        this.luminousintensityexp?.let { PhysicalDimension.addLuminousIntensityExp(builder, it) }
        this.temperatureexp?.let { PhysicalDimension.addTemperatureExp(builder, it) }
        this.timeexp?.let { PhysicalDimension.addTimeExp(builder, it) }

        return PhysicalDimension.endPhysicalDimension(builder)
    }

    private fun PROTOCOL.offset(mustBeCached: Boolean = false): Int {
        return protocolMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val diagLayer = (this as DIAGLAYER).offset()
            val comparamSpecs = this.comparamspecref?.let {
                val comParamSpec =
                    odx.comparamSpecs[it.idref]
                        ?: throw IllegalStateException("Couldn't find com param spec ${it.idref}")
                comParamSpec.offset()
            }
            val protStack = this.protstacksnref?.let {
                val stack = odx.comparamSpecs.values.flatMap { it.protstacks?.protstack ?: emptyList() }
                    .firstOrNull { it.shortname == it.shortname }
                    ?: throw IllegalStateException("Couldn't find protstack with short name ${it.shortname}")
                stack.offset(true)
            }
            if (this.parentrefs != null) {
                TODO("Prot stack parent refs not supported")
            }

            Protocol.startProtocol(builder)
            Protocol.addDiagLayer(builder, diagLayer)
            comparamSpecs?.let { Protocol.addComParamSpec(builder, it) }
            protStack?.let { Protocol.addProtStack(builder, it) }
            Protocol.endProtocol(builder)
        }
    }

    private fun COMPARAMSPEC.offset(): Int {
        val protStacks = this.protstacks?.protstack?.map {
            it.offset(true)
        }?.toIntArray()?.let {
            ComParamSpec.createProtStacksVector(builder, it)
        }
        ComParamSpec.startComParamSpec(builder)
        protStacks?.let { ComParamSpec.addProtStacks(builder, it) }
        return ComParamSpec.endComParamSpec(builder)
    }

    private fun PROTSTACK.offset(mustBeCached: Boolean = false): Int {
        return protStackMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val comparamSubSets = this.comparamsubsetrefs?.comparamsubsetref?.map {
                val comparamSubSet = odx.comParamSubSets[it.idref]
                    ?: throw IllegalStateException("Couldn't find com param subset ${it.idref}")
                comparamSubSet.offset(true)
            }?.toIntArray()?.let {
                ProtStack.createComparamSubsetRefsVector(builder, it)
            }
            val physicalLinkType = this.physicallinktype?.offset()
            val pduProtocolType = this.pduprotocoltype?.offset()

            ProtStack.startProtStack(builder)
            ProtStack.addShortName(builder, shortName)
            longName?.let { ProtStack.addLongName(builder, it) }
            comparamSubSets?.let { ProtStack.addComparamSubsetRefs(builder, it) }
            physicalLinkType?.let { ProtStack.addPhysicalLinkType(builder, it) }
            pduProtocolType?.let { ProtStack.addPduProtocolType(builder, it) }
            ProtStack.endProtStack(builder)
        }
    }

    private fun COMPARAMREF.offset(): Int {
        val comParam = odx.comparams[this.idref]?.offset(true)
            ?: odx.complexComparams[this.idref]?.offset(true)

        if (comParam == null) {
            if (!options.lenient) {
                throw IllegalStateException("Couldn't find COMPARAM ${this.idref} @ ${this.docref}")
            }
            logger.warning("Couldn't find COMPARAM ${this.idref} @ ${this.docref}")
        }

        val simpleValue = this.simplevalue?.offset()
        val complexValue = this.complexvalue?.offset()

        val protocol = this.protocolsnref?.shortname?.let { shortName ->
            val protocolOdx = odx.protocols.values.firstOrNull { it.shortname == shortName }
                ?: throw IllegalStateException("Couldn't find PROTOCOL $shortName")
            protocolOdx.offset(true)
        }

        val protStack = this.protstacksnref?.let {
            val protStackOdx = odx.protStacks.values.firstOrNull { it.shortname == this.protstacksnref.shortname }
                ?: throw IllegalStateException("Can't find protocol ${this.protstacksnref.shortname}")

            protStackOdx.offset(true)
        }

        ComParamRef.startComParamRef(builder)
        comParam?.let { ComParamRef.addComParam(builder, it) }
        simpleValue?.let { ComParamRef.addSimpleValue(builder, it) }
        complexValue?.let { ComParamRef.addComplexValue(builder, it) }
        protocol?.let { ComParamRef.addProtocol(builder, it) }
        protStack?.let { ComParamRef.addProtStack(builder, it) }
        return ComParamRef.endComParamRef(builder)

    }

    private fun STATECHART.offset(mustBeCached: Boolean = false): Int {
        return stateChartMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val semantic = this.semantic.offset()
            val stateTransitions = this.statetransitions?.statetransition?.let { transitions ->
                val data = transitions.map { it.offset(true) }.toIntArray()
                StateChart.createStateTransitionsVector(builder, data)
            }
            val startStateShortName = this.startstatesnref.shortname.offset()

            val states = this.states?.state?.let { states ->
                val data = states.map { it.offset(true) }.toIntArray()
                StateChart.createStatesVector(builder, data)
            }

            StateChart.startStateChart(builder)
            StateChart.addShortName(builder, shortName)
            StateChart.addSemantic(builder, semantic)
            stateTransitions?.let { StateChart.addStateTransitions(builder, it) }
            StateChart.addStartStateShortNameRef(builder, startStateShortName)
            states?.let { StateChart.addStates(builder, it) }

            StateChart.endStateChart(builder)
        }
    }

    private fun STATETRANSITION.offset(mustBeCached: Boolean = false): Int {
        return stateTransitionMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val sourceShortNameRef = this.sourcesnref.shortname.offset()
            val targetShortNameRef = this.targetsnref.shortname.offset()

            StateTransition.startStateTransition(builder)

            StateTransition.addShortName(builder, shortName)
            StateTransition.addSourceShortNameRef(builder, sourceShortNameRef)
            StateTransition.addTargetShortNameRef(builder, targetShortNameRef)

            StateTransition.endStateTransition(builder)
        }
    }

    private fun SWITCHKEY.offset(): Int {
        val dop = odx.combinedDataObjectProps[this.dataobjectpropref.idref]?.offset(true)
            ?: throw IllegalStateException("Couldn't find dop-ref ${this.dataobjectpropref.idref}")

        SwitchKey.startSwitchKey(builder)
        SwitchKey.addBytePosition(builder, this.byteposition.toUInt())
        this.bitposition?.let { SwitchKey.addBitPosition(builder, it.toUInt()) }
        dop.let { SwitchKey.addDop(builder, it) }
        return SwitchKey.endSwitchKey(builder)
    }

    private fun DEFAULTCASE.offset(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val structure = this.structureref?.let {
            val dop = odx.combinedDataObjectProps[it.idref]
                ?: throw IllegalStateException("Couldn't find dop-structure-ref ${this.structureref.idref}")
            dop.offset(true)
        }


        DefaultCase.startDefaultCase(builder)

        DefaultCase.addShortName(builder, shortName)
        longName?.let { DefaultCase.addLongName(builder, it) }
        structure?.let { DefaultCase.addStructure(builder, it) }
        return DefaultCase.endDefaultCase(builder)
    }

    private fun CASE.offset(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val lowerLimit = this.lowerlimit.offset()
        val upperLimit = this.upperlimit.offset()

        this.structuresnref?.shortname?.let {
            TODO("STRUCTURE shortnameref not supported for $this")
        }
        val structure = odx.combinedDataObjectProps[this.structureref.idref]?.offset(true)
            ?: throw IllegalStateException("Couldn't find dop-structure-ref ${this.structureref.idref}")

        Case.startCase(builder)
        Case.addShortName(builder, shortName)
        longName?.let { Case.addLongName(builder, it) }
        Case.addStructure(builder, structure)
        Case.addLowerLimit(builder, lowerLimit)
        Case.addUpperLimit(builder, upperLimit)
        return Case.endCase(builder)
    }

    private fun TABLEROW.offset(mustBeCached: Boolean = false): Int {
        return tableRowMap.getCachedOffset(this, mustBeCached = mustBeCached) {
            val shortName = this.shortname.offset()
            val semantic = this.semantic?.offset()
            val longName = this.longname?.offset()
            val key = this.key?.offset()

            this.dataobjectpropsnref?.let {
                error("Unsupported data object prop shortname ref ${this.structuresnref}")
            }
            val dop = this.dataobjectpropref?.idref?.let {
                val dop = odx.combinedDataObjectProps[it] ?: throw IllegalStateException("Couldn't find dop $it")
                dop.offset(true)
            }
            this.structuresnref?.let {
                error("Unsupported structure shortname ref ${this.structuresnref}")
            }
            val structure = this.structureref?.idref?.let {
                val structureDop = odx.structures[it] ?: throw IllegalStateException("Couldn't find structure $it")
                structureDop.offset(true)
            }
            val sdgs = this.sdgs?.offset(true)
            val audience = this.audience?.offset(true)
            val functClasses = this.functclassrefs?.functclassref?.map {
                val functClass =
                    odx.functClasses[it.idref] ?: throw IllegalStateException("Couldn't find funct class ${it.idref}")
                functClass.offset(true)
            }?.toIntArray()?.let {
                TableRow.createFunctClassRefsVector(builder, it)
            }

            val stateTransitionsRefs = this.statetransitionrefs?.statetransitionref?.map {
                it.offset(true)
            }?.toIntArray()?.let {
                TableRow.createStateTransitionRefsVector(builder, it)
            }

            val preconditionStateRefs = this.preconditionstaterefs?.preconditionstateref?.map {
                it.offset(true)
            }?.toIntArray()?.let {
                TableRow.createPreConditionStateRefsVector(builder, it)
            }

            TableRow.startTableRow(builder)
            TableRow.addShortName(builder, shortName)
            semantic?.let { TableRow.addSemantic(builder, it) }
            longName?.let { TableRow.addLongName(builder, it) }
            key?.let { TableRow.addKey(builder, it) }
            dop?.let { TableRow.addDop(builder, it) }
            structure?.let { TableRow.addStructure(builder, it) }
            sdgs?.let { TableRow.addSdgs(builder, it) }
            audience?.let { TableRow.addAudience(builder, it) }
            functClasses?.let { TableRow.addFunctClassRefs(builder, it) }
            stateTransitionsRefs?.let { TableRow.addStateTransitionRefs(builder, it) }
            preconditionStateRefs?.let { TableRow.addPreConditionStateRefs(builder, it) }
            TableRow.addIsExecutable(builder, this.isISEXECUTABLE)
            TableRow.addIsMandatory(builder, this.isISMANDATORY)
            TableRow.addIsFinal(builder, this.isISFINAL)
            TableRow.endTableRow(builder)
        }
    }

    private inline fun <K : Any, V : Any> MutableMap<K, V>.getOrCreate(k: K, create: (K) -> V): V {
        var value = this[k]
        if (value == null) {
            value = create(k)
            this[k] = value
        }
        return value
    }

    private fun <K : Any, V : Any> MutableMap<K, V>.fillMissing(keys: Collection<K>, create: (K) -> V) {
        keys.forEach {
            if (!this.containsKey(it)) {
                this[it] = create(it)
            }
        }
    }

    private fun String.offset(): Int {
        val offset = dedupStrMap[this]
        if (offset != null) {
            return offset
        }
        strOccurrences.getOrCreate(this, { AtomicInteger() }).addAndGet(1)
        return builder.createString(this)
    }

    private fun ByteArray.offset(): Int =
        builder.createByteVector(this)

    private fun <K> Map<K, Int>.getCachedOffset(
        value: K,
        forceCreation: Boolean = false,
        mustBeCached: Boolean = false,
        create: (K) -> Int
    ): Int {
        if (forceCreation) {
            return create(value)
        }
        val offset = this[value]
        if (offset != null) {
            return offset
        } else if (mustBeCached) {
            throw IllegalStateException("Entry should've been already created, but wasn't: $value")
        } else if (createdObjects.contains(value)) {
            throw IllegalStateException("${value?.javaClass?.simpleName} has to be created, but should've been already created: $value")
        }
        createdObjects.add(value)

        val created = create(value)
        return created
    }
}
