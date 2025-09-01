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

import com.google.protobuf.ByteString
import org.eclipse.opensovd.cda.mdd.*
import org.eclipse.opensovd.cda.mdd.ComParam.ComplexComParam
import org.eclipse.opensovd.cda.mdd.ComParam.RegularComParam
import org.eclipse.opensovd.cda.mdd.ComplexValue.ComplexValueEntry
import org.eclipse.opensovd.cda.mdd.CompuScale.CompuRationalCoEffs
import org.eclipse.opensovd.cda.mdd.DTC
import org.eclipse.opensovd.cda.mdd.DiagCodedType.LeadingLengthInfoType
import org.eclipse.opensovd.cda.mdd.DiagCodedType.StandardLengthType
import org.eclipse.opensovd.cda.mdd.Param.TableKey
import org.eclipse.opensovd.cda.mdd.SD
import org.eclipse.opensovd.cda.mdd.SDG
import org.eclipse.opensovd.cda.mdd.SDG.SDGCaption
import org.eclipse.opensovd.cda.mdd.SDGS
import org.eclipse.opensovd.cda.mdd.Unit
import schema.odx.*
import schema.odx.DTCDOP
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger

class DatabaseWriter(private val logger: Logger, private val odx: ODXCollection, private val options: ConverterOptions) {
    private val idSequence = AtomicInteger(1)

    private val objectIds = mutableMapOf<Any, Int>()
    private val predetermineObjectIds = mutableMapOf<Int, Any>()
    private val usedObjectIds = mutableMapOf<Int, Any>()

    private val sdMap: MutableMap<schema.odx.SD, SD> = mutableMapOf()
    private val sdgMap: MutableMap<schema.odx.SDG, SDG> = mutableMapOf()
    private val sdgsMap: MutableMap<schema.odx.SDGS, SDGS> = mutableMapOf()
    private val additionalAudienceMap: MutableMap<ADDITIONALAUDIENCE, AdditionalAudience> = mutableMapOf()
    private val audiencesMap: MutableMap<AUDIENCE, Audience> = mutableMapOf()
    private val diagCodedTypes: MutableMap<DIAGCODEDTYPE, DiagCodedType> = mutableMapOf()
    private val unitMap: MutableMap<UNIT, Unit> = mutableMapOf()
    private val dtcs: MutableMap<schema.odx.DTC, DTC> = mutableMapOf()
    private val dopMap: MutableMap<DOPBASE, DOP> = mutableMapOf()
    private val tableMap: MutableMap<TABLE, Table> = mutableMapOf()
    private val tableRowMap: MutableMap<TABLEROW, TableRow> = mutableMapOf()
    private val paramMap: MutableMap<PARAM, Param> = mutableMapOf()
    private val responseMap: MutableMap<RESPONSE, Response> = mutableMapOf()
    private val requestMap: MutableMap<REQUEST, Request> = mutableMapOf()
    private val functClasses: MutableMap<FUNCTCLASS, FunctClass> = mutableMapOf()
    private val stateMap: MutableMap<STATE, State> = mutableMapOf()
    private val stateTransitionMap: MutableMap<STATETRANSITION, StateTransition> = mutableMapOf()
    private val stateTransitionRefMap: MutableMap<STATETRANSITIONREF, StateTransitionRef> = mutableMapOf()
    private val stateChartMap: MutableMap<STATECHART, StateChart> = mutableMapOf()
    private val preConditionStateRefsMap: MutableMap<PRECONDITIONSTATEREF, PreConditionStateRef> = mutableMapOf()
    private val comParamMap: MutableMap<COMPARAM, ComParam> = mutableMapOf()
    private val complexComParamMap: MutableMap<COMPLEXCOMPARAM, ComParam> = mutableMapOf()
    private val comParamSubSetMap: MutableMap<COMPARAMSUBSET, ComParamSubSet> = mutableMapOf()
    private val protStackMap: MutableMap<PROTSTACK, ProtStack> = mutableMapOf()
    private val protocolMap: MutableMap<PROTOCOL, Protocol> = mutableMapOf()
    private val diagServicesMap: MutableMap<DIAGSERVICE, DiagService> = mutableMapOf()
    private val singleEcuJobsMap: MutableMap<SINGLEECUJOB, SingleEcuJob> = mutableMapOf()
    private val baseVariantMap: MutableMap<BASEVARIANT, Variant> = mutableMapOf()
    private val ecuVariantMap: MutableMap<ECUVARIANT, Variant> = mutableMapOf()
    private val functionalGroupMap: MutableMap<FUNCTIONALGROUP, FunctionalGroup> = mutableMapOf()
    private val physDimensionMap: MutableMap<PHYSICALDIMENSION, PhysicalDimension> = mutableMapOf()
    private val librariesMap: MutableMap<LIBRARY, Library> = mutableMapOf()

    init {
        librariesMap.fillMissing(odx.libraries.values) { it.toProtoBuf() }
        sdMap.fillMissing(odx.sds) { it.toProtoBuf() }
        sdgMap.fillMissing(odx.sdgs) { it.toProtoBuf() }
        sdgsMap.fillMissing(odx.sdgss) { it.toProtoBuf() }
        additionalAudienceMap.fillMissing(odx.additionalAudiences.values) { it.toProtoBuf() }
        audiencesMap.fillMissing(odx.audiences) { it.toProtoBuf() }
        diagCodedTypes.fillMissing(odx.diagCodedTypes) { it.toProtoBuf() }
        unitMap.fillMissing(odx.units.values) { it.toProtoBuf() }
        dtcs.fillMissing(odx.dtcs.values) { it.toProtoBuf() }
        dopMap.fillMissing(odx.combinedDataObjectProps.values) { it.toProtoBuf() }
        dopMap.fillMissing(odx.comParamSubSets.values.flatMap { it.dataobjectprops?.dataobjectprop ?: emptyList() }) { it.toProtoBuf() }

        tableMap.fillMissing(odx.tables.values) { it.toProtoBuf() }
        tableRowMap.fillMissing(odx.tableRows.values) { it.toProtoBuf() }
        paramMap.fillMissing(odx.params) { it.toProtoBuf() }
        responseMap.fillMissing(odx.responses) { it.toProtoBuf() }
        requestMap.fillMissing(odx.requests.values) { it.toProtoBuf() }
        functClasses.fillMissing(odx.functClasses.values) { it.toProtoBuf() }
        stateMap.fillMissing(odx.states.values) { it.toProtoBuf() }
        stateTransitionMap.fillMissing(odx.stateTransitions.values) { it.toProtoBuf() }
        stateTransitionRefMap.fillMissing(odx.stateTransitionsRefs) { it.toProtoBuf() }
        stateChartMap.fillMissing(odx.stateCharts.values) { it.toProtoBuf() }
        preConditionStateRefsMap.fillMissing(odx.preConditionstateRefs) { it.toProtoBuf() }
        comParamMap.fillMissing(odx.comparams.values) { it.toProtoBuf() }
        complexComParamMap.fillMissing(odx.complexComparams.values) { it.toProtoBuf() }
        comParamSubSetMap.fillMissing(odx.comParamSubSets.values) { it.toProtoBuf() }
        protStackMap.fillMissing(odx.protStacks.values) { it.toProtoBuf() }
        protocolMap.fillMissing(odx.protocols.values) { it.toProtoBuf() }
        diagServicesMap.fillMissing(odx.diagServices.values) { it.toProtoBuf() }
        singleEcuJobsMap.fillMissing(odx.singleEcuJobs.values) { it.toProtoBuf() }
        baseVariantMap.fillMissing(odx.basevariants.values) { it.toProtoBuf() }
        ecuVariantMap.fillMissing(odx.ecuvariants.values) { it.toProtoBuf() }
        functionalGroupMap.fillMissing(odx.functionalGroups.values) { it.toProtoBuf() }
        physDimensionMap.fillMissing(odx.physDimensions.values) { it.toProtoBuf() }

        val unused = mutableMapOf<Int, Any>().also { it.putAll(predetermineObjectIds) }
        usedObjectIds.keys.forEach { unused.remove(it) }
        logger.info("${odx.ecuName} referenced ${predetermineObjectIds.size} / total ${usedObjectIds.keys.size} / unused ${unused.size}")
        if (unused.isNotEmpty()) {
            // Unused entries indicate that a reference was created but never put into the protobuf structure
            logger.warning("Warning: Unused references - this usually means, that something was referenced, but that something was not saved to the output file: ${unused.values.joinToString(", ")}")
        }
    }

    private fun Any.objectId(): ObjectID {
        var value = objectIds[this]
        if (value == null) {
            value = idSequence.getAndIncrement()
            objectIds[this] = value
        }
        usedObjectIds[value] = this
        return ObjectID.newBuilder().setValue(value).build()
    }

    private fun Any.getObjectRef(): ObjectID {
        var value = objectIds[this]
        if (value == null) {
            value = idSequence.getAndIncrement()
            objectIds[this] = value
            predetermineObjectIds[value] = this
        }
        return ObjectID.newBuilder().setValue(value).build()
    }

