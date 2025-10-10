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

import schema.odx.ADDITIONALAUDIENCE
import schema.odx.AUDIENCE
import schema.odx.BASEVARIANT
import schema.odx.BASICSTRUCTURE
import schema.odx.CODEDCONST
import schema.odx.COMPARAM
import schema.odx.COMPARAMSPEC
import schema.odx.COMPARAMSUBSET
import schema.odx.COMPLEXCOMPARAM
import schema.odx.COMPUSCALE
import schema.odx.DATAOBJECTPROP
import schema.odx.DIAGCODEDTYPE
import schema.odx.DIAGDATADICTIONARYSPEC
import schema.odx.DIAGLAYERCONTAINER
import schema.odx.DIAGSERVICE
import schema.odx.DOPBASE
import schema.odx.DTC
import schema.odx.DTCDOP
import schema.odx.DYNAMICENDMARKERFIELD
import schema.odx.DYNAMICLENGTHFIELD
import schema.odx.ECUSHAREDDATA
import schema.odx.ECUVARIANT
import schema.odx.ENDOFPDUFIELD
import schema.odx.ENVDATA
import schema.odx.ENVDATADESC
import schema.odx.FUNCTCLASS
import schema.odx.FUNCTIONALGROUP
import schema.odx.GLOBALNEGRESPONSE
import schema.odx.LENGTHKEY
import schema.odx.LIBRARY
import schema.odx.MUX
import schema.odx.NEGRESPONSE
import schema.odx.NRCCONST
import schema.odx.ODX
import schema.odx.PARAM
import schema.odx.PHYSICALDIMENSION
import schema.odx.POSRESPONSE
import schema.odx.PRECONDITIONSTATEREF
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.REQUEST
import schema.odx.RESPONSE
import schema.odx.SD
import schema.odx.SDG
import schema.odx.SDGS
import schema.odx.SINGLEECUJOB
import schema.odx.STATE
import schema.odx.STATECHART
import schema.odx.STATETRANSITION
import schema.odx.STATETRANSITIONREF
import schema.odx.STATICFIELD
import schema.odx.STRUCTURE
import schema.odx.TABLE
import schema.odx.TABLEKEY
import schema.odx.TABLEROW
import schema.odx.UNIT
import schema.odx.UNITSPEC
import schema.odx.VALUE
import kotlin.collections.plus

class ODXCollection(val data: Map<String, ODX>) {
    val ecuName: String by lazy {
        baseVariantODX.diaglayercontainer?.basevariants?.basevariant?.first()?.shortname
            ?: throw IllegalStateException("No base variant")
    }
    val odxRevision: String by lazy {
        // sort by date, or semantic version of revision?
        baseVariantODX.diaglayercontainer?.admindata?.docrevisions?.docrevision?.lastOrNull()?.revisionlabel
            ?: throw IllegalStateException("No doc revisions")
    }

    val diagLayerContainer: Map<String, DIAGLAYERCONTAINER> by lazy {
        data.values
            .mapNotNull { it.diaglayercontainer }
            .associateBy { it.id }
    }

    val baseVariantODX: ODX by lazy {
        data.values.first { it.diaglayercontainer?.basevariants?.basevariant != null }
    }

    val ecuSharedDatas: Map<String, ECUSHAREDDATA> by lazy {
        val data = diagLayerContainer.flatMap { it.value.ecushareddatas?.ecushareddata ?: emptyList() }

        data.associateBy { it.id }
    }

    val functClasses: Map<String, FUNCTCLASS> by lazy {
        val data = basevariants.flatMap { it.value.functclasss?.functclass ?: emptyList() } +
                ecuvariants.flatMap { it.value.functclasss?.functclass ?: emptyList() } +
                ecuSharedDatas.flatMap { it.value.functclasss?.functclass ?: emptyList() }
        data.associateBy { it.id }
    }

    val basevariants: Map<String, BASEVARIANT> by lazy {
        data.values
            .flatMap { it.diaglayercontainer?.basevariants?.basevariant ?: emptyList() }
            .associateBy { it.id }
    }

    val ecuvariants: Map<String, ECUVARIANT> by lazy {
        data.values
            .flatMap { it.diaglayercontainer?.ecuvariants?.ecuvariant ?: emptyList() }
            .associateBy { it.id }
    }

