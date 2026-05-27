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
import schema.odx.COMPARAMSPEC
import schema.odx.COMPARAMSUBSET
import schema.odx.COMPLEXCOMPARAM
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
import schema.odx.ODX
import schema.odx.PARAM
import schema.odx.PHYSICALDIMENSION
import schema.odx.POSRESPONSE
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.REQUEST
import schema.odx.RESPONSE
import schema.odx.SD
import schema.odx.SDG
import schema.odx.SDGCAPTION
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

/**
 * Aggregates multiple [ODXCollection] instances (one per ODX file) and provides
 * cross-file merged views of all IDs and objects.
 */
class ODXCollectionGroup(
    val data: Map<String, ODX>,
    val rawSize: Long,
) {
    /** Individual per-file collections, keyed by the container short-name. */
    val collections: Map<String, ODXCollection> by lazy {
        data.values
            .map { ODXCollection(it) }
            .associateBy { it.containerKey }
    }

    val ecuName: String by lazy {
        val ecuName =
            baseVariantODX
                ?.diaglayercontainer
                ?.basevariants
                ?.basevariant
                ?.firstOrNull()
                ?.shortname
        ecuName
            ?: if (functionalGroupODX != null) {
                "functional_groups"
            } else {
                error("No base variant")
            }
    }

    val odxRevision: String? by lazy {
        baseVariantODX
            ?.diaglayercontainer
            ?.admindata
            ?.docrevisions
            ?.docrevision
            ?.lastOrNull()
            ?.revisionlabel
            ?: functionalGroupODX
                ?.diaglayercontainer
                ?.admindata
                ?.docrevisions
                ?.docrevision
                ?.lastOrNull()
                ?.revisionlabel
    }

    val diagLayerContainer: Map<String, DIAGLAYERCONTAINER> by lazy {
        collections.values
            .mapNotNull { it.diagLayerContainer }
            .associateBy { it.id }
    }

    val baseVariantODX: ODX? by lazy {
        data.values.firstOrNull { it.diaglayercontainer?.basevariants?.basevariant != null }
    }

    val functionalGroupODX: ODX? by lazy {
        data.values.firstOrNull {
            it.diaglayercontainer
                ?.functionalgroups
                ?.functionalgroup
                ?.isNotEmpty() == true
        }
    }

    val ecuSharedDatas: Map<String, ECUSHAREDDATA> by lazy {
        collections.values.flatMap { it.ecuSharedDatas.entries }.associate { it.toPair() }
    }

    val functClasses: Map<String, FUNCTCLASS> by lazy {
        collections.values.flatMap { it.functClasses.entries }.associate { it.toPair() }
    }

    val basevariants: Map<String, BASEVARIANT> by lazy {
        collections.values.flatMap { it.basevariants.entries }.associate { it.toPair() }
    }

    val ecuvariants: Map<String, ECUVARIANT> by lazy {
        collections.values.flatMap { it.ecuvariants.entries }.associate { it.toPair() }
    }

    val functionalGroups: Map<String, FUNCTIONALGROUP> by lazy {
        collections.values.flatMap { it.functionalGroups.entries }.associate { it.toPair() }
    }

    val diagServices: Map<String, DIAGSERVICE> by lazy {
        collections.values.flatMap { it.diagServices.entries }.associate { it.toPair() }
    }

    val singleEcuJobs: Map<String, SINGLEECUJOB> by lazy {
        collections.values.flatMap { it.singleEcuJobs.entries }.associate { it.toPair() }
    }

    val params: Set<PARAM> by lazy {
        collections.values.flatMap { it.params }.toSet()
    }

    val tableKeys: Map<String, TABLEKEY> by lazy {
        collections.values.flatMap { it.tableKeys.entries }.associate { it.toPair() }
    }

    val lengthKeys: Map<String, LENGTHKEY> by lazy {
        collections.values.flatMap { it.lengthKeys.entries }.associate { it.toPair() }
    }

    val requests: Map<String, REQUEST> by lazy {
        collections.values.flatMap { it.requests.entries }.associate { it.toPair() }
    }

    val responses: Set<RESPONSE> by lazy {
        collections.values.flatMap { it.responses }.toSet()
    }

    val posResponses: Map<String, POSRESPONSE> by lazy {
        collections.values.flatMap { it.posResponses.entries }.associate { it.toPair() }
    }

    val globalNegResponses: Map<String, GLOBALNEGRESPONSE> by lazy {
        collections.values.flatMap { it.globalNegResponses.entries }.associate { it.toPair() }
    }

    val negResponses: Map<String, NEGRESPONSE> by lazy {
        collections.values.flatMap { it.negResponses.entries }.associate { it.toPair() }
    }

    val comparams: Map<String, COMPARAM> by lazy {
        collections.values.flatMap { it.comparams.entries }.associate { it.toPair() }
    }

    val complexComparams: Map<String, COMPLEXCOMPARAM> by lazy {
        collections.values.flatMap { it.complexComparams.entries }.associate { it.toPair() }
    }

    val comParamSubSets: Map<String, COMPARAMSUBSET> by lazy {
        collections.values.flatMap { it.comParamSubSets.entries }.associate { it.toPair() }
    }

    val diagDataDictionaries: List<DIAGDATADICTIONARYSPEC> by lazy {
        collections.values.flatMap { it.diagDataDictionaries }
    }

    val diagCodedTypes: Set<DIAGCODEDTYPE> by lazy {
        collections.values.flatMap { it.diagCodedTypes }.toSet()
    }

    val combinedDataObjectProps: Map<String, DOPBASE> by lazy {
        collections.values.flatMap { it.combinedDataObjectProps.entries }.associate { it.toPair() }
    }

    val dataObjectProps: Map<String, DATAOBJECTPROP> by lazy {
        collections.values.flatMap { it.dataObjectProps.entries }.associate { it.toPair() }
    }

    val dtcDops: Map<String, DTCDOP> by lazy {
        collections.values.flatMap { it.dtcDops.entries }.associate { it.toPair() }
    }

    val envDatas: Map<String, ENVDATA> by lazy {
        collections.values.flatMap { it.envDatas.entries }.associate { it.toPair() }
    }

    val envDataDescs: Map<String, ENVDATADESC> by lazy {
        collections.values.flatMap { it.envDataDescs.entries }.associate { it.toPair() }
    }

    val structures: Map<String, STRUCTURE> by lazy {
        collections.values.flatMap { it.structures.entries }.associate { it.toPair() }
    }

    val tables: Map<String, TABLE> by lazy {
        collections.values.flatMap { it.tables.entries }.associate { it.toPair() }
    }

    val tableRows: Map<String, TABLEROW> by lazy {
        collections.values.flatMap { it.tableRows.entries }.associate { it.toPair() }
    }

    val endofpdufields: Map<String, ENDOFPDUFIELD> by lazy {
        collections.values.flatMap { it.endofpdufields.entries }.associate { it.toPair() }
    }

    val staticfields: Map<String, STATICFIELD> by lazy {
        collections.values.flatMap { it.staticfields.entries }.associate { it.toPair() }
    }

    val dynLengthFields: Map<String, DYNAMICLENGTHFIELD> by lazy {
        collections.values.flatMap { it.dynLengthFields.entries }.associate { it.toPair() }
    }

    val dynEndMarkerFields: Map<String, DYNAMICENDMARKERFIELD> by lazy {
        collections.values.flatMap { it.dynEndMarkerFields.entries }.associate { it.toPair() }
    }

    val muxs: Map<String, MUX> by lazy {
        collections.values.flatMap { it.muxs.entries }.associate { it.toPair() }
    }

    val units: Map<String, UNIT> by lazy {
        collections.values.flatMap { it.units.entries }.associate { it.toPair() }
    }

    val sds: Set<SD> by lazy {
        collections.values.flatMap { it.sds }.toSet()
    }

    val sdgCaptions: Map<String, SDGCAPTION> by lazy {
        collections.values.flatMap { it.sdgCaptions.entries }.associate { it.toPair() }
    }

    val sdgs: Set<SDG> by lazy {
        collections.values.flatMap { it.sdgs }.toSet()
    }

    val sdgss: List<SDGS> by lazy {
        collections.values.flatMap { it.sdgss }
    }

    val dtcs: Map<String, DTC> by lazy {
        collections.values.flatMap { it.dtcs.entries }.associate { it.toPair() }
    }

    val additionalAudiences: Map<String, ADDITIONALAUDIENCE> by lazy {
        collections.values.flatMap { it.additionalAudiences.entries }.associate { it.toPair() }
    }

    val stateCharts: Map<String, STATECHART> by lazy {
        collections.values.flatMap { it.stateCharts.entries }.associate { it.toPair() }
    }

    val states: Map<String, STATE> by lazy {
        collections.values.flatMap { it.states.entries }.associate { it.toPair() }
    }

    val stateTransitions: Map<String, STATETRANSITION> by lazy {
        collections.values.flatMap { it.stateTransitions.entries }.associate { it.toPair() }
    }

    val stateTransitionsRefs: Set<STATETRANSITIONREF> by lazy {
        collections.values.flatMap { it.stateTransitionsRefs }.toSet()
    }

    val unitSpecs: Set<UNITSPEC> by lazy {
        collections.values.flatMap { it.unitSpecs }.toSet()
    }

    val protocols: Map<String, PROTOCOL> by lazy {
        collections.values.flatMap { it.protocols.entries }.associate { it.toPair() }
    }

    val comparamSpecs: Map<String, COMPARAMSPEC> by lazy {
        collections.values.flatMap { it.comparamSpecs.entries }.associate { it.toPair() }
    }

    val physDimensions: Map<String, PHYSICALDIMENSION> by lazy {
        collections.values.flatMap { it.physDimensions.entries }.associate { it.toPair() }
    }

    val protStacks: Map<String, PROTSTACK> by lazy {
        collections.values.flatMap { it.protStacks.entries }.associate { it.toPair() }
    }

    val libraries: Map<String, LIBRARY> by lazy {
        collections.values.flatMap { it.libraries.entries }.associate { it.toPair() }
    }
}