    fun createEcuData(): EcuData {
        val builder = EcuData.newBuilder()
        builder.version = "2025-04-30"
        builder.ecuName = odx.ecuName
        builder.revision = odx.odxRevision

        builder.addAllAdditionalAudiences(additionalAudienceMap.values)
        builder.addAllAudiences(audiencesMap.values)
        builder.addAllFunctClasses(functClasses.values)
        builder.addAllUnits(unitMap.values)
        builder.addAllSds(sdMap.values)
        builder.addAllSdgs(sdgMap.values)
        builder.addAllSdgss(sdgsMap.values)
        builder.addAllDops(dopMap.values)
        builder.addAllTables(tableMap.values)
        builder.addAllTableRows(tableRowMap.values)
        builder.addAllDtcs(dtcs.values)
        builder.addAllDiagCodedTypes(diagCodedTypes.values)
        builder.addAllParams(paramMap.values)
        builder.addAllRequests(requestMap.values)
        builder.addAllDiagServices(diagServicesMap.values)
        builder.addAllSingleEcuJobs(singleEcuJobsMap.values)
        builder.addAllStates(stateMap.values)
        builder.addAllStateTransitions(stateTransitionMap.values)
        builder.addAllStateTransitionRefs(stateTransitionRefMap.values)
        builder.addAllPreConditionStateRefs(preConditionStateRefsMap.values)
        builder.addAllResponses(responseMap.values)
        builder.addAllStateCharts(stateChartMap.values)
        builder.addAllVariants(baseVariantMap.values + ecuVariantMap.values)
        builder.addAllFunctionalGroups(functionalGroupMap.values)
        builder.addAllComParams(comParamMap.values + complexComParamMap.values)
        builder.addAllComParamSubSets(comParamSubSetMap.values)
        builder.addAllProtocols(protocolMap.values)
        builder.addAllProtStacks(protStackMap.values)
        builder.addAllLibraries(librariesMap.values)

        return builder.build()
    }

    private fun DIAGSERVICE.toProtoBuf(): DiagService {
        val b = DiagService.newBuilder()
        b.setId(objectId())
        b.setDiagComm((this as DIAGCOMM).toProtoBuf())

        val request = odx.requests[this.requestref.idref]?.getRef()
            ?: throw IllegalStateException("Couldn't find requestref ${this.requestref.idref}")
        b.setRequest(request)

        val posResponses = this.posresponserefs?.posresponseref?.map {
            odx.posResponses[it.idref]?.getRef() ?: throw IllegalStateException("Couldn't find response ${it.idref}")
        } ?: emptyList()
        b.addAllPosResponses(posResponses)
        val negResponses = this.negresponserefs?.negresponseref?.map {
            odx.negResponses[it.idref]?.getRef() ?: throw IllegalStateException("Couldn't find response ${it.idref}")
        } ?: emptyList()
        b.addAllNegResponses(negResponses)

        this.addressing?.let { b.setAddressing(it.toProtoBufEnum()) }
        this.transmissionmode?.let { b.setTransmissionMode(it.toProtoBufEnum()) }
        b.setIsCyclic(this.isISCYCLIC)
        b.setIsMultiple(this.isISMULTIPLE)
        this.comparamrefs?.comparamref?.let {
            b.addAllComParamRefs(it.map { ref -> ref.toProtoBuf() })
        }

        return b.build()
    }

    private fun DOPBASE.toProtoBuf(): DOP {
        val dop = DOP.newBuilder()
        dop.setId(objectId())

        dop.setShortName(this.shortname)
        this.sdgs?.let { dop.setSdgs(it.getRef()) }

        if (this is DATAOBJECTPROP) {
            dop.setDopType(DOP.DOPType.REGULAR)
            dop.setNormalDop(this.toNormalDop())
        } else if (this is ENDOFPDUFIELD) {
            dop.setDopType(DOP.DOPType.END_OF_PDU_FIELD)
            dop.setEndOfPduField(this.toEndOfPduField())
        } else if (this is STATICFIELD) {
            dop.setDopType(DOP.DOPType.STATIC_FIELD)
            dop.setStaticField(this.toStaticField())
        } else if (this is ENVDATA) {
            dop.setDopType(DOP.DOPType.ENV_DATA)
            dop.setEnvData(this.toEnvData())
        } else if (this is ENVDATADESC) {
            dop.setDopType(DOP.DOPType.ENV_DATA_DESC)
            dop.setEnvDataDesc(this.toEnvDataDesc())
        } else if (this is DTCDOP) {
            dop.setDopType(DOP.DOPType.DTC)
            dop.setDtcDop(this.toDTCDOP())
        } else if (this is STRUCTURE) {
            dop.setDopType(DOP.DOPType.STRUCTURE)
            dop.setStructure(this.toStructure())
        } else if (this is MUX) {
            dop.setDopType(DOP.DOPType.MUX)
            dop.setMuxDop(this.toMUXDOP())
        } else if (this is DYNAMICLENGTHFIELD) {
            dop.setDopType(DOP.DOPType.DYNAMIC_LENGTH_FIELD)
            dop.setDynamicLengthField(this.toDynamicLengthField())
        } else {
            throw IllegalStateException("Unhandled dop type ${this::class.java.simpleName}")
        }

        return dop.build()
    }

    private fun TABLE.toProtoBuf(): Table {
        val table = Table.newBuilder()
        table.setId(objectId())
        this.semantic?.let { table.setSemantic(it) }
        table.setShortName(this.shortname)
        this.longname?.let { table.setLongName(it.toProtoBuf()) }
        this.keylabel?.let { table.setKeyLabel(it) }
        this.keydopref?.idref?.let {
            val dop = odx.combinedDataObjectProps[it]?.getRef() ?: throw IllegalStateException("Couldn't find dop ${it}")
            table.setKeyDop(dop)
        }
        this.structlabel?.let { table.setStructLabel(it) }

        table.addAllRows(this.rowwrapper.map { row ->
            if (row is TABLEROW) {
                row.getRef()
            } else {
                throw IllegalStateException("Unsupported row type ${row.javaClass.simpleName}")
            }
        })

        table.addAllDiagCommConnector(
            this.tablediagcommconnectors?.tablediagcommconnector?.map { it.toProtoBuf() } ?: emptyList()
        )

        this.sdgs?.let { table.setSdgs(it.getRef()) }

        return table.build()
    }

    private fun TABLEDIAGCOMMCONNECTOR.toProtoBuf(): TableDiagCommConnector {
        val tdc = TableDiagCommConnector.newBuilder()

        tdc.semantic = this.semantic

        if (this.diagcommref != null) {
            val diagService = this.diagcommref?.idref?.let { odx.diagServices[it] }
            val ecuJob = this.diagcommref?.idref?.let { odx.singleEcuJobs[it] }
            if (diagService == null && ecuJob == null) {
                throw IllegalStateException("Couldn't resolve ${this.diagcommref.idref}")
            }
        } else if (this.diagcommsnref != null) {
            throw IllegalStateException("Unsupported short name ref ${this.diagcommsnref}")
        }

        return tdc.build()
    }

    private fun schema.odx.SD.toProtoBuf(): SD {
        val sd = SD.newBuilder()

        sd.setId(objectId())

        this.value?.let { sd.setValue(it) }
        this.si?.let { sd.setSi(it) }
        this.ti?.let { sd.setTi(it) }

        return sd.build()
    }

    private fun schema.odx.SD.getRef(): SD.Ref {
        val ref = this.getObjectRef()
        return SD.Ref.newBuilder().setRef(ref).build()
    }

    private fun schema.odx.SDG.toProtoBuf(): SDG {
        val b = SDG.newBuilder()
        b.setId(objectId())

        this.si?.let { b.setSi(it) }
        this.sdgcaption?.shortname?.let { b.setCaption(SDGCaption.newBuilder().setShortName(it).build()) }

        b.addAllSds(this.sdgOrSD.map {
            val sdEntry = SDOrSDG.newBuilder()
            if (it is schema.odx.SD) {
                sdEntry.setSd(it.getRef())
            } else if (it is schema.odx.SDG) {
                sdEntry.setSdg(it.getRef())
            }
            sdEntry.build()
        })
        return b.build()
    }

    private fun schema.odx.SDG.getRef(): SDG.Ref {
        val ref = this.getObjectRef()
        return SDG.Ref.newBuilder().setRef(ref).build()
    }

    private fun schema.odx.SDGS.toProtoBuf(): SDGS {
        val sdgs = SDGS.newBuilder()

        sdgs.setId(objectId())
        sdgs.addAllSdgs(this.sdg.map {
            sdgMap[it]?.toSDGRef() ?: throw IllegalStateException("Couldn't find SDG $it")
        })

        return sdgs.build()
    }

    private fun REQUEST.toProtoBuf(): Request {
        val request = Request.newBuilder()
        request.setId(objectId())
        this.sdgs?.let {
            request.setSdgs(it.getRef())
        }
        request.addAllParams(this.params?.param?.map { it.getRef() } ?: emptyList())
        return request.build()
    }

    private fun RESPONSE.getRef(): Response.Ref {
        val response = this.getObjectRef()
        return Response.Ref.newBuilder()
            .setRef(response)
            .build()
    }

    private fun RESPONSE.toProtoBuf(): Response {
        val response = Response.newBuilder()
        response.setId(objectId())

        when (this) {
            is POSRESPONSE -> response.setResponseType(Response.ResponseType.POS_RESPONSE)
            is NEGRESPONSE -> response.setResponseType(Response.ResponseType.NEG_RESPONSE)
            is GLOBALNEGRESPONSE -> response.setResponseType(Response.ResponseType.GLOBAL_NEG_RESPONSE)
            else -> throw IllegalStateException("Unknown response type ${this::class.java.simpleName}")
        }

        this.sdgs?.let {
            response.setSdgs(it.getRef())
        }
        response.addAllParams(this.params?.param?.map { it.getRef() } ?: emptyList())
        return response.build()
    }

