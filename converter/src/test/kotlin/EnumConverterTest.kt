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

import assertk.assertThat
import assertk.assertions.isEqualTo
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
import dataformat.ValidType
import schema.odx.ADDRESSING
import schema.odx.CODEDCONST
import schema.odx.COMPUCATEGORY
import schema.odx.DATATYPE
import schema.odx.DIAGCLASSTYPE
import schema.odx.DYNAMIC
import schema.odx.INTERVALTYPE
import schema.odx.LEADINGLENGTHINFOTYPE
import schema.odx.LENGTHKEY
import schema.odx.MATCHINGREQUESTPARAM
import schema.odx.MINMAXLENGTHTYPE
import schema.odx.NRCCONST
import schema.odx.PARAMLENGTHINFOTYPE
import schema.odx.PHYSCONST
import schema.odx.PHYSICALDATATYPE
import schema.odx.RADIX
import schema.odx.RESERVED
import schema.odx.ROWFRAGMENT
import schema.odx.STANDARDISATIONLEVEL
import schema.odx.STANDARDLENGTHTYPE
import schema.odx.SYSTEM
import schema.odx.TABLEENTRY
import schema.odx.TABLEKEY
import schema.odx.TABLESTRUCT
import schema.odx.TERMINATION
import schema.odx.TRANSMODE
import schema.odx.USAGE
import schema.odx.VALIDTYPE
import schema.odx.VALUE
import kotlin.test.Test

class EnumConverterTest {
    @Test
    fun `TRANSMODE maps to TransmissionMode correctly`() {
        assertThat(TRANSMODE.RECEIVE_ONLY.toFileFormatEnum()).isEqualTo(TransmissionMode.RECEIVE_ONLY)
        assertThat(TRANSMODE.SEND_ONLY.toFileFormatEnum()).isEqualTo(TransmissionMode.SEND_ONLY)
        assertThat(TRANSMODE.SEND_OR_RECEIVE.toFileFormatEnum()).isEqualTo(TransmissionMode.SEND_OR_RECEIVE)
        assertThat(TRANSMODE.SEND_AND_RECEIVE.toFileFormatEnum()).isEqualTo(TransmissionMode.SEND_AND_RECEIVE)
    }

    @Test
    fun `ADDRESSING maps to Addressing correctly`() {
        assertThat(ADDRESSING.PHYSICAL.toFileFormatEnum()).isEqualTo(Addressing.PHYSICAL)
        assertThat(ADDRESSING.FUNCTIONAL.toFileFormatEnum()).isEqualTo(Addressing.FUNCTIONAL)
        assertThat(ADDRESSING.FUNCTIONAL_OR_PHYSICAL.toFileFormatEnum()).isEqualTo(Addressing.FUNCTIONAL_OR_PHYSICAL)
    }

    @Test
    fun `INTERVALTYPE maps to IntervalType correctly`() {
        assertThat(INTERVALTYPE.OPEN.toFileFormatEnum()).isEqualTo(IntervalType.OPEN)
        assertThat(INTERVALTYPE.INFINITE.toFileFormatEnum()).isEqualTo(IntervalType.INFINITE)
        assertThat(INTERVALTYPE.CLOSED.toFileFormatEnum()).isEqualTo(IntervalType.CLOSED)
    }

    @Test
    fun `COMPUCATEGORY maps to CompuCategory correctly`() {
        assertThat(COMPUCATEGORY.IDENTICAL.toFileFormatEnum()).isEqualTo(CompuCategory.IDENTICAL)
        assertThat(COMPUCATEGORY.LINEAR.toFileFormatEnum()).isEqualTo(CompuCategory.LINEAR)
        assertThat(COMPUCATEGORY.SCALE_LINEAR.toFileFormatEnum()).isEqualTo(CompuCategory.SCALE_LINEAR)
        assertThat(COMPUCATEGORY.TEXTTABLE.toFileFormatEnum()).isEqualTo(CompuCategory.TEXT_TABLE)
        assertThat(COMPUCATEGORY.COMPUCODE.toFileFormatEnum()).isEqualTo(CompuCategory.COMPU_CODE)
        assertThat(COMPUCATEGORY.TAB_INTP.toFileFormatEnum()).isEqualTo(CompuCategory.TAB_INTP)
        assertThat(COMPUCATEGORY.RAT_FUNC.toFileFormatEnum()).isEqualTo(CompuCategory.RAT_FUNC)
        assertThat(COMPUCATEGORY.SCALE_RAT_FUNC.toFileFormatEnum()).isEqualTo(CompuCategory.SCALE_RAT_FUNC)
    }

