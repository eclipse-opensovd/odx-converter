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

import schema.odx.BASEVARIANT
import schema.odx.DIAGLAYER
import schema.odx.DIAGSERVICE
import schema.odx.DOPBASE
import schema.odx.ECUVARIANT
import schema.odx.FUNCTIONALGROUP
import schema.odx.LENGTHKEY
import schema.odx.ODXLINK
import schema.odx.PARAM
import schema.odx.PARENTREF
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.SINGLEECUJOB
import schema.odx.TABLE
import schema.odx.TABLEKEY
import schema.odx.TABLEROW
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Categories of elements that can be resolved via SNREF within a DIAGLAYER's scope.
 */
enum class SnrefCategory {
    DOP,
    DIAG_SERVICE,
    TABLE,
    TABLE_ROW,
    PROTOCOL,
    PROT_STACK,
}

/**
 * Resolves shortname references (SNREFs) using ODX inheritance-aware scoping.
 *
 * Per the ODX specification, an SNREF resolves within the scope of the DIAGLAYER
 * that owns the referencing element. The effective scope includes:
 * 1. Elements defined locally in the layer
 * 2. Elements inherited from parent layers (via PARENT-REFs), excluding those
 *    listed in NOT-INHERITED-* exclusion lists
 *
 * Resolution is lazy — scope computation happens on first access for each
 * (layer, category) combination and is cached thereafter.
 */
