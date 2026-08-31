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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.types.file
import org.eclipse.opensovd.cda.mdd.Signature
import java.io.File

/**
 * `view` subcommand: prints the structure of the `.mdd` file format itself (file-level metadata,
 * and per-chunk size/metadata/signature info), without decoding the actual diagnostic description
 * content. For inspecting the decoded diagnostic description, use the standalone `viewer` binary.
 */
class ViewCommand : CliktCommand(name = "view") {
    val mddFiles: List<File> by argument(name = "mdd-files")
        .file(mustExist = true, mustBeReadable = true, canBeFile = true)
        .help("mdd files to inspect")
        .multiple()

    override fun run() {
        mddFiles.forEachIndexed { index, file ->
            if (index > 0) {
                println()
            }
            printFile(file)
        }
    }

    private fun printFile(file: File) {
        val mddFile = MddFileIO.read(file)

        println("File: ${file.name}")
        println("  Format version: ${mddFile.version}")
        println("  ECU name: ${mddFile.ecuName}")
        if (mddFile.hasRevision()) {
            println("  Revision: ${mddFile.revision}")
        }
        if (mddFile.featureFlagsList.isNotEmpty()) {
            println("  Feature flags: ${mddFile.featureFlagsList.joinToString(", ")}")
        }
        if (mddFile.metadataMap.isNotEmpty()) {
            println("  Metadata:")
            mddFile.metadataMap.forEach { (key, value) -> println("    $key: $value") }
        }
        if (mddFile.hasChunksSignature()) {
            println("  Whole-file signature: ${mddFile.chunksSignature.describe()}")
        } else {
            println("  Whole-file signature: none")
        }

        println("  Chunks (${mddFile.chunksCount}):")
        mddFile.chunksList.forEach { chunk ->
            val name = if (chunk.hasName()) " '${chunk.name}'" else ""
            println("    - ${chunk.type}$name")

            val size = chunk.data.size().toLong()
            if (chunk.hasCompressionAlgorithm()) {
                println(
                    "        compression: ${chunk.compressionAlgorithm}, compressed size: ${size.format()} bytes, " +
                        "uncompressed size: ${chunk.uncompressedSize.format()} bytes",
                )
            } else {
                println("        size: ${size.format()} bytes")
            }

            if (chunk.hasMimeType()) {
                println("        mime type: ${chunk.mimeType}")
            }

            if (chunk.metadataMap.isNotEmpty()) {
                println("        metadata:")
                chunk.metadataMap.forEach { (key, value) -> println("          $key: $value") }
            }

            if (chunk.hasEncryption()) {
                val encryption = chunk.encryption
                val keyInfo = if (encryption.hasKeyIdentifier()) ", key identifier: present" else ""
                println(
                    "        encryption: ${encryption.encryptionAlgorithm}$keyInfo, " +
                        "certificates: ${encryption.certificatesCount}",
                )
            }

            if (chunk.signaturesCount > 0) {
                println("        signatures (${chunk.signaturesCount}):")
                chunk.signaturesList.forEach { println("          - ${it.describe()}") }
            } else {
                println("        signatures: none")
            }
        }
    }

    private fun Signature.describe(): String {
        val parts = mutableListOf("algorithm: $algorithm")
        if (hasKeyIdentifier()) {
            parts.add("key identifier: present")
        }
        parts.add("signature size: ${signature.size().toLong().format()} bytes")
        if (certificatesCount > 0) {
            parts.add("certificates: $certificatesCount")
        }
        if (metadataMap.isNotEmpty()) {
            parts.add("metadata: ${metadataMap.entries.joinToString(", ") { "${it.key}=${it.value}" }}")
        }
        return parts.joinToString(", ")
    }
}
