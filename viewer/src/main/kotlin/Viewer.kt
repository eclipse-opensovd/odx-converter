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
import dataformat.SDGS
import dataformat.SDxorSDG
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import java.io.PrintStream
import java.lang.System
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

        lateinit var data: ByteBuffer
        val decompressTime = measureTime {
            LZMACompressorInputStream(diagnosticDescription.newInput()).use { inputStream ->
                data = ByteBuffer.wrap(inputStream.readAllBytes())
            }
        }
        println("Decompression took ${decompressTime.inWholeMilliseconds} ms")

        ecuData = EcuData.getRootAsEcuData(data)

        val o = System.out

        for (i in 0 until ecuData.dtcsLength) {
            val dtc = ecuData.dtcs(i) ?: throw IllegalStateException("dtc must exist")
            o.indentedPrintln(0, dtc.displayTroubleCode)
            dtc.sdgs?.output(o, 2)
        }
    }

    private fun SDGS.output(p: PrintStream, indent: Int) {
        for (i in 0 until this.sdgsLength) {
            val o = this.sdgs(i)
            o?.output(p, indent + 2)
        }
    }

    private fun SD.output(p: PrintStream, indent: Int) {
        p.indentedPrintln(indent, "${this.value} (si: ${this.si} ti: ${this.ti})")
    }

    private fun SDG.output(p: PrintStream, indent: Int) {
        this.caption?.shortName?.let {
            p.indentedPrintln(indent, "$it:")
        }

        for (i in 0 until this.sdsLength) {
            val sdOrSdg = this.sds(i) ?: throw IllegalStateException("sdOrSdg must exist")
            val obj = when (sdOrSdg.sdOrSdgType) {
                SDxorSDG.SD -> sdOrSdg.sdOrSdg(SD())
                SDxorSDG.SDG -> sdOrSdg.sdOrSdg(SDG())
                else -> throw IllegalStateException("sdOrSdg must be valid")
            }
            when (obj) {
                is SD -> obj.output(p, indent + 2)
                is SDG -> obj.output(p, indent + 2)
            }
        }

    }

    fun PrintStream.indentedPrintln(indent: Int, value: String?) {
        this.println(" ".repeat(indent) + value)
    }
}

fun main(args: Array<String>) {
    Viewer().main(args)
}