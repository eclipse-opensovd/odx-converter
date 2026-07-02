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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import converter.plugin.api.ConverterApi
import converter.plugin.api.ConverterPlugin
import converter.plugin.api.ConverterPluginProvider
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.ValidationEvent
import jakarta.xml.bind.ValidationEventHandler
import kotlinx.serialization.json.Json
import org.eclipse.opensovd.cda.mdd.Chunk
import org.eclipse.opensovd.cda.mdd.MDDFile
import schema.odx.ODX
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.util.ServiceLoader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.StreamHandler
import java.util.zip.ZipFile
import javax.xml.stream.XMLInputFactory
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.io.path.fileSize
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.measureTime

class ZipEntryInfos(
    val size: Long,
    val inputStream: () -> InputStream,
)

class FileConverter(
    private val logger: Logger,
    private val context: JAXBContext,
) {
    private fun retrievePlugins(): List<ConverterPlugin> {
        val provider = ServiceLoader.load(ConverterPluginProvider::class.java)
        return provider
            .flatMap { it.getPlugins() }
            .sortedBy { it.getPluginPriority() }
    }

    private fun getCurrentTimeReproducible(): Instant {
        val epochSeconds = System.getenv("SOURCE_DATE_EPOCH")?.toLong() ?: Instant.now().epochSecond
        return Instant.ofEpochSecond(epochSeconds)
    }

    private fun handleAndAddChunk(
        chunk: Chunk.Builder,
        plugins: List<ConverterPlugin>,
        pluginHandler: ConverterApi,
        stats: MutableList<ChunkStat>,
        mddFile: MDDFile.Builder,
    ): ChunkStat? {
        val initialData = chunk.data.toByteArray()

        val pluginsAsText =
            plugins.joinToString(", ") {
                it.getPluginIdentifier()
            }
        logger.fine("Chunk '${chunk.name}' (${chunk.type}) to be processed by plugins: $pluginsAsText")

        plugins.forEach { plugin ->
            val sizeBefore = chunk.data.size()
            logger.fine("Chunk '${chunk.name}' (${chunk.type}) to be processed by plugin '${plugin.getPluginIdentifier()}'")
            val handler = ChunkApiHandler(chunk)
            val pluginDuration =
                measureTime {
                    plugin.processChunk(pluginHandler, initialData, handler)
                }
            logger.fine(
                "Chunk '${chunk.name}' (${chunk.type}) was processed by plugin '${plugin.getPluginIdentifier()}' in $pluginDuration: $sizeBefore bytes -> ${chunk.data.size()} bytes",
            )
            if (handler.removeChunk) {
                logger.info(
                    "Chunk '${chunk.name}' (${chunk.type}) was removed by plugin '${plugin.getPluginIdentifier()}', processing aborted",
                )
                return null
            }
        }

        val stat =
            ChunkStat(
                chunkName = chunk.name,
                chunkType = chunk.type,
                uncompressedSize = initialData.size.toLong(),
                compressedSize = chunk.data.size().toLong(),
            )
        stats.add(stat)

        mddFile.addChunks(chunk)
        logger.info(
            "Chunk '${chunk.name}' (${chunk.type}) was added to the file with ${stat.compressedSize?.format()} bytes of data, initial size: ${stat.uncompressedSize.format()} bytes",
        )
        return stat
    }

    fun convert(
        inputFile: File,
        outputFile: File,
        options: ConverterOptions,
        stats: MutableList<ChunkStat>,
    ) {
        logger.info("Converting '${inputFile.name}' to mdd")

        val odxData = mutableMapOf<String, ODX>()

        val inputFileData = mutableMapOf<String, ZipEntryInfos>()

        val linkCollector = ODXLinkCollector()

        ZipFile(inputFile).use { zipFile ->
            val readParseFileDuration =
                measureTime {
                    fillInputFileData(zipFile, odxData, inputFileData, linkCollector)
                }
            logger.fine("Reading and parsing into objects took $readParseFileDuration")

            val odxRawSize = inputFileData.filter { it.key.contains(".odx") }.map { it.value.size }.sum()

            val odxCollection: ODXCollectionGroup
            val indexingDuration =
                measureTime {
                    odxCollection =
                        ODXCollectionGroup(odxData, odxRawSize, options, logger, linkCollector.linkToFile)
                }
            logger.fine("Building ODX collection index took $indexingDuration")

            if (options.withAudiences.isNotEmpty()) {
                val validAudiences = odxCollection.additionalAudiences.map { it.shortname }
                val invalidAudiences =
                    options.withAudiences.filter { requested ->
                        validAudiences.none { it.equals(requested, ignoreCase = true) }
                    }
                if (invalidAudiences.isNotEmpty()) {
                    logger.warning(
                        "The following audiences specified with --with-audience are not defined in the diagnostic description: " +
                            "${invalidAudiences.joinToString(", ")}. Valid audiences are: ${validAudiences.joinToString(", ")}",
                    )
                }
            }

            var compressionDuration: Duration = Duration.ZERO
            val plugins = retrievePlugins()

            var sizeUncompressed: Long = 0
            val writingDuration =
                measureTime {
                    val mddFile = MDDFile.newBuilder()
                    mddFile.version = "2025-05-21"
                    mddFile.ecuName = odxCollection.ecuName
                    odxCollection.odxRevision?.let {
                        mddFile.revision = it
                    }

                    mddFile.putMetadata("created", getCurrentTimeReproducible().toString())
                    mddFile.putMetadata("source", inputFile.name)
                    mddFile.putMetadata("options", Json.encodeToString(options))
                    mddFile.putMetadata(
                        "converter",
                        "${ManifestReader.title} ${ManifestReader.version} (${ManifestReader.commitHash.take(7)})",
                    )
                    mddFile.putMetadata(
                        "plugins",
                        plugins.joinToString(", ") { "${it.getPluginIdentifier()}@${it.getPluginVersion()}" },
                    )

                    val pluginHandler =
                        PluginApiHandler(mddFile, logger) { chunk, pluginApiHandler ->
                            logger.info("Chunk '${chunk.name}' (${chunk.type}) was added by a plugin")
                            handleAndAddChunk(chunk, plugins, pluginApiHandler, stats, mddFile)
                        }

                    plugins.forEach { plugin ->
                        plugin.beforeProcessing(pluginHandler)
                    }

                    val chunkBuilder = ChunkBuilder()
                    var buildDiagDescDuration: Duration = Duration.ZERO
                    compressionDuration =
                        measureTime {
                            val chunk: Chunk.Builder
                            buildDiagDescDuration =
                                measureTime {
                                    chunk = chunkBuilder.createEcuDataChunk(logger, odxCollection, options)
                                }
                            logger.fine("Building diagnostic description (FlatBuffers) took $buildDiagDescDuration")
                            val stat = handleAndAddChunk(chunk, plugins, pluginHandler, stats, mddFile)
                            stat?.rawSize = odxCollection.rawSize
                        }
                    logger.fine(
                        "Plugin processing (compression + hashing) of diagnostic description took ${compressionDuration - buildDiagDescDuration}",
                    )

                    var jobChunksDuration: Duration
                    val jobChunks: List<Chunk.Builder>
                    jobChunksDuration =
                        measureTime {
                            jobChunks = chunkBuilder.createJobsChunks(logger, inputFileData, odxCollection, options)
                            jobChunks.forEach { chunk ->
                                handleAndAddChunk(chunk, plugins, pluginHandler, stats, mddFile)
                            }
                        }
                    if (jobChunks.isNotEmpty()) {
                        logger.fine("Creating and processing ${jobChunks.size} job chunk(s) took $jobChunksDuration")
                    }

                    var partialChunksDuration: Duration
                    val partialChunks: List<Chunk.Builder>
                    partialChunksDuration =
                        measureTime {
                            partialChunks = chunkBuilder.createPartialChunks(logger, inputFileData, odxCollection, options)
                            partialChunks.forEach { chunk ->
                                handleAndAddChunk(chunk, plugins, pluginHandler, stats, mddFile)
                            }
                        }
                    if (partialChunks.isNotEmpty()) {
                        logger.fine("Creating and processing ${partialChunks.size} partial chunk(s) took $partialChunksDuration")
                    }

                    plugins.forEach { plugin ->
                        plugin.afterProcessing(pluginHandler)
                    }

                    sizeUncompressed = mddFile.chunksList.sumOf { it.uncompressedSize }

                    val serializationDuration =
                        measureTime {
                            val mddFileOut = mddFile.build()
                            BufferedOutputStream(outputFile.outputStream()).use {
                                it.write(FILE_MAGIC)
                                mddFileOut.writeTo(it)
                            }
                        }
                    logger.fine("MDD file serialization (protobuf build + write) took $serializationDuration")
                }

            val sizeCompressed = outputFile.toPath().fileSize()
            logger.info(
                "Writing database took $writingDuration total (compression: $compressionDuration) - sizes: odx raw: ${odxRawSize.format()} bytes, uncompressed chunks: ${sizeUncompressed.format()} bytes, compressed mdd: ${sizeCompressed.format()} bytes",
            )
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private fun fillInputFileData(
        zipFile: ZipFile,
        odxData: MutableMap<String, ODX>,
        inputFileData: MutableMap<String, ZipEntryInfos>,
        linkCollector: ODXLinkCollector,
    ) {
        val zipReadDuration =
            measureTime {
                zipFile.entries().toList().forEach { entry ->
                    if (entry.isDirectory) {
                        return@forEach
                    }
                    inputFileData[entry.name] =
                        ZipEntryInfos(
                            size = entry.size,
                        ) { zipFile.getInputStream(entry) }
                }
            }
        logger.fine("Reading ZIP entries took $zipReadDuration (${inputFileData.size} entries)")

        val odxEntries = inputFileData.filter { it.key.contains(".odx") }
        val hadParseErrors = AtomicBoolean(false)
        val xmlInputFactory = XMLInputFactory.newFactory()

        val xmlParsingDuration =
            measureTime {
                val results =
                    odxEntries.entries
                        .parallelStream()
                        .map { (fileName, entryInfo) ->
                            val perFileCollector = ODXLinkCollector()
                            perFileCollector.currentFile = fileName
                            val unmarshaller = context.createUnmarshaller()
                            unmarshaller.listener = perFileCollector
                            unmarshaller.eventHandler =
                                ValidationEventHandler { event ->
                                    val level =
                                        when (event.severity) {
                                            ValidationEvent.FATAL_ERROR -> Level.SEVERE
                                            ValidationEvent.ERROR -> Level.SEVERE
                                            ValidationEvent.WARNING -> Level.WARNING
                                            else -> Level.INFO
                                        }
                                    logger.log(level, "ODX error in $fileName: ${event.locator} ${event.message}")
                                    hadParseErrors.store(true)
                                    true // keep going
                                }
                            val odx =
                                entryInfo.inputStream.invoke().use {
                                    unmarshaller
                                        .unmarshal(
                                            xmlInputFactory.createXMLStreamReader(it),
                                            ODX::class.java,
                                        ).value
                                }
                            Triple(fileName, odx, perFileCollector.linkToFile)
                        }.toList()

                // Merge results back (sequential, fast)
                results.forEach { (fileName, odx, links) ->
                    odxData[fileName] = odx
                    linkCollector.linkToFile.putAll(links)
                }
            }
        logger.fine("XML parsing (JAXB unmarshalling) took $xmlParsingDuration (${odxEntries.size} ODX files, parallel)")

        if (hadParseErrors.load()) {
            error("Errors were encountered while parsing the ODX file, see log for details, aborting")
        }
    }
}

class Converter : CliktCommand(name = "odx-converter") {
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
                "Argument can be repeated, and is in the format: <regex for job-file-name pattern> <regex for content file-name pattern>.",
        ).pair()
        .multiple()

    val version: Boolean by option("-V", "--version")
        .flag()

    val logLevel: Level? by option("--log-level")
        .help("Sets the log level for the .mdd.log files")
        .choice(
            mapOf(
                "info" to Level.INFO,
                "debug" to Level.FINE,
                "trace" to Level.FINEST,
            ),
        )

    val logOnConsole: Boolean by option("--log-on-console")
        .help(
            "Whether to also log to console when processing multiple files (if only one file is processed, " +
                "logging is always done on console in addition to the log file)",
        ).flag(default = false)

    val parallel: Int by option("-j", "--parallel")
        .help("Maximum number of files to process in parallel (default: number of available processors)")
        .int()
        .default(Runtime.getRuntime().availableProcessors())

    val withAudiences: List<String> by option("--with-audience")
        .help(
            "Includes services only when audience short names match - can be used multiple times, services without " +
                "any enabled audience will always be included, but services with enabled audiences will only be " +
                "included if at least one of the audience entries matches",
        ).multiple()

    private var hadErrors: Boolean = false
    private val context: JAXBContext =
        org.eclipse.persistence.jaxb.JAXBContextFactory
            .createContext(arrayOf(ODX::class.java), null)

    private fun createConsoleLogHandler(fileName: String): StreamHandler? {
        if (pdxFiles.size == 1) {
            return ConsoleHandlerWithFile(logLevel ?: Level.INFO, null)
        } else if (logOnConsole) {
            return ConsoleHandlerWithFile(logLevel ?: Level.INFO, fileName)
        }
        return null
    }

    override fun run() {
        if (version) {
            println(ManifestReader.title)
            println("Version: " + ManifestReader.version + "+" + ManifestReader.commitHash.take(7))
            println("Built: " + ManifestReader.buildDate)
            println("Commit: " + ManifestReader.commitHash)
            exitProcess(0)
        }
        val stats = mutableListOf<ChunkStat>()
        if (parallel <= 0) {
            System.err.println("Invalid parallel value: $parallel, must be greater than 0")
            exitProcess(-1)
        }
        val executors = Executors.newFixedThreadPool(parallel)
        // sort by descending file size as a rough guesstimate of processing time
        pdxFiles.forEach { inputFile ->
            val outputDir = outputDir ?: inputFile.parentFile

            val fileLogLevel = logLevel ?: Level.INFO
            executors.submit {
                try {
                    println("Processing ${inputFile.name}")
                    val duration =
                        measureTime {
                            val logger = Logger.getLogger(inputFile.name)
                            val logFile = File(outputDir, "${inputFile.nameWithoutExtension}.mdd.log")

                            WriteToFileHandler(
                                fileLogLevel,
                                logFile,
                            ).use { handler ->
                                logger.level = fileLogLevel
                                logger.useParentHandlers = false
                                logger.addHandler(handler)

                                val consoleHandler = createConsoleLogHandler(inputFile.name)
                                consoleHandler?.let {
                                    logger.addHandler(it)
                                }
                                try {
                                    val outFile = File(outputDir, "${inputFile.nameWithoutExtension}.mdd")
                                    val options =
                                        ConverterOptions(
                                            lenient = this.lenient,
                                            includeJobFiles = this.includeJobFiles,
                                            partialJobFiles =
                                                this.partialJobFiles.map {
                                                    PartialFilePattern(
                                                        it.first,
                                                        it.second,
                                                    )
                                                },
                                            withAudiences = withAudiences,
                                        )
                                    val converter = FileConverter(logger, context)
                                    converter.convert(inputFile, outFile, options, stats)
                                } catch (e: Exception) {
                                    hadErrors = true
                                    logger.severe("Error while converting file ${inputFile.name}: ${e.message}", e)
                                    if (consoleHandler == null) {
                                        println("Error while processing ${inputFile.name}: ${e.stackTraceToString()} ")
                                    }
                                } finally {
                                    consoleHandler?.close()
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
        println(
            "Processed ${diagDescriptions.size.format()} diagnostic description chunks: total raw size ${rawSize.format()}, total uncompressed size: ${uncompressedSize.format()}, compressed size: ${compressedSize.format()}",
        )
    }
}

fun main(args: Array<String>) {
    val converter = Converter()
    println("${ManifestReader.title} - version: ${ManifestReader.version}+${ManifestReader.commitHash.take(7)}\n")
    if (args.isEmpty()) {
        converter.main(arrayOf("--help"))
    } else {
        converter.main(args)
    }
}
