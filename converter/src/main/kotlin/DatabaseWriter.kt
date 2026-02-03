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
import dataformat.DiagServiceOrJob
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
import dataformat.TableDiagCommConnector
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
import schema.odx.TABLEDIAGCOMMCONNECTOR
import schema.odx.TABLEENTRY
import schema.odx.TABLEKEY
import schema.odx.TABLEROW
import schema.odx.TABLESTRUCT
import schema.odx.TEXT
import schema.odx.UNIT
import schema.odx.UNITGROUP
import schema.odx.UNITSPEC
import schema.odx.VALUE
import java.util.logging.Logger
import kotlin.collections.toIntArray
import kotlin.collections.toUIntArray

class DatabaseWriter(
    private val logger: Logger,
    private val odx: ODXCollection,
    private val options: ConverterOptions,
) {
    private val builder = FlatBufferBuilder()

    private val cachedObjects: MutableMap<Any, Int> = mutableMapOf()

    private val dtcs: Map<schema.odx.DTC, Int>
    private val baseVariantMap: Map<BASEVARIANT, Int>
    private val ecuVariantMap: Map<ECUVARIANT, Int>
    private val functionalGroupMap: Map<FUNCTIONALGROUP, Int>

    init {
        dtcs = odx.dtcs.values.associateWith { it.offset() }
        baseVariantMap = odx.basevariants.values.associateWith { it.offset() }
        ecuVariantMap = odx.ecuvariants.values.associateWith { it.offset() }
        functionalGroupMap = odx.functionalGroups.values.associateWith { it.offset() }
    }

    fun createEcuData(): ByteArray {
        val version = "2025-05-10".offset()
        val ecuName = odx.ecuName.offset()
        val odxRevision = odx.odxRevision?.offset()

        val dtcs = EcuData.createDtcsVector(builder, dtcs.values.toIntArray())
        val variants =
            EcuData.createVariantsVector(
                builder,
                baseVariantMap.values.toIntArray() + ecuVariantMap.values.toIntArray(),
            )
        val functionalGroups = EcuData.createFunctionalGroupsVector(builder, functionalGroupMap.values.toIntArray())

        EcuData.startEcuData(builder)
        EcuData.addVersion(builder, version)
        EcuData.addEcuName(builder, ecuName)
        odxRevision?.let { EcuData.addRevision(builder, it) }

        EcuData.addDtcs(builder, dtcs)
        EcuData.addVariants(builder, variants)
        EcuData.addFunctionalGroups(builder, functionalGroups)

        val ecuData = EcuData.endEcuData(builder)

        builder.finish(ecuData)
        return builder.sizedByteArray()
    }

    private fun DOPBASE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val sdgs = this.sdgs?.offset()

            val specificData =
                when (this) {
                    is DATAOBJECTPROP -> this.toNormalDop()
                    is ENDOFPDUFIELD -> this.toEndOfPduField()
                    is STATICFIELD -> this.toStaticField()
                    is ENVDATA -> this.toEnvData()
                    is ENVDATADESC -> this.toEnvDataDesc()
                    is schema.odx.DTCDOP -> this.toDTCDOP()
                    is STRUCTURE -> this.toStructure()
                    is schema.odx.MUX -> this.toMUXDOP()
                    is DYNAMICLENGTHFIELD -> this.toDynamicLengthField()
                    else -> error("Unknown type of data for DOP: $this")
                }

            val specificDataType =
                when (this) {
                    is DATAOBJECTPROP -> SpecificDOPData.NormalDOP
                    is ENDOFPDUFIELD -> SpecificDOPData.EndOfPduField
                    is STATICFIELD -> SpecificDOPData.StaticField
                    is ENVDATA -> SpecificDOPData.EnvData
                    is ENVDATADESC -> SpecificDOPData.EnvDataDesc
                    is schema.odx.DTCDOP -> SpecificDOPData.DTCDOP
                    is STRUCTURE -> SpecificDOPData.Structure
                    is schema.odx.MUX -> SpecificDOPData.MUXDOP
                    is DYNAMICLENGTHFIELD -> SpecificDOPData.DynamicLengthField
                    else -> error("Unknown type of data for DOP: $this")
                }

            DOP.startDOP(builder)
            DOP.addShortName(builder, shortName)
            DOP.addSpecificData(builder, specificData)
            DOP.addSpecificDataType(builder, specificDataType)

            sdgs?.let { DOP.addSdgs(builder, it) }

            DOP.endDOP(builder)
        }

    private fun DIAGSERVICE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val diagComm = (this as DIAGCOMM).offsetInternal()
            val request =
                odx.requests[this.requestref.idref]?.offset()
                    ?: error("Couldn't find requestref ${this.requestref.idref}")
            val posResponses =
                this.posresponserefs
                    ?.posresponseref
                    ?.map {
                        val pr =
                            odx.posResponses[it.idref]
                                ?: error("Couldn't find response ${it.idref}")
                        pr.offset()
                    }?.toIntArray()
                    ?.let {
                        DiagService.createPosResponsesVector(builder, it)
                    }
            val negResponses =
                this.negresponserefs
                    ?.negresponseref
                    ?.map {
                        val nr =
                            odx.negResponses[it.idref]
                                ?: error("Couldn't find response ${it.idref}")
                        nr.offset()
                    }?.toIntArray()
                    ?.let {
                        DiagService.createNegResponsesVector(builder, it)
                    }
            val comParamRefs =
                this.comparamrefs
                    ?.comparamref
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        DiagService.createComParamRefsVector(builder, it)
                    }

            DiagService.startDiagService(builder)
            DiagService.addDiagComm(builder, diagComm)
            DiagService.addRequest(builder, request)
            posResponses?.let { DiagService.addPosResponses(builder, it) }
            negResponses?.let { DiagService.addNegResponses(builder, it) }
            comParamRefs?.let { DiagService.addComParamRefs(builder, it) }
            this.addressing?.let { DiagService.addAddressing(builder, it.toFileFormatEnum()) }
            this.transmissionmode?.let { DiagService.addTransmissionMode(builder, it.toFileFormatEnum()) }
            DiagService.addIsCyclic(builder, this.isISCYCLIC)
            DiagService.addIsMultiple(builder, this.isISMULTIPLE)
            DiagService.endDiagService(builder)
        }

    private fun TABLE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val semantic = this.semantic?.offset()
            val longName = this.longname?.offset()
            val keyLabel = this.keylabel?.offset()
            val keyDop =
                this.keydopref?.idref?.let {
                    val dop =
                        odx.combinedDataObjectProps[it] ?: error("Couldn't find dop $it")
                    dop.offset()
                }
            val structLabel = this.structlabel?.offset()
            val sdgs = this.sdgs?.offset()

            val rows =
                this.rowwrapper
                    .map { row ->
                        if (row is TABLEROW) {
                            row.offset()
                        } else {
                            error("Unsupported row type ${row.javaClass.simpleName}")
                        }
                    }.toIntArray()
                    .let {
                        TableDop.createRowsVector(builder, it)
                    }

            val diagCommConnectors =
                this.tablediagcommconnectors
                    ?.tablediagcommconnector
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        TableDop.createDiagCommConnectorVector(builder, it)
                    }

            TableDop.startTableDop(builder)
            TableDop.addShortName(builder, shortName)
            semantic?.let { TableDop.addSemantic(builder, it) }
            longName?.let { TableDop.addLongName(builder, it) }
            keyLabel?.let { TableDop.addKeyLabel(builder, it) }
            keyDop?.let { TableDop.addKeyDop(builder, it) }
            structLabel?.let { TableDop.addStructLabel(builder, it) }
            diagCommConnectors?.let { TableDop.addDiagCommConnector(builder, it) }
            TableDop.addRows(builder, rows)

            sdgs?.let { TableDop.addSdgs(builder, it) }

            TableDop.endTableDop(builder)
        }

    private fun TABLEDIAGCOMMCONNECTOR.offset(): Int =
        cachedObjects.getOrPut(this) {
            val semantic = this.semantic?.offset()

            val diagComm =
                if (this.diagcommref != null) {
                    val diagService = this.diagcommref?.idref?.let { odx.diagServices[it] }
                    val ecuJob = this.diagcommref?.idref?.let { odx.singleEcuJobs[it] }
                    if (diagService == null && ecuJob == null) {
                        error("Couldn't resolve ${this.diagcommref.idref}")
                    } else if (diagService != null) {
                        diagService.offset() to DiagServiceOrJob.DiagService
                    } else if (ecuJob != null) {
                        ecuJob.offset() to DiagServiceOrJob.SingleEcuJob
                    } else {
                        error("Invalid state, no diagService or SingleEcuJOb")
                    }
                } else if (this.diagcommsnref != null) {
                    error("Unsupported short name ref ${this.diagcommsnref}")
                } else {
                    error("Empty Diag Comm Connector $this")
                }

            TableDiagCommConnector.startTableDiagCommConnector(builder)
            semantic?.let { TableDiagCommConnector.addSemantic(builder, it) }
            TableDiagCommConnector.addDiagComm(builder, diagComm.first)
            TableDiagCommConnector.addDiagCommType(builder, diagComm.second)
            TableDiagCommConnector.endTableDiagCommConnector(builder)
        }

    private fun schema.odx.SD.offset(): Int =
        cachedObjects.getOrPut(this) {
            val value = this.value?.offset()
            val si = this.si?.offset()
            val ti = this.ti?.offset()

            SD.startSD(builder)

            value?.let { SD.addValue(builder, it) }
            si?.let { SD.addSi(builder, it) }
            ti?.let { SD.addTi(builder, it) }

            SD.endSD(builder)
        }

    private fun schema.odx.SDG.offset(): Int =
        cachedObjects.getOrPut(this) {
            val si = this.si?.offset()

            val caption =
                this.sdgcaption?.shortname?.offset() ?: this.sdgcaptionref
                    ?.idref
                    ?.let {
                        val sdgCaption =
                            odx.sdgCaptions[it] ?: error("Couldn't find sdg-caption $it")
                        sdgCaption.shortname
                    }?.offset()

            val sdg =
                this.sdgOrSD
                    ?.map {
                        val sdOrSdg =
                            when (it) {
                                is schema.odx.SD -> it.offset()
                                is schema.odx.SDG -> it.offset()
                                else -> error("Unknown sdg type: $it")
                            }
                        val sdOrSdgType =
                            when (it) {
                                is schema.odx.SD -> SDxorSDG.SD
                                is schema.odx.SDG -> SDxorSDG.SDG
                                else -> error("This path should never be reached -- unknown object type in SDOrSDG list $it")
                            }

                        SDOrSDG.startSDOrSDG(builder)
                        SDOrSDG.addSdOrSdg(builder, sdOrSdg)
                        SDOrSDG.addSdOrSdgType(builder, sdOrSdgType)
                        SDOrSDG.endSDOrSDG(builder)
                    }?.toIntArray()
                    ?.let {
                        SDG.createSdsVector(builder, it)
                    }

            SDG.startSDG(builder)
            si?.let { SDG.addSi(builder, it) }
            caption?.let { SDG.addCaptionSn(builder, it) }
            sdg?.let { SDG.addSds(builder, it) }
            SDG.endSDG(builder)
        }

    private fun schema.odx.SDGS.offset(): Int =
        cachedObjects.getOrPut(this) {
            val sdgs = SDGS.createSdgsVector(builder, this.sdg.map { it.offset() }.toIntArray())

            SDGS.startSDGS(builder)
            SDGS.addSdgs(builder, sdgs)
            SDGS.endSDGS(builder)
        }

    private fun REQUEST.offset(): Int =
        cachedObjects.getOrPut(this) {
            val sdgs = this.sdgs?.offset()
            val params =
                this.params
                    ?.param
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Request.createParamsVector(builder, it)
                    }

            Request.startRequest(builder)
            sdgs?.let { Request.addSdgs(builder, it) }
            params?.let { Request.addParams(builder, it) }
            Request.endRequest(builder)
        }

    private fun RESPONSE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val sdgs = this.sdgs?.offset()
            val params =
                this.params
                    ?.param
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Request.createParamsVector(builder, it)
                    }

            Response.startResponse(builder)
            when (this) {
                is POSRESPONSE -> Response.addResponseType(builder, ResponseType.POS_RESPONSE)
                is NEGRESPONSE -> Response.addResponseType(builder, ResponseType.NEG_RESPONSE)
                is GLOBALNEGRESPONSE -> Response.addResponseType(builder, ResponseType.GLOBAL_NEG_RESPONSE)
                else -> error("Unknown response type ${this::class.java.simpleName}")
            }
            sdgs?.let {
                Response.addSdgs(builder, it)
            }
            params?.let {
                Response.addParams(builder, it)
            }
            Response.endResponse(builder)
        }

    private fun PARAM.offset(): Int =
        cachedObjects.getOrPut(this) {
            try {
                val shortName = this.shortname.offset()
                val semantic = this.semantic?.offset()
                val sdgs = this.sdgs?.offset()

                val specificData =
                    when (this) {
                        is VALUE -> {
                            this.dopsnref?.shortname?.let {
                                TODO("dop shortname ref in VALUE not supported ${this.dopsnref.shortname}")
                            }
                            val dop =
                                this.dopref?.let {
                                    val dop =
                                        odx.combinedDataObjectProps[it.idref]
                                            ?: error("Couldn't find ${it.idref}")
                                    dop.offset()
                                }
                            val physicalDefaultValue = this.physicaldefaultvalue?.offset()

                            Value.startValue(builder)
                            dop?.let { Value.addDop(builder, it) }
                            physicalDefaultValue?.let { Value.addPhysicalDefaultValue(builder, it) }
                            Value.endValue(builder)
                        }

                        is CODEDCONST -> {
                            val diagCodedType = this.diagcodedtype.offset()
                            val codedValue = this.codedvalue?.offset()

                            CodedConst.startCodedConst(builder)
                            CodedConst.addDiagCodedType(builder, diagCodedType)
                            codedValue?.let { CodedConst.addCodedValue(builder, it) }
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
                            val dop =
                                this.dopref?.let {
                                    val dop =
                                        odx.combinedDataObjectProps[it.idref]
                                            ?: error("Couldn't find ${it.idref}")
                                    dop.offset()
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
                            val diagCodedType = this.diagcodedtype?.offset()
                            val codedValues =
                                this.codedvalues?.codedvalue?.map { it.offset() }?.toIntArray()?.let {
                                    NrcConst.createCodedValuesVector(builder, it)
                                }

                            NrcConst.startNrcConst(builder)
                            diagCodedType?.let { NrcConst.addDiagCodedType(builder, it) }
                            codedValues?.let { NrcConst.addCodedValues(builder, it) }
                            NrcConst.endNrcConst(builder)
                        }

                        is PHYSCONST -> {
                            val physConstValue = this.physconstantvalue?.offset()
                            val dop =
                                this.dopref?.let {
                                    val dop =
                                        odx.combinedDataObjectProps[it.idref]
                                            ?: error("couldn't find dop ${it.idref}")
                                    dop.offset()
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
                            val dop =
                                this.dopref?.let {
                                    val dop =
                                        odx.combinedDataObjectProps[it.idref]
                                            ?: error("Couldn't find DOP ${it.idref}")
                                    dop.offset()
                                }

                            dataformat.System.startSystem(builder)

                            dataformat.System.addSysParam(builder, sysParam)
                            dop?.let { dataformat.System.addDop(builder, it) }

                            dataformat.System.endSystem(builder)
                        }

                        is TABLEKEY -> {
                            val entry =
                                this.rest.firstOrNull()?.value
                                    ?: error("TABLE-KEY ${this.id} has no entries")
                            if (this.rest.size > 1) {
                                error("TABLE-KEY ${this.id} has more than one entry")
                            }
                            var tableKeyReference: Int
                            var tableKeyReferenceType: UByte
                            if (entry is ODXLINK) {
                                val table = odx.tables[entry.idref]
                                if (table == null) {
                                    val row =
                                        odx.tableRows[entry.idref]
                                            ?: error("ODXLINK ${this.id} is neither TABLE nor TABLE-KEY")
                                    tableKeyReference = row.offset()
                                    tableKeyReferenceType = TableKeyReference.TableRow
                                } else {
                                    tableKeyReference = table.offset()
                                    tableKeyReferenceType = TableKeyReference.TableDop
                                }
                            } else {
                                error("Unknown type for TABLE-KEY/TABLEROW ${this.id} entry ${entry.javaClass.simpleName}")
                            }

                            TableKey.startTableKey(builder)
                            TableKey.addTableKeyReference(builder, tableKeyReference)
                            TableKey.addTableKeyReferenceType(builder, tableKeyReferenceType)
                            TableKey.endTableKey(builder)
                        }

                        is TABLEENTRY -> {
                            val param = (this as PARAM).offset()
                            val target = this.target?.toFileFormatEnum()
                            val tableRow =
                                this.tablerowref.idref?.let {
                                    val row =
                                        odx.tableRows[it] ?: error("Couldn't find TABLE-ROW $it")
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
                            val tableKey =
                                odx.tableKeys[this.tablekeyref.idref]?.offset()
                                    ?: error("Couldn't find TABLE-KEY ${this.tablekeyref.idref}")

                            TableStruct.startTableStruct(builder)
                            tableKey.let { TableStruct.addTableKey(builder, it) }
                            TableStruct.endTableStruct(builder)
                        }

                        else -> {
                            error("Unknown object type ${this.javaClass.simpleName}")
                        }
                    }
                val specificDataType =
                    when (this) {
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
                        else -> error("Unknown object type ${this.javaClass.simpleName}")
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

    private fun FUNCTCLASS.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortname = this.shortname.offset()

            FunctClass.startFunctClass(builder)
            FunctClass.addShortName(builder, shortname)
            FunctClass.endFunctClass(builder)
        }

    private fun STANDARDLENGTHTYPE.toStandardLengthType(): Int {
        val bitmask = this.bitmask?.offset()

        StandardLengthType.startStandardLengthType(builder)
        bitmask?.let {
            StandardLengthType.addBitMask(builder, it)
        }
        StandardLengthType.addCondensed(builder, this.isCONDENSED)
        StandardLengthType.addBitLength(builder, this.bitlength.toUInt())

        return StandardLengthType.endStandardLengthType(builder)
    }

    private fun MINMAXLENGTHTYPE.toMinMaxLengthType(): Int {
        MinMaxLengthType.startMinMaxLengthType(builder)
        MinMaxLengthType.addMinLength(builder, this.minlength.toUInt())
        this.maxlength?.let {
            MinMaxLengthType.addMaxLength(builder, this.maxlength.toUInt())
        }
        this.termination?.let {
            MinMaxLengthType.addTermination(builder, this.termination.toFileFormatEnum())
        }
        return MinMaxLengthType.endMinMaxLengthType(builder)
    }

    private fun LEADINGLENGTHINFOTYPE.toLeadingLengthInfoType(): Int {
        LeadingLengthInfoType.startLeadingLengthInfoType(builder)
        LeadingLengthInfoType.addBitLength(builder, this.bitlength.toUInt())
        return LeadingLengthInfoType.endLeadingLengthInfoType(builder)
    }

    private fun PARAMLENGTHINFOTYPE.toParamLengthInfoType(): Int {
        val lengthKey =
            odx.lengthKeys[this.lengthkeyref.idref]?.offset()
                ?: error("Unknown length key reference ${this.lengthkeyref.idref}")

        ParamLengthInfoType.startParamLengthInfoType(builder)
        ParamLengthInfoType.addLengthKey(builder, lengthKey)
        return ParamLengthInfoType.endParamLengthInfoType(builder)
    }

    private fun DATAOBJECTPROP.toNormalDop(): Int {
        val diagCodedType = this.diagcodedtype?.offset()
        val unit =
            this.unitref?.let {
                val unit = odx.units[it.idref] ?: error("Couldn't find unit ${it.idref}")
                unit.offset()
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

    private fun DIAGCODEDTYPE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val baseTypeEncoding = this.basetypeencoding?.offset()

            val specificData =
                when (this) {
                    is STANDARDLENGTHTYPE -> {
                        this.toStandardLengthType()
                    }

                    is MINMAXLENGTHTYPE -> {
                        this.toMinMaxLengthType()
                    }

                    is LEADINGLENGTHINFOTYPE -> {
                        this.toLeadingLengthInfoType()
                    }

                    is PARAMLENGTHINFOTYPE -> {
                        this.toParamLengthInfoType()
                    }

                    else -> {
                        error("Unsupported diag coded type ${this::class.java.simpleName}")
                    }
                }
            val specificType =
                when (this) {
                    is STANDARDLENGTHTYPE -> {
                        SpecificDataType.StandardLengthType
                    }

                    is MINMAXLENGTHTYPE -> {
                        SpecificDataType.MinMaxLengthType
                    }

                    is LEADINGLENGTHINFOTYPE -> {
                        SpecificDataType.LeadingLengthInfoType
                    }

                    is PARAMLENGTHINFOTYPE -> {
                        SpecificDataType.ParamLengthInfoType
                    }

                    else -> {
                        error("Unsupported diag coded type ${this::class.java.simpleName}")
                    }
                }
            DiagCodedType.startDiagCodedType(builder)

            DiagCodedType.addType(builder, this.toTypeEnum())
            DiagCodedType.addBaseDataType(builder, this.basedatatype.toFileFormatEnum())
            baseTypeEncoding?.let {
                DiagCodedType.addBaseTypeEncoding(builder, it)
            }
            DiagCodedType.addIsHighLowByteOrder(builder, this.isISHIGHLOWBYTEORDER)
            DiagCodedType.addSpecificData(builder, specificData)
            DiagCodedType.addSpecificDataType(builder, specificType)

            DiagCodedType.endDiagCodedType(builder)
        }

    private fun UNIT.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val displayName = this.displayname.offset()
            val physicaldimension =
                this.physicaldimensionref?.let { ref ->
                    val physDimension =
                        odx.physDimensions[ref.idref]
                            ?: error("Couldn't find physical dimension ${ref.idref}")
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
        val basicStructure =
            this.basicstructureref?.let {
                val dop =
                    odx.combinedDataObjectProps[it.idref]
                        ?: error("Couldn't find dop ${it.idref}")
                dop.offset()
            }
        this.basicstructuresnref?.let {
            error("Short name reference for basic structure ref not supported")
        }
        val envDataRef =
            this.envdatadescref?.let {
                val dop =
                    odx.combinedDataObjectProps[it.idref]
                        ?: error("Couldn't find dop ${it.idref}")
                dop.offset()
            }
        this.envdatadescsnref?.let {
            error("Short name reference for envdata desc not supported")
        }

        Field.startField(builder)

        basicStructure?.let { Field.addBasicStructure(builder, it) }
        envDataRef?.let { Field.addEnvDataDesc(builder, it) }
        Field.addIsVisible(builder, this.isISVISIBLE)

        return Field.endField(builder)
    }

    private fun ENVDATA.toEnvData(): Int {
        val dtcValues =
            this.dtcvalues?.dtcvalue?.map { it.value.toUInt() }?.toUIntArray()?.let {
                EnvData.createDtcValuesVector(builder, it)
            }
        val params =
            this.params?.param?.map { it.offset() }?.toIntArray()?.let {
                EnvData.createParamsVector(builder, it)
            }

        EnvData.startEnvData(builder)
        dtcValues?.let { EnvData.addDtcValues(builder, dtcValues) }
        params?.let { EnvData.addParams(builder, params) }
        return EnvData.endEnvData(builder)
    }

    private fun ENVDATADESC.toEnvDataDesc(): Int {
        val envDatas =
            this.envdatarefs
                ?.envdataref
                ?.map {
                    val envData =
                        odx.envDatas[it.idref] ?: error("Couldn't find env data ${it.idref}")
                    envData.offset()
                }?.toIntArray()
                ?.let {
                    EnvDataDesc.createEnvDatasVector(builder, it)
                }

        val paramShortName = this.paramsnref?.shortname?.offset()
        val paramShortNamePath = this.paramsnpathref?.shortnamepath?.offset()

        EnvDataDesc.startEnvDataDesc(builder)

        envDatas?.let { EnvDataDesc.addEnvDatas(builder, it) }

        paramShortName?.let { EnvDataDesc.addParamShortName(builder, it) }
        paramShortNamePath?.let { EnvDataDesc.addParamPathShortName(builder, it) }

        return EnvDataDesc.endEnvDataDesc(builder)
    }

    private fun PHYSICALTYPE.offset(): Int =
        cachedObjects.getOrPut(this) {
            PhysicalType.startPhysicalType(builder)

            PhysicalType.addBaseDataType(builder, this.basedatatype.toFileFormatEnum())
            this.precision?.let { PhysicalType.addPrecision(builder, it.toUInt()) }
            this.displayradix?.let { PhysicalType.addDisplayRadix(builder, it.toFileFormatEnum()) }

            PhysicalType.endPhysicalType(builder)
        }

    private fun schema.odx.DTC.offset(): Int =
        cachedObjects.getOrPut(this) {
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

    private fun LONGNAME.offset(): Int =
        cachedObjects.getOrPut(this) {
            val tiOffset = this.ti?.offset()
            val valueOffset = this.value?.offset()

            LongName.startLongName(builder)
            tiOffset?.let { LongName.addTi(builder, tiOffset) }
            valueOffset?.let { LongName.addValue(builder, valueOffset) }
            LongName.endLongName(builder)
        }

    private fun TEXT.offset(): Int =
        cachedObjects.getOrPut(this) {
            val ti = this.ti?.offset()
            val value = this.value?.offset()

            Text.startText(builder)

            ti?.let { Text.addTi(builder, it) }
            value?.let { Text.addValue(builder, it) }

            Text.endText(builder)
        }

    private fun COMPUMETHOD.offset(): Int =
        cachedObjects.getOrPut(this) {
            val internalToPhys = this.compuinternaltophys?.offset()
            val physToInternal = this.compuphystointernal?.offset()

            CompuMethod.startCompuMethod(builder)

            this.category?.let { CompuMethod.addCategory(builder, it.toFileFormatEnum()) }
            internalToPhys?.let { CompuMethod.addInternalToPhys(builder, it) }
            physToInternal?.let { CompuMethod.addPhysToInternal(builder, it) }
            CompuMethod.endCompuMethod(builder)
        }

    private fun LIBRARY.offset(): Int =
        cachedObjects.getOrPut(this) {
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

    private fun PROGCODE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val codeFile = this.codefile?.offset()
            val encryption = this.entrypoint?.offset()
            val syntax = this.syntax?.offset()
            val revision = this.revision?.offset()
            val entrypoint = this.entrypoint?.offset()
            val libraries =
                this.libraryrefs
                    ?.libraryref
                    ?.map { ref ->
                        val library =
                            odx.libraries[ref.idref]
                                ?: error("Couldn't find LIBRARY ${ref.idref}")
                        library.offset()
                    }?.toIntArray()
                    ?.let {
                        ProgCode.createLibraryVector(builder, it)
                    }

            ProgCode.startProgCode(builder)
            codeFile?.let { ProgCode.addCodeFile(builder, it) }
            encryption?.let { ProgCode.addEncryption(builder, it) }
            syntax?.let { ProgCode.addSyntax(builder, it) }
            revision?.let { ProgCode.addRevision(builder, it) }
            entrypoint?.let { ProgCode.addEncryption(builder, it) }
            libraries?.let { ProgCode.addLibrary(builder, it) }

            ProgCode.endProgCode(builder)
        }

    private fun COMPUPHYSTOINTERNAL.offset(): Int =
        cachedObjects.getOrPut(this) {
            val progcode = this.progcode?.offset()
            val compuscales =
                this.compuscales?.compuscale?.map { it.offset() }?.toIntArray()?.let {
                    CompuPhysToInternal.createCompuScalesVector(builder, it)
                }
            val compudefaultvalue = this.compudefaultvalue?.offset()

            CompuPhysToInternal.startCompuPhysToInternal(builder)
            progcode?.let { CompuPhysToInternal.addProgCode(builder, it) }
            compuscales?.let { CompuPhysToInternal.addCompuScales(builder, it) }
            compudefaultvalue?.let { CompuPhysToInternal.addCompuDefaultValue(builder, it) }
            CompuPhysToInternal.endCompuPhysToInternal(builder)
        }

    private fun COMPUINTERNALTOPHYS.offset(): Int =
        cachedObjects.getOrPut(this) {
            val progcode = this.progcode?.offset()
            val compuscales =
                this.compuscales?.compuscale?.map { it.offset() }?.toIntArray()?.let {
                    CompuInternalToPhys.createCompuScalesVector(builder, it)
                }
            val compudefaultvalue = this.compudefaultvalue?.offset()

            CompuInternalToPhys.startCompuInternalToPhys(builder)
            progcode?.let { CompuInternalToPhys.addProgCode(builder, it) }
            compuscales?.let { CompuInternalToPhys.addCompuScales(builder, it) }
            compudefaultvalue?.let { CompuInternalToPhys.addCompuDefaultValue(builder, it) }
            CompuInternalToPhys.endCompuInternalToPhys(builder)
        }

    private fun COMPUSCALE.offset(): Int =
        cachedObjects.getOrPut(this) {
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
            CompuScale.endCompuScale(builder)
        }

    private fun LIMIT.offset(): Int =
        cachedObjects.getOrPut(this) {
            val value = this.value?.offset()

            Limit.startLimit(builder)
            value?.let { Limit.addValue(builder, value) }
            this.intervaltype?.let { Limit.addIntervalType(builder, it.toFileFormatEnum()) }
            Limit.endLimit(builder)
        }

    private fun COMPUINVERSEVALUE.offset(): Int =
        cachedObjects.getOrPut(this) {
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
            CompuValues.endCompuValues(builder)
        }

    private fun COMPUCONST.offset(): Int =
        cachedObjects.getOrPut(this) {
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
            CompuValues.endCompuValues(builder)
        }

    private fun COMPUDEFAULTVALUE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val vtTi = this.vt?.ti?.offset()
            val vtValue = this.vt?.value?.offset()
            val values =
                if (vtValue != null || vtTi != null || this.v?.value != null) {
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
                } else {
                    null
                }

            val invVtTi =
                this.compuinversevalue
                    ?.vt
                    ?.ti
                    ?.offset()
            val invVtValue =
                this.compuinversevalue
                    ?.vt
                    ?.value
                    ?.offset()
            val inverseValues =
                if (invVtTi != null || invVtValue != null || this.compuinversevalue?.vt?.value != null) {
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
                } else {
                    null
                }

            CompuDefaultValue.startCompuDefaultValue(builder)
            values?.let {
                CompuDefaultValue.addValues(builder, it)
            }
            inverseValues?.let {
                CompuDefaultValue.addInverseValues(builder, it)
            }
            CompuDefaultValue.endCompuDefaultValue(builder)
        }

    private fun COMPURATIONALCOEFFS.offset(): Int =
        cachedObjects.getOrPut(this) {
            val numerator =
                this.compunumerator?.v?.mapNotNull { it.value }?.let {
                    CompuRationalCoEffs.createNumeratorVector(builder, it.toDoubleArray())
                }
            val denominator =
                this.compudenominator?.v?.mapNotNull { it.value }?.let {
                    CompuRationalCoEffs.createDenominatorVector(builder, it.toDoubleArray())
                }
            CompuRationalCoEffs.startCompuRationalCoEffs(builder)
            numerator?.let { CompuRationalCoEffs.addNumerator(builder, it) }
            denominator?.let { CompuRationalCoEffs.addDenominator(builder, it) }
            CompuRationalCoEffs.endCompuRationalCoEffs(builder)
        }

    private fun schema.odx.DTCDOP.toDTCDOP(): Int {
        val diagCodedType = this.diagcodedtype.offset()
        val physicalType = this.physicaltype.offset()
        val compuMethod = this.compumethod.offset()

        val dtcs =
            this.dtcs.dtcproxy
                ?.map {
                    if (it is schema.odx.DTC) {
                        it.offset()
                    } else if (it is ODXLINK) {
                        val dop = odx.dtcs[it.idref] ?: error("Couldn't find DTC ${it.idref}")
                        dop.offset()
                    } else {
                        error("Unsupported DTC type ${it::class.java.simpleName}")
                    }
                }?.toIntArray()
                ?.let {
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
        val cases =
            this.cases
                ?.case
                ?.map { it.offset() }
                ?.toIntArray()
                ?.let { MUXDOP.createCasesVector(builder, it) }

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
        val params =
            this.params
                ?.param
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    Structure.createParamsVector(builder, it)
                }

        Structure.startStructure(builder)
        this.bytesize?.let { Structure.addByteSize(builder, it.toUInt()) }
        params?.let { Structure.addParams(builder, it) }
        Structure.addIsVisible(builder, this.isISVISIBLE)

        return Structure.endStructure(builder)
    }

    private fun DETERMINENUMBEROFITEMS.offset(): Int =
        cachedObjects.getOrPut(this) {
            val dop =
                this.dataobjectpropref?.let {
                    val dop =
                        odx.combinedDataObjectProps[it.idref]
                            ?: error("Couldn't find ${it.idref}")
                    dop.offset()
                }

            DetermineNumberOfItems.startDetermineNumberOfItems(builder)
            DetermineNumberOfItems.addBytePosition(builder, this.byteposition.toUInt())
            this.bitposition?.let { DetermineNumberOfItems.addBitPosition(builder, it.toUInt()) }
            dop?.let { DetermineNumberOfItems.addDop(builder, it) }
            DetermineNumberOfItems.endDetermineNumberOfItems(builder)
        }

    private fun ADDITIONALAUDIENCE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()

            AdditionalAudience.startAdditionalAudience(builder)

            AdditionalAudience.addShortName(builder, shortName)
            longName?.let {
                AdditionalAudience.addLongName(builder, it)
            }

            AdditionalAudience.endAdditionalAudience(builder)
        }

    private fun AUDIENCE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val enabledAudiences =
                this.enabledaudiencerefs?.enabledaudienceref?.let { aa ->
                    Audience.createEnabledAudiencesVector(
                        builder,
                        aa
                            .map {
                                val aud =
                                    odx.additionalAudiences[it.idref]
                                        ?: error("Can't find additional audience ${it.idref}")
                                aud.offset()
                            }.toIntArray(),
                    )
                }

            val disabledAudiences =
                this.disabledaudiencerefs?.disabledaudienceref?.let { aa ->
                    Audience.createDisabledAudiencesVector(
                        builder,
                        aa
                            .map {
                                val aud =
                                    odx.additionalAudiences[it.idref]
                                        ?: error("Can't find additional audience ${it.idref}")
                                aud.offset()
                            }.toIntArray(),
                    )
                }

            Audience.startAudience(builder)
            enabledAudiences?.let {
                Audience.addEnabledAudiences(
                    builder,
                    it,
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

    private fun PRECONDITIONSTATEREF.offset(): Int =
        cachedObjects.getOrPut(this) {
            val state =
                odx.states[this.idref]?.offset() ?: error("Couldn't find STATE ${this.idref}")
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

    private fun STATETRANSITIONREF.offset(): Int =
        cachedObjects.getOrPut(this) {
            val value = this.value?.offset()
            val stateTransition =
                this.idref?.let {
                    val stateTransition =
                        odx.stateTransitions[this.idref]
                            ?: error("Couldn't find STATETRANSITION ${this.idref}")
                    stateTransition.offset()
                }

            StateTransitionRef.startStateTransitionRef(builder)

            value?.let { StateTransitionRef.addValue(builder, it) }
            stateTransition?.let { StateTransitionRef.addStateTransition(builder, it) }

            StateTransitionRef.endStateTransitionRef(builder)
        }

    private fun INPUTPARAM.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val physicalDefaultValue = this.physicaldefaultvalue?.offset()
            val semantic = this.semantic?.offset()
            val dop =
                this.dopbaseref?.let {
                    val dop =
                        odx.combinedDataObjectProps[it.idref]
                            ?: error("Can't find DOP ${it.idref}")
                    dop.offset()
                }

            JobParam.startJobParam(builder)
            JobParam.addShortName(builder, shortName)
            longName?.let { JobParam.addLongName(builder, it) }
            physicalDefaultValue?.let { JobParam.addPhysicalDefaultValue(builder, it) }
            semantic?.let { JobParam.addSemantic(builder, it) }
            dop?.let { JobParam.addDopBase(builder, it) }
            JobParam.endJobParam(builder)
        }

    private fun OUTPUTPARAM.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val semantic = this.semantic?.offset()
            val dop =
                this.dopbaseref?.let {
                    val dop =
                        odx.combinedDataObjectProps[it.idref]
                            ?: error("Can't find DOP ${it.idref}")
                    dop.offset()
                }

            JobParam.startJobParam(builder)
            JobParam.addShortName(builder, shortName)
            longName?.let { JobParam.addLongName(builder, it) }
            semantic?.let { JobParam.addSemantic(builder, it) }
            dop?.let { JobParam.addDopBase(builder, it) }
            JobParam.endJobParam(builder)
        }

    private fun NEGOUTPUTPARAM.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val dop =
                this.dopbaseref?.let {
                    val dop =
                        odx.combinedDataObjectProps[it.idref]
                            ?: error("Can't find DOP ${it.idref}")
                    dop.offset()
                }

            JobParam.startJobParam(builder)
            JobParam.addShortName(builder, shortName)
            longName?.let { JobParam.addLongName(builder, it) }
            dop?.let { JobParam.addDopBase(builder, it) }
            JobParam.endJobParam(builder)
        }

    private fun DIAGCOMM.offsetInternal(): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val diagClass = this.diagnosticclass?.toFileFormatEnum()
        val functClasses =
            this.functclassrefs
                ?.functclassref
                ?.map {
                    val functClass =
                        odx.functClasses[it.idref]
                            ?: error("Couldn't find funct class ${it.idref}")
                    functClass.offset()
                }?.toIntArray()
                ?.let {
                    DiagComm.createFunctClassVector(builder, it)
                }
        val semantic = this.semantic?.offset()
        val preconditionStateRefs =
            this.preconditionstaterefs
                ?.preconditionstateref
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    DiagComm.createPreConditionStateRefsVector(builder, it)
                }
        val stateTransitionRefs =
            this.statetransitionrefs
                ?.statetransitionref
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    DiagComm.createStateTransitionRefsVector(builder, it)
                }
        val protocolRefs =
            this.protocolsnrefs
                ?.protocolsnref
                ?.map {
                    val protocol =
                        odx.protocols.values.firstOrNull { p -> p.shortname == it.shortname }
                            ?: error("Couldn't find protocol ${it.shortname}")
                    protocol.offset()
                }?.toIntArray()
                ?.let {
                    DiagComm.createProtocolsVector(builder, it)
                }
        val audience = this.audience?.offset()
        val sdgs = this.sdgs?.offset()

        DiagComm.startDiagComm(builder)
        DiagComm.addShortName(builder, shortName)
        longName?.let { DiagComm.addLongName(builder, it) }
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

    private fun SINGLEECUJOB.offset(): Int =
        cachedObjects.getOrPut(this) {
            val diagComm = (this as DIAGCOMM).offsetInternal()
            val progCodes =
                this.progcodes
                    ?.progcode
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        SingleEcuJob.createProgCodesVector(builder, it)
                    }
            val inputParams =
                this.inputparams
                    ?.inputparam
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        SingleEcuJob.createInputParamsVector(builder, it)
                    }
            val outputParams =
                this.outputparams
                    ?.outputparam
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        SingleEcuJob.createOutputParamsVector(builder, it)
                    }
            val negOutputParams =
                this.negoutputparams
                    ?.negoutputparam
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
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

    private fun DIAGLAYER.offsetInternal(comparamRefs: Int?): Int {
        val shortName = this.shortname.offset()
        val longName = this.longname?.offset()
        val sdgs = this.sdgs?.offset()
        val functClasses =
            this.functclasss
                ?.functclass
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createFunctClassesVector(builder, it)
                }
        val additionalAudiences =
            this.additionalaudiences
                ?.additionalaudience
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createAdditionalAudiencesVector(builder, it)
                }
        val resolvedLinks: List<DIAGCOMM> =
            this.diagcomms?.diagcommproxy?.filterIsInstance<ODXLINK>()?.map {
                odx.diagServices[it.idref] ?: odx.singleEcuJobs[it.idref]
                    ?: error("Couldn't find reference ${it.idref}")
            } ?: emptyList()

        val diagServicesRaw =
            resolvedLinks.filterIsInstance<DIAGSERVICE>().map {
                it.offset()
            } + (
                this.diagcomms?.diagcommproxy?.filterIsInstance<DIAGSERVICE>()?.map {
                    it.offset()
                } ?: emptyList()
            )

        val diagServices =
            diagServicesRaw.toIntArray().let {
                DiagLayer.createDiagServicesVector(builder, it)
            }

        val singleEcuJobsRaw =
            resolvedLinks.filterIsInstance<SINGLEECUJOB>().map {
                it.offset()
            } + (
                this.diagcomms?.diagcommproxy?.filterIsInstance<SINGLEECUJOB>()?.map {
                    it.offset()
                } ?: emptyList()
            )

        val singleEcuJobs =
            singleEcuJobsRaw.toIntArray().let {
                DiagLayer.createSingleEcuJobsVector(builder, it)
            }
        val stateCharts =
            statecharts
                ?.statechart
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createStateChartsVector(builder, it)
                }

        DiagLayer.startDiagLayer(builder)
        DiagLayer.addShortName(builder, shortName)
        longName?.let { DiagLayer.addLongName(builder, it) }
        sdgs?.let { DiagLayer.addSdgs(builder, it) }
        DiagLayer.addDiagServices(builder, diagServices)
        DiagLayer.addSingleEcuJobs(builder, singleEcuJobs)
        stateCharts?.let { DiagLayer.addStateCharts(builder, it) }
        functClasses?.let { DiagLayer.addFunctClasses(builder, it) }
        additionalAudiences?.let { DiagLayer.addAdditionalAudiences(builder, it) }
        comparamRefs?.let { DiagLayer.addComParamRefs(builder, it) }
        return DiagLayer.endDiagLayer(builder)
    }

    private fun DIAGLAYER.offsetType(): Int = this.offsetInternal(null)

    private fun HIERARCHYELEMENT.offsetType(): Int {
        // comparam refs are for hierarchyelements
        val comParamRefs =
            this.comparamrefs
                ?.comparamref
                ?.map {
                    it.offset()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createComParamRefsVector(builder, it)
                }

        val diagLayer = (this as DIAGLAYER).offsetInternal(comParamRefs)
        return diagLayer
    }

    private fun BASEVARIANT.offset(): Int =
        cachedObjects.getOrPut(this) {
            val diagLayer = (this as HIERARCHYELEMENT).offsetType()
            val pattern =
                this.basevariantpattern
                    ?.matchingbasevariantparameters
                    ?.matchingbasevariantparameter
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Variant.createVariantPatternVector(builder, it)
                    }
            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Variant.createParentRefsVector(builder, it)
                    }

            Variant.startVariant(builder)
            Variant.addDiagLayer(builder, diagLayer)
            Variant.addIsBaseVariant(builder, true)
            pattern?.let { Variant.addVariantPattern(builder, it) }
            parentRefs?.let { Variant.addParentRefs(builder, it) }
            Variant.endVariant(builder)
        }

    private fun ECUVARIANT.offset(): Int =
        cachedObjects.getOrPut(this) {
            val diagLayer = (this as HIERARCHYELEMENT).offsetType()
            val pattern =
                this.ecuvariantpatterns
                    ?.ecuvariantpattern
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Variant.createVariantPatternVector(builder, it)
                    }
            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Variant.createParentRefsVector(builder, it)
                    }

            Variant.startVariant(builder)
            Variant.addDiagLayer(builder, diagLayer)
            Variant.addIsBaseVariant(builder, false)
            pattern?.let { Variant.addVariantPattern(builder, it) }
            parentRefs?.let { Variant.addParentRefs(builder, it) }
            Variant.endVariant(builder)
        }

    private fun ECUSHAREDDATA.offset(): Int =
        cachedObjects.getOrPut(this) {
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

            val diagLayer = (this as DIAGLAYER).offsetType()

            EcuSharedData.startEcuSharedData(builder)
            EcuSharedData.addDiagLayer(builder, diagLayer)
            EcuSharedData.endEcuSharedData(builder)
        }

    fun PARENTREF.offset(): Int {
        val resolved =
            odx.basevariants[this.idref] ?: odx.ecuvariants[this.idref] ?: odx.protocols[this.idref]
                ?: odx.functionalGroups[this.idref] ?: odx.tables[this.idref] ?: odx.ecuSharedDatas[this.idref]
        val resolvedOffs =
            when (resolved) {
                is BASEVARIANT -> resolved.offset()
                is ECUVARIANT -> resolved.offset()
                is PROTOCOL -> resolved.offset()
                is TABLE -> resolved.offset()
                is FUNCTIONALGROUP -> resolved.offset()
                is ECUSHAREDDATA -> resolved.offset()
                else -> throw UnsupportedOperationException("Unsupported idref type: ${this.idref} / ${this.doctype?.value()} ($resolved)")
            }
        val resolvedOffsType =
            when (resolved) {
                is BASEVARIANT -> ParentRefType.Variant
                is ECUVARIANT -> ParentRefType.Variant
                is PROTOCOL -> ParentRefType.Protocol
                is TABLE -> ParentRefType.TableDop
                is FUNCTIONALGROUP -> ParentRefType.FunctionalGroup
                is ECUSHAREDDATA -> ParentRefType.EcuSharedData
                else -> throw UnsupportedOperationException("Unsupported idref type: ${this.idref} / ${this.doctype?.value()} ($resolved)")
            }
        val notInheritedDiagCommShortNames =
            this.notinheriteddiagcomms
                ?.notinheriteddiagcomm
                ?.map {
                    it.diagcommsnref.shortname.offset()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedDiagCommShortNamesVector(builder, it)
                }
        val notInheritedDopsShortNames =
            this.notinheriteddops
                ?.notinheriteddop
                ?.map {
                    it.dopbasesnref.shortname.offset()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedDopsShortNamesVector(builder, it)
                }
        val notInheritedTablesShortNames =
            this.notinheritedtables
                ?.notinheritedtable
                ?.map {
                    it.tablesnref.shortname.offset()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedTablesShortNamesVector(builder, it)
                }
        val notInheritedVariablesShortNames =
            this.notinheritedvariables
                ?.notinheritedvariable
                ?.map {
                    it.diagvariablesnref.shortname.offset()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedVariablesShortNamesVector(builder, it)
                }
        val notInheritedGlobalNegResponseShortNames =
            this.notinheritedglobalnegresponses
                ?.notinheritedglobalnegresponse
                ?.map {
                    it.globalnegresponsesnref.shortname.offset()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedGlobalNegResponsesShortNamesVector(builder, it)
                }

        ParentRef.startParentRef(builder)
        ParentRef.addRef(builder, resolvedOffs)
        ParentRef.addRefType(builder, resolvedOffsType)
        notInheritedDiagCommShortNames?.let { ParentRef.addNotInheritedDiagCommShortNames(builder, it) }
        notInheritedDopsShortNames?.let { ParentRef.addNotInheritedDopsShortNames(builder, it) }
        notInheritedTablesShortNames?.let { ParentRef.addNotInheritedTablesShortNames(builder, it) }
        notInheritedVariablesShortNames?.let { ParentRef.addNotInheritedVariablesShortNames(builder, it) }
        notInheritedGlobalNegResponseShortNames?.let {
            ParentRef.addNotInheritedGlobalNegResponsesShortNames(
                builder,
                it,
            )
        }
        return ParentRef.endParentRef(builder)
    }

    private fun FUNCTIONALGROUP.offset(): Int =
        cachedObjects.getOrPut(this) {
            val diagLayer = (this as HIERARCHYELEMENT).offsetType()
            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        FunctionalGroup.createParentRefsVector(builder, it)
                    }

            FunctionalGroup.startFunctionalGroup(builder)
            FunctionalGroup.addDiagLayer(builder, diagLayer)
            parentRefs?.let { FunctionalGroup.addParentRefs(builder, it) }
            FunctionalGroup.endFunctionalGroup(builder)
        }

    private fun MATCHINGBASEVARIANTPARAMETER.offset(): Int =
        cachedObjects.getOrPut(this) {
            if (this.outparamifsnref != null) {
                error("Unsupported outparam if sn ref")
            }

            if (this.outparamifsnpathref != null) {
                error("Unsupported outparam if sn path ref")
            }

            val expectedValue = this.expectedvalue.offset()
            lateinit var diagService: DIAGSERVICE
            val diagServiceOffset =
                this.diagcommsnref.shortname.let { shortname ->
                    diagService = odx.diagServices.values.firstOrNull { it.shortname == shortname }
                        ?: error("Couldn't find diag service $shortname")
                    diagService.offset()
                }

            MatchingParameter.startMatchingParameter(builder)
            MatchingParameter.addExpectedValue(builder, expectedValue)
            MatchingParameter.addDiagService(builder, diagServiceOffset)
            MatchingParameter.addUsePhysicalAddressing(builder, this.isUSEPHYSICALADDRESSING)
            MatchingParameter.endMatchingParameter(builder)
        }

    private fun MATCHINGPARAMETER.offset(): Int =
        cachedObjects.getOrPut(this) {
            val expectedValue = this.expectedvalue?.offset()
            lateinit var diagService: DIAGSERVICE
            val diagServiceOffset =
                this.diagcommsnref.shortname.let { shortname ->
                    diagService = odx.diagServices.values.firstOrNull { it.shortname == shortname }
                        ?: error("Couldn't find diag service $shortname")
                    diagService.offset()
                }
            val outParam =
                this.outparamifsnref?.shortname?.let { expectedShortName ->
                    diagService.posresponserefs
                        ?.posresponseref
                        ?.flatMap { pr ->
                            val posResponse =
                                odx.posResponses[pr.idref]
                                    ?: error("Couldn't find pos response ${pr.idref}")
                            posResponse.params?.param ?: emptyList()
                        }?.firstOrNull { params ->
                            params.shortname == expectedShortName
                        }?.offset()
                        ?: error("Couldn't find param for shortName $expectedShortName")
                }

            this.outparamifsnpathref?.let {
                error("Unsupported outparam if sn path ref")
            }

            MatchingParameter.startMatchingParameter(builder)
            diagServiceOffset.let { MatchingParameter.addDiagService(builder, it) }
            expectedValue?.let { MatchingParameter.addExpectedValue(builder, it) }
            outParam?.let { MatchingParameter.addOutParam(builder, it) }
            MatchingParameter.endMatchingParameter(builder)
        }

    private fun ECUVARIANTPATTERN.offset(): Int =
        cachedObjects.getOrPut(this) {
            val matchingParameter =
                this.matchingparameters
                    ?.matchingparameter
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        Variant.createVariantPatternVector(builder, it)
                    }
            VariantPattern.startVariantPattern(builder)
            matchingParameter?.let { VariantPattern.addMatchingParameter(builder, matchingParameter) }
            VariantPattern.endVariantPattern(builder)
        }

    private fun COMPARAMSUBSET.offset(): Int =
        cachedObjects.getOrPut(this) {
            val comParams =
                this.comparams
                    ?.comparam
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        ComParamSubSet.createComParamsVector(builder, it)
                    }
            val complexComParams =
                this.complexcomparams
                    ?.complexcomparam
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        ComParamSubSet.createComplexComParamsVector(builder, it)
                    }
            val dops =
                this.dataobjectprops
                    ?.dataobjectprop
                    ?.map {
                        val dop = odx.dataObjectProps[it.id] ?: error("Can't find DOP ${it.id}")
                        dop.offset()
                    }?.toIntArray()
                    ?.let {
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

    private fun UNITGROUP.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val units =
                this.unitrefs
                    ?.unitref
                    ?.map {
                        val unit = odx.units[it.idref] ?: error("Couldn't find unit $it")
                        unit.offset()
                    }?.toIntArray()
                    ?.let {
                        UnitGroup.createUnitrefsVector(builder, it)
                    }

            UnitGroup.startUnitGroup(builder)
            UnitGroup.addShortName(builder, shortName)
            longName?.let { UnitGroup.addLongName(builder, it) }
            units?.let { UnitGroup.addUnitrefs(builder, it) }
            UnitGroup.endUnitGroup(builder)
        }

    private fun UNITSPEC.offset(): Int =
        cachedObjects.getOrPut(this) {
            val unitGroups =
                this.unitgroups?.unitgroup?.map { it.offset() }?.toIntArray()?.let {
                    UnitSpec.createUnitGroupsVector(builder, it)
                }
            val physicalDimensions =
                this.physicaldimensions?.physicaldimension?.map { it.offset() }?.toIntArray()?.let {
                    UnitSpec.createPhysicalDimensionsVector(builder, it)
                }
            val units =
                this.units
                    ?.unit
                    ?.map {
                        val unit = odx.units[it.id] ?: error("Unit ${it.id} not found")
                        unit.offset()
                    }?.toIntArray()
                    ?.let {
                        UnitSpec.createUnitsVector(builder, it)
                    }
            val sdgs = this.sdgs?.let { it.offset() }

            UnitSpec.startUnitSpec(builder)
            unitGroups?.let { UnitSpec.addUnitGroups(builder, it) }
            physicalDimensions?.let { UnitSpec.addPhysicalDimensions(builder, it) }
            units?.let { UnitSpec.addUnits(builder, it) }
            sdgs?.let { UnitSpec.addSdgs(builder, it) }
            UnitSpec.endUnitSpec(builder)
        }

    private fun COMPARAM.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val paramClass = this.paramclass?.offset()
            val comParamType = this.cptype?.toFileFormatEnum()
            val comParamUsage = this.cpusage?.toFileFormatEnum()
            val displayLevel = this.displaylevel?.toUInt()

            val regularComParam =
                this.let {
                    val physicalDefaultValue = this.physicaldefaultvalue?.offset()
                    val dop =
                        this.dataobjectpropref?.let {
                            val dop =
                                odx.combinedDataObjectProps[it.idref]
                                    ?: error("Couldn't find ${it.idref}")
                            dop.offset()
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

    private fun SIMPLEVALUE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val value = this.value?.offset()
            SimpleValue.startSimpleValue(builder)
            value?.let { SimpleValue.addValue(builder, it) }
            SimpleValue.endSimpleValue(builder)
        }

    private fun COMPLEXVALUE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val entries =
                this.simplevalueOrCOMPLEXVALUE
                    ?.map {
                        when (it) {
                            is SIMPLEVALUE -> it.offset()
                            is COMPLEXVALUE -> it.offset()
                            else -> error("Unknown object type ${this.javaClass.simpleName}")
                        }
                    }?.toIntArray()
                    ?.let {
                        ComplexValue.createEntriesVector(builder, it)
                    }
            val entriesTypes =
                this.simplevalueOrCOMPLEXVALUE
                    ?.map {
                        when (it) {
                            is SIMPLEVALUE -> SimpleOrComplexValueEntry.SimpleValue
                            is COMPLEXVALUE -> SimpleOrComplexValueEntry.ComplexValue
                            else -> error("Unknown object type ${this.javaClass.simpleName}")
                        }
                    }?.toUByteArray()
                    ?.let {
                        ComplexValue.createEntriesTypeVector(builder, it)
                    }
            ComplexValue.startComplexValue(builder)
            entries?.let {
                ComplexValue.addEntries(builder, it)
                ComplexValue.addEntriesType(builder, entriesTypes ?: error("Inconsistent data"))
            }
            ComplexValue.endComplexValue(builder)
        }

    private fun COMPLEXCOMPARAM.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val paramClass = this.paramclass?.offset()
            val comParamType = this.cptype?.toFileFormatEnum()
            val comParamUsage = this.cpusage?.toFileFormatEnum()
            val displayLevel = this.displaylevel?.toUInt()
            val complexComParam =
                let {
                    val comParams =
                        this.comparamOrCOMPLEXCOMPARAM
                            ?.map {
                                when (it) {
                                    is COMPARAM -> it.offset()
                                    is COMPLEXCOMPARAM -> it.offset()
                                    else -> error("Unknown com param type ${it.id}")
                                }
                            }?.toIntArray()
                            ?.let {
                                ComplexComParam.createComParamsVector(builder, it)
                            }

                    val complexPhysicalDefaultValues =
                        this.complexphysicaldefaultvalue
                            ?.complexvalues
                            ?.complexvalue
                            ?.map {
                                it.offset()
                            }?.toIntArray()
                            ?.let {
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

    private fun STATE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()

            dataformat.State.startState(builder)
            dataformat.State.addShortName(builder, shortName)
            longName?.let { dataformat.State.addLongName(builder, it) }

            dataformat.State.endState(builder)
        }

    private fun PHYSICALDIMENSION.offset(): Int =
        cachedObjects.getOrPut(this) {
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

    private fun PROTOCOL.offset(): Int =
        cachedObjects.getOrPut(this) {
            val diagLayer = (this as DIAGLAYER).offsetType()
            val comparamSpecs =
                this.comparamspecref?.let {
                    val comParamSpec =
                        odx.comparamSpecs[it.idref]
                            ?: error("Couldn't find com param spec ${it.idref}")
                    comParamSpec.offset()
                }
            val protStack =
                this.protstacksnref?.let { protStack ->
                    val stack =
                        odx.comparamSpecs.values
                            .flatMap { it.protstacks?.protstack ?: emptyList() }
                            .firstOrNull { it.shortname == protStack.shortname }
                            ?: error("Couldn't find protstack with short name ${protStack.shortname}")
                    stack.offset()
                }

            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map { it.offset() }
                    ?.toIntArray()
                    ?.let {
                        Protocol.createParentRefsVector(builder, it)
                    }

            Protocol.startProtocol(builder)
            Protocol.addDiagLayer(builder, diagLayer)
            comparamSpecs?.let { Protocol.addComParamSpec(builder, it) }
            protStack?.let { Protocol.addProtStack(builder, it) }
            parentRefs?.let { Protocol.addParentRefs(builder, it) }
            Protocol.endProtocol(builder)
        }

    private fun COMPARAMSPEC.offset(): Int =
        cachedObjects.getOrPut(this) {
            val protStacks =
                this.protstacks
                    ?.protstack
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        ComParamSpec.createProtStacksVector(builder, it)
                    }
            ComParamSpec.startComParamSpec(builder)
            protStacks?.let { ComParamSpec.addProtStacks(builder, it) }
            ComParamSpec.endComParamSpec(builder)
        }

    private fun PROTSTACK.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val comparamSubSets =
                this.comparamsubsetrefs
                    ?.comparamsubsetref
                    ?.map {
                        val comparamSubSet =
                            odx.comParamSubSets[it.idref]
                                ?: error("Couldn't find com param subset ${it.idref}")
                        comparamSubSet.offset()
                    }?.toIntArray()
                    ?.let {
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

    private fun COMPARAMREF.offset(): Int =
        cachedObjects.getOrPut(this) {
            val comParam =
                odx.comparams[this.idref]?.offset()
                    ?: odx.complexComparams[this.idref]?.offset()

            if (comParam == null) {
                if (!options.lenient) {
                    error("Couldn't find COMPARAM ${this.idref} @ ${this.docref}")
                }
                logger.warning("Couldn't find COMPARAM ${this.idref} @ ${this.docref}")
            }

            val simpleValue = this.simplevalue?.offset()
            val complexValue = this.complexvalue?.offset()

            val protocol =
                this.protocolsnref?.shortname?.let { shortName ->
                    val protocolOdx =
                        odx.protocols.values.firstOrNull { it.shortname == shortName }
                            ?: error("Couldn't find PROTOCOL $shortName")
                    protocolOdx.offset()
                }

            val protStack =
                this.protstacksnref?.let {
                    val protStackOdx =
                        odx.protStacks.values.firstOrNull { it.shortname == this.protstacksnref.shortname }
                            ?: error("Can't find protocol ${this.protstacksnref.shortname}")

                    protStackOdx.offset()
                }

            ComParamRef.startComParamRef(builder)
            comParam?.let { ComParamRef.addComParam(builder, it) }
            simpleValue?.let { ComParamRef.addSimpleValue(builder, it) }
            complexValue?.let { ComParamRef.addComplexValue(builder, it) }
            protocol?.let { ComParamRef.addProtocol(builder, it) }
            protStack?.let { ComParamRef.addProtStack(builder, it) }
            ComParamRef.endComParamRef(builder)
        }

    private fun STATECHART.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val semantic = this.semantic.offset()
            val stateTransitions =
                this.statetransitions?.statetransition?.let { transitions ->
                    val data = transitions.map { it.offset() }.toIntArray()
                    StateChart.createStateTransitionsVector(builder, data)
                }
            val startStateShortName = this.startstatesnref.shortname.offset()

            val states =
                this.states?.state?.let { states ->
                    val data = states.map { it.offset() }.toIntArray()
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

    private fun STATETRANSITION.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val sourceShortNameRef = this.sourcesnref.shortname.offset()
            val targetShortNameRef = this.targetsnref.shortname.offset()

            StateTransition.startStateTransition(builder)

            StateTransition.addShortName(builder, shortName)
            StateTransition.addSourceShortNameRef(builder, sourceShortNameRef)
            StateTransition.addTargetShortNameRef(builder, targetShortNameRef)

            StateTransition.endStateTransition(builder)
        }

    private fun SWITCHKEY.offset(): Int =
        cachedObjects.getOrPut(this) {
            val dop =
                odx.combinedDataObjectProps[this.dataobjectpropref.idref]?.offset()
                    ?: error("Couldn't find dop-ref ${this.dataobjectpropref.idref}")

            SwitchKey.startSwitchKey(builder)
            SwitchKey.addBytePosition(builder, this.byteposition.toUInt())
            this.bitposition?.let { SwitchKey.addBitPosition(builder, it.toUInt()) }
            dop.let { SwitchKey.addDop(builder, it) }
            SwitchKey.endSwitchKey(builder)
        }

    private fun DEFAULTCASE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val structure =
                this.structureref?.let {
                    val dop =
                        odx.combinedDataObjectProps[it.idref]
                            ?: error("Couldn't find dop-structure-ref ${this.structureref.idref}")
                    dop.offset()
                }

            DefaultCase.startDefaultCase(builder)
            DefaultCase.addShortName(builder, shortName)
            longName?.let { DefaultCase.addLongName(builder, it) }
            structure?.let { DefaultCase.addStructure(builder, it) }
            DefaultCase.endDefaultCase(builder)
        }

    private fun CASE.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val longName = this.longname?.offset()
            val lowerLimit = this.lowerlimit.offset()
            val upperLimit = this.upperlimit.offset()

            this.structuresnref?.shortname?.let {
                TODO("STRUCTURE shortnameref not supported for $this")
            }
            val structure =
                odx.combinedDataObjectProps[this.structureref.idref]?.offset()
                    ?: error("Couldn't find dop-structure-ref ${this.structureref.idref}")

            Case.startCase(builder)
            Case.addShortName(builder, shortName)
            longName?.let { Case.addLongName(builder, it) }
            Case.addStructure(builder, structure)
            Case.addLowerLimit(builder, lowerLimit)
            Case.addUpperLimit(builder, upperLimit)
            Case.endCase(builder)
        }

    private fun TABLEROW.offset(): Int =
        cachedObjects.getOrPut(this) {
            val shortName = this.shortname.offset()
            val semantic = this.semantic?.offset()
            val longName = this.longname?.offset()
            val key = this.key?.offset()

            this.dataobjectpropsnref?.let {
                error("Unsupported data object prop shortname ref ${this.structuresnref}")
            }
            val dop =
                this.dataobjectpropref?.idref?.let {
                    val dop = odx.combinedDataObjectProps[it] ?: error("Couldn't find dop $it")
                    dop.offset()
                }
            this.structuresnref?.let {
                error("Unsupported structure shortname ref ${this.structuresnref}")
            }
            val structure =
                this.structureref?.idref?.let {
                    val structureDop = odx.structures[it] ?: error("Couldn't find structure $it")
                    structureDop.offset()
                }
            val sdgs = this.sdgs?.offset()
            val audience = this.audience?.offset()
            val functClasses =
                this.functclassrefs
                    ?.functclassref
                    ?.map {
                        val functClass =
                            odx.functClasses[it.idref]
                                ?: error("Couldn't find funct class ${it.idref}")
                        functClass.offset()
                    }?.toIntArray()
                    ?.let {
                        TableRow.createFunctClassRefsVector(builder, it)
                    }

            val stateTransitionsRefs =
                this.statetransitionrefs
                    ?.statetransitionref
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
                        TableRow.createStateTransitionRefsVector(builder, it)
                    }

            val preconditionStateRefs =
                this.preconditionstaterefs
                    ?.preconditionstateref
                    ?.map {
                        it.offset()
                    }?.toIntArray()
                    ?.let {
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

    private fun String.offset(): Int =
        cachedObjects.getOrPut(this) {
            builder.createString(this)
        }

    private fun ByteArray.offset(): Int = builder.createByteVector(this)
}
