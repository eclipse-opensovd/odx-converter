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
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.google.protobuf.ByteString
import jakarta.xml.bind.JAXBContext
import kotlinx.serialization.json.Json
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream
import org.apache.commons.io.output.CountingOutputStream
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import schema.odx.*
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.xml.stream.XMLInputFactory
import kotlin.io.path.fileSize
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.measureTime


private val context: JAXBContext = org.eclipse.persistence.jaxb.JAXBContextFactory
    .createContext(arrayOf(ODX::class.java), null)

fun convert(
    inputFile: File,
    outputFile: File,
    logger: Logger,
    options: ConverterOptions,
    stats: MutableList<ChunkStat>
) {
    logger.info("Converting ${inputFile.name} to mdd")
    val unmarshaller = context.createUnmarshaller()

    val odxData = mutableMapOf<String, ODX>()

    val inputFileData = mutableMapOf<String, ByteArray>()

    var odxRawSize = 0
    val readParseFileDuration = measureTime {
        ZipFile(inputFile).use { zip ->
            zip.entries().toList().forEach { entry ->
                if (entry.isDirectory) {
                    return@forEach
                }
                zip.getInputStream(entry).use { input ->
                    inputFileData[entry.name] = input.readBytes()
                }
            }
        }


        inputFileData.forEach { entry ->
            if (!entry.key.contains(".odx")) {
                return@forEach
            }
            odxRawSize += entry.value.size
            val odx = unmarshaller.unmarshal(
                XMLInputFactory.newFactory().createXMLEventReader(ByteArrayInputStream(entry.value)),
                ODX::class.java
            ).value
            odxData[entry.key] = odx
        }
    }
    logger.fine("Reading and parsing into objects took $readParseFileDuration")

    val odxCollection = ODXCollection(odxData, odxRawSize)

    var sizeUncompressed: Long = 0

    var compressionDuration: Duration = Duration.ZERO
    val writingDuration = measureTime {
        val mddFile = MDDFile.newBuilder()
        mddFile.version = "2025-05-21"
        mddFile.ecuName = odxCollection.ecuName
        mddFile.revision = odxCollection.odxRevision

        mddFile.putMetadata("created", Instant.now().toString())
        mddFile.putMetadata("source", inputFile.name)
        mddFile.putMetadata("options", Json.encodeToString(options))
        // additional metadata?

        compressionDuration = measureTime {
            val chunk = createEcuDataChunk(logger, odxCollection, options, stats)
            mddFile.addChunks(chunk)
            sizeUncompressed = chunk.uncompressedSize
        }
        mddFile.addAllChunks(createJobsChunks(logger, inputFileData, odxCollection, options, stats))
        mddFile.addAllChunks(createPartialChunks(logger, inputFileData, odxCollection, options, stats))

        val mddFileOut = mddFile.build()
        BufferedOutputStream(outputFile.outputStream()).use {
            it.write(FILE_MAGIC)
            mddFileOut.writeTo(it)
        }
    }


    val sizeCompressed = outputFile.toPath().fileSize()
    logger.info("Writing database took $writingDuration total (compression: $compressionDuration) - sizes: odx raw: ${odxRawSize.format()} bytes, uncompressed flatbuffers: ${sizeUncompressed.format()} bytes, compressed flatbuffers: ${sizeCompressed.format()} bytes - ratio: ${(sizeUncompressed.toFloat() / sizeCompressed).format()} ")
}

fun createJobsChunks(
    logger: Logger,
    inputData: Map<String, ByteArray>,
    odx: ODXCollection,
    options: ConverterOptions,
    chunkStats: MutableList<ChunkStat>,
): List<Chunk> {
    if (!options.includeJobFiles) {
        return emptyList()
    }
    val jobFiles = odx.singleEcuJobs.values.flatMap { it.progcodes?.progcode ?: emptyList() }.mapNotNull { it.codefile }
    val libraries = odx.libraries.values.mapNotNull { it.codefile }
    val files = (jobFiles + libraries).toSet()
    return files.mapNotNull { fileName ->
        val data = inputData[fileName]
        if (data == null) {
            throw IllegalStateException("File $fileName is not included in PDX")
        } else {
            logger.info("Including $fileName (${data.size} bytes)")
            chunkStats.add(ChunkStat(
                chunkName = fileName,
                chunkType = Chunk.DataType.CODE_FILE,
                rawSize = null,
                uncompressedSize = data.size.toLong(),
                compressedSize = null,
            ))
            Chunk.newBuilder()
                .setName(fileName)
                .setType(Chunk.DataType.CODE_FILE)
                .setUncompressedSize(data.size.toLong())
                .setData(ByteString.copyFrom(data))
                .build()
        }
    }
}

