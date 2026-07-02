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
import dataformat.InternalConstr
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
import dataformat.ScaleConstr
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
import schema.odx.INTERNALCONSTR
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
import schema.odx.SCALECONSTR
import schema.odx.SIMPLEVALUE
import schema.odx.SINGLEECUJOB
import schema.odx.SNREF
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
import kotlin.error

class DatabaseWriter(
    private val logger: Logger,
    private val odx: ODXCollectionGroup,
    private val options: ConverterOptions,
) {
    private val builder = FlatBufferBuilder()

    private val cachedObjects: MutableMap<Any, Int> = mutableMapOf()

    /**
     * Mutable element path tracking the current position in the ODX object hierarchy.
     * Pushed/popped via [withPathElement] at key processing boundaries. Used by error
     * reporting helpers to produce human-readable breadcrumb paths.
     */
    private val elementPath = mutableListOf<Any>()

    /** Executes [block] with [element] appended to [elementPath], removing it afterwards. */
    private inline fun <T> withPathElement(
        element: Any,
        block: () -> T,
    ): T {
        elementPath.add(element)
        try {
            return block()
        } finally {
            elementPath.removeLast()
        }
    }

    /**
     * Combines [cachedObjects] caching with [withPathElement] tracking.
     * The receiver is used as both the cache key and the path element.
     * The [block] runs with the receiver as `this`, so callers retain
     * natural access to the ODX object's properties.
     */
    private fun <T : Any> T.cachedWithPath(block: T.() -> Int): Int =
        cachedObjects.getOrPut(this) {
            withPathElement(this) {
                this@cachedWithPath.block()
            }
        }

    /** Returns the [ODXCollection] that [owner] was parsed from, for SNREF resolution. */
    private fun collectionOf(owner: Any): ODXCollection =
        odx.collectionFor(owner)
            ?: error("No source collection found for $owner")

    /**
     * Reports a short-name (SNREF) resolution failure with full context. Mirrors the
     * verbose ODXLINK reporting in [ODXCollectionGroup.resolveScoped]. Always throws.
     */
    private fun snrefFailure(
        expected: String,
        shortName: String,
        owner: Any,
        candidates: Collection<String>,
    ): Nothing =
        throw OdxResolutionException(
            resolutionMessage(
                ResolutionContext(
                    expected = expected,
                    refKind = "SNREF",
                    refValue = shortName,
                    sourceFile = odx.sourceFileFor(owner),
                    logicalPath = elementPath.takeIf { it.isNotEmpty() }?.let { formatElementPath(it) },
                    scopeSearched = odx.collectionFor(owner)?.containerKey,
                    candidates = candidates,
                ),
            ),
        )

    /**
     * Reports a SNREF resolution failure for a POS-RESPONSE output parameter. Used in
     * [offsetMatchingParameterBase] and [offsetMatchingParameter] to avoid duplicating
     * the same 12-line throw block in both functions.
     */
    private fun posResponseParamFailure(
        expectedShortName: String,
        owner: Any,
        diagService: DIAGSERVICE,
        allParams: List<schema.odx.PARAM>,
    ): Nothing =
        throw OdxResolutionException(
            resolutionMessage(
                ResolutionContext(
                    expected = "PARAM (in pos-response)",
                    refKind = "SNREF",
                    refValue = expectedShortName,
                    sourceFile = odx.sourceFileFor(owner) ?: odx.sourceFileFor(diagService),
                    logicalPath = formatElementPath(elementPath + diagService),
                    scopeSearched = odx.collectionFor(owner)?.containerKey,
                    candidates = allParams.map { it.shortname },
                ),
            ),
        )

    /**
     * Reports an ODXLINK resolution failure with full context. Used at call sites where
     * the resolve method returns null (e.g. in lenient mode) but the caller cannot
     * proceed without a result.
     */
    private fun odxlinkFailure(
        expected: String,
        link: ODXLINK,
        candidates: Collection<String> = emptyList(),
    ): Nothing =
        throw OdxResolutionException(
            resolutionMessage(
                ResolutionContext(
                    expected = expected,
                    refKind = "ODXLINK",
                    refValue = link.idref,
                    docref = link.docref,
                    sourceFile = odx.sourceFileFor(link),
                    logicalPath = elementPath.takeIf { it.isNotEmpty() }?.let { formatElementPath(it) },
                    scopeSearched = link.docref ?: odx.collectionFor(link)?.containerKey,
                    candidates = candidates,
                ),
            ),
        )

    /**
     * Reports an ODXLINK resolution failure for reference types other than [ODXLINK]
     * (e.g. [PRECONDITIONSTATEREF], [STATETRANSITIONREF]) that carry their own idref/docref.
     */
    private fun odxlinkFailure(
        expected: String,
        owner: Any,
        idref: String,
        docref: String? = null,
    ): Nothing =
        throw OdxResolutionException(
            resolutionMessage(
                ResolutionContext(
                    expected = expected,
                    refKind = "ODXLINK",
                    refValue = idref,
                    docref = docref,
                    sourceFile = odx.sourceFileFor(owner),
                    logicalPath =
                        elementPath.takeIf { it.isNotEmpty() }?.let { formatElementPath(it) },
                    scopeSearched = docref ?: odx.collectionFor(owner)?.containerKey,
                    candidates = emptyList(),
                ),
            ),
        )

    private val dtcs: Map<schema.odx.DTC, Int>
    private val baseVariantMap: Map<BASEVARIANT, Int>
    private val ecuVariantMap: Map<ECUVARIANT, Int>
    private val functionalGroupMap: Map<FUNCTIONALGROUP, Int>

    init {
        dtcs = odx.dtcs.associateWith { it.offsetDTC() }
        baseVariantMap = odx.basevariants.associateWith { it.offsetVariantBase() }
        ecuVariantMap = odx.ecuvariants.associateWith { it.offsetVariantEcu() }
        functionalGroupMap = odx.functionalGroups.associateWith { it.offsetFunctionalGroup() }
    }

    fun createEcuData(): ByteArray {
        val version = "2025-05-10".offsetString()
        val ecuName = odx.ecuName.offsetString()
        val odxRevision = odx.odxRevision?.offsetString()

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

    private fun DOPBASE.offsetDOP(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val sdgs = this.sdgs?.offsetSDGS()

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

    private fun DIAGSERVICE.offsetDiagService(): Int =
        this.cachedWithPath {
            val diagComm = (this as DIAGCOMM).offsetInternal()
            val request =
                odx.resolveRequest(this.requestref)?.offsetRequest()
                    ?: odxlinkFailure("REQUEST", this.requestref)
            val posResponses =
                this.posresponserefs
                    ?.posresponseref
                    ?.map {
                        val pr =
                            odx.resolvePosResponse(it)
                                ?: odxlinkFailure("POS-RESPONSE", it)
                        pr.offsetResponse()
                    }?.toIntArray()
                    ?.let {
                        DiagService.createPosResponsesVector(builder, it)
                    }
            val negResponses =
                this.negresponserefs
                    ?.negresponseref
                    ?.map {
                        val nr =
                            odx.resolveNegResponse(it)
                                ?: odxlinkFailure("NEG-RESPONSE", it)
                        nr.offsetResponse()
                    }?.toIntArray()
                    ?.let {
                        DiagService.createNegResponsesVector(builder, it)
                    }
            val comParamRefs =
                this.comparamrefs
                    ?.comparamref
                    ?.map {
                        it.offsetComParamRef()
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

    private fun TABLE.offsetTableDop(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val semantic = this.semantic?.offsetString()
            val longName = this.longname?.offsetLongName()
            val keyLabel = this.keylabel?.offsetString()
            val keyDop =
                this.keydopref?.let {
                    val dop =
                        odx.resolveCombinedDop(it) ?: odxlinkFailure("DOP", it)
                    dop.offsetDOP()
                }
            val structLabel = this.structlabel?.offsetString()
            val sdgs = this.sdgs?.offsetSDGS()

            val rows =
                this.rowwrapper
                    .map { row ->
                        when (row) {
                            is TABLEROW -> row.offsetTableRow()
                            is ODXLINK -> {
                                val resolved =
                                    odx.resolveTableRow(row)
                                        ?: odxlinkFailure("TABLE-ROW", row)
                                resolved.offsetTableRow()
                            }
                            else -> error("Unsupported row type ${row.javaClass.simpleName}")
                        }
                    }.toIntArray()
                    .let {
                        TableDop.createRowsVector(builder, it)
                    }

            val diagCommConnectors =
                this.tablediagcommconnectors
                    ?.tablediagcommconnector
                    ?.map {
                        it.offsetTableDiagCommConnector()
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

    private fun TABLEDIAGCOMMCONNECTOR.offsetTableDiagCommConnector(): Int =
        cachedWithPath {
            val semantic = this.semantic?.offsetString()

            val diagComm =
                if (this.diagcommref != null) {
                    val diagService = this.diagcommref?.let { odx.resolveDiagService(it) }
                    val ecuJob = this.diagcommref?.let { odx.resolveSingleEcuJob(it) }
                    if (diagService == null && ecuJob == null) {
                        odxlinkFailure("DIAG-SERVICE / SINGLE-ECU-JOB", this.diagcommref)
                    } else if (diagService != null) {
                        diagService.offsetDiagService() to DiagServiceOrJob.DiagService
                    } else if (ecuJob != null) {
                        ecuJob.offsetSingleEcuJob() to DiagServiceOrJob.SingleEcuJob
                    } else {
                        error("Invalid state, no diagService or SingleEcuJob")
                    }
                } else if (this.diagcommsnref != null) {
                    val shortName = this.diagcommsnref.shortname
                    val coll = collectionOf(this)
                    val diagService = coll.resolveDiagServiceByShortName(shortName)
                    val ecuJob = coll.resolveSingleEcuJobByShortName(shortName)
                    if (diagService != null) {
                        diagService.offsetDiagService() to DiagServiceOrJob.DiagService
                    } else if (ecuJob != null) {
                        ecuJob.offsetSingleEcuJob() to DiagServiceOrJob.SingleEcuJob
                    } else {
                        snrefFailure(
                            "DIAG-SERVICE/SINGLE-ECU-JOB",
                            shortName,
                            this,
                            coll.diagServicesByShortName.keys + coll.singleEcuJobsByShortName.keys,
                        )
                    }
                } else {
                    error("Empty Diag Comm Connector $this")
                }

            TableDiagCommConnector.startTableDiagCommConnector(builder)
            semantic?.let { TableDiagCommConnector.addSemantic(builder, it) }
            TableDiagCommConnector.addDiagComm(builder, diagComm.first)
            TableDiagCommConnector.addDiagCommType(builder, diagComm.second)
            TableDiagCommConnector.endTableDiagCommConnector(builder)
        }

    private fun schema.odx.SD.offsetSD(): Int =
        cachedWithPath {
            val value = this.value?.offsetString()
            val si = this.si?.offsetString()
            val ti = this.ti?.offsetString()

            SD.startSD(builder)

            value?.let { SD.addValue(builder, it) }
            si?.let { SD.addSi(builder, it) }
            ti?.let { SD.addTi(builder, it) }

            SD.endSD(builder)
        }

    private fun schema.odx.SDG.offsetSDG(): Int =
        cachedWithPath {
            val si = this.si?.offsetString()

            val caption =
                this.sdgcaption?.shortname?.offsetString() ?: this.sdgcaptionref
                    ?.let {
                        val sdgCaption =
                            odx.resolveSdgCaption(it) ?: odxlinkFailure("SDG-CAPTION", it)
                        sdgCaption.shortname
                    }?.offsetString()

            val sdg =
                this.sdgOrSD
                    ?.map {
                        val sdOrSdg =
                            when (it) {
                                is schema.odx.SD -> it.offsetSD()
                                is schema.odx.SDG -> it.offsetSDG()
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

    private fun schema.odx.SDGS.offsetSDGS(): Int =
        cachedWithPath {
            val sdgs = SDGS.createSdgsVector(builder, this.sdg.map { it.offsetSDG() }.toIntArray())

            SDGS.startSDGS(builder)
            SDGS.addSdgs(builder, sdgs)
            SDGS.endSDGS(builder)
        }

    private fun REQUEST.offsetRequest(): Int =
        cachedWithPath {
            val sdgs = this.sdgs?.offsetSDGS()
            val params =
                this.params
                    ?.param
                    ?.map {
                        it.offsetParam()
                    }?.toIntArray()
                    ?.let {
                        Request.createParamsVector(builder, it)
                    }

            Request.startRequest(builder)
            sdgs?.let { Request.addSdgs(builder, it) }
            params?.let { Request.addParams(builder, it) }
            Request.endRequest(builder)
        }

    private fun RESPONSE.offsetResponse(): Int =
        cachedWithPath {
            val sdgs = this.sdgs?.offsetSDGS()
            val params =
                this.params
                    ?.param
                    ?.map {
                        it.offsetParam()
                    }?.toIntArray()
                    ?.let {
                        Response.createParamsVector(builder, it)
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

    private fun PARAM.offsetParam(): Int =
        this.cachedWithPath {
            try {
                val shortName = this.shortname.offsetString()
                val semantic = this.semantic?.offsetString()
                val sdgs = this.sdgs?.offsetSDGS()

                val specificData =
                    when (this) {
                        is VALUE -> {
                            val dop =
                                this.dopsnref?.shortname?.let {
                                    val dop =
                                        collectionOf(this).resolveDopByShortName(it)
                                            ?: snrefFailure(
                                                "DATA-OBJECT-PROP",
                                                it,
                                                this,
                                                collectionOf(this).dataObjectPropsByShortName.keys,
                                            )
                                    dop.offsetDOP()
                                } ?: this.dopref?.let {
                                    val dop =
                                        odx.resolveCombinedDop(it)
                                            ?: odxlinkFailure("DOP", it)
                                    dop.offsetDOP()
                                }
                            val physicalDefaultValue = this.physicaldefaultvalue?.offsetString()

                            Value.startValue(builder)
                            dop?.let { Value.addDop(builder, it) }
                            physicalDefaultValue?.let { Value.addPhysicalDefaultValue(builder, it) }
                            Value.endValue(builder)
                        }

                        is CODEDCONST -> {
                            val diagCodedType = this.diagcodedtype.offsetDiagCodedType()
                            val codedValue = this.codedvalue?.offsetString()

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
                            val dop =
                                this.dopsnref?.shortname?.let {
                                    val dop =
                                        collectionOf(this).resolveDopByShortName(it)
                                            ?: snrefFailure(
                                                "DATA-OBJECT-PROP",
                                                it,
                                                this,
                                                collectionOf(this).dataObjectPropsByShortName.keys,
                                            )
                                    dop.offsetDOP()
                                } ?: this.dopref?.let {
                                    val dop =
                                        odx.resolveCombinedDop(it)
                                            ?: odxlinkFailure("DOP", it)
                                    dop.offsetDOP()
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
                            val diagCodedType = this.diagcodedtype?.offsetDiagCodedType()
                            val codedValues =
                                this.codedvalues?.codedvalue?.map { it.offsetString() }?.toIntArray()?.let {
                                    NrcConst.createCodedValuesVector(builder, it)
                                }

                            NrcConst.startNrcConst(builder)
                            diagCodedType?.let { NrcConst.addDiagCodedType(builder, it) }
                            codedValues?.let { NrcConst.addCodedValues(builder, it) }
                            NrcConst.endNrcConst(builder)
                        }

                        is PHYSCONST -> {
                            val physConstValue = this.physconstantvalue?.offsetString()
                            val dop =
                                this.dopsnref?.shortname?.let {
                                    val dop =
                                        collectionOf(this).resolveDopByShortName(it)
                                            ?: snrefFailure(
                                                "DATA-OBJECT-PROP",
                                                it,
                                                this,
                                                collectionOf(this).dataObjectPropsByShortName.keys,
                                            )
                                    dop.offsetDOP()
                                } ?: this.dopref?.let {
                                    val dop =
                                        odx.resolveCombinedDop(it)
                                            ?: odxlinkFailure("DOP", it)
                                    dop.offsetDOP()
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
                            val sysParam = this.sysparam.offsetString()
                            val dop =
                                this.dopsnref?.shortname?.let {
                                    val dop =
                                        collectionOf(this).resolveDopByShortName(it)
                                            ?: snrefFailure(
                                                "DATA-OBJECT-PROP",
                                                it,
                                                this,
                                                collectionOf(this).dataObjectPropsByShortName.keys,
                                            )
                                    dop.offsetDOP()
                                } ?: this.dopref?.let {
                                    val dop =
                                        odx.resolveCombinedDop(it)
                                            ?: odxlinkFailure("DOP", it)
                                    dop.offsetDOP()
                                }

                            dataformat.System.startSystem(builder)

                            dataformat.System.addSysParam(builder, sysParam)
                            dop?.let { dataformat.System.addDop(builder, it) }

                            dataformat.System.endSystem(builder)
                        }

                        is TABLEKEY -> {
                            val firstEntry =
                                this.rest.firstOrNull()
                                    ?: error("TABLE-KEY ${this.id} has no entries")
                            if (this.rest.size > 1) {
                                if (!options.lenient) {
                                    error("TABLE-KEY ${this.id} has more than one entry, which is not supported in the file format")
                                } else {
                                    logger.warning(
                                        "TABLE-KEY ${this.id} has more than one entry, which is not supported in the file format. Only the first entry will be used.",
                                    )
                                }
                            }
                            val entry = firstEntry.value
                            var tableKeyReference: Int
                            var tableKeyReferenceType: UByte
                            if (entry is ODXLINK) {
                                val table = odx.resolveTable(entry)
                                if (table == null) {
                                    val row =
                                        odx.resolveTableRow(entry)
                                            ?: odxlinkFailure("TABLE / TABLE-ROW", entry)
                                    tableKeyReference = row.offsetTableRow()
                                    tableKeyReferenceType = TableKeyReference.TableRow
                                } else {
                                    tableKeyReference = table.offsetTableDop()
                                    tableKeyReferenceType = TableKeyReference.TableDop
                                }
                            } else if (entry is SNREF) {
                                // SNREFs are local-scope references (ODX spec §7.3.5): they
                                // resolve within the diag-layer that contains the TABLE-KEY,
                                // not across files. collectionOf(this) gives the owning
                                // ODXCollection; no cross-file fallback is attempted, unlike
                                // the ODXLINK path above which uses docref-aware resolution.
                                val elementName = firstEntry.name.localPart
                                val collection = collectionOf(this)
                                if (elementName == "TABLE-SNREF") {
                                    val table =
                                        collection.resolveTableByShortName(entry.shortname)
                                            ?: snrefFailure("TABLE", entry.shortname, this, collection.tablesByShortName.keys)
                                    tableKeyReference = table.offsetTableDop()
                                    tableKeyReferenceType = TableKeyReference.TableDop
                                } else if (elementName == "TABLE-ROW-SNREF") {
                                    val row =
                                        collection.resolveTableRowByShortName(entry.shortname)
                                            ?: snrefFailure("TABLE-ROW", entry.shortname, this, collection.tableRowsByShortName.keys)
                                    tableKeyReference = row.offsetTableRow()
                                    tableKeyReferenceType = TableKeyReference.TableRow
                                } else {
                                    error("Unexpected SNREF element name '$elementName' for TABLE-KEY ${this.id}")
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
                            val param = (this as PARAM).offsetParam()
                            val target = this.target?.toFileFormatEnum()
                            val tableRow =
                                this.tablerowref?.let {
                                    val row =
                                        odx.resolveTableRow(it) ?: odxlinkFailure("TABLE-ROW", it)
                                    row.offsetTableRow()
                                }

                            TableEntry.startTableEntry(builder)
                            TableEntry.addParam(builder, param)
                            target?.let { TableEntry.addTarget(builder, it) }
                            tableRow?.let { TableEntry.addTableRow(builder, it) }
                            TableEntry.endTableEntry(builder)
                        }

                        is TABLESTRUCT -> {
                            val tableKey =
                                this.tablekeysnref?.shortname?.let { shortName ->
                                    collectionOf(this)
                                        .tableKeys.values
                                        .firstOrNull { it.shortname == shortName }
                                        ?.offsetParam()
                                        ?: snrefFailure(
                                            "TABLE-KEY",
                                            shortName,
                                            this,
                                            collectionOf(this).tableKeys.values.map { it.shortname },
                                        )
                                } ?: (
                                    odx.resolveTableKey(this.tablekeyref)?.offsetParam()
                                        ?: odxlinkFailure("TABLE-KEY", this.tablekeyref)
                                )

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

    private fun FUNCTCLASS.offsetFunctClass(): Int =
        this.cachedWithPath {
            val shortname = this.shortname.offsetString()

            FunctClass.startFunctClass(builder)
            FunctClass.addShortName(builder, shortname)
            FunctClass.endFunctClass(builder)
        }

    private fun STANDARDLENGTHTYPE.toStandardLengthType(): Int {
        val bitmask = this.bitmask?.offsetByteArray()

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
            odx.resolveLengthKey(this.lengthkeyref)?.offsetParam()
                ?: odxlinkFailure("LENGTH-KEY", this.lengthkeyref)

        ParamLengthInfoType.startParamLengthInfoType(builder)
        ParamLengthInfoType.addLengthKey(builder, lengthKey)
        return ParamLengthInfoType.endParamLengthInfoType(builder)
    }

    private fun DATAOBJECTPROP.toNormalDop(): Int {
        val diagCodedType = this.diagcodedtype?.offsetDiagCodedType()
        val unit =
            this.unitref?.let {
                val unit = odx.resolveUnit(it) ?: odxlinkFailure("UNIT", it)
                unit.offsetUnit()
            }
        val physicalType = this.physicaltype?.offsetPhysicalType()
        val compuMethod = this.compumethod?.offsetCompuMethod()
        val internalConstr = this.internalconstr?.offsetInternalConstr()
        val physConstr = this.physconstr?.offsetInternalConstr()

        NormalDOP.startNormalDOP(builder)
        diagCodedType?.let { NormalDOP.addDiagCodedType(builder, it) }
        unit?.let { NormalDOP.addUnitRef(builder, it) }
        physicalType?.let { NormalDOP.addPhysicalType(builder, it) }
        compuMethod?.let { NormalDOP.addCompuMethod(builder, it) }
        internalConstr?.let { NormalDOP.addInternalConstr(builder, it) }
        physConstr?.let { NormalDOP.addPhysConstr(builder, it) }

        return NormalDOP.endNormalDOP(builder)
    }

    private fun DIAGCODEDTYPE.offsetDiagCodedType(): Int =
        cachedWithPath {
            val baseTypeEncoding = this.basetypeencoding?.offsetString()

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

    private fun UNIT.offsetUnit(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val displayName = this.displayname.offsetString()
            val physicaldimension =
                this.physicaldimensionref?.let { ref ->
                    val physDimension =
                        odx.resolvePhysDimension(ref)
                            ?: odxlinkFailure("PHYSICAL-DIMENSION", ref)
                    physDimension.offsetPhysicalDimension()
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
            this.basicstructuresnref?.shortname?.let {
                val dop =
                    collectionOf(this).resolveStructureByShortName(it)
                        ?: snrefFailure("BASIC-STRUCTURE", it, this, collectionOf(this).structuresByShortName.keys)
                dop.offsetDOP()
            } ?: this.basicstructureref?.let {
                val dop =
                    odx.resolveCombinedDop(it)
                        ?: odxlinkFailure("DOP (BASIC-STRUCTURE)", it)
                dop.offsetDOP()
            }
        val envDataRef =
            this.envdatadescsnref?.shortname?.let {
                val dop =
                    collectionOf(this).resolveEnvDataDescByShortName(it)
                        ?: snrefFailure("ENV-DATA-DESC", it, this, collectionOf(this).envDataDescsByShortName.keys)
                dop.offsetDOP()
            } ?: this.envdatadescref?.let {
                val dop =
                    odx.resolveCombinedDop(it)
                        ?: odxlinkFailure("DOP (ENV-DATA-DESC)", it)
                dop.offsetDOP()
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
            this.params?.param?.map { it.offsetParam() }?.toIntArray()?.let {
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
                        odx.resolveEnvData(it) ?: odxlinkFailure("ENV-DATA", it)
                    envData.offsetDOP()
                }?.toIntArray()
                ?.let {
                    EnvDataDesc.createEnvDatasVector(builder, it)
                }

        val paramShortName = this.paramsnref?.shortname?.offsetString()
        val paramShortNamePath = this.paramsnpathref?.shortnamepath?.offsetString()

        EnvDataDesc.startEnvDataDesc(builder)

        envDatas?.let { EnvDataDesc.addEnvDatas(builder, it) }

        paramShortName?.let { EnvDataDesc.addParamShortName(builder, it) }
        paramShortNamePath?.let { EnvDataDesc.addParamPathShortName(builder, it) }

        return EnvDataDesc.endEnvDataDesc(builder)
    }

    private fun PHYSICALTYPE.offsetPhysicalType(): Int =
        cachedWithPath {
            PhysicalType.startPhysicalType(builder)

            PhysicalType.addBaseDataType(builder, this.basedatatype.toFileFormatEnum())
            this.precision?.let { PhysicalType.addPrecision(builder, it.toUInt()) }
            this.displayradix?.let { PhysicalType.addDisplayRadix(builder, it.toFileFormatEnum()) }

            PhysicalType.endPhysicalType(builder)
        }

    private fun SCALECONSTR.offsetScaleConstr(): Int =
        cachedWithPath {
            val shortLabel = this.shortlabel?.offsetText()
            val lowerLimit = this.lowerlimit?.offsetLimit()
            val upperLimit = this.upperlimit?.offsetLimit()

            ScaleConstr.startScaleConstr(builder)
            shortLabel?.let { ScaleConstr.addShortLabel(builder, it) }
            lowerLimit?.let { ScaleConstr.addLowerLimit(builder, it) }
            upperLimit?.let { ScaleConstr.addUpperLimit(builder, it) }
            ScaleConstr.addValidity(builder, this.validity.toFileFormatEnum())
            ScaleConstr.endScaleConstr(builder)
        }

    private fun INTERNALCONSTR.offsetInternalConstr(): Int =
        cachedWithPath {
            val lowerLimit = this.lowerlimit?.offsetLimit()
            val upperLimit = this.upperlimit?.offsetLimit()
            val scaleConstrs =
                this.scaleconstrs
                    ?.scaleconstr
                    ?.map { it.offsetScaleConstr() }
                    ?.toIntArray()
                    ?.let {
                        InternalConstr.createScaleConstrVector(builder, it)
                    }

            InternalConstr.startInternalConstr(builder)
            lowerLimit?.let { InternalConstr.addLowerLimit(builder, it) }
            upperLimit?.let { InternalConstr.addUpperLimit(builder, it) }
            scaleConstrs?.let { InternalConstr.addScaleConstr(builder, it) }
            InternalConstr.endInternalConstr(builder)
        }

    private fun schema.odx.DTC.offsetDTC(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val displayTroubleCode = this.displaytroublecode?.offsetString()
            val text = this.text?.offsetText()
            val sdgs = this.sdgs?.offsetSDGS()

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

    private fun LONGNAME.offsetLongName(): Int =
        cachedWithPath {
            val tiOffset = this.ti?.offsetString()
            val valueOffset = this.value?.offsetString()

            LongName.startLongName(builder)
            tiOffset?.let { LongName.addTi(builder, tiOffset) }
            valueOffset?.let { LongName.addValue(builder, valueOffset) }
            LongName.endLongName(builder)
        }

    private fun TEXT.offsetText(): Int =
        cachedWithPath {
            val ti = this.ti?.offsetString()
            val value = this.value?.offsetString()

            Text.startText(builder)

            ti?.let { Text.addTi(builder, it) }
            value?.let { Text.addValue(builder, it) }

            Text.endText(builder)
        }

    private fun COMPUMETHOD.offsetCompuMethod(): Int =
        cachedWithPath {
            val internalToPhys = this.compuinternaltophys?.offsetCompuInternalToPhys()
            val physToInternal = this.compuphystointernal?.offsetCompuPhysToInternal()

            CompuMethod.startCompuMethod(builder)

            this.category?.let { CompuMethod.addCategory(builder, it.toFileFormatEnum()) }
            internalToPhys?.let { CompuMethod.addInternalToPhys(builder, it) }
            physToInternal?.let { CompuMethod.addPhysToInternal(builder, it) }
            CompuMethod.endCompuMethod(builder)
        }

    private fun LIBRARY.offsetLibrary(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val codeFile = this.codefile.offsetString()
            val encryption = this.encryption?.offsetString()
            val syntax = this.syntax.offsetString()
            val entrypoint = this.entrypoint?.offsetString()

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

    private fun PROGCODE.offsetProgCode(): Int =
        cachedWithPath {
            val codeFile = this.codefile?.offsetString()
            val encryption = this.encryption?.offsetString()
            val syntax = this.syntax?.offsetString()
            val revision = this.revision?.offsetString()
            val entrypoint = this.entrypoint?.offsetString()
            val libraries =
                this.libraryrefs
                    ?.libraryref
                    ?.map { ref ->
                        val library =
                            odx.resolveLibrary(ref)
                                ?: odxlinkFailure("LIBRARY", ref)
                        library.offsetLibrary()
                    }?.toIntArray()
                    ?.let {
                        ProgCode.createLibraryVector(builder, it)
                    }

            ProgCode.startProgCode(builder)
            codeFile?.let { ProgCode.addCodeFile(builder, it) }
            encryption?.let { ProgCode.addEncryption(builder, it) }
            syntax?.let { ProgCode.addSyntax(builder, it) }
            revision?.let { ProgCode.addRevision(builder, it) }
            entrypoint?.let { ProgCode.addEntrypoint(builder, it) }
            libraries?.let { ProgCode.addLibrary(builder, it) }

            ProgCode.endProgCode(builder)
        }

    private fun COMPUPHYSTOINTERNAL.offsetCompuPhysToInternal(): Int =
        cachedWithPath {
            val progcode = this.progcode?.offsetProgCode()
            val compuscales =
                this.compuscales?.compuscale?.map { it.offsetCompuScale() }?.toIntArray()?.let {
                    CompuPhysToInternal.createCompuScalesVector(builder, it)
                }
            val compudefaultvalue = this.compudefaultvalue?.offsetCompuDefaultValue()

            CompuPhysToInternal.startCompuPhysToInternal(builder)
            progcode?.let { CompuPhysToInternal.addProgCode(builder, it) }
            compuscales?.let { CompuPhysToInternal.addCompuScales(builder, it) }
            compudefaultvalue?.let { CompuPhysToInternal.addCompuDefaultValue(builder, it) }
            CompuPhysToInternal.endCompuPhysToInternal(builder)
        }

    private fun COMPUINTERNALTOPHYS.offsetCompuInternalToPhys(): Int =
        cachedWithPath {
            val progcode = this.progcode?.offsetProgCode()
            val compuscales =
                this.compuscales?.compuscale?.map { it.offsetCompuScale() }?.toIntArray()?.let {
                    CompuInternalToPhys.createCompuScalesVector(builder, it)
                }
            val compudefaultvalue = this.compudefaultvalue?.offsetCompuDefaultValue()

            CompuInternalToPhys.startCompuInternalToPhys(builder)
            progcode?.let { CompuInternalToPhys.addProgCode(builder, it) }
            compuscales?.let { CompuInternalToPhys.addCompuScales(builder, it) }
            compudefaultvalue?.let { CompuInternalToPhys.addCompuDefaultValue(builder, it) }
            CompuInternalToPhys.endCompuInternalToPhys(builder)
        }

    private fun COMPUSCALE.offsetCompuScale(): Int =
        cachedWithPath {
            val shortLabel = this.shortlabel?.offsetText()
            val lowerLimit = this.lowerlimit?.offsetLimit()
            val upperLimit = this.upperlimit?.offsetLimit()
            val compuInverseValue = this.compuinversevalue?.offsetCompuValuesInverseValue()
            val compuConst = this.compuconst?.offsetCompuValuesConst()
            val rationalCoEffs = this.compurationalcoeffs?.offsetCompuRationalCoEffs()

            CompuScale.startCompuScale(builder)
            shortLabel?.let { CompuScale.addShortLabel(builder, it) }
            lowerLimit?.let { CompuScale.addLowerLimit(builder, it) }
            upperLimit?.let { CompuScale.addUpperLimit(builder, it) }
            compuInverseValue?.let { CompuScale.addInverseValues(builder, it) }
            compuConst?.let { CompuScale.addConsts(builder, it) }
            rationalCoEffs?.let { CompuScale.addRationalCoEffs(builder, it) }
            CompuScale.endCompuScale(builder)
        }

    private fun LIMIT.offsetLimit(): Int =
        cachedWithPath {
            val value = this.value?.offsetString()

            Limit.startLimit(builder)
            value?.let { Limit.addValue(builder, value) }
            this.intervaltype?.let { Limit.addIntervalType(builder, it.toFileFormatEnum()) }
            Limit.endLimit(builder)
        }

    private fun COMPUINVERSEVALUE.offsetCompuValuesInverseValue(): Int =
        cachedWithPath {
            val vtValue = this.vt?.value?.offsetString()
            val vtTi = this.vt?.ti?.offsetString()

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

    private fun COMPUCONST.offsetCompuValuesConst(): Int =
        cachedWithPath {
            val vtValue = this.vt?.value?.offsetString()
            val vtTi = this.vt?.ti?.offsetString()

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

    private fun COMPUDEFAULTVALUE.offsetCompuDefaultValue(): Int =
        cachedWithPath {
            val vtTi = this.vt?.ti?.offsetString()
            val vtValue = this.vt?.value?.offsetString()
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
                    ?.offsetString()
            val invVtValue =
                this.compuinversevalue
                    ?.vt
                    ?.value
                    ?.offsetString()
            val inverseValues =
                if (invVtTi != null || invVtValue != null || this.compuinversevalue?.v?.value != null) {
                    CompuValues.startCompuValues(builder)
                    this.compuinversevalue?.v?.value?.let {
                        CompuValues.addV(builder, it)
                    }
                    invVtValue?.let {
                        CompuValues.addVt(builder, it)
                    }
                    invVtTi?.let {
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

    private fun COMPURATIONALCOEFFS.offsetCompuRationalCoEffs(): Int =
        cachedWithPath {
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
        val diagCodedType = this.diagcodedtype.offsetDiagCodedType()
        val physicalType = this.physicaltype.offsetPhysicalType()
        val compuMethod = this.compumethod.offsetCompuMethod()

        val dtcs =
            this.dtcs.dtcproxy
                ?.map {
                    if (it is schema.odx.DTC) {
                        it.offsetDTC()
                    } else if (it is ODXLINK) {
                        val dop = odx.resolveDtc(it) ?: odxlinkFailure("DTC", it)
                        dop.offsetDTC()
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
        val switchKey = this.switchkey.offsetSwitchKey()
        val defaultCase = this.defaultcase?.offsetDefaultCase()
        val cases =
            this.cases
                ?.case
                ?.map { it.offsetCase() }
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
        val determineNumberOfItems = this.determinenumberofitems.offsetDetermineNumberOfItems()

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
                    it.offsetParam()
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

    private fun DETERMINENUMBEROFITEMS.offsetDetermineNumberOfItems(): Int =
        cachedWithPath {
            val dop =
                this.dataobjectpropref?.let {
                    val dop =
                        odx.resolveCombinedDop(it)
                            ?: odxlinkFailure("DOP", it)
                    dop.offsetDOP()
                }

            DetermineNumberOfItems.startDetermineNumberOfItems(builder)
            DetermineNumberOfItems.addBytePosition(builder, this.byteposition.toUInt())
            this.bitposition?.let { DetermineNumberOfItems.addBitPosition(builder, it.toUInt()) }
            dop?.let { DetermineNumberOfItems.addDop(builder, it) }
            DetermineNumberOfItems.endDetermineNumberOfItems(builder)
        }

    private fun ADDITIONALAUDIENCE.offsetAdditionalAudience(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()

            AdditionalAudience.startAdditionalAudience(builder)

            AdditionalAudience.addShortName(builder, shortName)
            longName?.let {
                AdditionalAudience.addLongName(builder, it)
            }

            AdditionalAudience.endAdditionalAudience(builder)
        }

    private fun AUDIENCE.offsetAudience(): Int =
        cachedWithPath {
            val enabledAudiences =
                this.enabledaudiencerefs?.enabledaudienceref?.let { aa ->
                    Audience.createEnabledAudiencesVector(
                        builder,
                        aa
                            .map {
                                val aud =
                                    odx.resolveAdditionalAudience(it)
                                        ?: odxlinkFailure("ADDITIONAL-AUDIENCE", it)
                                aud.offsetAdditionalAudience()
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
                                    odx.resolveAdditionalAudience(it)
                                        ?: odxlinkFailure("ADDITIONAL-AUDIENCE", it)
                                aud.offsetAdditionalAudience()
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

    private fun PRECONDITIONSTATEREF.offsetPreConditionStateRef(): Int =
        cachedWithPath {
            val state =
                odx.resolveState(this)?.offsetState() ?: odxlinkFailure("STATE", this, this.idref, this.docref)
            val value = this.value?.offsetString()
            val inParamIfSnRef = this.inparamifsnref?.shortname?.offsetString()
            val inParamIfSnPathRef = this.inparamifsnpathref?.shortnamepath?.offsetString()

            PreConditionStateRef.startPreConditionStateRef(builder)
            PreConditionStateRef.addState(builder, state)
            value?.let { PreConditionStateRef.addValue(builder, it) }
            inParamIfSnRef?.let { PreConditionStateRef.addInParamIfShortName(builder, it) }
            inParamIfSnPathRef?.let { PreConditionStateRef.addInParamPathShortName(builder, it) }
            PreConditionStateRef.endPreConditionStateRef(builder)
        }

    private fun STATETRANSITIONREF.offsetStateTransitionRef(): Int =
        cachedWithPath {
            val value = this.value?.offsetString()
            val stateTransition =
                this.idref?.let {
                    val stateTransition =
                        odx.resolveStateTransition(this)
                            ?: odxlinkFailure("STATE-TRANSITION", this, this.idref!!, this.docref)
                    stateTransition.offsetStateTransition()
                }

            StateTransitionRef.startStateTransitionRef(builder)

            value?.let { StateTransitionRef.addValue(builder, it) }
            stateTransition?.let { StateTransitionRef.addStateTransition(builder, it) }

            StateTransitionRef.endStateTransitionRef(builder)
        }

    private fun INPUTPARAM.offsetJobParamInput(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val physicalDefaultValue = this.physicaldefaultvalue?.offsetString()
            val semantic = this.semantic?.offsetString()
            val dop =
                this.dopbaseref?.let {
                    val dop =
                        odx.resolveCombinedDop(it)
                            ?: odxlinkFailure("DOP", it)
                    dop.offsetDOP()
                }

            JobParam.startJobParam(builder)
            JobParam.addShortName(builder, shortName)
            longName?.let { JobParam.addLongName(builder, it) }
            physicalDefaultValue?.let { JobParam.addPhysicalDefaultValue(builder, it) }
            semantic?.let { JobParam.addSemantic(builder, it) }
            dop?.let { JobParam.addDopBase(builder, it) }
            JobParam.endJobParam(builder)
        }

    private fun OUTPUTPARAM.offsetJobParamOutput(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val semantic = this.semantic?.offsetString()
            val dop =
                this.dopbaseref?.let {
                    val dop =
                        odx.resolveCombinedDop(it)
                            ?: odxlinkFailure("DOP", it)
                    dop.offsetDOP()
                }

            JobParam.startJobParam(builder)
            JobParam.addShortName(builder, shortName)
            longName?.let { JobParam.addLongName(builder, it) }
            semantic?.let { JobParam.addSemantic(builder, it) }
            dop?.let { JobParam.addDopBase(builder, it) }
            JobParam.endJobParam(builder)
        }

    private fun NEGOUTPUTPARAM.offsetJobParamNegOutput(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val dop =
                this.dopbaseref?.let {
                    val dop =
                        odx.resolveCombinedDop(it)
                            ?: odxlinkFailure("DOP", it)
                    dop.offsetDOP()
                }

            JobParam.startJobParam(builder)
            JobParam.addShortName(builder, shortName)
            longName?.let { JobParam.addLongName(builder, it) }
            dop?.let { JobParam.addDopBase(builder, it) }
            JobParam.endJobParam(builder)
        }

    private fun DIAGCOMM.offsetInternal(): Int {
        val shortName = this.shortname.offsetString()
        val longName = this.longname?.offsetLongName()
        val diagClass = this.diagnosticclass?.toFileFormatEnum()
        val functClasses =
            this.functclassrefs
                ?.functclassref
                ?.map {
                    val functClass =
                        odx.resolveFunctClass(it)
                            ?: odxlinkFailure("FUNCT-CLASS", it)
                    functClass.offsetFunctClass()
                }?.toIntArray()
                ?.let {
                    DiagComm.createFunctClassVector(builder, it)
                }
        val semantic = this.semantic?.offsetString()
        val preconditionStateRefs =
            this.preconditionstaterefs
                ?.preconditionstateref
                ?.map {
                    it.offsetPreConditionStateRef()
                }?.toIntArray()
                ?.let {
                    DiagComm.createPreConditionStateRefsVector(builder, it)
                }
        val stateTransitionRefs =
            this.statetransitionrefs
                ?.statetransitionref
                ?.map {
                    it.offsetStateTransitionRef()
                }?.toIntArray()
                ?.let {
                    DiagComm.createStateTransitionRefsVector(builder, it)
                }
        val protocolRefs =
            this.protocolsnrefs
                ?.protocolsnref
                ?.map {
                    val protocol =
                        odx.resolveProtocolByShortName(it.shortname)
                            ?: snrefFailure("PROTOCOL", it.shortname, this, odx.protocols.map { it.shortname })
                    protocol.offsetProtocol()
                }?.toIntArray()
                ?.let {
                    DiagComm.createProtocolsVector(builder, it)
                }
        val audience = this.audience?.offsetAudience()
        val sdgs = this.sdgs?.offsetSDGS()

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

    private fun SINGLEECUJOB.offsetSingleEcuJob(): Int =
        this.cachedWithPath {
            val diagComm = (this as DIAGCOMM).offsetInternal()
            val progCodes =
                this.progcodes
                    ?.progcode
                    ?.map {
                        it.offsetProgCode()
                    }?.toIntArray()
                    ?.let {
                        SingleEcuJob.createProgCodesVector(builder, it)
                    }
            val inputParams =
                this.inputparams
                    ?.inputparam
                    ?.map {
                        it.offsetJobParamInput()
                    }?.toIntArray()
                    ?.let {
                        SingleEcuJob.createInputParamsVector(builder, it)
                    }
            val outputParams =
                this.outputparams
                    ?.outputparam
                    ?.map {
                        it.offsetJobParamOutput()
                    }?.toIntArray()
                    ?.let {
                        SingleEcuJob.createOutputParamsVector(builder, it)
                    }
            val negOutputParams =
                this.negoutputparams
                    ?.negoutputparam
                    ?.map {
                        it.offsetJobParamNegOutput()
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
        val shortName = this.shortname.offsetString()
        val longName = this.longname?.offsetLongName()
        val sdgs = this.sdgs?.offsetSDGS()
        val functClasses =
            this.functclasss
                ?.functclass
                ?.map {
                    it.offsetFunctClass()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createFunctClassesVector(builder, it)
                }
        val additionalAudiences =
            this.additionalaudiences
                ?.additionalaudience
                ?.map {
                    it.offsetAdditionalAudience()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createAdditionalAudiencesVector(builder, it)
                }
        val resolvedLinks: List<DIAGCOMM> =
            this.diagcomms
                ?.diagcommproxy
                ?.filterIsInstance<ODXLINK>()
                ?.map {
                    odx.resolveDiagService(it) ?: odx.resolveSingleEcuJob(it)
                        ?: odxlinkFailure("DIAG-SERVICE / SINGLE-ECU-JOB", it)
                }?.filterByConverterOptions(options) ?: emptyList()

        val diagServicesRaw =
            resolvedLinks.filterIsInstance<DIAGSERVICE>().map {
                it.offsetDiagService()
            } + (
                this.diagcomms
                    ?.diagcommproxy
                    ?.filterIsInstance<DIAGSERVICE>()
                    ?.filterByConverterOptions(options)
                    ?.map {
                        it.offsetDiagService()
                    } ?: emptyList()
            )

        val diagServices =
            diagServicesRaw.toIntArray().let {
                DiagLayer.createDiagServicesVector(builder, it)
            }

        val singleEcuJobsRaw =
            resolvedLinks.filterIsInstance<SINGLEECUJOB>().map {
                it.offsetSingleEcuJob()
            } + (
                this.diagcomms
                    ?.diagcommproxy
                    ?.filterIsInstance<SINGLEECUJOB>()
                    ?.filterByConverterOptions(options)
                    ?.map {
                        it.offsetSingleEcuJob()
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
                    it.offsetStateChart()
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
                    it.offsetComParamRef()
                }?.toIntArray()
                ?.let {
                    DiagLayer.createComParamRefsVector(builder, it)
                }

        val diagLayer = (this as DIAGLAYER).offsetInternal(comParamRefs)
        return diagLayer
    }

    private fun BASEVARIANT.offsetVariantBase(): Int =
        this.cachedWithPath {
            val diagLayer = (this as HIERARCHYELEMENT).offsetType()
            val pattern =
                this.basevariantpattern
                    ?.matchingbasevariantparameters
                    ?.matchingbasevariantparameter
                    ?.map {
                        it.offsetMatchingParameterBase()
                    }?.toIntArray()
                    ?.let {
                        Variant.createVariantPatternVector(builder, it)
                    }
            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map {
                        it.offsetParentRef()
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

    private fun ECUVARIANT.offsetVariantEcu(): Int =
        this.cachedWithPath {
            val diagLayer = (this as HIERARCHYELEMENT).offsetType()
            val pattern =
                this.ecuvariantpatterns
                    ?.ecuvariantpattern
                    ?.map {
                        it.offsetVariantPattern()
                    }?.toIntArray()
                    ?.let {
                        Variant.createVariantPatternVector(builder, it)
                    }
            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map {
                        it.offsetParentRef()
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

    private fun ECUSHAREDDATA.offsetEcuSharedData(): Int =
        this.cachedWithPath {
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

    fun PARENTREF.offsetParentRef(): Int {
        val resolved =
            odx.resolveParent(this) ?: odxlinkFailure("PARENT", this, this.idref, this.docref)
        val resolvedOffs =
            when (resolved) {
                is BASEVARIANT -> resolved.offsetVariantBase()
                is ECUVARIANT -> resolved.offsetVariantEcu()
                is PROTOCOL -> resolved.offsetProtocol()
                is TABLE -> resolved.offsetTableDop()
                is FUNCTIONALGROUP -> resolved.offsetFunctionalGroup()
                is ECUSHAREDDATA -> resolved.offsetEcuSharedData()
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
                    it.diagcommsnref.shortname.offsetString()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedDiagCommShortNamesVector(builder, it)
                }
        val notInheritedDopsShortNames =
            this.notinheriteddops
                ?.notinheriteddop
                ?.map {
                    it.dopbasesnref.shortname.offsetString()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedDopsShortNamesVector(builder, it)
                }
        val notInheritedTablesShortNames =
            this.notinheritedtables
                ?.notinheritedtable
                ?.map {
                    it.tablesnref.shortname.offsetString()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedTablesShortNamesVector(builder, it)
                }
        val notInheritedVariablesShortNames =
            this.notinheritedvariables
                ?.notinheritedvariable
                ?.map {
                    it.diagvariablesnref.shortname.offsetString()
                }?.toIntArray()
                ?.let {
                    ParentRef.createNotInheritedVariablesShortNamesVector(builder, it)
                }
        val notInheritedGlobalNegResponseShortNames =
            this.notinheritedglobalnegresponses
                ?.notinheritedglobalnegresponse
                ?.map {
                    it.globalnegresponsesnref.shortname.offsetString()
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

    private fun FUNCTIONALGROUP.offsetFunctionalGroup(): Int =
        this.cachedWithPath {
            val diagLayer = (this as HIERARCHYELEMENT).offsetType()
            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map {
                        it.offsetParentRef()
                    }?.toIntArray()
                    ?.let {
                        FunctionalGroup.createParentRefsVector(builder, it)
                    }

            FunctionalGroup.startFunctionalGroup(builder)
            FunctionalGroup.addDiagLayer(builder, diagLayer)
            parentRefs?.let { FunctionalGroup.addParentRefs(builder, it) }
            FunctionalGroup.endFunctionalGroup(builder)
        }

    private fun MATCHINGBASEVARIANTPARAMETER.offsetMatchingParameterBase(): Int =
        this.cachedWithPath {
            if (this.outparamifsnpathref != null) {
                error("Unsupported outparam if sn path ref")
            }

            val expectedValue = this.expectedvalue.offsetString()
            lateinit var diagService: DIAGSERVICE
            val diagServiceOffset =
                this.diagcommsnref.shortname.let { shortname ->
                    diagService = collectionOf(this).resolveDiagServiceByShortName(shortname)
                        ?: snrefFailure("DIAG-SERVICE", shortname, this, collectionOf(this).diagServicesByShortName.keys)
                    diagService.offsetDiagService()
                }

            val outParam =
                this.outparamifsnref?.shortname?.let { expectedShortName ->
                    val allParams =
                        diagService.posresponserefs
                            ?.posresponseref
                            ?.flatMap { pr ->
                                val posResponse =
                                    odx.resolvePosResponse(pr)
                                        ?: return@let null
                                posResponse.params?.param ?: emptyList()
                            } ?: emptyList()
                    allParams.firstOrNull { it.shortname == expectedShortName }?.offsetParam()
                        ?: posResponseParamFailure(expectedShortName, this, diagService, allParams)
                }

            MatchingParameter.startMatchingParameter(builder)
            MatchingParameter.addExpectedValue(builder, expectedValue)
            MatchingParameter.addDiagService(builder, diagServiceOffset)
            MatchingParameter.addUsePhysicalAddressing(builder, this.isUSEPHYSICALADDRESSING)
            outParam?.let { MatchingParameter.addOutParam(builder, it) }
            MatchingParameter.endMatchingParameter(builder)
        }

    private fun MATCHINGPARAMETER.offsetMatchingParameter(): Int =
        this.cachedWithPath {
            val expectedValue = this.expectedvalue?.offsetString()
            lateinit var diagService: DIAGSERVICE
            val diagServiceOffset =
                this.diagcommsnref.shortname.let { shortname ->
                    diagService = collectionOf(this).resolveDiagServiceByShortName(shortname)
                        ?: snrefFailure("DIAG-SERVICE", shortname, this, collectionOf(this).diagServicesByShortName.keys)
                    diagService.offsetDiagService()
                }
            val outParam =
                this.outparamifsnref?.shortname?.let { expectedShortName ->
                    val allParams =
                        diagService.posresponserefs
                            ?.posresponseref
                            ?.flatMap { pr ->
                                val posResponse =
                                    odx.resolvePosResponse(pr)
                                        ?: return@let null
                                posResponse.params?.param ?: emptyList()
                            } ?: emptyList()
                    allParams.firstOrNull { it.shortname == expectedShortName }?.offsetParam()
                        ?: posResponseParamFailure(expectedShortName, this, diagService, allParams)
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

    private fun ECUVARIANTPATTERN.offsetVariantPattern(): Int =
        this.cachedWithPath {
            val matchingParameter =
                this.matchingparameters
                    ?.matchingparameter
                    ?.map {
                        it.offsetMatchingParameter()
                    }?.toIntArray()
                    ?.let {
                        Variant.createVariantPatternVector(builder, it)
                    }
            VariantPattern.startVariantPattern(builder)
            matchingParameter?.let { VariantPattern.addMatchingParameter(builder, matchingParameter) }
            VariantPattern.endVariantPattern(builder)
        }

    private fun COMPARAMSUBSET.offsetComParamSubSet(): Int =
        cachedWithPath {
            val comParams =
                this.comparams
                    ?.comparam
                    ?.map {
                        it.offsetComParamSimple()
                    }?.toIntArray()
                    ?.let {
                        ComParamSubSet.createComParamsVector(builder, it)
                    }
            val complexComParams =
                this.complexcomparams
                    ?.complexcomparam
                    ?.map {
                        it.offsetComParamComplex()
                    }?.toIntArray()
                    ?.let {
                        ComParamSubSet.createComplexComParamsVector(builder, it)
                    }
            val dops =
                this.dataobjectprops
                    ?.dataobjectprop
                    ?.map {
                        it.offsetDOP()
                    }?.toIntArray()
                    ?.let {
                        ComParamSubSet.createDataObjectPropsVector(builder, it)
                    }
            val unitSpec = this.unitspec?.offsetUnitSpec()

            ComParamSubSet.startComParamSubSet(builder)
            comParams?.let { ComParamSubSet.addComParams(builder, it) }
            complexComParams?.let { ComParamSubSet.addComplexComParams(builder, it) }
            dops?.let { ComParamSubSet.addDataObjectProps(builder, it) }
            unitSpec?.let { ComParamSubSet.addUnitSpec(builder, it) }
            ComParamSubSet.endComParamSubSet(builder)
        }

    private fun UNITGROUP.offsetUnitGroup(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val units =
                this.unitrefs
                    ?.unitref
                    ?.map {
                        val unit = odx.resolveUnit(it) ?: odxlinkFailure("UNIT", it)
                        unit.offsetUnit()
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

    private fun UNITSPEC.offsetUnitSpec(): Int =
        cachedWithPath {
            val unitGroups =
                this.unitgroups?.unitgroup?.map { it.offsetUnitGroup() }?.toIntArray()?.let {
                    UnitSpec.createUnitGroupsVector(builder, it)
                }
            val physicalDimensions =
                this.physicaldimensions?.physicaldimension?.map { it.offsetPhysicalDimension() }?.toIntArray()?.let {
                    UnitSpec.createPhysicalDimensionsVector(builder, it)
                }
            val units =
                this.units
                    ?.unit
                    ?.map {
                        it.offsetUnit()
                    }?.toIntArray()
                    ?.let {
                        UnitSpec.createUnitsVector(builder, it)
                    }
            val sdgs = this.sdgs?.let { it.offsetSDGS() }

            UnitSpec.startUnitSpec(builder)
            unitGroups?.let { UnitSpec.addUnitGroups(builder, it) }
            physicalDimensions?.let { UnitSpec.addPhysicalDimensions(builder, it) }
            units?.let { UnitSpec.addUnits(builder, it) }
            sdgs?.let { UnitSpec.addSdgs(builder, it) }
            UnitSpec.endUnitSpec(builder)
        }

    private fun COMPARAM.offsetComParamSimple(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val paramClass = this.paramclass?.offsetString()
            val comParamType = this.cptype?.toFileFormatEnum()
            val comParamUsage = this.cpusage?.toFileFormatEnum()
            val displayLevel = this.displaylevel?.toUInt()

            val regularComParam =
                this.let {
                    val physicalDefaultValue = this.physicaldefaultvalue?.offsetString()
                    val dop =
                        this.dataobjectpropref?.let {
                            val dop =
                                odx.resolveCombinedDop(it)
                                    ?: odxlinkFailure("DOP", it)
                            dop.offsetDOP()
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

    private fun SIMPLEVALUE.offsetSimpleValue(): Int =
        cachedWithPath {
            val value = this.value?.offsetString()
            SimpleValue.startSimpleValue(builder)
            value?.let { SimpleValue.addValue(builder, it) }
            SimpleValue.endSimpleValue(builder)
        }

    private fun COMPLEXVALUE.offsetComplexValue(): Int =
        cachedWithPath {
            val entries =
                this.simplevalueOrCOMPLEXVALUE
                    ?.map {
                        when (it) {
                            is SIMPLEVALUE -> it.offsetSimpleValue()
                            is COMPLEXVALUE -> it.offsetComplexValue()
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

    private fun COMPLEXCOMPARAM.offsetComParamComplex(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val paramClass = this.paramclass?.offsetString()
            val comParamType = this.cptype?.toFileFormatEnum()
            val comParamUsage = this.cpusage?.toFileFormatEnum()
            val displayLevel = this.displaylevel?.toUInt()
            val complexComParam =
                let {
                    val comParams =
                        this.comparamOrCOMPLEXCOMPARAM
                            ?.map {
                                when (it) {
                                    is COMPARAM -> it.offsetComParamSimple()
                                    is COMPLEXCOMPARAM -> it.offsetComParamComplex()
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
                                it.offsetComplexValue()
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

    private fun STATE.offsetState(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()

            dataformat.State.startState(builder)
            dataformat.State.addShortName(builder, shortName)
            longName?.let { dataformat.State.addLongName(builder, it) }

            dataformat.State.endState(builder)
        }

    private fun PHYSICALDIMENSION.offsetPhysicalDimension(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longname = this.longname?.offsetLongName()

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

            PhysicalDimension.endPhysicalDimension(builder)
        }

    private fun PROTOCOL.offsetProtocol(): Int =
        this.cachedWithPath {
            val diagLayer = (this as DIAGLAYER).offsetType()
            val comparamSpecs =
                this.comparamspecref?.let {
                    val comParamSpec =
                        odx.resolveComParamSpec(it)
                            ?: odxlinkFailure("COMPARAM-SPEC", it)
                    comParamSpec.offsetComParamSpec()
                }
            val protStack =
                this.protstacksnref?.let { protStack ->
                    val stack =
                        odx.resolveProtStackByShortName(protStack.shortname)
                            ?: snrefFailure("PROT-STACK", protStack.shortname, this, odx.protStacks.map { it.shortname })
                    stack.offsetProtStack()
                }

            val parentRefs =
                this.parentrefs
                    ?.parentref
                    ?.map { it.offsetParentRef() }
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

    private fun COMPARAMSPEC.offsetComParamSpec(): Int =
        cachedWithPath {
            val protStacks =
                this.protstacks
                    ?.protstack
                    ?.map {
                        it.offsetProtStack()
                    }?.toIntArray()
                    ?.let {
                        ComParamSpec.createProtStacksVector(builder, it)
                    }
            ComParamSpec.startComParamSpec(builder)
            protStacks?.let { ComParamSpec.addProtStacks(builder, it) }
            ComParamSpec.endComParamSpec(builder)
        }

    private fun PROTSTACK.offsetProtStack(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val comparamSubSets =
                this.comparamsubsetrefs
                    ?.comparamsubsetref
                    ?.map {
                        val comparamSubSet =
                            odx.resolveComParamSubSet(it)
                                ?: odxlinkFailure("COMPARAM-SUBSET", it)
                        comparamSubSet.offsetComParamSubSet()
                    }?.toIntArray()
                    ?.let {
                        ProtStack.createComparamSubsetRefsVector(builder, it)
                    }
            val physicalLinkType = this.physicallinktype?.offsetString()
            val pduProtocolType = this.pduprotocoltype?.offsetString()

            ProtStack.startProtStack(builder)
            ProtStack.addShortName(builder, shortName)
            longName?.let { ProtStack.addLongName(builder, it) }
            comparamSubSets?.let { ProtStack.addComparamSubsetRefs(builder, it) }
            physicalLinkType?.let { ProtStack.addPhysicalLinkType(builder, it) }
            pduProtocolType?.let { ProtStack.addPduProtocolType(builder, it) }
            ProtStack.endProtStack(builder)
        }

    private fun COMPARAMREF.offsetComParamRef(): Int =
        cachedWithPath {
            val comParam =
                odx.resolveComparam(this)?.offsetComParamSimple()
                    ?: odx.resolveComplexComparam(this)?.offsetComParamComplex()

            if (comParam == null) {
                val message =
                    resolutionMessage(
                        ResolutionContext(
                            expected = "COMPARAM / COMPLEX-COMPARAM",
                            refKind = "ODXLINK",
                            refValue = this.idref,
                            docref = this.docref,
                            scopeSearched = this.docref ?: odx.collectionFor(this)?.containerKey,
                            sourceFile = odx.sourceFileFor(this),
                            logicalPath =
                                elementPath.takeIf { it.isNotEmpty() }?.let { formatElementPath(it) },
                            candidates = odx.comparamCandidates(this),
                        ),
                    )
                if (!options.lenient) {
                    throw OdxResolutionException(message)
                }
                logger.warning(message)
            }

            val simpleValue = this.simplevalue?.offsetSimpleValue()
            val complexValue = this.complexvalue?.offsetComplexValue()

            val protocol =
                this.protocolsnref?.shortname?.let { shortName ->
                    val protocolOdx =
                        odx.resolveProtocolByShortName(shortName)
                            ?: snrefFailure("PROTOCOL", shortName, this, odx.protocols.map { it.shortname })
                    protocolOdx.offsetProtocol()
                }

            val protStack =
                this.protstacksnref?.let {
                    val protStackOdx =
                        odx.resolveProtStackByShortName(this.protstacksnref.shortname)
                            ?: snrefFailure(
                                "PROT-STACK",
                                this.protstacksnref.shortname,
                                this,
                                odx.protStacks.map { it.shortname },
                            )

                    protStackOdx.offsetProtStack()
                }

            ComParamRef.startComParamRef(builder)
            comParam?.let { ComParamRef.addComParam(builder, it) }
            simpleValue?.let { ComParamRef.addSimpleValue(builder, it) }
            complexValue?.let { ComParamRef.addComplexValue(builder, it) }
            protocol?.let { ComParamRef.addProtocol(builder, it) }
            protStack?.let { ComParamRef.addProtStack(builder, it) }
            ComParamRef.endComParamRef(builder)
        }

    private fun STATECHART.offsetStateChart(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val semantic = this.semantic.offsetString()
            val stateTransitions =
                this.statetransitions?.statetransition?.let { transitions ->
                    val data = transitions.map { it.offsetStateTransition() }.toIntArray()
                    StateChart.createStateTransitionsVector(builder, data)
                }
            val startStateShortName = this.startstatesnref.shortname.offsetString()

            val states =
                this.states?.state?.let { states ->
                    val data = states.map { it.offsetState() }.toIntArray()
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

    private fun STATETRANSITION.offsetStateTransition(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val sourceShortNameRef = this.sourcesnref.shortname.offsetString()
            val targetShortNameRef = this.targetsnref.shortname.offsetString()

            StateTransition.startStateTransition(builder)

            StateTransition.addShortName(builder, shortName)
            StateTransition.addSourceShortNameRef(builder, sourceShortNameRef)
            StateTransition.addTargetShortNameRef(builder, targetShortNameRef)

            StateTransition.endStateTransition(builder)
        }

    private fun SWITCHKEY.offsetSwitchKey(): Int =
        cachedWithPath {
            val dop =
                odx.resolveCombinedDop(this.dataobjectpropref)?.offsetDOP()
                    ?: odxlinkFailure("DOP", this.dataobjectpropref)

            SwitchKey.startSwitchKey(builder)
            SwitchKey.addBytePosition(builder, this.byteposition.toUInt())
            this.bitposition?.let { SwitchKey.addBitPosition(builder, it.toUInt()) }
            dop.let { SwitchKey.addDop(builder, it) }
            SwitchKey.endSwitchKey(builder)
        }

    private fun DEFAULTCASE.offsetDefaultCase(): Int =
        cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val structure =
                this.structureref?.let {
                    val dop =
                        odx.resolveCombinedDop(it)
                            ?: odxlinkFailure("STRUCTURE", it)
                    dop.offsetDOP()
                }

            DefaultCase.startDefaultCase(builder)
            DefaultCase.addShortName(builder, shortName)
            longName?.let { DefaultCase.addLongName(builder, it) }
            structure?.let { DefaultCase.addStructure(builder, it) }
            DefaultCase.endDefaultCase(builder)
        }

    private fun CASE.offsetCase(): Int =
        cachedWithPath {
            val shortName = this.shortname.offsetString()
            val longName = this.longname?.offsetLongName()
            val lowerLimit = this.lowerlimit.offsetLimit()
            val upperLimit = this.upperlimit.offsetLimit()

            val structure =
                this.structuresnref?.shortname?.let {
                    collectionOf(this).resolveStructureByShortName(it)?.offsetDOP()
                        ?: snrefFailure("STRUCTURE", it, this, collectionOf(this).structuresByShortName.keys)
                } ?: this.structureref?.let {
                    odx.resolveCombinedDop(it)?.offsetDOP()
                        ?: odxlinkFailure("STRUCTURE", it)
                }

            Case.startCase(builder)
            Case.addShortName(builder, shortName)
            longName?.let { Case.addLongName(builder, it) }
            structure?.let { Case.addStructure(builder, it) }
            Case.addLowerLimit(builder, lowerLimit)
            Case.addUpperLimit(builder, upperLimit)
            Case.endCase(builder)
        }

    private fun TABLEROW.offsetTableRow(): Int =
        this.cachedWithPath {
            val shortName = this.shortname.offsetString()
            val semantic = this.semantic?.offsetString()
            val longName = this.longname?.offsetLongName()
            val key = this.key?.offsetString()

            val dop =
                this.dataobjectpropsnref?.shortname?.let {
                    val dop =
                        collectionOf(this).resolveDopByShortName(it)
                            ?: snrefFailure("DATA-OBJECT-PROP", it, this, collectionOf(this).dataObjectPropsByShortName.keys)
                    dop.offsetDOP()
                } ?: this.dataobjectpropref?.let {
                    val dop = odx.resolveCombinedDop(it) ?: odxlinkFailure("DOP", it)
                    dop.offsetDOP()
                }
            val structure =
                this.structuresnref?.shortname?.let {
                    val structureDop =
                        collectionOf(this).resolveStructureByShortName(it)
                            ?: snrefFailure("STRUCTURE", it, this, collectionOf(this).structuresByShortName.keys)
                    structureDop.offsetDOP()
                } ?: this.structureref?.let {
                    val structureDop = odx.resolveStructure(it) ?: odxlinkFailure("STRUCTURE", it)
                    structureDop.offsetDOP()
                }
            val sdgs = this.sdgs?.offsetSDGS()
            val audience = this.audience?.offsetAudience()
            val functClasses =
                this.functclassrefs
                    ?.functclassref
                    ?.map {
                        val functClass =
                            odx.resolveFunctClass(it)
                                ?: odxlinkFailure("FUNCT-CLASS", it)
                        functClass.offsetFunctClass()
                    }?.toIntArray()
                    ?.let {
                        TableRow.createFunctClassRefsVector(builder, it)
                    }

            val stateTransitionsRefs =
                this.statetransitionrefs
                    ?.statetransitionref
                    ?.map {
                        it.offsetStateTransitionRef()
                    }?.toIntArray()
                    ?.let {
                        TableRow.createStateTransitionRefsVector(builder, it)
                    }

            val preconditionStateRefs =
                this.preconditionstaterefs
                    ?.preconditionstateref
                    ?.map {
                        it.offsetPreConditionStateRef()
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

    private fun String.offsetString(): Int =
        cachedWithPath {
            builder.createString(this)
        }

    private fun ByteArray.offsetByteArray(): Int = builder.createByteVector(this)

    fun <T : DIAGCOMM> Collection<T>.filterByConverterOptions(options: ConverterOptions): List<T> =
        this.filter { it.isIncludedWithOption(options) }

    fun <T : DIAGCOMM> T.isIncludedWithOption(options: ConverterOptions): Boolean {
        if (options.withAudiences.isEmpty() ||
            this.audience?.enabledaudiencerefs?.enabledaudienceref == null ||
            this.audience.enabledaudiencerefs.enabledaudienceref
                .isNullOrEmpty()
        ) {
            return true
        }
        val audiences =
            this.audience.enabledaudiencerefs.enabledaudienceref
                .map {
                    odx.resolveAdditionalAudience(it)
                        ?: odxlinkFailure("ADDITIONAL-AUDIENCE", it)
                }.map {
                    it.shortname
                }
        return options.withAudiences.any { aud -> audiences.any { aud.equals(it, true) } }
    }
}