class InheritanceResolver(
    private val odx: ODXCollection,
    private val options: ConverterOptions,
    private val logger: Logger,
) {
    /**
     * Maps element ID → owning DIAGLAYER ID.
     * Built lazily on first SNREF resolution.
     */
    private val ownershipMap: Map<String, String> by lazy { buildOwnershipMap() }

    /**
     * Cache: (layerId, category) → Map<shortname, element>.
     * Each entry is the full effective scope for that layer+category.
     */
    private val scopeCache = mutableMapOf<Pair<String, SnrefCategory>, Map<String, Any>>()

    /**
     * All DIAGLAYER instances by ID (basevariants + ecuvariants + functionalGroups + ecuSharedDatas + protocols).
     */
    private val allLayers: Map<String, DIAGLAYER> by lazy {
        val map = mutableMapOf<String, DIAGLAYER>()
        odx.basevariants.forEach { (id, layer) -> map[id] = layer }
        odx.ecuvariants.forEach { (id, layer) -> map[id] = layer }
        odx.functionalGroups.forEach { (id, layer) -> map[id] = layer }
        odx.ecuSharedDatas.forEach { (id, layer) -> map[id] = layer }
        odx.protocols.forEach { (id, layer) -> map[id] = layer }
        map
    }

    /**
     * Resolve a DOP shortname reference within the scope of the layer that owns [containerId].
     * [containerId] is the ID of the element containing the SNREF (e.g., REQUEST, RESPONSE, STRUCTURE).
     */
    fun resolveDop(
        containerId: String,
        shortname: String,
    ): DOPBASE? = resolve(containerId, shortname, SnrefCategory.DOP)

    /**
     * Resolve a DIAG-SERVICE shortname reference within the scope of the given layer.
     * [layerId] is the ID of the DIAGLAYER performing the resolution.
     */
    fun resolveDiagService(
        layerId: String,
        shortname: String,
    ): DIAGSERVICE? = resolve(layerId, shortname, SnrefCategory.DIAG_SERVICE)

    /**
     * Resolve a TABLE shortname reference within the scope of the layer that owns [containerId].
     */
    fun resolveTable(
        containerId: String,
        shortname: String,
    ): TABLE? = resolve(containerId, shortname, SnrefCategory.TABLE)

    /**
     * Resolve a TABLE-ROW shortname reference within the scope of the layer that owns [containerId].
     */
    fun resolveTableRow(
        containerId: String,
        shortname: String,
    ): TABLEROW? = resolve(containerId, shortname, SnrefCategory.TABLE_ROW)

    /**
     * Resolve a PROTOCOL shortname reference within the scope of the layer that owns [containerId].
     */
    fun resolveProtocol(
        containerId: String,
        shortname: String,
    ): PROTOCOL? = resolve(containerId, shortname, SnrefCategory.PROTOCOL)

    /**
     * Resolve a PROT-STACK shortname reference within the scope of the layer that owns [containerId].
     */
    fun resolveProtStack(
        containerId: String,
        shortname: String,
    ): PROTSTACK? = resolve(containerId, shortname, SnrefCategory.PROT_STACK)

    /**
     * Generic resolution: finds the owning layer for [containerId], then looks up [shortname]
     * in the effective scope of that layer for the given [category].
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> resolve(
        containerId: String,
        shortname: String,
        category: SnrefCategory,
    ): T? {
        val layerId = ownershipMap[containerId]
        if (layerId == null) {
            // containerId itself might be a layer (e.g., for PROTOCOL resolving its own protstack)
            if (containerId in allLayers) {
                return resolveInLayer(containerId, shortname, category)
            }
            val message =
                "Cannot determine owning layer for element '$containerId' " +
                    "while resolving SNREF '$shortname' (category: $category)"
            if (!options.lenient) {
                throw IllegalStateException(message)
            }
            logger.log(Level.SEVERE, message)
            return null
        }
        return resolveInLayer(layerId, shortname, category)
    }

    /**
     * Resolves [shortname] in the effective scope of [layerId] for [category].
     * Checks for duplicates at the local level.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> resolveInLayer(
        layerId: String,
        shortname: String,
        category: SnrefCategory,
    ): T? {
        val scope = getEffectiveScope(layerId, category)
        return scope[shortname] as T?
    }

    /**
     * Returns the effective scope (shortname → element) for a layer + category,
     * computing and caching it lazily.
     */
    private fun getEffectiveScope(
        layerId: String,
        category: SnrefCategory,
    ): Map<String, Any> {
        return scopeCache.getOrPut(layerId to category) {
            val layer = allLayers[layerId]
            if (layer == null) {
                logger.warning("Layer '$layerId' not found in allLayers while computing scope")
                return@getOrPut emptyMap()
            }
            computeEffectiveScope(layer, category, mutableSetOf())
        }
    }

    /**
     * Recursively computes the effective scope for a layer:
     * local elements + inherited from parents (minus excluded) with local taking priority.
     */
    private fun computeEffectiveScope(
        layer: DIAGLAYER,
        category: SnrefCategory,
        visited: MutableSet<String>,
    ): Map<String, Any> {
        if (!visited.add(layer.id)) return emptyMap() // cycle guard

        val local = getLocalElements(layer, category)

        // Check for duplicates at the local level
        checkLocalDuplicates(layer, category, local)

        // Collect inherited elements from parent refs
        val inherited = mutableMapOf<String, Any>()
        val parentRefs = getParentRefs(layer)
        for (parentRef in parentRefs) {
            val parentLayer = allLayers[parentRef.idref]
            if (parentLayer == null) {
                logger.warning("Parent layer '${parentRef.idref}' not found for layer '${layer.id}'")
                continue
            }
            val excluded = getExcludedShortnames(parentRef, category)
            val parentScope = computeEffectiveScope(parentLayer, category, visited)
            for ((sn, elem) in parentScope) {
                if (sn !in excluded) {
                    inherited.putIfAbsent(sn, elem)
                }
            }
        }

        // Local overrides inherited
        return inherited + local
    }

    /**
     * Checks for duplicate shortnames within the local elements of a layer.
     * This indicates an ODX authoring error.
     */
    private fun checkLocalDuplicates(
        layer: DIAGLAYER,
        category: SnrefCategory,
        localScope: Map<String, Any>,
    ) {
        // The localScope is already de-duplicated by associateBy (last wins).
        // We need to check the raw list for duplicates.
        val rawElements = getLocalElementsList(layer, category)
        val seen = mutableSetOf<String>()
        val duplicates = mutableSetOf<String>()
        for ((shortname, _) in rawElements) {
            if (!seen.add(shortname)) {
                duplicates.add(shortname)
            }
        }
        for (dup in duplicates) {
            val message =
                "Duplicate shortname '$dup' in layer '${layer.shortname}' (${layer.id}) " +
                    "for category $category. Please fix the ODX file."
            if (!options.lenient) {
                throw IllegalStateException(message)
            }
            logger.log(Level.SEVERE, message)
        }
    }

    /**
     * Returns local elements as a Map<shortname, element> for a given layer + category.
     */
    private fun getLocalElements(
        layer: DIAGLAYER,
        category: SnrefCategory,
    ): Map<String, Any> = getLocalElementsList(layer, category).associate { it }

    /**
     * Returns local elements as a list of (shortname, element) pairs.
     * This preserves duplicates for checking purposes.
     */
    private fun getLocalElementsList(
        layer: DIAGLAYER,
        category: SnrefCategory,
    ): List<Pair<String, Any>> =
        when (category) {
            SnrefCategory.DOP -> getLocalDops(layer)
            SnrefCategory.DIAG_SERVICE -> getLocalDiagServices(layer)
            SnrefCategory.TABLE -> getLocalTables(layer)
            SnrefCategory.TABLE_ROW -> getLocalTableRows(layer)
            SnrefCategory.PROTOCOL -> getLocalProtocols(layer)
            SnrefCategory.PROT_STACK -> getLocalProtStacks(layer)
        }

    private fun getLocalDops(layer: DIAGLAYER): List<Pair<String, Any>> {
        val spec = layer.diagdatadictionaryspec ?: return emptyList()
        val dops = mutableListOf<Pair<String, Any>>()
        spec.dataobjectprops?.dataobjectprop?.forEach { dops.add(it.shortname to it) }
        spec.structures?.structure?.forEach { dops.add(it.shortname to it) }
        spec.staticfields?.staticfield?.forEach { dops.add(it.shortname to it) }
        spec.endofpdufields?.endofpdufield?.forEach { dops.add(it.shortname to it) }
        spec.dynamiclengthfields?.dynamiclengthfield?.forEach { dops.add(it.shortname to it) }
        spec.dynamicendmarkerfields?.dynamicendmarkerfield?.forEach { dops.add(it.shortname to it) }
        spec.muxs?.mux?.forEach { dops.add(it.shortname to it) }
        spec.envdatas?.envdata?.forEach { dops.add(it.shortname to it) }
        spec.envdatadescs?.envdatadesc?.forEach { dops.add(it.shortname to it) }
        spec.dtcdops?.dtcdop?.forEach { dops.add(it.shortname to it) }
        return dops
    }

    private fun getLocalDiagServices(layer: DIAGLAYER): List<Pair<String, Any>> =
        layer.diagcomms
            ?.diagcommproxy
            ?.filterIsInstance<DIAGSERVICE>()
            ?.map { it.shortname to it as Any }
            ?: emptyList()

    private fun getLocalTables(layer: DIAGLAYER): List<Pair<String, Any>> =
        layer.diagdatadictionaryspec
            ?.tables
            ?.table
            ?.map { it.shortname to it as Any }
            ?: emptyList()

    private fun getLocalTableRows(layer: DIAGLAYER): List<Pair<String, Any>> =
        layer.diagdatadictionaryspec
            ?.tables
            ?.table
            ?.flatMap { it.rowwrapper }
            ?.filterIsInstance<TABLEROW>()
            ?.map { it.shortname to it as Any }
            ?: emptyList()

    private fun getLocalProtocols(layer: DIAGLAYER): List<Pair<String, Any>> {
        // Protocols are not defined within a DIAGLAYER but at the DIAG-LAYER-CONTAINER level.
        // For SNREF resolution within a layer, protocols are visible globally.
        // We return all protocols as they don't follow the inheritance model.
        return odx.protocols.values.map { it.shortname to it as Any }
    }

    private fun getLocalProtStacks(layer: DIAGLAYER): List<Pair<String, Any>> {
        // Prot stacks are defined in COMPARAM-SPECs, not within DIAG-LAYERs.
        // They are visible globally for SNREF resolution.
        return odx.protStacks.values.map { it.shortname to it as Any }
    }

    /**
     * Gets the parent refs for a layer. Only HIERARCHYELEMENT subclasses
     * (BASEVARIANT, ECUVARIANT, FUNCTIONALGROUP, PROTOCOL) have parent refs.
     */
    private fun getParentRefs(layer: DIAGLAYER): List<PARENTREF> =
        when (layer) {
            is BASEVARIANT -> layer.parentrefs?.parentref ?: emptyList()
            is ECUVARIANT -> layer.parentrefs?.parentref ?: emptyList()
            is FUNCTIONALGROUP -> layer.parentrefs?.parentref ?: emptyList()
            is PROTOCOL -> layer.parentrefs?.parentref ?: emptyList()
            else -> emptyList() // ECUSHAREDDATA has no parent refs
        }

    /**
     * Gets the set of shortnames excluded from inheritance for a given parent ref + category.
     */
    private fun getExcludedShortnames(
        parentRef: PARENTREF,
        category: SnrefCategory,
    ): Set<String> =
        when (category) {
            SnrefCategory.DOP ->
                parentRef.notinheriteddops
                    ?.notinheriteddop
                    ?.map { it.dopbasesnref.shortname }
                    ?.toSet() ?: emptySet()
            SnrefCategory.DIAG_SERVICE ->
                parentRef.notinheriteddiagcomms
                    ?.notinheriteddiagcomm
                    ?.map { it.diagcommsnref.shortname }
                    ?.toSet() ?: emptySet()
            SnrefCategory.TABLE ->
                parentRef.notinheritedtables
                    ?.notinheritedtable
                    ?.map { it.tablesnref.shortname }
                    ?.toSet() ?: emptySet()
            SnrefCategory.TABLE_ROW ->
                // Table rows don't have a dedicated NOT-INHERITED list;
                // they follow the TABLE exclusion (if table is excluded, its rows are too)
                emptySet()
            SnrefCategory.PROTOCOL ->
                // Protocols don't follow inheritance (global scope)
                emptySet()
            SnrefCategory.PROT_STACK ->
                // Prot stacks don't follow inheritance (global scope)
                emptySet()
        }

    /**
     * Builds the ownership map: element ID → owning DIAGLAYER ID.
     * Maps requests, responses, diag services, DOPs, tables, etc. to their owning layer.
     */
    private fun buildOwnershipMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        for ((layerId, layer) in allLayers) {
            // Map diag services (and single ecu jobs) to their layer
            layer.diagcomms?.diagcommproxy?.forEach { comm ->
                when (comm) {
                    is DIAGSERVICE -> map[comm.id] = layerId
                    is SINGLEECUJOB -> map[comm.id] = layerId
                    is ODXLINK -> {} // references, not definitions
                }
            }

            // Map requests to their layer
            layer.requests?.request?.forEach { map[it.id] = layerId }

            // Map pos responses to their layer
            layer.posresponses?.posresponse?.forEach { map[it.id] = layerId }

            // Map neg responses to their layer
            layer.negresponses?.negresponse?.forEach { map[it.id] = layerId }

            // Map global neg responses to their layer
            layer.globalnegresponses?.globalnegresponse?.forEach { map[it.id] = layerId }

            // Map DOPs (from diag data dictionary spec) to their layer
            val spec = layer.diagdatadictionaryspec
            if (spec != null) {
                spec.dataobjectprops?.dataobjectprop?.forEach { map[it.id] = layerId }
                spec.structures?.structure?.forEach { structure ->
                    map[structure.id] = layerId
                    // Map params within structures (e.g., TABLEKEYs, LENGTHKEYs) by ID
                    structure.params?.param?.forEach { param ->
                        when (param) {
                            is TABLEKEY -> map[param.id] = layerId
                            is LENGTHKEY -> map[param.id] = layerId
                        }
                    }
                }
                spec.staticfields?.staticfield?.forEach { map[it.id] = layerId }
                spec.endofpdufields?.endofpdufield?.forEach { map[it.id] = layerId }
                spec.dynamiclengthfields?.dynamiclengthfield?.forEach { map[it.id] = layerId }
                spec.dynamicendmarkerfields?.dynamicendmarkerfield?.forEach { map[it.id] = layerId }
                spec.muxs?.mux?.forEach { map[it.id] = layerId }
                spec.envdatas?.envdata?.forEach { map[it.id] = layerId }
                spec.envdatadescs?.envdatadesc?.forEach { map[it.id] = layerId }
                spec.dtcdops?.dtcdop?.forEach { map[it.id] = layerId }
                spec.tables?.table?.forEach { table ->
                    map[table.id] = layerId
                    table.rowwrapper?.filterIsInstance<TABLEROW>()?.forEach { row ->
                        map[row.id] = layerId
                    }
                }
            }

            // Map params with IDs from requests and responses to their layer
            // (TABLEKEY and LENGTHKEY have IDs and can be referenced by IDREF)
            fun mapParamsWithIds(params: List<PARAM>?) {
                params?.forEach { param ->
                    when (param) {
                        is TABLEKEY -> map[param.id] = layerId
                        is LENGTHKEY -> map[param.id] = layerId
                    }
                }
            }
            layer.requests?.request?.forEach { mapParamsWithIds(it.params?.param) }
            layer.posresponses?.posresponse?.forEach { mapParamsWithIds(it.params?.param) }
            layer.negresponses?.negresponse?.forEach { mapParamsWithIds(it.params?.param) }
            layer.globalnegresponses?.globalnegresponse?.forEach { mapParamsWithIds(it.params?.param) }
        }

        return map
    }
}
