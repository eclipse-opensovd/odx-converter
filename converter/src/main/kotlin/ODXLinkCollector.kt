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

import jakarta.xml.bind.Unmarshaller
import schema.odx.CASE
import schema.odx.COMPARAMREF
import schema.odx.DIAGCOMM
import schema.odx.ECUVARIANT
import schema.odx.FIELD
import schema.odx.MATCHINGBASEVARIANTPARAMETER
import schema.odx.MATCHINGPARAMETER
import schema.odx.ODXLINK
import schema.odx.PARAM
import schema.odx.PARENTREF
import schema.odx.PRECONDITIONSTATEREF
import schema.odx.STATETRANSITIONREF
import schema.odx.TABLEDIAGCOMMCONNECTOR
import schema.odx.TABLEROW
import java.util.IdentityHashMap

/**
 * JAXB [Unmarshaller.Listener] that records the source file for every ODXLINK-like
 * reference object and SNREF-containing object encountered during unmarshalling.
 * Set [currentFile] before unmarshalling each file so that objects are associated
 * with the correct source.
 */
class ODXLinkCollector : Unmarshaller.Listener() {
    val linkToFile: IdentityHashMap<Any, String> = IdentityHashMap()
    var currentFile: String = ""

    override fun afterUnmarshal(
        target: Any,
        parent: Any?,
    ) {
        when (target) {
            // ODXLINK reference types (for ID-based resolution)
            is ODXLINK,
            is PARENTREF,
            is COMPARAMREF,
            is PRECONDITIONSTATEREF,
            is STATETRANSITIONREF,
            // SNREF-containing types (for short-name-based resolution)
            is PARAM,
            is FIELD,
            is TABLEDIAGCOMMCONNECTOR,
            is CASE,
            is TABLEROW,
            is MATCHINGBASEVARIANTPARAMETER,
            is MATCHINGPARAMETER,
            is DIAGCOMM,
            is ECUVARIANT,
            -> linkToFile[target] = currentFile
        }
    }
}