    @Test
    fun `PHYSICALDATATYPE maps to DataType correctly`() {
        assertThat(PHYSICALDATATYPE.A_INT_32.toFileFormatEnum()).isEqualTo(DataType.A_INT_32)
        assertThat(PHYSICALDATATYPE.A_UINT_32.toFileFormatEnum()).isEqualTo(DataType.A_UINT_32)
        assertThat(PHYSICALDATATYPE.A_FLOAT_32.toFileFormatEnum()).isEqualTo(DataType.A_FLOAT_32)
        assertThat(PHYSICALDATATYPE.A_FLOAT_64.toFileFormatEnum()).isEqualTo(DataType.A_FLOAT_64)
        assertThat(PHYSICALDATATYPE.A_BYTEFIELD.toFileFormatEnum()).isEqualTo(DataType.A_BYTEFIELD)
        assertThat(PHYSICALDATATYPE.A_UNICODE_2_STRING.toFileFormatEnum()).isEqualTo(DataType.A_UNICODE_2_STRING)
    }

    @Test
    fun `RADIX maps to Radix correctly`() {
        assertThat(RADIX.HEX.toFileFormatEnum()).isEqualTo(Radix.HEX)
        assertThat(RADIX.OCT.toFileFormatEnum()).isEqualTo(Radix.OCT)
        assertThat(RADIX.BIN.toFileFormatEnum()).isEqualTo(Radix.BIN)
        assertThat(RADIX.DEC.toFileFormatEnum()).isEqualTo(Radix.DEC)
    }

    @Test
    fun `TERMINATION maps to Termination correctly`() {
        assertThat(TERMINATION.ZERO.toFileFormatEnum()).isEqualTo(Termination.ZERO)
        assertThat(TERMINATION.END_OF_PDU.toFileFormatEnum()).isEqualTo(Termination.END_OF_PDU)
        assertThat(TERMINATION.HEX_FF.toFileFormatEnum()).isEqualTo(Termination.HEX_FF)
    }

    @Test
    fun `STANDARDISATIONLEVEL maps to ComParamStandardisationLevel correctly`() {
        assertThat(STANDARDISATIONLEVEL.STANDARD.toFileFormatEnum()).isEqualTo(ComParamStandardisationLevel.STANDARD)
        assertThat(STANDARDISATIONLEVEL.OPTIONAL.toFileFormatEnum()).isEqualTo(ComParamStandardisationLevel.OPTIONAL)
        assertThat(STANDARDISATIONLEVEL.OEM_OPTIONAL.toFileFormatEnum()).isEqualTo(ComParamStandardisationLevel.OEM_OPTIONAL)
        assertThat(STANDARDISATIONLEVEL.OEM_SPECIFIC.toFileFormatEnum()).isEqualTo(ComParamStandardisationLevel.OEM_SPECIFIC)
    }

    @Test
    fun `USAGE maps to ComParamUsage correctly`() {
        assertThat(USAGE.TESTER.toFileFormatEnum()).isEqualTo(ComParamUsage.TESTER)
        assertThat(USAGE.APPLICATION.toFileFormatEnum()).isEqualTo(ComParamUsage.APPLICATION)
        assertThat(USAGE.ECU_COMM.toFileFormatEnum()).isEqualTo(ComParamUsage.ECU_COMM)
        assertThat(USAGE.ECU_SOFTWARE.toFileFormatEnum()).isEqualTo(ComParamUsage.ECU_SOFTWARE)
    }

    @Test
    fun `ROWFRAGMENT maps to TableEntryRowFragment correctly`() {
        assertThat(ROWFRAGMENT.KEY.toFileFormatEnum()).isEqualTo(TableEntryRowFragment.KEY)
        assertThat(ROWFRAGMENT.STRUCT.toFileFormatEnum()).isEqualTo(TableEntryRowFragment.STRUCT)
    }

    @Test
    fun `VALIDTYPE maps to ValidType correctly`() {
        assertThat(VALIDTYPE.VALID.toFileFormatEnum()).isEqualTo(ValidType.VALID)
        assertThat(VALIDTYPE.NOT_VALID.toFileFormatEnum()).isEqualTo(ValidType.NOT_VALID)
        assertThat(VALIDTYPE.NOT_DEFINED.toFileFormatEnum()).isEqualTo(ValidType.NOT_DEFINED)
        assertThat(VALIDTYPE.NOT_AVAILABLE.toFileFormatEnum()).isEqualTo(ValidType.NOT_AVAILABLE)
    }

    @Test
    fun `DIAGCLASSTYPE maps to DiagClassType correctly`() {
        assertThat(DIAGCLASSTYPE.STARTCOMM.toFileFormatEnum()).isEqualTo(DiagClassType.START_COMM)
        assertThat(DIAGCLASSTYPE.DYN_DEF_MESSAGE.toFileFormatEnum()).isEqualTo(DiagClassType.DYN_DEF_MESSAGE)
        assertThat(DIAGCLASSTYPE.STOPCOMM.toFileFormatEnum()).isEqualTo(DiagClassType.STOP_COMM)
        assertThat(DIAGCLASSTYPE.READ_DYN_DEF_MESSAGE.toFileFormatEnum()).isEqualTo(DiagClassType.READ_DYN_DEF_MESSAGE)
        assertThat(DIAGCLASSTYPE.VARIANTIDENTIFICATION.toFileFormatEnum()).isEqualTo(DiagClassType.VARIANT_IDENTIFICATION)
        assertThat(DIAGCLASSTYPE.CLEAR_DYN_DEF_MESSAGE.toFileFormatEnum()).isEqualTo(DiagClassType.CLEAR_DYN_DEF_MESSAGE)
    }

