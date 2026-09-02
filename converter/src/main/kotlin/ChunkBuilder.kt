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
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/** A code/job file reference together with the PDX it originates from. */
private data class CodeFileRef(
    val pdxName: String,
    val codeFile: String,
)

class ChunkBuilder {
    fun createJobsChunks(
        logger: Logger,
        inputData: Map<String, ZipEntryInfos>,
        odx: ODXCollectionGroup,
        options: ConverterOptions,
    ): List<Chunk.Builder> {
        if (!options.includeJobFiles) {
            return emptyList()
        }
        // Collect (sourcePdxName, codeFilePath) pairs per source collection so that
        // each code file can be looked up scoped to its origin PDX and named with
        // the same (possibly PDX-prefixed) key that is stored in the flatbuffer.
        val files =
            odx
                .collectionsWithPdxName()
                .flatMap { (collection, pdxName) ->
                    val jobFiles =
                        collection.singleEcuJobs.values
                            .flatMap { it.progcodes?.progcode ?: emptyList() }
                            .mapNotNull { it.codefile }
                    val libraries = collection.libraries.values.mapNotNull { it.codefile }
                    (jobFiles + libraries).map { codeFile -> CodeFileRef(pdxName, codeFile) }
                }.toSet()

        return files.mapNotNull { ref ->
            val lookupKey = "${ref.pdxName}${ODXCollectionGroup.PDX_NAME_SEPARATOR}${ref.codeFile}"
            val data = inputData[lookupKey]

            checkNotNull(data) {
                "File ${ref.codeFile} is not included in PDX ${ref.pdxName}"
            }
            val chunkName = odx.codeFileKey(ref.pdxName, ref.codeFile)
            logger.info("Including $chunkName (${data.size} bytes)")
            Chunk
                .newBuilder()
                .setName(chunkName)
                .setType(Chunk.DataType.CODE_FILE)
                .setUncompressedSize(data.size)
                .setData(ByteString.copyFrom(data.inputStream.invoke().use { it.readAllBytes() }))
        }
    }

    fun createPartialChunks(
        logger: Logger,
        inputData: Map<String, ZipEntryInfos>,
        odx: ODXCollectionGroup,
        options: ConverterOptions,
    ): List<Chunk.Builder> {
        if (options.partialJobFiles.isEmpty()) {
            return emptyList()
        }

        val files =
            odx
                .collectionsWithPdxName()
                .flatMap { (collection, pdxName) ->
                    val jobFiles =
                        collection.singleEcuJobs.values
                            .flatMap { it.progcodes?.progcode ?: emptyList() }
                            .mapNotNull { it.codefile }
                    val libraries = collection.libraries.values.mapNotNull { it.codefile }
                    (jobFiles + libraries).map { codeFile -> CodeFileRef(pdxName, codeFile) }
                }.toSet()

        return files.flatMap { ref ->
            val jobFileName = ref.codeFile
            val lookupKey = "${ref.pdxName}${ODXCollectionGroup.PDX_NAME_SEPARATOR}$jobFileName"
            // The (possibly PDX-prefixed) key used for chunk naming, matching the
            // reference stored in the flatbuffer.
            val codeFileKey = odx.codeFileKey(ref.pdxName, jobFileName)
            options.partialJobFiles
                .mapNotNull { partial ->
                    if (!jobFileName.matches(Regex(partial.jobFilePattern))) {
                        null
                    } else {
                        logger.fine("Job file $jobFileName matches pattern")
                        check(inputData.containsKey(lookupKey)) {
                            "File $jobFileName is not included in PDX ${ref.pdxName}"
                        }
                        PartialJobFilePattern(jobFileName, partial)
                    }
                }.groupBy {
                    it.jobFileName
                }.flatMap { entry ->
                    val data =
                        inputData[lookupKey] ?: error("File $jobFileName is not included in PDX ${ref.pdxName}")
                    if (jobFileName.endsWith(".jar", ignoreCase = true) ||
                        jobFileName.endsWith(".zip", ignoreCase = true)
                    ) {
                        ZipInputStream(data.inputStream.invoke()).use { zip ->
                            val matches =
                                extractMatchingFilesFromZip(
                                    logger,
                                    zip,
                                    entry.value.map { pjfp -> Regex(pjfp.partialFilePattern.includePattern) },
                                )
                            matches.map { match ->
                                val filename = match.first
                                val matchData = match.second

                                logger.info("Including $filename from $codeFileKey (${matchData.size} bytes)")

                                Chunk
                                    .newBuilder()
                                    .setName("$codeFileKey::$filename")
                                    .setType(Chunk.DataType.CODE_FILE_PARTIAL)
                                    .setUncompressedSize(matchData.size.toLong())
                                    .setData(ByteString.copyFrom(matchData))
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
        odxCollection: ODXCollectionGroup,
        options: ConverterOptions,
    ): Chunk.Builder {
        val dw = DatabaseWriter(logger = logger, odx = odxCollection, options = options)
        val data = dw.createOdxData()
        return Chunk
            .newBuilder()
            .setName("diagnostic_description")
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
