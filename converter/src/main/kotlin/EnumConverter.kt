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

import dataformat.Addressing
import dataformat.ComParamStandardisationLevel
import dataformat.ComParamUsage
import dataformat.CompuCategory
import dataformat.DataType
import dataformat.DiagClassType
import dataformat.DiagCodedTypeName
import dataformat.IntervalType
import dataformat.ParamType
import dataformat.Radix
import dataformat.TableEntryRowFragment
import dataformat.Termination
import dataformat.TransmissionMode
import schema.odx.*

fun TRANSMODE.toFileFormatEnum(): Byte =
    when (this) {
        TRANSMODE.RECEIVE_ONLY -> TransmissionMode.RECEIVE_ONLY
        TRANSMODE.SEND_ONLY -> TransmissionMode.SEND_ONLY
        TRANSMODE.SEND_OR_RECEIVE -> TransmissionMode.SEND_OR_RECEIVE
        TRANSMODE.SEND_AND_RECEIVE -> TransmissionMode.SEND_AND_RECEIVE
    }

fun ADDRESSING.toFileFormatEnum(): Byte =
    when (this) {
        ADDRESSING.PHYSICAL -> Addressing.PHYSICAL
        ADDRESSING.FUNCTIONAL -> Addressing.FUNCTIONAL
        ADDRESSING.FUNCTIONAL_OR_PHYSICAL -> Addressing.FUNCTIONAL_OR_PHYSICAL
    }

fun INTERVALTYPE.toFileFormatEnum(): Byte =
    when (this) {
        INTERVALTYPE.OPEN -> IntervalType.OPEN
        INTERVALTYPE.INFINITE -> IntervalType.INFINITE
        INTERVALTYPE.CLOSED -> IntervalType.CLOSED
    }

fun COMPUCATEGORY.toFileFormatEnum(): Byte =
    when (this) {
        COMPUCATEGORY.IDENTICAL -> CompuCategory.IDENTICAL
        COMPUCATEGORY.LINEAR -> CompuCategory.LINEAR
        COMPUCATEGORY.SCALE_LINEAR -> CompuCategory.SCALE_LINEAR
        COMPUCATEGORY.TEXTTABLE -> CompuCategory.TEXT_TABLE
        COMPUCATEGORY.COMPUCODE -> CompuCategory.COMPU_CODE
        COMPUCATEGORY.TAB_INTP -> CompuCategory.TAB_INTP
        COMPUCATEGORY.RAT_FUNC -> CompuCategory.RAT_FUNC
        COMPUCATEGORY.SCALE_RAT_FUNC -> CompuCategory.SCALE_RAT_FUNC
    }

fun PHYSICALDATATYPE.toFileFormatEnum(): Byte =
    when (this) {
        PHYSICALDATATYPE.A_INT_32 -> DataType.A_INT_32
        PHYSICALDATATYPE.A_UINT_32 -> DataType.A_UINT_32
        PHYSICALDATATYPE.A_FLOAT_32 -> DataType.A_FLOAT_32
        PHYSICALDATATYPE.A_FLOAT_64 -> DataType.A_FLOAT_64
        PHYSICALDATATYPE.A_BYTEFIELD -> DataType.A_BYTEFIELD
        PHYSICALDATATYPE.A_UNICODE_2_STRING -> DataType.A_UNICODE_2_STRING
    }

fun RADIX.toFileFormatEnum(): Byte =
    when (this) {
        RADIX.HEX -> Radix.HEX
        RADIX.OCT -> Radix.OCT
        RADIX.BIN -> Radix.BIN
        RADIX.DEC -> Radix.DEC
    }

fun TERMINATION.toFileFormatEnum(): Byte =
    when (this) {
        TERMINATION.ZERO -> Termination.ZERO
        TERMINATION.END_OF_PDU -> Termination.END_OF_PDU
        TERMINATION.HEX_FF -> Termination.HEX_FF
    }

fun STANDARDISATIONLEVEL.toFileFormatEnum(): Byte =
    when (this) {
        STANDARDISATIONLEVEL.STANDARD -> ComParamStandardisationLevel.STANDARD
        STANDARDISATIONLEVEL.OPTIONAL -> ComParamStandardisationLevel.OPTIONAL
        STANDARDISATIONLEVEL.OEM_OPTIONAL -> ComParamStandardisationLevel.OEM_OPTIONAL
        STANDARDISATIONLEVEL.OEM_SPECIFIC -> ComParamStandardisationLevel.OEM_SPECIFIC
    }