    @Test
    fun `DATATYPE maps to DataType correctly`() {
        assertThat(DATATYPE.A_ASCIISTRING.toFileFormatEnum()).isEqualTo(DataType.A_ASCIISTRING)
        assertThat(DATATYPE.A_UTF_8_STRING.toFileFormatEnum()).isEqualTo(DataType.A_UTF_8_STRING)
        assertThat(DATATYPE.A_UNICODE_2_STRING.toFileFormatEnum()).isEqualTo(DataType.A_UNICODE_2_STRING)
        assertThat(DATATYPE.A_BYTEFIELD.toFileFormatEnum()).isEqualTo(DataType.A_BYTEFIELD)
        assertThat(DATATYPE.A_INT_32.toFileFormatEnum()).isEqualTo(DataType.A_INT_32)
        assertThat(DATATYPE.A_UINT_32.toFileFormatEnum()).isEqualTo(DataType.A_UINT_32)
        assertThat(DATATYPE.A_FLOAT_32.toFileFormatEnum()).isEqualTo(DataType.A_FLOAT_32)
        assertThat(DATATYPE.A_FLOAT_64.toFileFormatEnum()).isEqualTo(DataType.A_FLOAT_64)
    }

    @Test
    fun `PARAM subtypes map to ParamType correctly`() {
        assertThat(CODEDCONST().toParamTypeEnum()).isEqualTo(ParamType.CODED_CONST)
        assertThat(DYNAMIC().toParamTypeEnum()).isEqualTo(ParamType.DYNAMIC)
        assertThat(LENGTHKEY().toParamTypeEnum()).isEqualTo(ParamType.LENGTH_KEY)
        assertThat(MATCHINGREQUESTPARAM().toParamTypeEnum()).isEqualTo(ParamType.MATCHING_REQUEST_PARAM)
        assertThat(NRCCONST().toParamTypeEnum()).isEqualTo(ParamType.NRC_CONST)
        assertThat(PHYSCONST().toParamTypeEnum()).isEqualTo(ParamType.PHYS_CONST)
        assertThat(RESERVED().toParamTypeEnum()).isEqualTo(ParamType.RESERVED)
        assertThat(SYSTEM().toParamTypeEnum()).isEqualTo(ParamType.SYSTEM)
        assertThat(TABLEENTRY().toParamTypeEnum()).isEqualTo(ParamType.TABLE_ENTRY)
        assertThat(TABLEKEY().toParamTypeEnum()).isEqualTo(ParamType.TABLE_KEY)
        assertThat(TABLESTRUCT().toParamTypeEnum()).isEqualTo(ParamType.TABLE_STRUCT)
        assertThat(VALUE().toParamTypeEnum()).isEqualTo(ParamType.VALUE)
    }

    @Test
    fun `DIAGCODEDTYPE subtypes map to DiagCodedTypeName correctly`() {
        assertThat(LEADINGLENGTHINFOTYPE().toTypeEnum()).isEqualTo(DiagCodedTypeName.LEADING_LENGTH_INFO_TYPE)
        assertThat(MINMAXLENGTHTYPE().toTypeEnum()).isEqualTo(DiagCodedTypeName.MIN_MAX_LENGTH_TYPE)
        assertThat(PARAMLENGTHINFOTYPE().toTypeEnum()).isEqualTo(DiagCodedTypeName.PARAM_LENGTH_INFO_TYPE)
        assertThat(STANDARDLENGTHTYPE().toTypeEnum()).isEqualTo(DiagCodedTypeName.STANDARD_LENGTH_TYPE)
    }

    @Test
    fun `all TRANSMODE values are covered`() {
        TRANSMODE.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all ADDRESSING values are covered`() {
        ADDRESSING.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all INTERVALTYPE values are covered`() {
        INTERVALTYPE.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all COMPUCATEGORY values are covered`() {
        COMPUCATEGORY.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all PHYSICALDATATYPE values are covered`() {
        PHYSICALDATATYPE.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all RADIX values are covered`() {
        RADIX.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all TERMINATION values are covered`() {
        TERMINATION.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all STANDARDISATIONLEVEL values are covered`() {
        STANDARDISATIONLEVEL.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all USAGE values are covered`() {
        USAGE.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all ROWFRAGMENT values are covered`() {
        ROWFRAGMENT.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all VALIDTYPE values are covered`() {
        VALIDTYPE.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all DIAGCLASSTYPE values are covered`() {
        DIAGCLASSTYPE.values().forEach { it.toFileFormatEnum() }
    }

    @Test
    fun `all DATATYPE values are covered`() {
        DATATYPE.values().forEach { it.toFileFormatEnum() }
    }
}
