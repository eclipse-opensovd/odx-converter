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
import org.eclipse.opensovd.cda.mdd.ComParam.ComplexComParam
import org.eclipse.opensovd.cda.mdd.ComplexValue.ComplexValueEntry
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import java.io.BufferedInputStream
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import kotlin.time.measureTime

fun Param.display(): String {
    val sb = StringBuilder()
    sb.append(this.shortName)

    if (this.codedConst != null && this.codedConst.codedValue.isNotBlank()) {
        sb.append(" (0x${this.codedConst.codedValue.toLong().toString(16).uppercase()})")
    } else {
        sb.append(" (${this.paramType})")
    }


    return sb.trim().toString()
}

class ReaderTest(private val file: File) {
    private lateinit var sdMap: Map<Int, SD>
    private lateinit var sdgMap: Map<Int, SDG>
    private lateinit var sdgssMap: Map<Int, SDGS>
    private lateinit var servicesMap: Map<Int, DiagService>
    private lateinit var jobsMap: Map<Int, SingleEcuJob>
    private lateinit var requestMap: Map<Int, Request>
    private lateinit var responseMap: Map<Int, Response>
    private lateinit var paramMap: Map<Int, Param>
    private lateinit var preconditionStateRefMap: Map<Int, PreConditionStateRef>
    private lateinit var stateTransitionRefMap: Map<Int, StateTransitionRef>
    private lateinit var stateTransitionMap: Map<Int, StateTransition>
    private lateinit var statesMap: Map<Int, State>
    private lateinit var audienceMap: Map<Int, Audience>
    private lateinit var additionalAudienceMap: Map<Int, AdditionalAudience>
    private lateinit var dopMap: Map<Int, DOP>
    private lateinit var comParamMap: Map<Int, ComParam>
    private lateinit var comParamSubsetMap: Map<Int, ComParamSubSet>
    private lateinit var protStackMap: Map<Int, ProtStack>
    private lateinit var protocolMap: Map<Int, Protocol>