fun USAGE.toFileFormatEnum(): Byte =
    when (this) {
        USAGE.TESTER -> ComParamUsage.TESTER
        USAGE.APPLICATION -> ComParamUsage.APPLICATION
        USAGE.ECU_COMM -> ComParamUsage.ECU_COMM
        USAGE.ECU_SOFTWARE -> ComParamUsage.ECU_SOFTWARE
    }

fun ROWFRAGMENT.toFileFormatEnum(): Byte =
    when (this) {
        ROWFRAGMENT.KEY -> TableEntryRowFragment.KEY
        ROWFRAGMENT.STRUCT -> TableEntryRowFragment.STRUCT
    }

fun DIAGCLASSTYPE.toFileFormatEnum(): Byte =
    when (this) {
        DIAGCLASSTYPE.STARTCOMM -> DiagClassType.START_COMM
        DIAGCLASSTYPE.DYN_DEF_MESSAGE -> DiagClassType.DYN_DEF_MESSAGE
        DIAGCLASSTYPE.STOPCOMM -> DiagClassType.STOP_COMM
        DIAGCLASSTYPE.READ_DYN_DEF_MESSAGE -> DiagClassType.READ_DYN_DEF_MESSAGE
        DIAGCLASSTYPE.VARIANTIDENTIFICATION -> DiagClassType.VARIANT_IDENTIFICATION
        DIAGCLASSTYPE.CLEAR_DYN_DEF_MESSAGE -> DiagClassType.CLEAR_DYN_DEF_MESSAGE
    }

fun DATATYPE.toFileFormatEnum(): Byte =
    when (this) {
        DATATYPE.A_ASCIISTRING -> DataType.A_ASCIISTRING
        DATATYPE.A_UTF_8_STRING -> DataType.A_UTF_8_STRING
        DATATYPE.A_UNICODE_2_STRING -> DataType.A_UNICODE_2_STRING
        DATATYPE.A_BYTEFIELD -> DataType.A_BYTEFIELD
        DATATYPE.A_INT_32 -> DataType.A_INT_32
        DATATYPE.A_UINT_32 -> DataType.A_UINT_32
        DATATYPE.A_FLOAT_32 -> DataType.A_FLOAT_32
        DATATYPE.A_FLOAT_64 -> DataType.A_FLOAT_64
    }

/**
 * Converts the class type of PARAM (abstract) to an enum representation
 * Since it's not a simple 1:1 translation of an enum, it's named differently
 */
fun PARAM.toParamTypeEnum(): Byte =
    when (this) {
        is CODEDCONST -> ParamType.CODED_CONST
        is DYNAMIC -> ParamType.DYNAMIC
        is LENGTHKEY -> ParamType.LENGTH_KEY
        is MATCHINGREQUESTPARAM -> ParamType.MATCHING_REQUEST_PARAM
        is NRCCONST -> ParamType.NRC_CONST
        is PHYSCONST -> ParamType.PHYS_CONST
        is RESERVED -> ParamType.RESERVED
        is SYSTEM -> ParamType.SYSTEM
        is TABLEENTRY -> ParamType.TABLE_ENTRY
        is TABLEKEY -> ParamType.TABLE_KEY
        is TABLESTRUCT -> ParamType.TABLE_STRUCT
        is VALUE -> ParamType.VALUE
        else -> throw IllegalStateException("Unknown param type ${this::class.java.simpleName}")
    }

/**
 * Converts the class type of DIAGCODEDTYPE (abstract) to an enum representation
 * Since it's not a simple 1:1 translation of an enum, it's named differently
 */
fun DIAGCODEDTYPE.toTypeEnum(): Byte =
    when (this) {
        is LEADINGLENGTHINFOTYPE -> DiagCodedTypeName.LEADING_LENGTH_INFO_TYPE
        is MINMAXLENGTHTYPE -> DiagCodedTypeName.MIN_MAX_LENGTH_TYPE
        is PARAMLENGTHINFOTYPE -> DiagCodedTypeName.PARAM_LENGTH_INFO_TYPE
        is STANDARDLENGTHTYPE -> DiagCodedTypeName.STANDARD_LENGTH_TYPE
        else -> throw IllegalStateException("Unknown diag coded type ${this::class.java.simpleName}")
    }