    val functionalGroups: Map<String, FUNCTIONALGROUP> by lazy {
        val data = data.values
            .flatMap { it.diaglayercontainer?.functionalgroups?.functionalgroup ?: emptyList() }

        data.associateBy { it.id }
    }

    val diagServices: Map<String, DIAGSERVICE> by lazy {
        basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<DIAGSERVICE>()
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .associateBy { it.id }
    }

    val singleEcuJobs: Map<String, SINGLEECUJOB> by lazy {
        basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<SINGLEECUJOB>()
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<SINGLEECUJOB>()
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<SINGLEECUJOB>()
                    .associateBy { it.id }
    }

    val params: Set<PARAM> by lazy {
        (requests.values.flatMap { it.params?.param ?: emptyList() } +
                posResponses.values.flatMap { it.params?.param ?: emptyList() } +
                negResponses.values.flatMap { it.params?.param ?: emptyList() } +
                globalNegResponses.values.flatMap { it.params?.param ?: emptyList() } +
                combinedDataObjectProps.values.filterIsInstance<BASICSTRUCTURE>()
                    .flatMap { it.params?.param ?: emptyList() } +
                envDatas.values.flatMap { it.params?.param ?: emptyList() }
                ).toSet()
    }

    val tableKeys: Map<String, TABLEKEY> by lazy {
        params.filterIsInstance<TABLEKEY>().associateBy { it.id }
    }

    val lengthKeys: Map<String, LENGTHKEY> by lazy {
        params.filterIsInstance<LENGTHKEY>().associateBy { it.id }
    }

    val requests: Map<String, REQUEST> by lazy {
        basevariants.values
            .flatMap { it.requests?.request ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.requests?.request ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.requests?.request ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.requests?.request ?: emptyList() }
                    .associateBy { it.id }
    }

    val responses: Set<RESPONSE> by lazy {
        (posResponses.values + negResponses.values + globalNegResponses.values).toSet()
    }

    val posResponses: Map<String, POSRESPONSE> by lazy {
        basevariants.values
            .flatMap { it.posresponses?.posresponse ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.posresponses?.posresponse ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.posresponses?.posresponse ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.posresponses?.posresponse ?: emptyList() }
                    .associateBy { it.id }
    }

    val globalNegResponses: Map<String, GLOBALNEGRESPONSE> by lazy {
        basevariants.values
            .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
                    .associateBy { it.id }
    }

    val negResponses: Map<String, NEGRESPONSE> by lazy {
        basevariants.values
            .flatMap { it.negresponses?.negresponse ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.negresponses?.negresponse ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.negresponses?.negresponse ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.negresponses?.negresponse ?: emptyList() }
                    .associateBy { it.id }
    }

    val comparams: Map<String, COMPARAM> by lazy {
        comParamSubSets.values
            .flatMap { it.comparams?.comparam ?: emptyList() }
            .associateBy { it.id } +
                complexComparams.values
                    .flatMap { it.comparamOrCOMPLEXCOMPARAM ?: emptyList() }
                    .filterIsInstance<COMPARAM>()
                    .associateBy { it.id }
    }

    val complexComparams: Map<String, COMPLEXCOMPARAM> by lazy {
        comParamSubSets.values.flatMap { it.complexcomparams?.complexcomparam ?: emptyList() }
            .associateBy { it.id }
    }

    val comParamSubSets: Map<String, COMPARAMSUBSET> by lazy {
        val data = data.values.flatMap { listOf(it.comparamsubset) }.filterNotNull()
        data.associateBy { it.id }
    }

    val diagDataDictionaries: List<DIAGDATADICTIONARYSPEC> by lazy {
        basevariants.values.mapNotNull { it.diagdatadictionaryspec } +
                ecuvariants.values.mapNotNull { it.diagdatadictionaryspec } +
                functionalGroups.values.mapNotNull { it.diagdatadictionaryspec } +
                ecuSharedDatas.values.mapNotNull { it.diagdatadictionaryspec }
    }