    fun readData() {
        println("Reading ${file.name}")

        BufferedInputStream(file.inputStream()).use { inputStream ->
            if (inputStream.available() < 4) {
                throw IllegalArgumentException("Not an MDD file")
            }
            val magic = inputStream.readNBytes(FILE_MAGIC.size)
            if (!magic.contentEquals(FILE_MAGIC)) {
                throw IllegalArgumentException("Not an MDD file")
            }

            val mddFile: MDDFile
            lateinit var ecuData: EcuData
            val readAndDecompressTime = measureTime {
                mddFile = MDDFile.parser().parseFrom(inputStream)
                val diagnosticDescription = mddFile.chunksList.first { chunk -> chunk.type.equals(Chunk.DataType.DIAGNOSTIC_DESCRIPTION) }.data
                LZMACompressorInputStream(diagnosticDescription.newInput()).use { inputStream ->
                    ecuData = EcuData.parseFrom(inputStream)
                }
            }
            val writer = PrintWriter(FileWriter("./out.log"))

            writer.println(ecuData.ecuName)
            writer.println(ecuData.revision)


            val outputTime = measureTime {
                sdMap = ecuData.sdsList.associateBy { it.id.value }
                sdgMap = ecuData.sdgsList.associateBy { it.id.value }
                sdgssMap = ecuData.sdgssList.associateBy { it.id.value }
                servicesMap = ecuData.diagServicesList.associateBy { it.id.value }
                jobsMap = ecuData.singleEcuJobsList.associateBy { it.id.value }
                requestMap = ecuData.requestsList.associateBy { it.id.value }
                responseMap = ecuData.responsesList.associateBy { it.id.value }
                paramMap = ecuData.paramsList.associateBy { it.id.value }
                preconditionStateRefMap = ecuData.preConditionStateRefsList.associateBy { it.id.value }
                stateTransitionRefMap = ecuData.stateTransitionRefsList.associateBy { it.id.value }
                stateTransitionMap = ecuData.stateTransitionsList.associateBy { it.id.value }
                statesMap = ecuData.statesList.associateBy { it.id.value }
                audienceMap = ecuData.audiencesList.associateBy { it.id.value }
                additionalAudienceMap = ecuData.additionalAudiencesList.associateBy { it.id.value }
                dopMap = ecuData.dopsList.associateBy { it.id.value }
                comParamMap = ecuData.comParamsList.associateBy { it.id.value }
                comParamSubsetMap = ecuData.comParamSubSetsList.associateBy { it.id.value }
                protStackMap = ecuData.protStacksList.associateBy { it.id.value }
                protocolMap = ecuData.protocolsList.associateBy { it.id.value }

//                dopMap.values.forEach {
//                    if (inputStream.hasEndOfPduField()) {
//                        println("${inputStream.shortName}: ${inputStream.endOfPduField.field}")
//                        if (inputStream.endOfPduField.field.basicStructure != null) {
//                            val dop = dopMap[inputStream.endOfPduField.field.basicStructure.ref.value] ?: throw IllegalStateException("xxx")
//                            println("DOP DOP DOP $dop")
//                        }
//                    }
//                }
//
                ecuData.variantsList.forEach { variant ->
                    writer.println("Variant: ${variant.diagLayer.shortName}")

                    variant.diagLayer.comParamRefsList.forEach { cpRef ->
                        writer.println("\tCP-Ref: ${cpRef.valueAsString()}")
                    }
                    if (variant.variantPatternList.isNotEmpty()) {
                        variant.variantPatternList.forEachIndexed { index, variantPattern ->
                            writer.println("\tVariant Pattern ${index + 1}:")
                            variantPattern.matchingParameterList?.forEach { matchParam ->
                                val diagService = servicesMap[matchParam.diagService.ref.value] ?: throw IllegalArgumentException("DiagService not found")
                                val param = paramMap[matchParam.outParam.ref.value] ?: throw IllegalArgumentException("Parameter not found")
                                val expectedValue = matchParam.expectedValue?.toIntOrNull()?.toString(16) ?: matchParam.expectedValue
                                writer.println("\t\t${diagService.diagComm.shortName}#${param.shortName} == $expectedValue")
                            }
                        }
                    }

                    variant.diagLayer.diagServicesList.forEach { serviceRef ->
                        val service = servicesMap[serviceRef.ref.value]!!
                        writer.println("\t${service.diagComm.shortName} (${service.diagComm.semantic}):")
                        val request = requestMap[service.request.ref.value]!!

                        writer.println("\t\tRequest:")
                        request.paramsList.forEach { paramRef ->
                            val param = paramMap[paramRef.ref.value]!!
                            writer.println("\t\t\t${param.display()}")
                        }

                        writer.println("\t\tPos-Responses:")
                        service.posResponsesList.forEach { posResponseRef ->
                            val posResponse = responseMap[posResponseRef.ref.value]!!
                            posResponse.paramsList.forEach { paramRef ->
                                val param = paramMap[paramRef.ref.value]!!
                                writer.println("\t\t\t${param.display()}")
                            }
                        }

                        writer.println("\t\tNeg-Responses:")
                        service.negResponsesList.forEach { negResponseRef ->
                            val negResponse = responseMap[negResponseRef.ref.value]!!
                            negResponse.paramsList.forEach { paramRef ->
                                val param = paramMap[paramRef.ref.value]!!
                                writer.println("\t\t\t${param.display()}")
                            }
                        }

                        if (service.diagComm.preConditionStateRefsList.isNotEmpty()) {
                            writer.println("\t\tPreconditions:")
                            service.diagComm.preConditionStateRefsList?.forEach { ref ->
                                val preConditionStateRef = preconditionStateRefMap[ref.ref.value]!!
                                val state = statesMap[preConditionStateRef.state.ref.value]!!
                                writer.println("\t\t\t${state.shortName}")
                            }
                        }

                        if (service.diagComm.stateTransitionRefsList.isNotEmpty()) {
                            writer.println("\t\tState-Transitions:")
                            service.diagComm.stateTransitionRefsList.forEach { ref ->
                                val stateTransitionRef = stateTransitionRefMap[ref.ref.value]!!
                                val stateTransition = stateTransitionMap[stateTransitionRef.stateTransition.ref.value]!!
                                val source = stateTransition.sourceShortNameRef
                                val target = stateTransition.targetShortNameRef
                                writer.println("\t\t\t${stateTransition.shortName}: $source -> $target")
                            }
                        }

                        if (service.diagComm.audience != null) {
                            val audience = audienceMap[service.diagComm.audience.ref.value] ?: throw IllegalStateException("Couldn't find audience ${service.diagComm.audience.ref.value}")
                            writer.println("\t\tAudience: supplier=${audience.isSupplier} development=${audience.isDevelopment} afterSales=${audience.isAfterSales} afterMarket=${audience.isAfterMarket} manufacturing=${audience.isManufacturing}")
                            if (audience.enabledAudiencesList.isNotEmpty()) {
                                val enabled = audience.enabledAudiencesList?.joinToString(", ") { enabledAudience ->
                                    val additionalAudience = additionalAudienceMap[enabledAudience.ref.value] ?: throw IllegalStateException("Couldn't find additional audience ${enabledAudience.ref.value}")
                                    additionalAudience.shortName
                                }
                                writer.println("\t\t\tEnabled Audiences: $enabled")
                            }
                            if  (audience.disabledAudiencesList.isNotEmpty()) {
                                val disabled = audience.disabledAudiencesList?.joinToString(", ") { enabledAudience ->
                                    val additionalAudience =
                                        additionalAudienceMap[enabledAudience.ref.value] ?: throw IllegalStateException(
                                            "Couldn't find additional audience ${enabledAudience.ref.value}"
                                        )
                                    additionalAudience.shortName
                                }
                                writer.println("\t\t\tDisabled Audiences: $disabled")
                            }
                        }
                    }
                }

                writer.println("State-Charts:")
                ecuData.stateChartsList.forEach { stateChart ->
                    writer.println("\t${stateChart.shortName}:")
                    val states = stateChart.statesList.map { stateRef ->
                        val state = statesMap[stateRef.ref.value]!!
                        state.shortName
                    }
                    writer.println("\t\tStates: ${states.joinToString(", ")}")

                    writer.println("\t\tTransitions:")
                    stateChart.stateTransitionsList.forEach { ref ->
                        val stateTransition = stateTransitionMap[ref.ref.value]!!
                        val source = stateTransition.sourceShortNameRef
                        val target = stateTransition.targetShortNameRef
                        writer.println("\t\t\t${stateTransition.shortName}: $source -> $target")
                    }
                }

                writer.println("ComParams:")
                ecuData.comParamsList.forEach { comParam ->
                    if (comParam.comParamType == ComParam.ComParamType.REGULAR) {
                        val dop = dopMap[comParam.regular.dop.ref.value] ?: throw IllegalStateException("Unknown dop ${comParam.regular.dop.ref.value}")
                        writer.println("\t${comParam.shortName}='${comParam.regular.physicalDefaultValue}' (${dop.shortName})")
                    } else {
                        writer.println("\t${comParam.valueAsString()} (COMPLEX)")
                    }
                }

                writer.println("Protocols:")
                ecuData.protocolsList.forEach { protocol ->
                    writer.println("\t${protocol.diagLayer.shortName}:")
                    if (protocol.diagLayer.comParamRefsList.isNotEmpty()) {
                        writer.println("\t\tComParam Refs:")
                        protocol.diagLayer.comParamRefsList.forEach { cpRef ->
                            val comParam = comParamMap[cpRef.comParam.ref.value]
                                ?: throw IllegalStateException("Couldn't find com param ${cpRef.comParam.ref.value}")
                            writer.println("\t\t\t'${cpRef.simpleValue}' -> ${comParam.shortName}")
                        }
                    }
                    protocol.comParamSpec?.let { comParamSpec ->
                        writer.println("\t\tCom Param Spec:")
                        comParamSpec.protStacksList?.forEach { protStackRef ->
                            val protStack = protStackMap[protStackRef.ref.value] ?: throw IllegalStateException("Couldn't find prot stack ${protStackRef.ref.value}")

                            writer.println("\t\tProtocol Stack '${protStack.shortName}' (${protStack.physicalLinkType}) (${protStack.pduProtocolType}):")
                            protStack.comparamSubSetRefsList?.forEach { compamSubSetRef ->
                                val comParamSubSet = comParamSubsetMap[compamSubSetRef.ref.value] ?: throw IllegalStateException("Couldn't find com param sub set ${compamSubSetRef.ref.value}")
                                if (comParamSubSet.comParamsList.isNotEmpty() || comParamSubSet.complexComParamsList.isNotEmpty()) {
                                    writer.println("\t\t\tCom Param Subset:")
                                    comParamSubSet.comParamsList?.forEach { cpRef ->
                                        val cp = comParamMap[cpRef.ref.value]
                                            ?: throw IllegalStateException("Couldn't find com param sub set ${cpRef.ref.value}")
                                        writer.println("\t\t\t\t${cp.shortName}")
                                    }
                                    comParamSubSet.complexComParamsList?.forEach { cpRef ->
                                        val cp = comParamMap[cpRef.ref.value]
                                            ?: throw IllegalStateException("Couldn't find com param sub set ${cpRef.ref.value}")
                                        writer.println("\t\t\t\t${cp.shortName} (COMPLEX)")
                                    }
                                }
                            }

                        }
                    }
                    protocol.protStack?.let { protStackRef ->
                        val protStack = protStackMap[protStackRef.ref.value] ?: throw IllegalStateException("couldn't find prot stack ${protStackRef.ref.value}")
                        writer.println("\t\tProtocol Stack: ${protStack.shortName}")
                    }
                }
            }
            println("Sort and Output took $outputTime (read/decompression: $readAndDecompressTime)")

        }
    }

