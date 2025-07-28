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

import org.eclipse.opensovd.cda.mdd.*
import org.eclipse.opensovd.cda.mdd.DiagCodedType.MinMaxLengthType.Termination
import org.eclipse.opensovd.cda.mdd.Param.ParamType
import schema.odx.*

fun TRANSMODE.toProtoBufEnum(): DiagService.TransmissionMode =
    when (this) {
        TRANSMODE.RECEIVE_ONLY -> DiagService.TransmissionMode.RECEIVE_ONLY
        TRANSMODE.SEND_ONLY -> DiagService.TransmissionMode.SEND_ONLY
        TRANSMODE.SEND_OR_RECEIVE -> DiagService.TransmissionMode.SEND_OR_RECEIVE
        TRANSMODE.SEND_AND_RECEIVE -> DiagService.TransmissionMode.SEND_AND_RECEIVE
    }

fun ADDRESSING.toProtoBufEnum(): DiagService.Addressing =
    when (this) {
        ADDRESSING.PHYSICAL -> DiagService.Addressing.PHYSICAL
        ADDRESSING.FUNCTIONAL -> DiagService.Addressing.FUNCTIONAL
        ADDRESSING.FUNCTIONAL_OR_PHYSICAL -> DiagService.Addressing.FUNCTIONAL_OR_PHYSICAL
    }

fun INTERVALTYPE.toProtoBufEnum(): Limit.IntervalType =
    when (this) {
        INTERVALTYPE.OPEN -> Limit.IntervalType.OPEN
        INTERVALTYPE.INFINITE -> Limit.IntervalType.INFINITE
        INTERVALTYPE.CLOSED -> Limit.IntervalType.CLOSED
    }

fun COMPUCATEGORY.toProtoBufEnum(): CompuMethod.CompuCategory =
    when (this) {
        COMPUCATEGORY.IDENTICAL -> CompuMethod.CompuCategory.IDENTICAL
        COMPUCATEGORY.LINEAR -> CompuMethod.CompuCategory.LINEAR
        COMPUCATEGORY.SCALE_LINEAR -> CompuMethod.CompuCategory.SCALE_LINEAR
        COMPUCATEGORY.TEXTTABLE -> CompuMethod.CompuCategory.TEXT_TABLE
        COMPUCATEGORY.COMPUCODE -> CompuMethod.CompuCategory.COMPU_CODE
        COMPUCATEGORY.TAB_INTP -> CompuMethod.CompuCategory.TAB_INTP
        COMPUCATEGORY.RAT_FUNC -> CompuMethod.CompuCategory.RAT_FUNC
        COMPUCATEGORY.SCALE_RAT_FUNC -> CompuMethod.CompuCategory.SCALE_RAT_FUNC
    }

fun PHYSICALDATATYPE.toProtoBufEnum(): PhysicalType.DataType =
    when (this) {
        PHYSICALDATATYPE.A_INT_32 -> PhysicalType.DataType.A_INT_32
        PHYSICALDATATYPE.A_UINT_32 -> PhysicalType.DataType.A_UINT_32
        PHYSICALDATATYPE.A_FLOAT_32 -> PhysicalType.DataType.A_FLOAT_32
        PHYSICALDATATYPE.A_FLOAT_64 -> PhysicalType.DataType.A_FLOAT_64
        PHYSICALDATATYPE.A_BYTEFIELD -> PhysicalType.DataType.A_BYTEFIELD
        PHYSICALDATATYPE.A_UNICODE_2_STRING -> PhysicalType.DataType.A_UNICODE_2_STRING
    }

fun RADIX.toProtoBufEnum(): PhysicalType.Radix =
    when (this) {
        RADIX.HEX -> PhysicalType.Radix.HEX
        RADIX.OCT -> PhysicalType.Radix.OCT
        RADIX.BIN -> PhysicalType.Radix.BIN
        RADIX.DEC -> PhysicalType.Radix.DEC
    }