    val diagCodedTypes: Set<DIAGCODEDTYPE> by lazy {
        val data = dataObjectProps.values.flatMap { listOf(it.diagcodedtype) } +
                params.filterIsInstance<CODEDCONST>().flatMap { listOf(it.diagcodedtype) } +
                params.filterIsInstance<NRCCONST>().flatMap { listOf(it.diagcodedtype) } +
                dtcDops.values.flatMap { listOf(it.diagcodedtype) }

        data.filterNotNull().toSet()
    }

    val combinedDataObjectProps: Map<String, DOPBASE> by lazy {
        dataObjectProps + dtcDops + structures + staticfields + endofpdufields + dynLengthFields +
                dynEndMarkerFields + muxs + envDatas + envDataDescs
    }

    val dataObjectProps: Map<String, DATAOBJECTPROP> by lazy {
        val data = diagDataDictionaries
            .flatMap { it.dataobjectprops?.dataobjectprop ?: emptyList() } +
                comParamSubSets.values
                    .flatMap { it.dataobjectprops?.dataobjectprop ?: emptyList() }

        data.associateBy { it.id }
    }

    val dtcDops: Map<String, DTCDOP> by lazy {
        diagDataDictionaries
            .flatMap { it.dtcdops?.dtcdop ?: emptyList() }
            .associateBy { it.id }
    }

    val envDatas: Map<String, ENVDATA> by lazy {
        diagDataDictionaries
            .flatMap { it.envdatas?.envdata ?: emptyList() }
            .associateBy { it.id }
    }

    val envDataDescs: Map<String, ENVDATADESC> by lazy {
        diagDataDictionaries
            .flatMap { it.envdatadescs?.envdatadesc ?: emptyList() }
            .associateBy { it.id }
    }

    val structures: Map<String, STRUCTURE> by lazy {
        diagDataDictionaries
            .flatMap { it.structures?.structure ?: emptyList() }
            .associateBy { it.id }
    }

    val tables: Map<String, TABLE> by lazy {
        diagDataDictionaries
            .flatMap { it.tables?.table ?: emptyList() }
            .associateBy { it.id }
    }

    val tableRows: Map<String, TABLEROW> by lazy {
        diagDataDictionaries
            .flatMap { it.tables?.table ?: emptyList() }
            .flatMap { it.rowwrapper }
            .map {
                it as? TABLEROW ?: error("Unexpected type: ${it::class.java.simpleName}")
            }.associateBy { it.id }
    }