    private fun ComplexComParam.valueAsString(): String {
        return this.comParamsList.joinToString("; ") {
            val comParam = comParamMap[it.ref.value] ?: throw IllegalStateException("Couldn't find com param ${it.ref.value}")
            comParam.valueAsString()
        }
    }

    private fun ComplexValueEntry.valueAsString(): String {
        if (this.hasSimpleValue()) {
            return this.simpleValue
        } else if (this.hasComplexValue()) {
            return this.complexValue.entriesList.joinToString(";") { it.valueAsString() }
        } else {
            throw IllegalStateException("No value for ComplexValueEntry")
        }
    }

    private fun ComParam.valueAsString(): String {
        val sb = StringBuilder("${this.shortName} = [")
        if (this.comParamType == ComParam.ComParamType.REGULAR) {
            sb.append(this.regular.physicalDefaultValue)
        } else if (this.comParamType == ComParam.ComParamType.COMPLEX) {
            sb.append(this.complex.valueAsString())
        }
        sb.append("]")
        return sb.toString()
    }

    private fun ComplexValue.valueAsString(): String =
        this.entriesList.joinToString(";") {
            it.valueAsString()
        }

    private fun ComParamRef.valueAsString(): String {
        val protocol = if (this.hasProtocol()) protocolMap[this.protocol.ref.value] ?: throw IllegalStateException("Couldn't find protocol ${this.protocol.ref.value}") else null
        val protStack = if (this.hasProtStack()) protStackMap[this.protStack.ref.value] ?: throw IllegalStateException("Couldn't find prot stack ${this.protStack.ref.value}") else null
        val comParam = comParamMap[this.comParam.ref.value] ?: throw IllegalStateException("Couldn't find com param ${this.comParam.ref.value}")
        if (this.hasSimpleValue()) {
            return "(${protocol?.diagLayer?.shortName}, ${protStack?.shortName}) ${this.simpleValue} -> ${comParam.valueAsString()}"
        } else if (this.hasComplexValue()) {
            return "(${protocol?.diagLayer?.shortName}, ${protStack?.shortName}) ${this.complexValue.valueAsString()} -> [${comParam.valueAsString()}]"
        } else {
            throw IllegalStateException("No value for ComplexValueEntry")
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val readerTest = ReaderTest(File(args[0]))
            readerTest.readData()
//            readerTest.readData()
        }
    }

}

