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
import schema.odx.ECUSHAREDDATA
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
 * A single ECU within the diagnostic description, identified by its BASE-VARIANT.
 * The ECU-VARIANTs are the variants whose PARENT-REF chain resolves back to
 * [baseVariant].
 */
class EcuGroup(
    val name: String,
    val revision: String?,
    val baseVariant: BASEVARIANT,
    val ecuVariants: List<ECUVARIANT>,
)

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
    // Individual per-file collections, keyed by the source filename. Keying by
    // file (rather than container short-name) keeps collections distinct even
    // when multiple merged PDX files contain containers with the same short-name.
    val collections: Map<String, ODXCollection> by lazy {
        data.mapValues { (_, odx) -> ODXCollection(odx) }
    }

    // Secondary index by container short-name, used to resolve explicit docref
    // attributes (a docref references a container by its short-name, not a file).
    private val collectionsByContainer: Map<String, ODXCollection> by lazy {
        collections.values.associateBy { it.containerKey }
    }

    // Maps source filename to the ODXCollection created from that file.
    private val fileToCollection: Map<String, ODXCollection> by lazy {
        collections
    }

    /**
     * All ECUs in this description, one per BASE-VARIANT. ECU-VARIANTs are
     * grouped to the BASE-VARIANT their PARENT-REF chain resolves to.
     */
    val ecuGroups: List<EcuGroup> by lazy {
        // Resolve each ECU-VARIANT to its owning BASE-VARIANT (walking parent
        // chains, since an ECU-VARIANT may have another ECU-VARIANT as parent).
        val variantsByBase = LinkedHashMap<BASEVARIANT, MutableList<ECUVARIANT>>()
        basevariants.forEach { variantsByBase[it] = mutableListOf() }

        ecuvariants.forEach { ecuVariant ->
            val base = resolveOwningBaseVariant(ecuVariant)
            if (base != null) {
                variantsByBase.getOrPut(base) { mutableListOf() }.add(ecuVariant)
            } else {
                val fallback = basevariants.firstOrNull()
                if (fallback == null || !options.lenient) {
                    error("Could not determine BASE-VARIANT for ECU-VARIANT ${ecuVariant.shortname}")
                }
                logger.warning(
                    "Could not determine BASE-VARIANT for ECU-VARIANT ${ecuVariant.shortname}, " +
                        "attaching to ${fallback.shortname}",
                )
                variantsByBase.getValue(fallback).add(ecuVariant)
            }
        }

        variantsByBase.map { (base, variants) ->
            EcuGroup(
                name = base.shortname,
                revision = revisionFor(base),
                baseVariant = base,
                ecuVariants = variants,
            )
        }
    }

    val ecuNames: List<String> by lazy { ecuGroups.map { it.name } }

    /**
     * Walks the PARENT-REF chain of [start] until a BASE-VARIANT is reached.
     * Returns null if no BASE-VARIANT can be found (guards against cycles).
     */
    private fun resolveOwningBaseVariant(start: ECUVARIANT): BASEVARIANT? {
        var current: Any = start
        val visited = HashSet<Any>()
        while (visited.add(current)) {
            val parentRefs =
                when (current) {
                    is ECUVARIANT -> current.parentrefs?.parentref
                    is BASEVARIANT -> return current
                    else -> null
                } ?: return null
            val parent =
                parentRefs
                    .asSequence()
                    .mapNotNull { resolveParent(it) }
                    .firstOrNull { it is BASEVARIANT || it is ECUVARIANT }
                    ?: return null
            if (parent is BASEVARIANT) {
                return parent
            }
            current = parent
        }
        return null
    }

    /** Latest doc-revision label of the container that the given diag layer was parsed from. */
    private fun revisionFor(layer: Any): String? =
        collectionFor(layer)
            ?.diagLayerContainer
            ?.admindata
            ?.docrevisions
            ?.docrevision
            ?.lastOrNull()
            ?.revisionlabel

    val basevariants: List<BASEVARIANT> by lazy {
        collections.values.flatMap { it.basevariants.values }
    }

    val ecuvariants: List<ECUVARIANT> by lazy {
        collections.values.flatMap { it.ecuvariants.values }
    }

    val functionalGroups: List<FUNCTIONALGROUP> by lazy {
        collections.values.flatMap { it.functionalGroups.values }
    }

    val ecuSharedDatas: List<ECUSHAREDDATA> by lazy {
        collections.values.flatMap { it.ecuSharedDatas.values }
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

    // --- Code/job file scoping -------------------------------------------------
    //
    // When multiple PDX files are merged into a single MDD, code/job files from
    // different PDX files may share the same in-PDX path. To keep chunk names
    // (and the codefile references stored in the flatbuffer) unique and
    // attributable, code file keys are prefixed with the source PDX name. For
    // the common single-PDX case the raw codefile path is used unchanged.

    /** Reverse index: ODXCollection identity -> its source file key. */
    private val fileKeyForCollection: IdentityHashMap<ODXCollection, String> by lazy {
        IdentityHashMap<ODXCollection, String>().apply {
            collections.forEach { (fileKey, collection) -> put(collection, fileKey) }
        }
    }

    /** The PDX name a source file key belongs to (the prefix before [PDX_NAME_SEPARATOR]). */
    private fun pdxNameForFileKey(fileKey: String): String = fileKey.substringBefore(PDX_NAME_SEPARATOR)

    /** Whether this group aggregates ODX files originating from more than one PDX. */
    val isMultiPdx: Boolean by lazy {
        collections.keys
            .map { pdxNameForFileKey(it) }
            .distinct()
            .size > 1
    }

    /** The PDX name the given (owning) object was parsed from, or null if unknown. */
    fun pdxNameFor(owner: Any): String? {
        val collection = collectionFor(owner) ?: return null
        val fileKey = fileKeyForCollection[collection] ?: return null
        return pdxNameForFileKey(fileKey)
    }

    /**
     * The key used to identify a code/job file both as a chunk name and as the
     * codefile reference stored in the flatbuffer. When merging multiple PDX
     * files, the key is prefixed with [pdxName] to keep it unique; otherwise the
     * raw [codeFile] path is returned unchanged.
     */
    fun codeFileKey(
        pdxName: String?,
        codeFile: String,
    ): String =
        if (isMultiPdx && pdxName != null) {
            "$pdxName$CODE_FILE_SCOPE_SEPARATOR$codeFile"
        } else {
            codeFile
        }

    /** Pairs each [ODXCollection] with the source PDX name of its file key. */
    fun collectionsWithPdxName(): List<Pair<ODXCollection, String>> =
        collections.entries.map { (fileKey, collection) -> collection to pdxNameForFileKey(fileKey) }

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
        val collection =
            if (docref != null) {
                collectionsByContainer[docref]
            } else {
                sourceCollectionFor(link)
            }
        if (collection != null) {
            return perFileAccessor(collection)[idref]
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
        val collection =
            if (ref.docref != null) {
                collectionsByContainer[ref.docref]
            } else {
                sourceCollectionFor(ref)
            }
        if (collection == null) {
            logger.warning("Could not resolve parent ${ref.idref}: no docref and no source collection found")
            return null
        }
        collection.basevariants[ref.idref]?.let { return it }
        collection.ecuvariants[ref.idref]?.let { return it }
        collection.protocols[ref.idref]?.let { return it }
        collection.functionalGroups[ref.idref]?.let { return it }
        collection.tables[ref.idref]?.let { return it }
        collection.ecuSharedDatas[ref.idref]?.let { return it }
        return null
    }

    companion object {
        /** Separates the PDX name prefix from the in-PDX entry name in ODX file keys. */
        const val PDX_NAME_SEPARATOR = "!"

        /** Separates the PDX name prefix from the codefile path in scoped code file keys. */
        const val CODE_FILE_SCOPE_SEPARATOR = "::"
    }
}