fun createPartialChunks(
    logger: Logger,
    inputData: Map<String, ByteArray>,
    odx: ODXCollection,
    options: ConverterOptions,
    chunkStats: MutableList<ChunkStat>,
): List<Chunk> {
    if (options.partialJobFiles.isEmpty()) {
        return emptyList()
    }

    val jobFiles = odx.singleEcuJobs.values.flatMap { it.progcodes?.progcode ?: emptyList() }.mapNotNull { it.codefile }
    val libraries = odx.libraries.values.mapNotNull { it.codefile }
    val files = (jobFiles + libraries).toSet()
    return files.flatMap { jobFileName ->
        options.partialJobFiles.mapNotNull { partial ->
            if (!jobFileName.matches(Regex(partial.jobFilePattern))) {
                null
            } else {
                logger.fine("Job file $jobFileName matches pattern")
                val data = inputData[jobFileName]
                if (data == null) {
                    throw IllegalStateException("File $jobFileName is not included in PDX")
                }
                PartialJobFilePattern(jobFileName, partial)
            }
        }.groupBy {
            it.jobFileName
        }.flatMap {
            val data = inputData[it.key] ?: throw IllegalStateException("File $jobFileName is not included in PDX")
            if (it.key.endsWith(".jar", ignoreCase = true) || it.key.endsWith(".zip", ignoreCase = true)) {
                ZipInputStream(ByteArrayInputStream(data)).use { zip ->
                    val matches = extractMatchingFilesFromZip(
                        logger,
                        zip,
                        it.value.map { pjfp -> Regex(pjfp.partialFilePattern.includePattern) })
                    matches.map { match ->
                        val filename = match.first
                        val data = match.second

                        val out = ByteArrayOutputStream()
                        LZMACompressorOutputStream(out).use { outputStream ->
                            outputStream.write(data)
                        }
                        val compressed = out.toByteArray()
                        logger.info("Including $filename from $jobFileName (${compressed.size} bytes compressed / ${data.size} bytes uncompressed)")

                        chunkStats.add(ChunkStat(
                            chunkName = "$jobFileName::$filename",
                            chunkType = Chunk.DataType.CODE_FILE_PARTIAL,
                            rawSize = null,
                            uncompressedSize = data.size.toLong(),
                            compressedSize = compressed.size.toLong(),
                        ))

                        Chunk.newBuilder()
                            .setName("$jobFileName::$filename")
                            .setType(Chunk.DataType.CODE_FILE_PARTIAL)
                            .setUncompressedSize(data.size.toLong())
                            .setCompressionAlgorithm("lzma")
                            .setData(ByteString.copyFrom(compressed))
                            .build()
                    }
                }
            } else {
                emptyList()
            }
        }
    }
}

fun extractMatchingFilesFromZip(
    logger: Logger,
    zip: ZipInputStream,
    patterns: List<Regex>
): List<Pair<String, ByteArray>> {
    val files = mutableListOf<Pair<String, ByteArray>>()

    var entry: ZipEntry? = zip.nextEntry
    while (entry != null) {
        logger.finest { "Checking ${entry?.name} against patterns $patterns" }
        if (patterns.any { entry?.name?.matches(it) == true }) {
            files.add(Pair(entry.name, zip.readAllBytes()))
        }
        zip.closeEntry()
        entry = zip.nextEntry
    }
    return files
}

data class PartialJobFilePattern(
    val jobFileName: String,
    val partialFilePattern: PartialFilePattern,
)