    private fun PARAM.toProtoBuf(): Param {
        try {
            val param = Param.newBuilder()
            param.setId(objectId())
            param.setParamType(this.toParamTypeEnum())
            param.setShortName(this.shortname)
            this.semantic?.let { param.setSemantic(it) }

            this.sdgs?.let {
                param.setSdgs(it.getRef())
            }
            if (this is POSITIONABLEPARAM) {
                this.byteposition?.let { param.setBytePosition(it.toInt()) }
                this.bitposition?.let { param.setBitPosition(it.toInt()) }
            }

            if (this is VALUE) {
                val value = Param.Value.newBuilder()

                this.dopref?.let {
                    value.setDop(
                        odx.combinedDataObjectProps[it.idref]?.getRef()
                            ?: throw IllegalStateException("Couldn't find ${it.idref}")
                    )
                }
                if (this.dopsnref?.shortname != null) {
                    TODO("dop shortname ref in VALUE not supported ${this.dopsnref.shortname}")
                }


                this.physicaldefaultvalue?.let { value.setPhysicalDefaultValue(it) }

                param.setValue(value.build())
            } else if (this is CODEDCONST) {
                val codedConst = Param.CodedConst.newBuilder()

                codedConst.setDiagCodedType(this.diagcodedtype.getRef())
                this.codedvalue?.let { codedConst.setCodedValue(it) }

                param.setCodedConst(codedConst.build())
            } else if (this is DYNAMIC) {
                val dynamic = Param.Dynamic.newBuilder()
                param.setDynamic(dynamic)
            } else if (this is LENGTHKEY) {
                val lengthKey = Param.LengthKeyRef.newBuilder()

                this.dopref?.let {
                    lengthKey.setDop(odx.combinedDataObjectProps[it.idref]?.getRef())
                        ?: throw IllegalStateException("Couldn't find ${it.idref}")
                }
                if (this.dopsnref?.shortname != null) {
                    TODO("DOP short name not supported ${this.dopsnref.shortname}")
                }

                param.setLengthKeyRef(lengthKey.build())
            } else if (this is MATCHINGREQUESTPARAM) {
                val matchingrequestparam = Param.MatchingRequestParam.newBuilder()
                matchingrequestparam.setRequestBytePos(this.requestbytepos)
                matchingrequestparam.setByteLength(this.bytelength.toInt())
                param.setMatchingRequestParam(matchingrequestparam.build())
            } else if (this is NRCCONST) {
                val nrcconst = Param.NrcConst.newBuilder()
                this.diagcodedtype?.let { nrcconst.setDiagCodedType(it.getRef()) }
                nrcconst.addAllCodedValues(this.codedvalues?.codedvalue ?: emptyList())

                param.setNrcConst(nrcconst)
            } else if (this is PHYSCONST) {
                val physConst = Param.PhysConst.newBuilder()
                this.physconstantvalue?.let { physConst.setPhysConstantValue(it) }
                this.dopref?.let {
                    val dop = odx.combinedDataObjectProps[it.idref]
                        ?: throw IllegalStateException("couldn't find dop ${it.idref}")
                    physConst.setDop(dop.getRef())
                }
                if (this.dopsnref?.shortname != null) {
                    TODO("DOP short name not supported ${this.dopsnref.shortname}")
                }
                param.setPhysConst(physConst.build())
            } else if (this is RESERVED) {
                val reserved = Param.Reserved.newBuilder()
                reserved.setBitLength(this.bitlength.toInt())
                param.setReserved(reserved.build())
            } else if (this is SYSTEM) {
                val system = Param.System.newBuilder()

                if (this.dopsnref?.shortname != null) {
                    TODO("DOP short name ref ${this.dopsnref.shortname}")
                }
                this.dopref?.let {
                    val dop = odx.combinedDataObjectProps[it.idref]
                        ?: throw IllegalStateException("Couldn't find DOP ${it.idref}")
                    system.setDop(dop.getRef())
                }
                system.setSysParam(this.sysparam)

                param.setSystem(system)
            } else if (this is TABLEKEY) {
                val tableKey = TableKey.newBuilder()
                val entry = this.rest.firstOrNull()?.value ?: throw IllegalStateException("TABLE-KEY ${this.id} has no entries")
                if (this.rest.size > 1) {
                    throw IllegalStateException("TABLE-KEY ${this.id} has more than one entry")
                }
                if (entry is ODXLINK) {
                    val table = odx.tables[entry.idref]
                    if (table == null) {
                        val row = odx.tableRows[entry.idref] ?: throw IllegalStateException("ODXLINK ${this.id} is neither TABLE nor TABLE-KEY")
                        tableKey.setTableRow(row.getRef())
                    } else {
                        tableKey.setTable(table.getRef())
                    }
                } else {
                    throw IllegalStateException("Unknown type for TABLE-KEY/TABLEROW ${this.id} entry ${entry.javaClass.simpleName}")
                }
                param.setTableKey(tableKey.build())
            } else if (this is TABLEENTRY) {
                val tableEntry = Param.TableEntry.newBuilder()
                tableEntry.setParam(this.toProtoBuf())
                this.target?.let { tableEntry.setTarget(it.toProtoBuf()) }
                this.tablerowref.idref?.let {
                    val row = odx.tableRows[it] ?: throw IllegalStateException("Couldn't find TABLE-ROW $it")
                    tableEntry.setTableRow(row.getRef())
                }
                param.setTableEntry(tableEntry.build())
            } else if (this is TABLESTRUCT) {
                val tableStruct = Param.TableStruct.newBuilder()
                if (this.tablekeysnref != null) {
                    TODO("TABLE-KEY-SNREF not supported ${this.shortname}")
                }
                val tableKey = odx.tableKeys[this.tablekeyref.idref] ?: throw IllegalStateException("Couldn't find TABLE-KEY ${this.tablekeyref.idref}")
                tableStruct.setTableKey(tableKey.getRef())
                param.setTableStruct(tableStruct.build())
            } else {
                throw IllegalStateException("Unknown param type ${this::class.java.simpleName}")
            }

            return param.build()
        } catch (e: Exception) {
            throw IllegalStateException("Error in Param ${this.shortname}", e)
        }
    }

    private fun SDG.toSDGRef() =
        SDG.Ref.newBuilder()
            .setRef(ObjectID.newBuilder().setValue(this.id.value).build())
            .build()

    private fun REQUEST.getRef(): Request.Ref {
        val request = this.getObjectRef()
        return Request.Ref.newBuilder()
            .setRef(request)
            .build()
    }

    private fun PARAM.getRef(): Param.Ref {
        val request = this.getObjectRef()
        return Param.Ref.newBuilder()
            .setRef(request)
            .build()
    }

    private fun schema.odx.SDGS.getRef(): SDGS.Ref {
        val sdgs = this.getObjectRef()

        return SDGS.Ref.newBuilder()
            .setRef(sdgs)
            .build()
    }

    private fun FUNCTCLASS.toProtoBuf(): FunctClass {
        val functClass = FunctClass.newBuilder()
        functClass.setId(objectId())
        functClass.setShortName(this.shortname)
        return functClass.build()
    }

