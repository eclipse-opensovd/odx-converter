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
import schema.odx.COMPARAMREF
import schema.odx.COMPARAMSPEC
import schema.odx.COMPARAMSUBSET
import schema.odx.COMPLEXCOMPARAM
import schema.odx.DIAGSERVICE
import schema.odx.DOPBASE
import schema.odx.DTC
import schema.odx.ECUVARIANT
import schema.odx.ENVDATA
import schema.odx.FUNCTCLASS
import schema.odx.FUNCTIONALGROUP
import schema.odx.LENGTHKEY
import schema.odx.LIBRARY
import schema.odx.NEGRESPONSE
import schema.odx.ODX
import schema.odx.ODXLINK
import schema.odx.PARENTREF
import schema.odx.PHYSICALDIMENSION
import schema.odx.POSRESPONSE
import schema.odx.PRECONDITIONSTATEREF
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.REQUEST
import schema.odx.SDGCAPTION
import schema.odx.SINGLEECUJOB
import schema.odx.STATE
import schema.odx.STATETRANSITION
import schema.odx.STATETRANSITIONREF
import schema.odx.STRUCTURE
import schema.odx.TABLE
import schema.odx.TABLEKEY
import schema.odx.TABLEROW
import schema.odx.UNIT
import java.util.IdentityHashMap
import java.util.logging.Logger

/**
 * Aggregates multiple [ODXCollection] instances (one per ODX file) and provides
 * cross-file merged views of all IDs and objects.
 */