fun createEcuDataChunk(
    logger: Logger,
    odxCollection: ODXCollection,
    options: ConverterOptions,
    chunkStats: MutableList<ChunkStat>
): Chunk {
    ByteString.newOutput().use { out ->
        var sizeUncompressed: Long
        LZMACompressorOutputStream(out).use { outputStream ->
            val dw = DatabaseWriter(logger = logger, odx = odxCollection, options = options)
            val countingOutputStream = CountingOutputStream(outputStream)
            val data = dw.createEcuData()
            countingOutputStream.write(data)
            sizeUncompressed = countingOutputStream.byteCount
        }
        chunkStats.add(ChunkStat(
            chunkName = odxCollection.ecuName,
            chunkType = Chunk.DataType.DIAGNOSTIC_DESCRIPTION,
            rawSize = odxCollection.rawSize.toLong(),
            uncompressedSize = sizeUncompressed.toLong(),
            compressedSize = out.size().toLong(),
        ))
        return Chunk.newBuilder()
            .setName(odxCollection.ecuName)
            .setType(Chunk.DataType.DIAGNOSTIC_DESCRIPTION)
            .setCompressionAlgorithm("lzma")
            .setUncompressedSize(sizeUncompressed)
            .setData(out.toByteString())
            .build()
    }
}

class Converter : CliktCommand() {
    val pdxFiles: List<File> by argument(name = "pdx-files")
        .file(mustExist = true, mustBeReadable = true, canBeFile = true)
        .help("pdx files to convert")
        .multiple()

    val outputDir: File? by option("-O", "--output-directory")
        .help("output directory for files (default: same as pdx-file)")
        .file(mustExist = true, canBeDir = true, mustBeWritable = true)

    val lenient: Boolean by option("-L", "--lenient")
        .flag(default = false)

    val includeJobFiles: Boolean by option("--include-job-files")
        .help("Include job files & libraries referenced in single ecu jobs")
        .flag(default = false)

    val partialJobFiles: List<Pair<String, String>> by option("--partial-job-files")
        .help(
            "Include job files partially, and spread the contents as individual chunks. " +
                    "Argument can be repeated, and are in the format: <regex for job-file-name pattern> <regex for content file-name pattern>."
        )
        .pair()
        .multiple()

    private var hadErrors: Boolean = false

    override fun run() {
        val stats = mutableListOf<ChunkStat>()
        val executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        pdxFiles.forEach { inputFile ->
            val outputDir = outputDir ?: inputFile.parentFile

            val fileLogLevel = Level.INFO
            executors.submit {
                try {
                    println("Processing ${inputFile.name}")
                    val duration = measureTime {
                        val logger = Logger.getLogger(inputFile.name)
                        WriteToFileHandler(
                            fileLogLevel,
                            File(outputDir, "${inputFile.nameWithoutExtension}.mdd.log")
                        ).use { handler ->
                            logger.level = fileLogLevel
                            logger.useParentHandlers = false
                            logger.addHandler(handler)
                            try {
                                val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.mdd")
                                val options = ConverterOptions(
                                    lenient = this.lenient,
                                    includeJobFiles = this.includeJobFiles,
                                    partialJobFiles = this.partialJobFiles.map {
                                        PartialFilePattern(
                                            it.first,
                                            it.second
                                        )
                                    },
                                )
                                convert(inputFile, outFile, logger, options, stats)
                            } catch (e: Exception) {
                                hadErrors = true
                                logger.severe("Error while converting file ${inputFile.name}: ${e.message}", e)
                                println("Error while processing ${inputFile.name}: ${e.stackTraceToString()} ")
                            }
                        }
                    }
                    println("Finished processing ${inputFile.name} after $duration")
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
        executors.shutdown()
        executors.awaitTermination(1, TimeUnit.HOURS)
        if (hadErrors) {
            exitProcess(1)
        }
        val diagDescriptions = stats.filter { it.chunkType == Chunk.DataType.DIAGNOSTIC_DESCRIPTION }
        val rawSize = diagDescriptions.sumOf { it.rawSize ?: 0 }
        val uncompressedSize = diagDescriptions.sumOf { it.uncompressedSize }
        val compressedSize = diagDescriptions.sumOf { it.compressedSize ?: 0 }
        println("Processed ${diagDescriptions.size.format()} diagnostic description chunks: total raw size ${rawSize.format()}, total uncompressed size: ${uncompressedSize.format()}, compressed size: ${compressedSize.format()}")
    }
}

fun Number.format(): String =
    NumberFormat.getNumberInstance().format(this)

fun main(args: Array<String>) =
    Converter().main(args)

