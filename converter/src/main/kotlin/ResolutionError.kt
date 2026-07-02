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
import schema.odx.CASE
import schema.odx.COMPARAM
import schema.odx.COMPARAMREF
import schema.odx.COMPARAMSPEC
import schema.odx.COMPARAMSUBSET
import schema.odx.COMPLEXCOMPARAM
import schema.odx.DEFAULTCASE
import schema.odx.DIAGCOMM
import schema.odx.DIAGLAYER
import schema.odx.DIAGSERVICE
import schema.odx.DOPBASE
import schema.odx.ECUSHAREDDATA
import schema.odx.ECUVARIANT
import schema.odx.ECUVARIANTPATTERN
import schema.odx.FUNCTCLASS
import schema.odx.FUNCTIONALGROUP
import schema.odx.GLOBALNEGRESPONSE
import schema.odx.INPUTPARAM
import schema.odx.LIBRARY
import schema.odx.MATCHINGBASEVARIANTPARAMETER
import schema.odx.MATCHINGPARAMETER
import schema.odx.NEGOUTPUTPARAM
import schema.odx.NEGRESPONSE
import schema.odx.OUTPUTPARAM
import schema.odx.PARAM
import schema.odx.PHYSICALDIMENSION
import schema.odx.POSRESPONSE
import schema.odx.PRECONDITIONSTATEREF
import schema.odx.PROTOCOL
import schema.odx.PROTSTACK
import schema.odx.REQUEST
import schema.odx.SINGLEECUJOB
import schema.odx.STATE
import schema.odx.STATECHART
import schema.odx.STATETRANSITION
import schema.odx.STATETRANSITIONREF
import schema.odx.TABLE
import schema.odx.TABLEDIAGCOMMCONNECTOR
import schema.odx.TABLEROW
import schema.odx.UNITGROUP

/**
 * Thrown when an ODX reference (ODXLINK by id-ref, or SNREF by short-name) cannot be
 * resolved. Extends [IllegalStateException].
 */
class OdxResolutionException(
    message: String,
) : IllegalStateException(message)

/**
 * All the context known at a reference-resolution failure. Kept free of `schema.odx`
 * types so the formatter can be unit-tested without the build-time-generated classes.
 */
data class ResolutionContext(
    /** Expected target type, e.g. "REQUEST" or "DATA-OBJECT-PROP". */
    val expected: String,
    /** Kind of reference: "ODXLINK" or "SNREF". */
    val refKind: String,
    /** The id-ref or short-name that could not be resolved. */
    val refValue: String,
    /** Explicit doc-ref if the reference carried one. */
    val docref: String? = null,
    /** Container/file scope that was actually searched. */
    val scopeSearched: String? = null,
    /** Source file the broken reference was parsed from. */
    val sourceFile: String? = null,
    /** Logical element path, e.g. "EcuVariant_X / Service_Y / Request_Z". */
    val logicalPath: String? = null,
    /** Available keys in the searched scope (retained for potential debug logging). */
    val candidates: Collection<String> = emptyList(),
)

/**
 * Renders a [ResolutionContext] as a concise, multi-line message showing what failed,
 * where the broken reference lives (breadcrumb path + file), and what scope was searched.
 */
fun resolutionMessage(ctx: ResolutionContext): String {
    val lines = mutableListOf<String>()
    lines += "Failed to resolve ${ctx.expected} via ${ctx.refKind} '${ctx.refValue}'"

    val location =
        buildString {
            ctx.logicalPath?.takeIf { it.isNotBlank() }?.let { path ->
                append(path.replace(" / ", " > "))
                append(" ")
            }
            append("(file: ${ctx.sourceFile ?: "<unknown file>"})")
        }
    lines += "  in:       $location"

    val searched =
        buildString {
            append(ctx.scopeSearched ?: "<unknown scope>")
            append(" (type: ${ctx.expected}, ${ctx.candidates.size} entries)")
        }
    lines += "  searched: $searched"

    if (ctx.candidates.isNotEmpty()) {
        val maxShow = 5
        val sorted = ctx.candidates.sorted()
        val shown = sorted.take(maxShow)
        val remainder = ctx.candidates.size - shown.size
        val candidateLine =
            if (remainder > 0) {
                shown.joinToString(", ") + " … and $remainder more"
            } else {
                shown.joinToString(", ")
            }
        lines += "  candidates: $candidateLine"
    }

    ctx.docref?.let { lines += "  docref:   $it" }

    return lines.joinToString("\n")
}

