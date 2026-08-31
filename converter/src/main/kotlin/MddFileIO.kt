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

import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import java.io.BufferedOutputStream
import java.io.File

/**
 * Shared helpers to read/write `.mdd` files, used by the `sign`, `verify` and `view` commands.
 */
object MddFileIO {
    fun read(file: File): MDDFile {
        file.inputStream().use { inputStream ->
            if (inputStream.available() < FILE_MAGIC.size) {
                throw IllegalArgumentException("Not an MDD file: ${file.name}")
            }
            val magic = inputStream.readNBytes(FILE_MAGIC.size)
            if (!magic.contentEquals(FILE_MAGIC)) {
                throw IllegalArgumentException("Not an MDD file: ${file.name}")
            }
            return MDDFile.parser().parseFrom(inputStream)
        }
    }

    fun write(
        file: File,
        mddFile: MDDFile,
    ) {
        BufferedOutputStream(file.outputStream()).use {
            it.write(FILE_MAGIC)
            mddFile.writeTo(it)
        }
    }

    /** Returns the decompressed payload of a chunk, based on its `compression_algorithm`. */
    fun decompress(chunk: Chunk): ByteArray {
        val raw = chunk.data.toByteArray()
        if (!chunk.hasCompressionAlgorithm()) {
            return raw
        }
        return when (chunk.compressionAlgorithm) {
            "lzma" -> LZMACompressorInputStream(raw.inputStream()).use { it.readAllBytes() }
            else -> error("Unsupported compression algorithm '${chunk.compressionAlgorithm}' for chunk '${chunk.name}'")
        }
    }
}