    val endofpdufields: Map<String, ENDOFPDUFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.endofpdufields?.endofpdufield ?: emptyList() }
            .associateBy { it.id }
    }

    val staticfields: Map<String, STATICFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.staticfields?.staticfield ?: emptyList() }
            .associateBy { it.id }
    }

    val dynLengthFields: Map<String, DYNAMICLENGTHFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.dynamiclengthfields?.dynamiclengthfield ?: emptyList() }
            .associateBy { it.id }
    }

    val dynEndMarkerFields: Map<String, DYNAMICENDMARKERFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.dynamicendmarkerfields?.dynamicendmarkerfield ?: emptyList() }
            .associateBy { it.id }
    }

    val muxs: Map<String, MUX> by lazy {
        diagDataDictionaries
            .flatMap { it.muxs?.mux ?: emptyList() }
            .associateBy { it.id }

    }

    val units: Map<String, UNIT> by lazy {
        diagDataDictionaries
            .flatMap { it.unitspec?.units?.unit ?: emptyList() }
            .associateBy { it.id } +
                data.values
                    .flatMap {
                        (it.comparamsubset?.unitspec?.units?.unit ?: emptyList())
                    }
                    .associateBy { it.id }
    }

    val sds: Set<SD> by lazy {
        val sds = sdgss.flatMap { it.sdg }.flatMap { it.sdgOrSD.filterIsInstance<SD>() }

        sds.toSet()
    }

    val sdgs: Set<SDG> by lazy {
        sdgss.flatMap { it.sdg }.toSet()
    }

    val sdgss: List<SDGS> by lazy {
        val data =
            diagDataDictionaries.flatMap { listOf(it.sdgs) } +
                    diagServices.flatMap { listOf(it.value.sdgs) } +
                    singleEcuJobs.flatMap { listOf(it.value.sdgs) } +
                    diagLayerContainer.values.flatMap { listOf(it.sdgs) } +
                    basevariants.values.flatMap { listOf(it.sdgs) } +
                    ecuvariants.values.flatMap { listOf(it.sdgs) } +
                    functionalGroups.values.flatMap { listOf(it.sdgs) } +
                    requests.values.flatMap { listOf(it.sdgs) } +
                    posResponses.values.flatMap { listOf(it.sdgs) } +
                    negResponses.values.flatMap { listOf(it.sdgs) } +
                    globalNegResponses.values.flatMap { listOf(it.sdgs) } +
                    params.flatMap { listOf(it.sdgs) } +
                    combinedDataObjectProps.values.flatMap { listOf(it.sdgs) } +
                    dtcs.values.flatMap { listOf(it.sdgs) } +
                    tables.values.flatMap { listOf(it.sdgs) } +
                    tableRows.values.flatMap { listOf(it.sdgs) }


        data.filterNotNull()
    }

    val dtcs: Map<String, DTC> by lazy {
        dtcDops.values.flatMap { it.dtcs?.dtcproxy?.filterIsInstance<DTC>() ?: emptyList() }.associateBy { it.id }
    }

    val additionalAudiences: Map<String, ADDITIONALAUDIENCE> by lazy {
        val data = basevariants.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() } +
                ecuvariants.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() } +
                functionalGroups.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() } +
                ecuSharedDatas.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() }

        data.associateBy { it.id }
    }

    val stateCharts: Map<String, STATECHART> by lazy {
        val data = basevariants.values.flatMap { it.statecharts?.statechart ?: emptyList() } +
                ecuvariants.values.flatMap { it.statecharts?.statechart ?: emptyList() } +
                functionalGroups.values.flatMap { it.statecharts?.statechart ?: emptyList() } +
                ecuSharedDatas.values.flatMap { it.statecharts?.statechart ?: emptyList() }
        data.associateBy { it.id }
    }

    val states: Map<String, STATE> by lazy {
        stateCharts.values.flatMap { it.states?.state ?: emptyList() }.associateBy { it.id }
    }

    val stateTransitions: Map<String, STATETRANSITION> by lazy {
        stateCharts.values.flatMap { it.statetransitions?.statetransition ?: emptyList() }
            .associateBy { it.id }
    }

    val stateTransitionsRefs: Set<STATETRANSITIONREF> by lazy {
        val data = basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<DIAGSERVICE>()
            .flatMap { it.statetransitionrefs?.statetransitionref ?: emptyList() } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .flatMap { it.statetransitionrefs?.statetransitionref ?: emptyList() } +
                tableRows.values.flatMap { it.statetransitionrefs?.statetransitionref ?: emptyList() }

        data.toSet()
    }

    val unitSpecs: Set<UNITSPEC> by lazy {
        val data = comParamSubSets.values.flatMap { listOf(it.unitspec) } +
                diagDataDictionaries.flatMap { listOf(it.unitspec) }

        data.filterNotNull().toSet()
    }

    val protocols: Map<String, PROTOCOL> by lazy {
        diagLayerContainer.values.flatMap { it.protocols?.protocol ?: emptyList() }
            .associateBy { it.id }
    }

    val comparamSpecs: Map<String, COMPARAMSPEC> by lazy {
        data.values.flatMap { listOf(it.comparamspec) }
            .filterNotNull()
            .associateBy { it.id }
    }

    val physDimensions: Map<String, PHYSICALDIMENSION> by lazy {
        unitSpecs.flatMap { it.physicaldimensions?.physicaldimension ?: emptyList() }
            .associateBy { it.id }
    }

    val protStacks: Map<String, PROTSTACK> by lazy {
        comparamSpecs.values
            .flatMap { it.protstacks?.protstack ?: emptyList() }
            .associateBy { it.id }
    }

    val libraries: Map<String, LIBRARY> by lazy {
        val data = basevariants.values.flatMap { it.librarys?.library ?: emptyList() } +
                ecuvariants.values.flatMap { it.librarys?.library ?: emptyList() } +
                functionalGroups.values.flatMap { it.librarys?.library ?: emptyList() }

        data.associateBy { it.id }
    }

}