/**
 * Formats a list of ODX element objects into a human-readable breadcrumb path.
 * Each element is rendered as "TYPE (short-name)" when identifying information is available,
 * or just the class simple name as a fallback.
 * The resulting string uses " / " separators which [resolutionMessage] renders as " > ".
 */
fun formatElementPath(path: List<Any>): String =
    path.joinToString(" / ") { element ->
        when (element) {
            // DIAGLAYER hierarchy (subtypes before supertypes)
            is ECUVARIANT -> "ECU-VARIANT (${element.shortname})"
            is BASEVARIANT -> "BASE-VARIANT (${element.shortname})"
            is FUNCTIONALGROUP -> "FUNCTIONAL-GROUP (${element.shortname})"
            is PROTOCOL -> "PROTOCOL (${element.shortname})"
            is ECUSHAREDDATA -> "ECU-SHARED-DATA (${element.shortname})"
            is DIAGLAYER -> "DIAG-LAYER (${element.shortname})"
            // DIAGCOMM hierarchy (subtypes before supertypes)
            is DIAGSERVICE -> "DIAG-SERVICE (${element.shortname})"
            is SINGLEECUJOB -> "SINGLE-ECU-JOB (${element.shortname})"
            is DIAGCOMM -> "DIAG-COMM (${element.shortname})"
            // Table types
            is TABLE -> "TABLE (${element.shortname})"
            is TABLEROW -> "TABLE-ROW (${element.shortname})"
            is TABLEDIAGCOMMCONNECTOR -> "TABLE-DIAG-COMM-CONNECTOR"
            is DEFAULTCASE -> "DEFAULT-CASE (${element.shortname})"
            is CASE -> "CASE (${element.shortname})"
            // Parameters
            is PARAM -> "PARAM (${element.shortname})"
            is INPUTPARAM -> "INPUT-PARAM (${element.shortname})"
            is OUTPUTPARAM -> "OUTPUT-PARAM (${element.shortname})"
            is NEGOUTPUTPARAM -> "NEG-OUTPUT-PARAM (${element.shortname})"
            // Request / Response (subtypes before supertype)
            is REQUEST -> "REQUEST (${element.shortname})"
            is POSRESPONSE -> "POS-RESPONSE (${element.shortname})"
            is NEGRESPONSE -> "NEG-RESPONSE (${element.shortname})"
            is GLOBALNEGRESPONSE -> "GLOBAL-NEG-RESPONSE (${element.shortname})"
            // Variant pattern (no short-name)
            is ECUVARIANTPATTERN -> "VARIANT-PATTERN"
            is MATCHINGPARAMETER -> "MATCHING-PARAM"
            is MATCHINGBASEVARIANTPARAMETER -> "MATCHING-BASE-PARAM"
            // Named entities
            is DOPBASE -> "DOP (${element.shortname})"
            is FUNCTCLASS -> "FUNCT-CLASS (${element.shortname})"
            is schema.odx.UNIT -> "UNIT (${element.shortname})"
            is schema.odx.DTC -> "DTC (${element.shortname})"
            is LIBRARY -> "LIBRARY (${element.shortname})"
            is ADDITIONALAUDIENCE -> "ADDITIONAL-AUDIENCE (${element.shortname})"
            is UNITGROUP -> "UNIT-GROUP (${element.shortname})"
            is COMPARAM -> "COMPARAM (${element.shortname})"
            is COMPLEXCOMPARAM -> "COMPLEX-COMPARAM (${element.shortname})"
            is STATE -> "STATE (${element.shortname})"
            is PHYSICALDIMENSION -> "PHYSICAL-DIMENSION (${element.shortname})"
            is PROTSTACK -> "PROT-STACK (${element.shortname})"
            is STATECHART -> "STATE-CHART (${element.shortname})"
            is STATETRANSITION -> "STATE-TRANSITION (${element.shortname})"
            is COMPARAMSUBSET -> "COMPARAM-SUBSET (${element.shortname})"
            is COMPARAMSPEC -> "COMPARAM-SPEC (${element.shortname})"
            is COMPARAMREF -> "COMPARAM-REF (${element.idref})"
            is PRECONDITIONSTATEREF -> "PRE-CONDITION-STATE-REF (${element.idref})"
            is STATETRANSITIONREF -> "STATE-TRANSITION-REF (${element.idref ?: "<unresolved>"})"
            // Fallback: just the class name
            else -> element.javaClass.simpleName
        }
    }
