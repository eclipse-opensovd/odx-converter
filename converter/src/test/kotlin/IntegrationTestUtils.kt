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

import dataformat.EcuData
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Packs all files from a resource directory into a PDX (ZIP) archive.
 */
fun packPdx(
    resourceDir: String,
    outputFile: File,
) {
    val resourceRoot =
        object {}::class.java.getResource(resourceDir)
            ?: error("Resource directory '$resourceDir' not found")

    val dir = File(resourceRoot.toURI())
    require(dir.isDirectory) { "'$resourceDir' is not a directory" }

    ZipOutputStream(outputFile.outputStream().buffered()).use { zos ->
        dir.listFiles()?.filter { it.isFile }?.forEach { file ->
            zos.putNextEntry(ZipEntry(file.name))
            file.inputStream().use { it.copyTo(zos) }
            zos.closeEntry()
        }
    }
}

/**
 * Reads an MDD file and returns the deserialized EcuData from the DIAGNOSTIC_DESCRIPTION chunk.
 */
fun readMddEcuData(mddFile: File): EcuData {
    val inputStream = mddFile.inputStream()

    val magic = inputStream.readNBytes(FILE_MAGIC.size)
    require(magic.contentEquals(FILE_MAGIC)) { "Not a valid MDD file: bad magic header" }

    val parsedFile: MDDFile = MDDFile.parser().parseFrom(inputStream)

    val diagnosticChunk =
        parsedFile.chunksList
            .firstOrNull { it.type == Chunk.DataType.DIAGNOSTIC_DESCRIPTION }
            ?: error("No DIAGNOSTIC_DESCRIPTION chunk found in MDD file")

    val data: ByteBuffer =
        LZMACompressorInputStream(diagnosticChunk.data.newInput()).use { lzma ->
            ByteBuffer.wrap(lzma.readAllBytes())
        }

    return EcuData.getRootAsEcuData(data)
}

/**
 * Returns the parsed MDDFile protobuf object (for metadata assertions).
 */
fun readMddFile(mddFile: File): MDDFile {
    val inputStream = mddFile.inputStream()
    inputStream.readNBytes(FILE_MAGIC.size) // skip magic
    return MDDFile.parser().parseFrom(inputStream)
}
