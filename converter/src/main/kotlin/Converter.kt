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
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
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
            plugin.processChunk(pluginHandler, initialData, handler)
            logger.fine(
                "Chunk '${chunk.name}' (${chunk.type}) was processed by plugin '${plugin.getPluginIdentifier()}': $sizeBefore bytes -> ${chunk.data.size()} bytes",
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

        ZipFile(inputFile).use { zipFile ->
            val readParseFileDuration =
                measureTime {
                    fillInputFileData(zipFile, odxData, inputFileData)
                }
            logger.fine("Reading and parsing into objects took $readParseFileDuration")

            val odxRawSize = inputFileData.filter { it.key.contains(".odx") }.map { it.value.size }.sum()

            val odxCollection = ODXCollection(odxData, odxRawSize)

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
                    // additional metadata?

                    val pluginHandler =
                        PluginApiHandler(mddFile, logger) { chunk, pluginApiHandler ->
                            logger.info("Chunk '${chunk.name}' (${chunk.type}) was added by a plugin")
                            handleAndAddChunk(chunk, plugins, pluginApiHandler, stats, mddFile)
                        }

                    plugins.forEach { plugin ->
                        plugin.beforeProcessing(pluginHandler)
                    }

                    val chunkBuilder = ChunkBuilder()
                    compressionDuration =
                        measureTime {
                            val chunk = chunkBuilder.createEcuDataChunk(logger, odxCollection, options)
                            val stat = handleAndAddChunk(chunk, plugins, pluginHandler, stats, mddFile)
                            stat?.rawSize = odxCollection.rawSize
                        }
                    val jobChunks = chunkBuilder.createJobsChunks(logger, inputFileData, odxCollection, options)
                    jobChunks.forEach { chunk ->
                        handleAndAddChunk(chunk, plugins, pluginHandler, stats, mddFile)
                    }
                    val partialChunks = chunkBuilder.createPartialChunks(logger, inputFileData, odxCollection, options)
                    partialChunks.forEach { chunk ->
                        handleAndAddChunk(chunk, plugins, pluginHandler, stats, mddFile)
                    }

                    plugins.forEach { plugin ->
                        plugin.afterProcessing(pluginHandler)
                    }

                    sizeUncompressed = mddFile.chunksList.sumOf { it.uncompressedSize }

                    val mddFileOut = mddFile.build()
                    BufferedOutputStream(outputFile.outputStream()).use {
                        it.write(FILE_MAGIC)
                        mddFileOut.writeTo(it)
                    }
                }

            val sizeCompressed = outputFile.toPath().fileSize()
            logger.info(
                "Writing database took $writingDuration total (compression: $compressionDuration) - sizes: odx raw: ${odxRawSize.format()} bytes, uncompressed chunks: ${sizeUncompressed.format()} bytes, compressed mdd: ${sizeCompressed.format()} bytes",
            )
        }
    }

    private fun fillInputFileData(
        zipFile: ZipFile,
        odxData: MutableMap<String, ODX>,
        inputFileData: MutableMap<String, ZipEntryInfos>,
    ) {
        zipFile.entries().toList().forEach { entry ->
            if (entry.isDirectory) {
                return@forEach
            }
            inputFileData[entry.name] =
                ZipEntryInfos(
                    size = entry.size,
                ) { zipFile.getInputStream(entry) }
        }

        val unmarshaller = context.createUnmarshaller()

        var hadParseErrors = false
        // Output ODX validation errors to log file
        unmarshaller.eventHandler =
            ValidationEventHandler { event ->
                val level =
                    when (event.severity) {
                        ValidationEvent.FATAL_ERROR -> Level.SEVERE
                        ValidationEvent.ERROR -> Level.SEVERE
                        ValidationEvent.WARNING -> Level.WARNING
                        else -> Level.INFO
                    }
                logger.log(level, "ODX error: ${event.locator} ${event.message}")
                hadParseErrors = true
                true // keep going
            }

        inputFileData.forEach { entry ->
            if (!entry.key.contains(".odx")) {
                return@forEach
            }
            val odx =
                entry.value.inputStream.invoke().use {
                    unmarshaller
                        .unmarshal(
                            XMLInputFactory.newFactory().createXMLEventReader(it),
                            ODX::class.java,
                        ).value
                }
            odxData[entry.key] = odx
        }

        if (hadParseErrors) {
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
            "Whether to also log to console when processing multiple files (if only one file is processed, logging is always done on console in addition to the log file)",
        ).flag(default = false)

    private var hadErrors: Boolean = false
    private val context: JAXBContext =
        org.eclipse.persistence.jaxb.JAXBContextFactory
            .createContext(arrayOf(ODX::class.java), null)

    private fun createConsoleLogHandler(fileName: String): StreamHandler? {
        if (pdxFiles.size == 1) {
            return ConsoleHandlerWithFile(Level.INFO, null)
        } else if (logOnConsole) {
            return ConsoleHandlerWithFile(Level.INFO, fileName)
        }
        return null
    }

    override fun run() {
        if (version) {
            println("Version: " + ManifestReader.version)
            println("Built: " + ManifestReader.buildDate)
            println("Commit: " + ManifestReader.commitHash)
            exitProcess(0)
        }
        val stats = mutableListOf<ChunkStat>()
        val executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
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
    println("${converter.commandName} - version: ${ManifestReader.version}\n")
    if (args.isEmpty()) {
        converter.main(arrayOf("--help"))
    } else {
        converter.main(args)
    }
}
