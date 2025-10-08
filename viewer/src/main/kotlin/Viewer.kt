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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import dataformat.EcuData
import dataformat.SD
import dataformat.SDG
import dataformat.SDOrSDG
import dataformat.SDxorSDG
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import java.nio.ByteBuffer
import kotlin.time.measureTime

class Viewer : CliktCommand() {
    val file by argument(name = "file").file(
            mustExist = true,
            canBeFile = true,
            canBeDir = false,
            mustBeWritable = false,
            mustBeReadable = true
        )

    override fun run() {
        val mddFile: MDDFile

        lateinit var ecuData: EcuData

        val readAndDecompressTime = measureTime {
            val inputStream = file.inputStream()

            if (inputStream.available() < FILE_MAGIC.size) {
                throw IllegalArgumentException("Not an MDD file")
            }

            val magic = inputStream.readNBytes(FILE_MAGIC.size)

            if (!magic.contentEquals(FILE_MAGIC)) {
                throw IllegalArgumentException("Not an MDD file")
            }

            mddFile = MDDFile.parser().parseFrom(inputStream)

            val diagnosticDescription =
                mddFile.chunksList.first { chunk -> chunk.type.equals(Chunk.DataType.DIAGNOSTIC_DESCRIPTION) }.data

            LZMACompressorInputStream(diagnosticDescription.newInput()).use { inputStream ->
                val bb = ByteBuffer.wrap(inputStream.readAllBytes())
                ecuData = EcuData.getRootAsEcuData(bb)
                for (i in 0 until ecuData.dtcsLength) {
                    val dtc = ecuData.dtcs(i) ?: throw IllegalStateException("dtc must exist")
                    println(dtc.displayTroubleCode)
                    dtc.sdgs?.let {
                        for (j in 0 until it.sdgsLength) {
                            val sdg = it.sdgs(j) ?: throw IllegalStateException("sdg must exist")
                            println(sdg.caption)
                            for (k in 0 until sdg.sdsLength) {
                                val sdOrSdg = sdg.sds(k) ?: throw IllegalStateException("sdOrSdg must exist")
                                val obj = when (sdOrSdg.sdOrSdgType) {
                                    SDxorSDG.SD -> sdOrSdg.sdOrSdg(SD())
                                    SDxorSDG.SDG -> sdOrSdg.sdOrSdg(SDG())
                                    else -> throw IllegalStateException("sdOrSdg must be valid")
                                }
                                println(obj)
                            }
                        }
                    }
                }
            }
        }

        println("Took ${readAndDecompressTime.inWholeMilliseconds} ms to read and decompress")

    }
}

fun main(args: Array<String>) {
    Viewer().main(args)
}