fun TERMINATION.toProtoBufEnum(): Termination =
    when (this) {
        TERMINATION.ZERO -> Termination.ZERO
        TERMINATION.END_OF_PDU -> Termination.END_OF_PDU
        TERMINATION.HEX_FF -> Termination.HEX_FF
    }

fun DIAGCODEDTYPE.toTypeEnum(): DiagCodedType.DiagCodedTypeName =
    when (this) {
        is LEADINGLENGTHINFOTYPE -> DiagCodedType.DiagCodedTypeName.LEADING_LENGTH_INFO_TYPE
        is MINMAXLENGTHTYPE -> DiagCodedType.DiagCodedTypeName.MIN_MAX_LENGTH_TYPE
        is PARAMLENGTHINFOTYPE -> DiagCodedType.DiagCodedTypeName.PARAM_LENGTH_INFO_TYPE
        is STANDARDLENGTHTYPE -> DiagCodedType.DiagCodedTypeName.STANDARD_LENGTH_TYPE
        else -> throw IllegalStateException("Unknown diag coded type ${this::class.java.simpleName}")
    }

fun DATATYPE.toProtoBufDiagCodedTypeEnum(): DiagCodedType.DataType =
    when (this) {
        DATATYPE.A_ASCIISTRING -> DiagCodedType.DataType.A_ASCIISTRING
        DATATYPE.A_UTF_8_STRING -> DiagCodedType.DataType.A_UTF_8_STRING
        DATATYPE.A_UNICODE_2_STRING -> DiagCodedType.DataType.A_UNICODE_2_STRING
        DATATYPE.A_BYTEFIELD -> DiagCodedType.DataType.A_BYTEFIELD
        DATATYPE.A_INT_32 -> DiagCodedType.DataType.A_INT_32
        DATATYPE.A_UINT_32 -> DiagCodedType.DataType.A_UINT_32
        DATATYPE.A_FLOAT_32 -> DiagCodedType.DataType.A_FLOAT_32
        DATATYPE.A_FLOAT_64 -> DiagCodedType.DataType.A_FLOAT_64
    }

fun DIAGCLASSTYPE.toProtoBufEnum(): DiagComm.DiagClassType =
    when (this) {
        DIAGCLASSTYPE.STARTCOMM -> DiagComm.DiagClassType.START_COMM
        DIAGCLASSTYPE.DYN_DEF_MESSAGE -> DiagComm.DiagClassType.DYN_DEF_MESSAGE
        DIAGCLASSTYPE.STOPCOMM -> DiagComm.DiagClassType.STOP_COMM
        DIAGCLASSTYPE.READ_DYN_DEFINED_MESSAGE -> DiagComm.DiagClassType.READ_DYN_DEFINED_MESSAGE
        DIAGCLASSTYPE.VARIANTIDENTIFICATION -> DiagComm.DiagClassType.VARIANT_IDENTIFICATION
        DIAGCLASSTYPE.CLEAR_DYN_DEF_MESSAGE -> DiagComm.DiagClassType.CLEAR_DYN_DEF_MESSAGE
    }

fun PARAM.toParamTypeEnum(): ParamType =
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

fun STANDARDISATIONLEVEL.toProtoBufEnum(): ComParam.StandardisationLevel =
    when (this) {
        STANDARDISATIONLEVEL.STANDARD -> ComParam.StandardisationLevel.STANDARD
        STANDARDISATIONLEVEL.OPTIONAL -> ComParam.StandardisationLevel.OPTIONAL
        STANDARDISATIONLEVEL.OEM_OPTIONAL -> ComParam.StandardisationLevel.OEM_OPTIONAL
        STANDARDISATIONLEVEL.OEM_SPECIFIC -> ComParam.StandardisationLevel.OEM_SPECIFIC
    }


fun USAGE.toProtoBufEnum(): ComParam.Usage =
    when (this) {
        USAGE.TESTER -> ComParam.Usage.TESTER
        USAGE.APPLICATION -> ComParam.Usage.APPLICATION
        USAGE.ECU_COMM -> ComParam.Usage.ECU_COMM
        USAGE.ECU_SOFTWARE -> ComParam.Usage.ECU_SOFTWARE
    }