    private fun FUNCTCLASS.getRef(): FunctClass.Ref {
        val functClass = functClasses.getOrCreate(this) { it.toProtoBuf() }

        return FunctClass.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(functClass.id.value)).build()
    }

    private fun DOPBASE.getRef(): DOP.Ref {
        val dopBase = dopMap.getOrCreate(this) { it.toProtoBuf()}

        return DOP.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(dopBase.id.value)).build()
    }

    private fun DIAGCODEDTYPE.getRef(): DiagCodedType.Ref {
        val diagCodedType = diagCodedTypes.getOrCreate(this) { it.toProtoBuf() }
        return DiagCodedType.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(diagCodedType.id.value)).build()
    }

    private fun DIAGCODEDTYPE.toProtoBuf(): DiagCodedType {
        val diagCodedType = DiagCodedType.newBuilder()
        diagCodedType.setId(objectId())
        diagCodedType.setType(this.toTypeEnum())
        diagCodedType.setBaseDataType(this.basedatatype.toProtoBufDiagCodedTypeEnum())
        this.basetypeencoding?.let {
            diagCodedType.setBaseTypeEncoding(it)
        }
        diagCodedType.setIsHighLowByteOrder(this.isISHIGHLOWBYTEORDER)
        when (this) {
            is STANDARDLENGTHTYPE -> {
                val standardlengthtype = StandardLengthType.newBuilder()
                this.bitmask?.let { standardlengthtype.setBitMask(ByteString.copyFrom(it)) }
                standardlengthtype.setCondensed(this.isCONDENSED)
                standardlengthtype.setBitLength(this.bitlength.toInt())
                diagCodedType.setStandardLengthType(standardlengthtype.build())
            }

            is MINMAXLENGTHTYPE -> {
                val minmaxlengthtype = DiagCodedType.MinMaxLengthType.newBuilder()
                minmaxlengthtype.setMinLength(this.minlength.toInt())
                this.maxlength?.let { minmaxlengthtype.setMaxLength(it.toInt()) }
                this.termination?.let { minmaxlengthtype.setTermination(it.toProtoBufEnum()) }
                diagCodedType.setMinMaxLengthType(minmaxlengthtype.build())
            }

            is LEADINGLENGTHINFOTYPE -> {
                val leadinglengthinfotype = LeadingLengthInfoType.newBuilder()
                leadinglengthinfotype.setBitLength(this.bitlength.toInt())
                diagCodedType.setLeadingLengthInfoType(leadinglengthinfotype.build())
            }

            is PARAMLENGTHINFOTYPE -> {
                val paramLengthInfoType = DiagCodedType.ParamLengthInfoType.newBuilder()
                val param = odx.lengthKeys[this.lengthkeyref.idref] ?: throw IllegalStateException("Unknown length key reference ${this.lengthkeyref.idref}")
                paramLengthInfoType.setLengthKey(param.getRef())
                diagCodedType.setParamLengthInfoType(paramLengthInfoType.build())
            }

            else -> {
                // PARAMLENGTHINFOTYPE not supported yet
                throw IllegalStateException("Unsupported diag coded type ${this::class.java.simpleName}")
            }
        }
        return diagCodedType.build()
    }

    private fun UNIT.toProtoBuf(): Unit {
        val unit = Unit.newBuilder()
        unit.setId(objectId())
        unit.setShortName(this.shortname)
        unit.setDisplayName(this.displayname)
        this.factorsitounit?.let { unit.setFactorsitounit(it) }
        this.offsetsitounit?.let { unit.setOffsetitounit(it) }
        this.physicaldimensionref?.let { ref ->
            val physDimension = odx.unitSpecs.flatMap { it.physicaldimensions?.physicaldimension ?: emptyList() }
                .firstOrNull { it.id == ref.idref }
                ?: throw IllegalStateException("Couldn't find physical dimension ${ref.idref}")
            unit.setPhysicalDimension(physDimension.getRef())
        }
        return unit.build()
    }

    private fun UNIT.getRef(): Unit.Ref {
        val unit = unitMap.getOrCreate(this) { it.toProtoBuf() }
        return Unit.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(unit.id.value)).build()
    }

    private fun DATAOBJECTPROP.toNormalDop(): DOP.NormalDOP {
        val normalDop = DOP.NormalDOP.newBuilder()

        this.diagcodedtype?.let { normalDop.setDiagCodedType(it.getRef()) }
        this.unitref?.let {
            val unit = odx.units[it.idref] ?: throw IllegalStateException("Couldn't find unit ${it.idref}")
            normalDop.setUnitRef(unit.getRef())
        }
        this.physicaltype?.let { normalDop.setPhysicalType(it.toProtoBuf()) }
        this.compumethod?.let { normalDop.setCompuMethod(it.toProtoBuf()) }
        return normalDop.build()
    }

    private fun PHYSICALTYPE.toProtoBuf(): PhysicalType {
        val physicalType = PhysicalType.newBuilder()

        physicalType.setBaseDataType(this.basedatatype.toProtoBufEnum())
        this.precision?.let { physicalType.setPrecision(it.toInt()) }
        this.displayradix?.let { physicalType.setDisplayRadix(it.toProtoBufEnum()) }

        return physicalType.build()
    }

    private fun ENDOFPDUFIELD.toEndOfPduField(): DOP.EndOfPduField {
        val endOfPduField = DOP.EndOfPduField.newBuilder()

        this.maxnumberofitems?.let { endOfPduField.setMaxNumberOfItems(it.toInt()) }
        this.minnumberofitems?.let { endOfPduField.setMinNumberOfItems(it.toInt()) }
        endOfPduField.setField(this.toField())

        return endOfPduField.build()
    }

    private fun STATICFIELD.toStaticField(): DOP.StaticField {
        val staticField = DOP.StaticField.newBuilder()

        staticField.setFixedNumberOfItems(this.fixednumberofitems.toInt())
        staticField.setItemByteSize(this.itembytesize.toInt())
        staticField.setField(this.toField())

        return staticField.build()
    }

    private fun FIELD.toField(): DOP.Field {
        val field = DOP.Field.newBuilder()
        this.basicstructureref?.let {
            val dop =
                odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find dop ${it.idref}")
            field.setBasicStructure(dop.getRef())
        }
        this.basicstructuresnref?.let {
            field.setBasicStructureShortNameRef(it.shortname)
        }
        this.envdatadescref?.let {
            val dop =
                odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find dop ${it.idref}")
            field.setEnvDataDesc(dop.getRef())
        }
        this.envdatadescsnref?.let {
            field.setEnvDataDescShortNameRef(it.shortname)
        }
        field.setIsVisible(this.isISVISIBLE)
        return field.build()
    }

    private fun ENVDATA.toEnvData(): DOP.EnvData {
        val envData = DOP.EnvData.newBuilder()

        val data = this.dtcvalues?.dtcvalue?.map { it.value } ?: emptyList()
        envData.addAllDtcValues(data.map { it.toInt() })
        val params = this.params?.param?.map {
            it.getRef()
        } ?: emptyList()
        envData.addAllParams(params)
        return envData.build()
    }

    private fun ENVDATADESC.toEnvDataDesc(): DOP.EnvDataDesc {
        val envDataDesc = DOP.EnvDataDesc.newBuilder()

        envDataDesc.addAllEnvDatas(this.envdatarefs?.envdataref?.map {
            val envData =
                odx.envDatas[it.idref] ?: throw IllegalStateException("Couldn't find env data ${it.idref}")
            envData.getRef()
        })

        this.paramsnref?.let { envDataDesc.setParamShortName(it.shortname) }
        this.paramsnpathref?.let { envDataDesc.setParamPathShortName(it.shortnamepath) }

        return envDataDesc.build()
    }


    private fun schema.odx.DTC.getRef(): DTC.Ref {
        val dtc = dtcs.getOrCreate(this) { it.toProtoBuf() }
        return DTC.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(dtc.id.value).build()).build()
    }

    private fun schema.odx.DTC.toProtoBuf(): DTC {
        val dtc = DTC.newBuilder()

        dtc.setId(objectId())
        dtc.setShortName(this.shortname)
        dtc.setTroubleCode(this.troublecode.toUInt().toInt())
        this.displaytroublecode?.let { dtc.setDisplayTroubleCode(it) }
        this.text?.let { dtc.setText(it.toProtoBuf()) }
        this.level?.let { dtc.setLevel(it.toInt()) }
        this.sdgs?.let { dtc.setSdgs(it.getRef()) }
        dtc.setIsTemporary(this.isISTEMPORARY)

        return dtc.build()
    }

    private fun LONGNAME.toProtoBuf(): LongName {
        val longName = LongName.newBuilder()

        this.ti?.let { longName.setTi(it) }
        this.value?.let { longName.setValue(it) }

        return longName.build()
    }

    private fun TEXT.toProtoBuf(): Text {
        val text = Text.newBuilder()

        this.ti?.let { text.setTi(it) }
        this.value?.let { text.setValue(it) }

        return text.build()
    }

    private fun COMPUMETHOD.toProtoBuf(): CompuMethod {
        val compuMethod = CompuMethod.newBuilder()
        this.category?.let { compuMethod.setCategory(it.toProtoBufEnum()) }
        this.compuinternaltophys?.let { compuMethod.setInternalToPhys(it.toProtoBuf()) }
        this.compuphystointernal?.let { compuMethod.setPhysToInternal(it.toProtoBuf()) }
        return compuMethod.build()
    }

    private fun COMPUINTERNALTOPHYS.toProtoBuf(): CompuMethod.CompuInternalToPhys {
        val compuInternalToPhys = CompuMethod.CompuInternalToPhys.newBuilder()

        if (this.progcode != null) {
            this.progcode?.let { compuInternalToPhys.setProgCode(it.toProtoBuf()) }
        }

        this.compuscales?.let { compuInternalToPhys.addAllCompuScales(it.compuscale.map { sc -> sc.toProtoBuf() }) }
        this.compudefaultvalue?.let { compuInternalToPhys.setCompuDefaultValue(it.toProtoBuf()) }

        return compuInternalToPhys.build()
    }

    private fun COMPUSCALE.toProtoBuf(): CompuScale {
        val compuScale = CompuScale.newBuilder()

        this.shortlabel?.let { compuScale.setShortLabel(it.toProtoBuf()) }
        this.lowerlimit?.let { compuScale.setLowerLimit(it.toProtoBuf()) }
        this.upperlimit?.let { compuScale.setUpperLimit(it.toProtoBuf()) }
        this.compuinversevalue?.let { compuScale.setInverseValues(it.toProtoBuf()) }
        this.compuconst?.let { compuScale.setConsts(it.toProtoBuf()) }
        this.compurationalcoeffs?.let { compuScale.setRationalCoEffs(it.toProtoBuf()) }

        return compuScale.build()
    }

    private fun LIMIT.toProtoBuf(): Limit {
        val limit = Limit.newBuilder()

        this.value?.let { limit.setValue(it) }
        this.intervaltype?.let { limit.setIntervalType(it.toProtoBufEnum()) }

        return limit.build()
    }

    private fun COMPUDEFAULTVALUE.toProtoBuf(): CompuMethod.CompuDefaultValue {
        val compuDefaultValue = CompuMethod.CompuDefaultValue.newBuilder()
        if (this.v != null || this.vt != null) {
            val values = compuDefaultValue.valuesBuilder

            this.v?.let { values.setV(it.value) }
            this.vt?.value?.let {
                values.setVt(it)
            }
            this.vt?.ti?.let {
                values.setVtTi(it)
            }

            compuDefaultValue.setValues(values.build())
        }
        return compuDefaultValue.build()
    }

    private fun COMPUPHYSTOINTERNAL.toProtoBuf(): CompuMethod.CompuPhysToInternal {
        val compuPhysToInternal = CompuMethod.CompuPhysToInternal.newBuilder()

        this.progcode?.let { compuPhysToInternal.setProgCode(it.toProtoBuf()) }
        this.compuscales?.let { compuPhysToInternal.addAllCompuScales(it.compuscale.map { sc -> sc.toProtoBuf() }) }
        if (this.progcode != null) {
            TODO("PROGCODE in COMPUINTERNALTOPHYS not supported")
        }
        this.compudefaultvalue?.let { compuPhysToInternal.setCompuDefaultValue(it.toProtoBuf()) }

        return compuPhysToInternal.build()
    }

    private fun COMPUINVERSEVALUE.toProtoBuf(): CompuValues {
        val values = CompuValues.newBuilder()

        this.v?.let { values.setV(it.value) }
        this.vt?.value?.let { values.setVt(it) }
        this.vt?.ti?.let { values.setVtTi(it) }

        return values.build()
    }

    private fun COMPUCONST.toProtoBuf(): CompuValues {
        val values = CompuValues.newBuilder()

        this.v?.value?.let { values.setV(it) }
        this.vt?.value?.let { values.setVt(it) }
        this.vt?.ti?.let { values.setVtTi(it) }

        return values.build()
    }

    private fun COMPURATIONALCOEFFS.toProtoBuf(): CompuRationalCoEffs {
        val coEffs = CompuRationalCoEffs.newBuilder()

        this.compunumerator?.v?.let { coEffs.addAllNumerator(it.map { v -> v.value }) }
        this.compudenominator?.v?.let { coEffs.addAllDenominator(it.map { v -> v.value }) }

        return coEffs.build()
    }

    private fun DTCDOP.toDTCDOP(): DOP.DTCDOP {
        val dtcDop = DOP.DTCDOP.newBuilder()

        dtcDop.setDiagCodedType(this.diagcodedtype.getRef())
        dtcDop.setPhysicalType(this.physicaltype.toProtoBuf())
        dtcDop.setCompuMethod(this.compumethod.toProtoBuf())
        val dtcs = this.dtcs?.dtcproxy?.map {
            if (it is schema.odx.DTC) {
                it.getRef()
            } else if (it is ODXLINK) {
                val dop = odx.dtcs[it.idref] ?: throw IllegalStateException("Couldn't find DTC ${it.idref}")
                dop.getRef()
            } else {
                throw IllegalStateException("Unsupported DTC type ${it::class.java.simpleName}")
            }
        } ?: emptyList()
        dtcDop.addAllDtcs(dtcs)

        dtcDop.setIsVisible(this.isISVISIBLE)

        return dtcDop.build()
    }

    private fun MUX.toMUXDOP(): DOP.MUXDOP {
        val muxDop = DOP.MUXDOP.newBuilder()

        muxDop.setBytePosition(this.byteposition.toInt())
        muxDop.setSwitchKey(this.switchkey.toProtoBuf())
        this.defaultcase?.let { muxDop.setDefaultCase(it.toProtoBuf()) }
        this.cases?.case?.let { cases -> muxDop.addAllCases(cases.map { it.toProtoBuf() }) }
        muxDop.setIsVisible(this.isISVISIBLE)

        return muxDop.build()
    }

    private fun DYNAMICLENGTHFIELD.toDynamicLengthField(): DOP.DynamicLengthField {
        val dlf = DOP.DynamicLengthField.newBuilder()

        dlf.setOffset(this.offset.toInt())
        dlf.setField((this as FIELD).toField())
        dlf.setDetermineNumberOfItems(this.determinenumberofitems.toDetermineNumberOfItems())

        return dlf.build()
    }

    private fun DETERMINENUMBEROFITEMS.toDetermineNumberOfItems(): DOP.DynamicLengthField.DetermineNumberOfItems {
        val noi = DOP.DynamicLengthField.DetermineNumberOfItems.newBuilder()

        noi.setBytePosition(this.byteposition.toInt())
        this.bitposition?.let { noi.setBitPosition(it.toInt()) }

        this.dataobjectpropref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find ${it.idref}")
            noi.setDop(dop.getRef())
        }
        return noi.build()
    }

    private fun STRUCTURE.toStructure(): DOP.Structure {
        val structure = DOP.Structure.newBuilder()

        this.params?.param?.let {
            structure.addAllParams(it.map { p -> p.getRef() })
        }
        this.bytesize?.let { structure.setByteSize(it.toInt()) }
        structure.setIsVisible(this.isISVISIBLE)

        return structure.build()
    }

    private fun ADDITIONALAUDIENCE.toProtoBuf(): AdditionalAudience {
        val aa = AdditionalAudience.newBuilder()

        aa.setId(objectId())
        aa.setShortName(this.shortname)
        this.longname?.let { aa.setLongName(it.toProtoBuf()) }

        return aa.build()
    }

    private fun AUDIENCE.toProtoBuf(): Audience {
        val audience = Audience.newBuilder()

        audience.setId(objectId())

        this.enabledaudiencerefs?.enabledaudienceref?.let { aa ->
            audience.addAllEnabledAudiences(
                aa.map {
                    val aud = odx.additionalAudiences[it.idref]
                        ?: throw IllegalStateException("Can't find additional audience ${it.idref}")
                    aud.getRef()
                }
            )
        }

        this.disabledaudiencerefs?.disabledaudienceref?.let { aa ->
            audience.addAllDisabledAudiences(
                aa.map {
                    val aud = odx.additionalAudiences[it.idref]
                        ?: throw IllegalStateException("Can't find additional audience ${it.idref}")
                    aud.getRef()
                }
            )
        }

        audience.setIsSupplier(this.isISSUPPLIER)
        audience.setIsDevelopment(this.isISDEVELOPMENT)
        audience.setIsManufacturing(this.isISMANUFACTURING)
        audience.setIsAfterSales(this.isISAFTERSALES)
        audience.setIsAfterMarket(this.isISAFTERMARKET)

        return audience.build()
    }

    private fun AUDIENCE.getRef(): Audience.Ref {
        val audience = this.getObjectRef()
        return Audience.Ref.newBuilder().setRef(audience).build()
    }

    private fun ADDITIONALAUDIENCE.getRef(): AdditionalAudience.Ref {
        val ref = this.getObjectRef()

        return AdditionalAudience.Ref.newBuilder().setRef(ref).build()
    }

    private fun DIAGCOMM.toProtoBuf(): DiagComm {
        val diagComm = DiagComm.newBuilder()

        diagComm.setShortName(this.shortname)
        this.longname?.let { diagComm.setLongName(it.toProtoBuf()) }
        this.diagnosticclass?.let { diagComm.setDiagClassType(it.toProtoBufEnum()) }
        this.functclassrefs?.functclassref?.map {
            val functClass = odx.functClasses[it.idref] ?: throw IllegalStateException("Couldn't find funct class ${it.idref}")
            diagComm.functClass = functClass.getRef()
        }
        this.semantic?.let { diagComm.setSemantic(it) }
        this.preconditionstaterefs?.preconditionstateref?.let {
            diagComm.addAllPreConditionStateRefs(it.map { stateRef ->
                stateRef.getRef()
            })
        }
        this.statetransitionrefs?.statetransitionref?.let { stateTransitionRefs ->
            diagComm.addAllStateTransitionRefs(stateTransitionRefs.map { it.getRef() })
        }

        this.protocolsnrefs?.protocolsnref?.let {
            diagComm.addAllProtocols(it.map {
                val protocol = odx.protocols.values.firstOrNull { p -> p.shortname == it.shortname }
                    ?: throw IllegalStateException("Couldn't find protocol ${it.shortname}")
                protocol.getRef()
            })
        }

        this.audience?.let { diagComm.setAudience(it.getRef()) }
        this.sdgs?.let { diagComm.setSdgs(it.getRef()) }

        diagComm.setIsFinal(this.isISFINAL)
        diagComm.setIsMandatory(this.isISMANDATORY)
        diagComm.setIsExecutable(this.isISEXECUTABLE)

        return diagComm.build()
    }

    private fun SINGLEECUJOB.toProtoBuf(): SingleEcuJob {
        val singleEcuJob = SingleEcuJob.newBuilder()

        singleEcuJob.setId(objectId())
        singleEcuJob.setDiagComm((this as DIAGCOMM).toProtoBuf())

        this.progcodes?.progcode?.let { singleEcuJob.addAllProgCodes(it.map { pc -> pc.toProtoBuf() }) }
        this.inputparams?.inputparam?.let { singleEcuJob.addAllInputParams(it.map { ip -> ip.toProtoBuf() }) }
        this.outputparams?.outputparam?.let { singleEcuJob.addAllOutputParams(it.map { op -> op.toProtoBuf() }) }
        this.negoutputparams?.negoutputparam?.let { singleEcuJob.addAllNegOutputParams(it.map { nop -> nop.toProtoBuf() }) }


        return singleEcuJob.build()
    }

    private fun INPUTPARAM.toProtoBuf(): JobParam {
        val jobParam = JobParam.newBuilder()

        jobParam.setShortName(this.shortname)
        this.longname?.let { jobParam.setLongName(it.toProtoBuf()) }
        this.physicaldefaultvalue?.let { jobParam.setPhysicalDefaultValue(it) }
        this.dopbaseref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Can't find DOP ${it.idref}")
            jobParam.setDopBase(dop.getRef())
        }
        this.semantic?.let { jobParam.setSemantic(it) }

        return jobParam.build()
    }

    private fun OUTPUTPARAM.toProtoBuf(): JobParam {
        val jobParam = JobParam.newBuilder()

        jobParam.setShortName(this.shortname)
        this.longname?.let { jobParam.setLongName(it.toProtoBuf()) }
        this.dopbaseref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Can't find DOP ${it.idref}")
            jobParam.setDopBase(dop.getRef())
        }
        this.semantic?.let { jobParam.setSemantic(it) }

        return jobParam.build()
    }

    private fun NEGOUTPUTPARAM.toProtoBuf(): JobParam {
        val jobParam = JobParam.newBuilder()

        jobParam.setShortName(this.shortname)
        this.longname?.let { jobParam.setLongName(it.toProtoBuf()) }
        this.dopbaseref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Can't find DOP ${it.idref}")
            jobParam.setDopBase(dop.getRef())
        }

        return jobParam.build()
    }

    private fun PROGCODE.toProtoBuf(): ProgCode {
        val progCode = ProgCode.newBuilder()

        this.codefile?.let { progCode.setCodeFile(it) }
        this.encryption?.let { progCode.setEncryption(it) }
        this.syntax?.let { progCode.setSyntax(it) }
        this.revision?.let { progCode.setRevision(it) }
        this.entrypoint?.let { progCode.setEntrypoint(it) }
        this.libraryrefs?.libraryref?.let {
            progCode.addAllLibrary(it.map { ref ->
                val library = odx.libraries[ref.idref] ?: throw IllegalStateException("Couldn't find LIBRARY ${ref.idref}")
                library.getRef()
            })
        }

        return progCode.build()
    }

    private fun STATE.toProtoBuf(): State {
        val state = State.newBuilder()

        state.setId(objectId())
        state.setShortName(this.shortname)
        this.longname?.let { state.setLongName(it.toProtoBuf()) }

        return state.build()
    }

    private fun STATECHART.toProtoBuf(): StateChart {
        val stateChart = StateChart.newBuilder()

        stateChart.setId(objectId())
        stateChart.setShortName(this.shortname)
        stateChart.setSemantic(this.semantic)
        this.statetransitions?.statetransition?.let { transitions ->
            stateChart.addAllStateTransitions(transitions.map { it.getRef() })
        }
        stateChart.setStartStateShortNameRef(this.startstatesnref.shortname)
        this.states?.state?.let { states ->
            stateChart.addAllStates(states.map { it.getRef() })
        }
        return stateChart.build()
    }

    private fun STATE.getRef(): State.Ref {
        val state = stateMap.getOrCreate(this) { it.toProtoBuf() }

        return State.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(state.id.value).build()).build()
    }

    private fun STATETRANSITION.toProtoBuf(): StateTransition {
        val stateTransition = StateTransition.newBuilder()

        stateTransition.setId(objectId())
        stateTransition.setShortName(this.shortname)
        stateTransition.setSourceShortNameRef(this.sourcesnref.shortname)
        stateTransition.setTargetShortNameRef(this.targetsnref.shortname)

        return stateTransition.build()
    }

    private fun STATETRANSITION.getRef(): StateTransition.Ref {
        val stateTransition =
            stateTransitionMap.getOrCreate(this) { it.toProtoBuf() }

        return StateTransition.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(stateTransition.id.value).build())
            .build()
    }

    private fun HIERARCHYELEMENT.toProtoBuf(): DiagLayer {
        val diagLayer = DiagLayer.newBuilder()

        diagLayer.setShortName(this.shortname)
        this.longname?.let { diagLayer.setLongName(it.toProtoBuf()) }
        this.sdgs?.let { diagLayer.setSdgs(it.getRef()) }

        val resolvedLinks: List<DIAGCOMM> = this.diagcomms?.diagcommproxy?.filterIsInstance<ODXLINK>()?.map {
            odx.diagServices[it.idref] ?: odx.singleEcuJobs[it.idref]
            ?: throw IllegalStateException("Couldn't find reference ${it.idref}")
        } ?: emptyList()

        resolvedLinks.filterIsInstance<DIAGSERVICE>().forEach { diagLayer.addDiagServices(it.getRef()) }

        resolvedLinks.filterIsInstance<SINGLEECUJOB>().forEach { diagLayer.addSingleEcuJobs(it.getRef()) }

        this.diagcomms?.diagcommproxy?.filterIsInstance<DIAGSERVICE>()?.let { diagServices ->
            diagLayer.addAllDiagServices(diagServices.map { it.getRef() })
        }

        this.diagcomms?.diagcommproxy?.filterIsInstance<SINGLEECUJOB>()?.let { singleEcuJobs ->
            diagLayer.addAllSingleEcuJobs(singleEcuJobs.map { it.getRef() })
        }

        this.diagcomms?.diagcommproxy?.filterIsInstance<MULTIPLEECUJOB>()?.let {
            // TODO - not supported yet
        }

        this.comparamrefs?.comparamref?.let {
            diagLayer.addAllComParamRefs(it.map { ref -> ref.toProtoBuf() })
        }
        this.functclasss?.functclass?.let { diagLayer.addAllFunctClasses(it.map { fc -> fc.getRef() }) }

        this.additionalaudiences?.additionalaudience?.let { diagLayer.addAllAdditionalAudiences(it.map { aa -> aa.getRef() }) }

        return diagLayer.build()
    }

    private fun BASEVARIANT.toProtoBuf(): Variant {
        val variant = Variant.newBuilder()

        variant.setId(objectId())
        variant.setDiagLayer((this as HIERARCHYELEMENT).toProtoBuf())
        variant.setIsBaseVariant(true)

        this.basevariantpattern?.matchingbasevariantparameters?.matchingbasevariantparameter?.let { patterns ->
            val vp = VariantPattern.newBuilder().addAllMatchingParameter(patterns.map { it.toProtoBuf() }).build()
            variant.addAllVariantPattern(listOf(vp))
        }

        return variant.build()
    }

    private fun ECUVARIANT.toProtoBuf(): Variant {
        val variant = Variant.newBuilder()

        variant.setId(objectId())
        variant.setDiagLayer((this as HIERARCHYELEMENT).toProtoBuf())
        variant.setIsBaseVariant(false)

        this.ecuvariantpatterns?.ecuvariantpattern?.let { patterns ->
            variant.addAllVariantPattern(patterns.map { it.toProtoBuf() })
        }

        return variant.build()
    }

    private fun FUNCTIONALGROUP.toProtoBuf(): FunctionalGroup {
        val functionalGroup = FunctionalGroup.newBuilder()

        functionalGroup.setId(objectId())
        functionalGroup.setDiagLayer((this as HIERARCHYELEMENT).toProtoBuf())

        return functionalGroup.build()
    }


    private fun DIAGSERVICE.getRef(): DiagService.Ref {
        val service = diagServicesMap.getOrCreate(this) { it.toProtoBuf() }

        return DiagService.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(service.id.value).build()).build()
    }

    private fun SINGLEECUJOB.getRef(): SingleEcuJob.Ref {
        val service = singleEcuJobsMap.getOrCreate(this) { it.toProtoBuf() }

        return SingleEcuJob.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(service.id.value).build()).build()
    }

    private fun STATETRANSITIONREF.toProtoBuf(): StateTransitionRef {
        val stateTransitionRef = StateTransitionRef.newBuilder()

        stateTransitionRef.setId(objectId())
        this.value?.let { stateTransitionRef.setValue(it) }
        this.idref?.let {
            val stateTransition = odx.stateTransitions[this.idref]
                ?: throw IllegalStateException("Couldn't find STATETRANSITION ${this.idref}")
            stateTransitionRef.setStateTransition(stateTransition.getRef())
        }

        return stateTransitionRef.build()
    }

    private fun STATETRANSITIONREF.getRef(): StateTransitionRef.Ref {
        val stateTransitionRef =
            stateTransitionRefMap.getOrCreate(this) { it.toProtoBuf() }
        return StateTransitionRef.Ref.newBuilder()
            .setRef(ObjectID.newBuilder().setValue(stateTransitionRef.id.value).build()).build()
    }

    private fun PRECONDITIONSTATEREF.toProtoBuf(): PreConditionStateRef {
        val preconditionStateRef = PreConditionStateRef.newBuilder()

        preconditionStateRef.setId(objectId())
        val state = odx.states[this.idref] ?: throw IllegalStateException("Couldn't find STATE ${this.idref}")
        preconditionStateRef.setState(state.getRef())
        this.value?.let { preconditionStateRef.setValue(it) }
        this.inparamifsnref?.let { TODO("inparamifsnref not supported in ${this}") }
        this.inparamifsnpathref?.let { TODO("inparamifsnpathref not supported in ${this}") }

        return preconditionStateRef.build()
    }

    private fun PRECONDITIONSTATEREF.getRef(): PreConditionStateRef.Ref {
        val ref =
            preConditionStateRefsMap.getOrCreate(this) { it.toProtoBuf() }
        return PreConditionStateRef.Ref.newBuilder().setRef(ObjectID.newBuilder().setValue(ref.id.value).build())
            .build()
    }

    private fun MATCHINGBASEVARIANTPARAMETER.toProtoBuf(): MatchingParameter {
        val matchingParameter = MatchingParameter.newBuilder()

        matchingParameter.setExpectedValue(this.expectedvalue)
        val diagService = odx.diagServices.values.firstOrNull { it.shortname == this.diagcommsnref.shortname }
            ?: throw IllegalStateException("Couldn't find diag service ${this.diagcommsnref.shortname}")

        matchingParameter.setDiagService(diagService.getRef())

        if (this.outparamifsnref != null) {
            throw IllegalStateException("Unsupported outparam if ef")
        }

        if (this.outparamifsnpathref != null) {
            throw IllegalStateException("Unsupported outparam if sn path ref")
        }

        this.isUSEPHYSICALADDRESSING?.let { matchingParameter.setUsePhysicalAddressing(it) }

        return matchingParameter.build()
    }

    private fun MATCHINGPARAMETER.toProtoBuf(): MatchingParameter {
        val matchingParameter = MatchingParameter.newBuilder()

        matchingParameter.setExpectedValue(this.expectedvalue)
        val diagService = odx.diagServices.values.firstOrNull { it.shortname == this.diagcommsnref.shortname }
            ?: throw IllegalStateException("Couldn't find diag service ${this.diagcommsnref.shortname}")

        matchingParameter.setDiagService(diagService.getRef())

        this.outparamifsnref?.shortname?.let { expectedShortName ->
            val param = diagService.posresponserefs?.posresponseref?.flatMap { pr ->
                    val posResponse = odx.posResponses[pr.idref] ?: throw IllegalStateException("Couldn't find pos response ${pr.idref}")
                    posResponse.params?.param ?: emptyList()
                }?.firstOrNull { params ->
                    params.shortname == expectedShortName
                } ?: throw IllegalStateException("Couldn't find param for shortName $expectedShortName")

            matchingParameter.setOutParam(param.getRef())
        }

        if (this.outparamifsnpathref != null) {
            throw IllegalStateException("Unsupported outparam if sn path ref")
        }

        return matchingParameter.build()
    }

    private fun ECUVARIANTPATTERN.toProtoBuf(): VariantPattern {
        val variantPattern = VariantPattern.newBuilder()

        this.matchingparameters?.matchingparameter?.let { variantPattern.addAllMatchingParameter(it.map { p -> p.toProtoBuf() }) }

        return variantPattern.build()
    }

    private fun COMPARAM.toProtoBuf(): ComParam {
        val comParam = ComParam.newBuilder()

        comParam.setId(objectId())
        comParam.setComParamType(ComParam.ComParamType.REGULAR)
        comParam.setShortName(this.shortname)
        this.longname?.let { comParam.setLongName(it.toProtoBuf()) }

        this.paramclass?.let { comParam.setParamClass(it) }
        this.cptype?.let { comParam.setCpType(it.toProtoBufEnum()) }
        this.cpusage?.let { comParam.setCpUsage(it.toProtoBufEnum()) }
        this.displaylevel?.let { comParam.setDisplayLevel(it.toInt()) }


        val regularComParam = RegularComParam.newBuilder()
        this.physicaldefaultvalue?.let {
            regularComParam.setPhysicalDefaultValue(it)
        }
        this.dataobjectpropref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find ${it.idref}")
            regularComParam.setDop(dop.getRef())
        }
        comParam.setRegular(regularComParam.build())

        return comParam.build()
    }

    private fun COMPLEXCOMPARAM.toProtoBuf(): ComParam {
        val comParam = ComParam.newBuilder()

        comParam.setId(objectId())
        comParam.setComParamType(ComParam.ComParamType.COMPLEX)
        comParam.setShortName(this.shortname)
        this.longname?.let { comParam.setLongName(it.toProtoBuf()) }

        this.paramclass?.let { comParam.setParamClass(it) }
        this.cptype?.let { comParam.setCpType(it.toProtoBufEnum()) }
        this.cpusage?.let { comParam.setCpUsage(it.toProtoBufEnum()) }
        this.displaylevel?.let { comParam.setDisplayLevel(it.toInt()) }

        val complexComParam = ComplexComParam.newBuilder()

        complexComParam.addAllComParams(this.comparamOrCOMPLEXCOMPARAM.map { it.getRef() })
        complexComParam.addAllComplexPhysicalDefaultValues(this.complexphysicaldefaultvalue?.complexvalues?.complexvalue?.map { it.toProtoBuf() }
            ?: emptyList())

        comParam.setComplex(complexComParam.build())

        return comParam.build()
    }

    private fun BASECOMPARAM.getRef(): ComParam.Ref {
        val comParam: ObjectID
        if (this is COMPARAM) {
            comParam = this.getObjectRef()
        } else if (this is COMPLEXCOMPARAM) {
            comParam = this.getObjectRef()
        } else {
            throw IllegalStateException("Unhandled Comparam-type ${this::class.java.simpleName}")
        }
        return ComParam.Ref.newBuilder().setRef(comParam)
            .build()
    }

    private fun COMPLEXVALUE.toProtoBuf(): ComplexValue {
        val complexValue = ComplexValue.newBuilder()

        complexValue.setId(objectId())
        complexValue.addAllEntries(this.simplevalueOrCOMPLEXVALUE.map {
            val values = ComplexValueEntry.newBuilder()
            if (it is SIMPLEVALUE) {
                values.setSimpleValue(it.value)
            } else if (it is COMPLEXVALUE) {
                values.setComplexValue(it.toProtoBuf())
            }
            values.build()
        })

        return complexValue.build()
    }

    private fun COMPARAMSUBSET.toProtoBuf(): ComParamSubSet {
        val cpSubSet = ComParamSubSet.newBuilder()

        cpSubSet.setId(objectId())

        this.comparams?.comparam?.let { cpSubSet.addAllComParams(it.map { cp -> cp.getRef() }) }
        this.complexcomparams?.complexcomparam?.let { cpSubSet.addAllComplexComParams(it.map { ccp -> ccp.getRef() }) }
        this.dataobjectprops?.dataobjectprop?.let { cpSubSet.addAllDataObjectProps(it.map { cp -> cp.getRef() }) }
        this.unitspec?.let { cpSubSet.setUnitSpec(it.toProtoBuf()) }

        return cpSubSet.build()
    }

    private fun UNITSPEC.toProtoBuf(): UnitSpec {
        val unitSpec = UnitSpec.newBuilder()

        this.unitgroups?.unitgroup?.let { unitSpec.addAllUnitGroups(it.map { unitGroup -> unitGroup.toProtoBuf() }) }
        this.physicaldimensions?.physicaldimension?.let { unitSpec.addAllPhysicalDimensions(it.map { physDimension -> physDimension.toProtoBuf() }) }
        this.units?.unit?.let { unitSpec.addAllUnits(it.map { unit -> unit.toProtoBuf() }) }
        this.sdgs?.let { unitSpec.setSdgs(it.toProtoBuf()) }

        return unitSpec.build()
    }

    private fun PHYSICALDIMENSION.toProtoBuf(): PhysicalDimension {
        val physDim = PhysicalDimension.newBuilder()

        physDim.setId(objectId())
        physDim.setShortName(this.shortname)

        this.longname?.let { physDim.setLongName(it.toProtoBuf()) }
        this.currentexp?.let { physDim.setCurrentExp(it) }
        this.lengthexp?.let { physDim.setLengthExp(it) }
        this.massexp?.let { physDim.setMassExp(it) }
        this.molaramountexp?.let { physDim.setMolarAmountExp(it) }
        this.luminousintensityexp?.let { physDim.setLuminousIntensityExp(it) }
        this.temperatureexp?.let { physDim.setTemperatureExp(it) }
        this.timeexp?.let { physDim.setTimeExp(it) }

        return physDim.build()
    }

    private fun UNITGROUP.toProtoBuf(): UnitGroup {
        val unitGroup = UnitGroup.newBuilder()

        unitGroup.setShortName(this.shortname)
        this.longname?.let { unitGroup.setLongName(it.toProtoBuf()) }
        this.unitrefs?.unitref?.let {
            unitGroup.addAllUnitrefs(it.map { unitRef ->
                val unit =
                    odx.units[unitRef.idref] ?: throw IllegalStateException("Couldn't find unit ${unitRef.idref}")
                unit.getRef()
            })
        }
        return unitGroup.build()
    }

    private fun PROTOCOL.toProtoBuf(): Protocol {
        val protocol = Protocol.newBuilder()

        protocol.setId(objectId())
        protocol.setDiagLayer((this as HIERARCHYELEMENT).toProtoBuf())

        this.comparamspecref?.let {
            val comParamSpec =
                odx.comparamSpecs[it.idref] ?: throw IllegalStateException("Couldn't find com param spec ${it.idref}")
            protocol.setComParamSpec(comParamSpec.toProtoBuf())
        }

        this.protstacksnref?.let { shortNameRef ->
            val stack = odx.comparamSpecs.values.flatMap { it.protstacks?.protstack ?: emptyList() }
                .firstOrNull { it.shortname == shortNameRef.shortname }
                ?: throw IllegalStateException("Couldn't find protstack with short name ${shortNameRef.shortname}")
            protocol.setProtStack(stack.getRef())
        }

        if (this.parentrefs != null) {
            TODO("Prot stack parent refs not supported")
        }

        return protocol.build()
    }

    private fun COMPARAMSPEC.toProtoBuf(): ComParamSpec {
        val comParamSpec = ComParamSpec.newBuilder()

        this.protstacks?.protstack?.let { comParamSpec.addAllProtStacks(it.map { ref -> ref.getRef() }) }

        return comParamSpec.build()
    }

    private fun PROTSTACK.toProtoBuf(): ProtStack {
        val protStack = ProtStack.newBuilder()

        protStack.setId(objectId())
        protStack.setShortName(this.shortname)
        this.longname?.let { protStack.setLongName(it.toProtoBuf()) }
        this.comparamsubsetrefs?.comparamsubsetref?.let {
            protStack.addAllComparamSubSetRefs(it.map { ref ->
                val comparamSubSet = odx.comParamSubSets[ref.idref]
                    ?: throw IllegalStateException("Couldn't find com param subset ${ref.idref}")
                comparamSubSet.getRef()
            })
        }
        this.physicallinktype?.let { protStack.setPhysicalLinkType(it) }
        this.pduprotocoltype?.let { protStack.setPduProtocolType(it) }

        return protStack.build()
    }

    private fun PROTSTACK.getRef(): ProtStack.Ref {
        val protStack = this.getObjectRef()

        return ProtStack.Ref.newBuilder().setRef(protStack).build()
    }


    private fun TABLE.getRef(): Table.Ref {
        val table = this.getObjectRef()

        return Table.Ref.newBuilder().setRef(table).build()
    }

    private fun TABLEROW.getRef(): TableRow.Ref {
        val ref = this.getObjectRef()
        return TableRow.Ref.newBuilder().setRef(ref).build()
    }

    private fun COMPARAMSUBSET.getRef(): ComParamSubSet.Ref {
        val ref = this.getObjectRef()

        return ComParamSubSet.Ref.newBuilder().setRef(ref).build()
    }

    private fun PHYSICALDIMENSION.getRef(): PhysicalDimension.Ref {
        val physDim =
            this.getObjectRef()
        return PhysicalDimension.Ref.newBuilder().setRef(physDim).build()
    }

    private fun COMPARAMREF.toProtoBuf(): ComParamRef {
        val comParamRef = ComParamRef.newBuilder()

        val comParam = odx.comparams[this.idref]
            ?: odx.complexComparams[this.idref]

        if (comParam == null) {
            if (!options.lenient) {
                throw IllegalStateException("Couldn't find COMPARAM ${this.idref} @ ${this.docref}")
            }
            logger.warning("Couldn't find COMPARAM ${this.idref} @ ${this.docref}")
        } else {
            comParamRef.setComParam(comParam.getRef())
        }

        this.protocolsnref?.shortname?.let { shortName ->
            val protocolOdx = odx.protocols.values.firstOrNull { it.shortname == shortName }  ?: throw IllegalStateException("Couldn't find PROTOCOL $shortName")
            val protocol = protocolOdx.getObjectRef()
            comParamRef.setProtocol(
                Protocol.Ref.newBuilder().setRef(protocol).build()
            )
        }

        this.protstacksnref?.let {
            val protStackOdx = odx.protStacks.values.firstOrNull { it.shortname == this.protstacksnref.shortname } ?:
                                    throw IllegalStateException("Can't find protocol ${this.protstacksnref.shortname}")

            val protStack = protStackOdx.getObjectRef()

            comParamRef.setProtStack(
                ProtStack.Ref.newBuilder().setRef(protStack).build()
            )
        }
        this.simplevalue?.value?.let { comParamRef.setSimpleValue(it) }
        this.complexvalue?.let { comParamRef.setComplexValue(it.toProtoBuf()) }

        return comParamRef.build()
    }

    private fun PROTOCOL.getRef(): Protocol.Ref {
        val protocolId = this.getObjectRef()
        return Protocol.Ref.newBuilder().setRef(protocolId).build()
    }

    private fun LIBRARY.toProtoBuf(): Library {
        val library = Library.newBuilder()

        library.setId(objectId())

        library.setShortName(this.shortname)
        this.longname?.let { library.setLongName(it.toProtoBuf()) }
        library.setCodeFile(this.codefile)
        this.encryption?.let { library.setEncryption(it) }
        library.setSyntax(this.syntax)
        this.entrypoint?.let { library.setEntryPoint(it) }

        return library.build()
    }

    private fun LIBRARY.getRef(): Library.Ref {
        val ref = this.getObjectRef()

        return Library.Ref.newBuilder().setRef(ref).build()
    }

    private fun SWITCHKEY.toProtoBuf(): DOP.MUXDOP.SwitchKey {
        val switchKey = DOP.MUXDOP.SwitchKey.newBuilder()
        switchKey.bytePosition = this.byteposition.toInt()
        this.bitposition?.let { switchKey.bitPosition = it.toInt() }
        val dop = odx.combinedDataObjectProps[this.dataobjectpropref.idref] ?: throw IllegalStateException("Couldn't find dop-ref ${this.dataobjectpropref.idref}")
        switchKey.dop = dop.getRef()
        return switchKey.build()
    }

    private fun DEFAULTCASE.toProtoBuf(): DOP.MUXDOP.DefaultCase {
        val defaultCase = DOP.MUXDOP.DefaultCase.newBuilder()
        defaultCase.shortName = this.shortname
        this.longname?.let { defaultCase.longName = it.toProtoBuf() }
        this.structureref?.let {
            val dop = odx.combinedDataObjectProps[it.idref] ?: throw IllegalStateException("Couldn't find dop-structure-ref ${this.structureref.idref}")
            defaultCase.structure = dop.getRef()
        }
        this.structuresnref?.shortname?.let {
            TODO("STRUCTURE shortnameref not supported for $this")
        }

        return defaultCase.build()
    }

    private fun CASE.toProtoBuf(): DOP.MUXDOP.Case {
        val case = DOP.MUXDOP.Case.newBuilder()
        case.shortName = this.shortname
        this.longname?.let { case.longName = it.toProtoBuf() }
        val dop = odx.combinedDataObjectProps[this.structureref.idref] ?: throw IllegalStateException("Couldn't find dop-structure-ref ${this.structureref.idref}")
        this.structuresnref?.shortname?.let {
            TODO("STRUCTURE shortnameref not supported for $this")
        }
        case.structure = dop.getRef()
        case.lowerLimit = this.lowerlimit.toProtoBuf()
        case.upperLimit = this.upperlimit.toProtoBuf()

        return case.build()
    }

    private fun TABLEROW.toProtoBuf(): TableRow {
        val tableRow = TableRow.newBuilder()
        tableRow.setId(objectId())
        tableRow.setShortName(this.shortname)
        this.semantic?.let { tableRow.semantic = it }

        this.longname?.let { tableRow.longName = it.toProtoBuf() }
        this.key?.let { tableRow.setKey(it) }

        this.dataobjectpropref?.idref?.let {
            val dop = odx.combinedDataObjectProps[it] ?: throw IllegalStateException("Couldn't find dop $it")
            tableRow.dop = dop.getRef()
        }
        if (this.dataobjectpropsnref != null) {
            error("Unsupported data object prop shortname ref ${this.structuresnref}")
        }
        this.structureref?.idref?.let {
            val structureDop = odx.structures[it] ?: throw IllegalStateException("Couldn't find structure $it")
            tableRow.structure = structureDop.getRef()
        }

        if (this.structuresnref != null) {
            error("Unsupported structure shortname ref ${this.structuresnref}")
        }
        this.sdgs?.getRef()?.let { tableRow.sdgs = it }
        this.audience?.let { tableRow.audience = it.getRef() }
        tableRow.addAllFunctClassRefs(this.functclassrefs?.functclassref?.map {
            val functClass = odx.functClasses[it.idref] ?: throw IllegalStateException("Couldn't find funct class ${it.idref}")
            functClass.getRef()
        } ?: emptyList())

        tableRow.addAllStateTransitionRefs(this.statetransitionrefs?.statetransitionref?.map { it.getRef() } ?: emptyList())
        tableRow.addAllPreConditionStateRefs(this.preconditionstaterefs?.preconditionstateref?.map { it.getRef() } ?: emptyList())

        tableRow.setIsExecutable(this.isISEXECUTABLE)
        tableRow.setIsMandatory(this.isISMANDATORY)
        tableRow.setIsFinal(this.isISFINAL)

        return tableRow.build()
    }

    private inline fun <K: Any, V: Any> MutableMap<K, V>.getOrCreate(k: K, create: (K) -> V): V {
        var value = this[k]
        if (value == null) {
            value = create(k)
            this[k] = value
        }
        return value
    }

    private fun <K: Any, V: Any> MutableMap<K, V>.fillMissing(keys: Collection<K>, create: (K) -> V) {
        keys.forEach {
            if (!this.containsKey(it)) {
                this[it] = create(it)
            }
        }
    }
}

private fun ROWFRAGMENT.toProtoBuf(): Param.TableEntry.RowFragment =
    when (this) {
        ROWFRAGMENT.KEY -> Param.TableEntry.RowFragment.KEY
        ROWFRAGMENT.STRUCT -> Param.TableEntry.RowFragment.STRUCT
    }
