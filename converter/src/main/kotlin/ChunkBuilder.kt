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

import com.google.protobuf.ByteString
import org.eclipse.opensovd.cda.mdd.Chunk
import java.io.ByteArrayInputStream
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ChunkBuilder {
    fun createJobsChunks(
        logger: Logger,
        inputData: Map<String, ByteArray>,
        odx: ODXCollection,
        options: ConverterOptions,
    ): List<Chunk.Builder> {
        if (!options.includeJobFiles) {
            return emptyList()
        }
        val jobFiles =
            odx.singleEcuJobs.values
                .flatMap { it.progcodes?.progcode ?: emptyList() }
                .mapNotNull { it.codefile }
        val libraries = odx.libraries.values.mapNotNull { it.codefile }
        val files = (jobFiles + libraries).toSet()
        return files.mapNotNull { fileName ->
            val data = inputData[fileName]

            checkNotNull(data) {
                "File $fileName is not included in PDX"
            }
            logger.info("Including $fileName (${data.size} bytes)")
            Chunk
                .newBuilder()
                .setName(fileName)
                .setType(Chunk.DataType.CODE_FILE)
                .setUncompressedSize(data.size.toLong())
                .setData(ByteString.copyFrom(data))
        }
    }

    fun createPartialChunks(
        logger: Logger,
        inputData: Map<String, ByteArray>,
        odx: ODXCollection,
        options: ConverterOptions,
    ): List<Chunk.Builder> {
        if (options.partialJobFiles.isEmpty()) {
            return emptyList()
        }

        val jobFiles =
            odx.singleEcuJobs.values
                .flatMap { it.progcodes?.progcode ?: emptyList() }
                .mapNotNull { it.codefile }
        val libraries = odx.libraries.values.mapNotNull { it.codefile }
        val files = (jobFiles + libraries).toSet()
        return files.flatMap { jobFileName ->
            options.partialJobFiles
                .mapNotNull { partial ->
                    if (!jobFileName.matches(Regex(partial.jobFilePattern))) {
                        null
                    } else {
                        logger.fine("Job file $jobFileName matches pattern")
                        check(inputData.containsKey(jobFileName)) {
                            "File $jobFileName is not included in PDX"
                        }
                        PartialJobFilePattern(jobFileName, partial)
                    }
                }.groupBy {
                    it.jobFileName
                }.flatMap {
                    val data =
                        inputData[it.key] ?: throw IllegalStateException("File $jobFileName is not included in PDX")
                    if (it.key.endsWith(".jar", ignoreCase = true) || it.key.endsWith(".zip", ignoreCase = true)) {
                        ZipInputStream(ByteArrayInputStream(data)).use { zip ->
                            val matches =
                                extractMatchingFilesFromZip(
                                    logger,
                                    zip,
                                    it.value.map { pjfp -> Regex(pjfp.partialFilePattern.includePattern) },
                                )
                            matches.map { match ->
                                val filename = match.first
                                val data = match.second

                                logger.info("Including $filename from $jobFileName (${data.size} bytes)")

                                Chunk
                                    .newBuilder()
                                    .setName("$jobFileName::$filename")
                                    .setType(Chunk.DataType.CODE_FILE_PARTIAL)
                                    .setUncompressedSize(data.size.toLong())
                                    .setData(ByteString.copyFrom(data))
                            }
                        }
                    } else {
                        emptyList()
                    }
                }
        }
    }

    fun createEcuDataChunk(
        logger: Logger,
        odxCollection: ODXCollection,
        options: ConverterOptions,
    ): Chunk.Builder {
        val dw = DatabaseWriter(logger = logger, odx = odxCollection, options = options)
        val data = dw.createEcuData()
        return Chunk
            .newBuilder()
            .setName(odxCollection.ecuName)
            .setType(Chunk.DataType.DIAGNOSTIC_DESCRIPTION)
            .setUncompressedSize(data.size.toLong())
            .setData(ByteString.copyFrom(data))
    }

    private fun extractMatchingFilesFromZip(
        logger: Logger,
        zip: ZipInputStream,
        patterns: List<Regex>,
    ): List<Pair<String, ByteArray>> {
        val files = mutableListOf<Pair<String, ByteArray>>()

        var entry: ZipEntry? = zip.nextEntry
        while (entry != null) {
            logger.finest { "Checking ${entry?.name} against patterns $patterns" }
            if (patterns.any { entry.name.matches(it) }) {
                files.add(Pair(entry.name, zip.readAllBytes()))
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
        return files
    }
}