class ODXCollectionGroup(
    val data: Map<String, ODX>,
    val rawSize: Long,
    val options: ConverterOptions,
    private val logger: Logger,
    private val linkOwnership: IdentityHashMap<Any, String>,
) {
    // Individual per-file collections, keyed by the container short-name.
    val collections: Map<String, ODXCollection> by lazy {
        data.values
            .map { ODXCollection(it) }
            .associateBy { it.containerKey }
    }

    // Maps source filename to the ODXCollection created from that file.
    private val fileToCollection: Map<String, ODXCollection> by lazy {
        data.entries.associate { (filename, odx) ->
            filename to collections.values.first { it.odx === odx }
        }
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

    val basevariants: List<BASEVARIANT> by lazy {
        collections.values.flatMap { it.basevariants.values }
    }

    val ecuvariants: List<ECUVARIANT> by lazy {
        collections.values.flatMap { it.ecuvariants.values }
    }

    val functionalGroups: List<FUNCTIONALGROUP> by lazy {
        collections.values.flatMap { it.functionalGroups.values }
    }

    val diagServices: List<DIAGSERVICE> by lazy {
        collections.values.flatMap { it.diagServices.values }
    }

    val singleEcuJobs: List<SINGLEECUJOB> by lazy {
        collections.values.flatMap { it.singleEcuJobs.values }
    }

    val dtcs: List<DTC> by lazy {
        collections.values.flatMap { it.dtcs.values }
    }

    val additionalAudiences: List<ADDITIONALAUDIENCE> by lazy {
        collections.values.flatMap { it.additionalAudiences.values }
    }

    val protocols: List<PROTOCOL> by lazy {
        collections.values.flatMap { it.protocols.values }
    }

    val comparamSpecs: List<COMPARAMSPEC> by lazy {
        collections.values.flatMap { it.comparamSpecs.values }
    }

    val protStacks: List<PROTSTACK> by lazy {
        collections.values.flatMap { it.protStacks.values }
    }

    val libraries: List<LIBRARY> by lazy {
        collections.values.flatMap { it.libraries.values }
    }

    // Global short-name resolution for cross-file SNREF types (protocols, prot-stacks)

    fun resolveProtocolByShortName(shortName: String): PROTOCOL? = protocols.firstOrNull { it.shortname == shortName }

    fun resolveProtStackByShortName(shortName: String): PROTSTACK? = protStacks.firstOrNull { it.shortname == shortName }

    // ODXLINK resolution methods

    /**
     * Looks up the [ODXCollection] that the given link object was parsed from,
     * using the identity-based [linkOwnership] map.
     */
    fun collectionFor(owner: Any): ODXCollection? {
        val filename = linkOwnership[owner] ?: return null
        return fileToCollection[filename]
    }

    private fun sourceCollectionFor(link: Any): ODXCollection? = collectionFor(link)

    /**
     * Scoped resolution helper. Uses the explicit docref if present, otherwise
     * determines the source collection via [linkOwnership]. No global fallback.
     */
    private fun <T> resolveScoped(
        link: Any,
        idref: String,
        docref: String?,
        perFileAccessor: (ODXCollection) -> Map<String, T>,
    ): T? {
        val effectiveDocref = docref ?: sourceCollectionFor(link)?.containerKey
        if (effectiveDocref != null) {
            val collection = collections[effectiveDocref]
            if (collection != null) {
                return perFileAccessor(collection)[idref]
            }
        }
        logger.warning("Could not resolve $idref: no docref and no source collection found")
        return null
    }

    fun resolveRequest(link: ODXLINK): REQUEST? = resolveScoped(link, link.idref, link.docref) { it.requests }

    fun resolvePosResponse(link: ODXLINK): POSRESPONSE? = resolveScoped(link, link.idref, link.docref) { it.posResponses }

    fun resolveNegResponse(link: ODXLINK): NEGRESPONSE? = resolveScoped(link, link.idref, link.docref) { it.negResponses }

    fun resolveDiagService(link: ODXLINK): DIAGSERVICE? = resolveScoped(link, link.idref, link.docref) { it.diagServices }

    fun resolveSingleEcuJob(link: ODXLINK): SINGLEECUJOB? = resolveScoped(link, link.idref, link.docref) { it.singleEcuJobs }

    fun resolveCombinedDop(link: ODXLINK): DOPBASE? = resolveScoped(link, link.idref, link.docref) { it.combinedDataObjectProps }

    fun resolveTable(link: ODXLINK): TABLE? = resolveScoped(link, link.idref, link.docref) { it.tables }

    fun resolveTableRow(link: ODXLINK): TABLEROW? = resolveScoped(link, link.idref, link.docref) { it.tableRows }

    fun resolveTableKey(link: ODXLINK): TABLEKEY? = resolveScoped(link, link.idref, link.docref) { it.tableKeys }

    fun resolveLengthKey(link: ODXLINK): LENGTHKEY? = resolveScoped(link, link.idref, link.docref) { it.lengthKeys }

    fun resolveUnit(link: ODXLINK): UNIT? = resolveScoped(link, link.idref, link.docref) { it.units }

    fun resolvePhysDimension(link: ODXLINK): PHYSICALDIMENSION? = resolveScoped(link, link.idref, link.docref) { it.physDimensions }

    fun resolveEnvData(link: ODXLINK): ENVDATA? = resolveScoped(link, link.idref, link.docref) { it.envDatas }

    fun resolveLibrary(link: ODXLINK): LIBRARY? = resolveScoped(link, link.idref, link.docref) { it.libraries }

    fun resolveDtc(link: ODXLINK): schema.odx.DTC? = resolveScoped(link, link.idref, link.docref) { it.dtcs }

    fun resolveAdditionalAudience(link: ODXLINK): ADDITIONALAUDIENCE? =
        resolveScoped(link, link.idref, link.docref) { it.additionalAudiences }

    fun resolveState(ref: PRECONDITIONSTATEREF): STATE? = resolveScoped(ref, ref.idref, ref.docref) { it.states }

    fun resolveStateTransition(ref: STATETRANSITIONREF): STATETRANSITION? =
        resolveScoped(ref, ref.idref, ref.docref) { it.stateTransitions }

    fun resolveFunctClass(link: ODXLINK): FUNCTCLASS? = resolveScoped(link, link.idref, link.docref) { it.functClasses }

    fun resolveComParamSpec(link: ODXLINK): COMPARAMSPEC? = resolveScoped(link, link.idref, link.docref) { it.comparamSpecs }

    fun resolveComParamSubSet(link: ODXLINK): COMPARAMSUBSET? = resolveScoped(link, link.idref, link.docref) { it.comParamSubSets }

    fun resolveComparam(ref: COMPARAMREF): COMPARAM? = resolveScoped(ref, ref.idref, ref.docref) { it.comparams }

    fun resolveComplexComparam(ref: COMPARAMREF): COMPLEXCOMPARAM? = resolveScoped(ref, ref.idref, ref.docref) { it.complexComparams }

    fun resolveSdgCaption(link: ODXLINK): SDGCAPTION? = resolveScoped(link, link.idref, link.docref) { it.sdgCaptions }

    fun resolveStructure(link: ODXLINK): STRUCTURE? = resolveScoped(link, link.idref, link.docref) { it.structures }

    /**
     * Resolves a PARENTREF by trying basevariants, ecuvariants, protocols,
     * functionalGroups, tables, and ecuSharedDatas — scoped by docref when available.
     */
    fun resolveParent(ref: PARENTREF): Any? {
        val effectiveDocref = ref.docref ?: sourceCollectionFor(ref)?.containerKey
        if (effectiveDocref == null) {
            logger.warning("Could not resolve parent ${ref.idref}: no docref and no source collection found")
            return null
        }
        val collection = collections[effectiveDocref] ?: return null
        collection.basevariants[ref.idref]?.let { return it }
        collection.ecuvariants[ref.idref]?.let { return it }
        collection.protocols[ref.idref]?.let { return it }
        collection.functionalGroups[ref.idref]?.let { return it }
        collection.tables[ref.idref]?.let { return it }
        collection.ecuSharedDatas[ref.idref]?.let { return it }
        return null
    }
}
