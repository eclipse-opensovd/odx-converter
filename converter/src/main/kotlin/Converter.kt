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
import org.eclipse.opensovd.cda.mdd.EcuData
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

class ODXCollection(val data: Map<String, ODX>) {
    val ecuName: String by lazy {
        baseVariantODX.diaglayercontainer?.basevariants?.basevariant?.first()?.shortname
            ?: throw IllegalStateException("No base variant")
    }
    val odxRevision: String by lazy {
        // sort by date, or semantic version of revision?
        baseVariantODX.diaglayercontainer?.admindata?.docrevisions?.docrevision?.lastOrNull()?.revisionlabel
            ?: throw IllegalStateException("No doc revisions")
    }

    val diagLayerContainer: Map<String, DIAGLAYERCONTAINER> by lazy {
        data.values
            .mapNotNull { it.diaglayercontainer }
            .associateBy { it.id }
    }

    val baseVariantODX: ODX by lazy {
        data.values.filter { it.diaglayercontainer?.basevariants?.basevariant != null }.first()
    }

    val ecuSharedDatas: Map<String, ECUSHAREDDATA> by lazy {
        val data = diagLayerContainer.flatMap { it.value.ecushareddatas?.ecushareddata ?: emptyList() }

        data.associateBy { it.id }
    }

    val functClasses: Map<String, FUNCTCLASS> by lazy {
        val data = basevariants.flatMap { it.value.functclasss?.functclass ?: emptyList() } +
                ecuvariants.flatMap { it.value.functclasss?.functclass ?: emptyList() } +
                ecuSharedDatas.flatMap { it.value.functclasss?.functclass ?: emptyList() }
        data.associateBy { it.id }
    }

    val basevariants: Map<String, BASEVARIANT> by lazy {
        data.values
            .flatMap { it.diaglayercontainer?.basevariants?.basevariant ?: emptyList() }
            .associateBy { it.id }
    }

    val ecuvariants: Map<String, ECUVARIANT> by lazy {
        data.values
            .flatMap { it.diaglayercontainer?.ecuvariants?.ecuvariant ?: emptyList() }
            .associateBy { it.id }
    }

    val functionalGroups: Map<String, FUNCTIONALGROUP> by lazy {
        val data = data.values
            .flatMap { it.diaglayercontainer?.functionalgroups?.functionalgroup ?: emptyList() }

        data.associateBy { it.id }
    }

    val diagServices: Map<String, DIAGSERVICE> by lazy {
        basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<DIAGSERVICE>()
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .associateBy { it.id }
    }

    val singleEcuJobs: Map<String, SINGLEECUJOB> by lazy {
        basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<SINGLEECUJOB>()
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<SINGLEECUJOB>()
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<SINGLEECUJOB>()
                    .associateBy { it.id }
    }

    val params: Set<PARAM> by lazy {
        (requests.values.flatMap { it.params?.param ?: emptyList() } +
                posResponses.values.flatMap { it.params?.param ?: emptyList() } +
                negResponses.values.flatMap { it.params?.param ?: emptyList() } +
                globalNegResponses.values.flatMap { it.params?.param ?: emptyList() } +
                combinedDataObjectProps.values.filterIsInstance<BASICSTRUCTURE>()
                    .flatMap { it.params?.param ?: emptyList() } +
                envDatas.values.flatMap { it.params?.param ?: emptyList() }
                ).toSet()
    }

    val tableKeys: Map<String, TABLEKEY> by lazy {
        params.filterIsInstance<TABLEKEY>().associateBy { it.id }
    }

    val lengthKeys: Map<String, LENGTHKEY> by lazy {
        params.filterIsInstance<LENGTHKEY>().associateBy { it.id }
    }

    val requests: Map<String, REQUEST> by lazy {
        basevariants.values
            .flatMap { it.requests?.request ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.requests?.request ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.requests?.request ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.requests?.request ?: emptyList() }
                    .associateBy { it.id }
    }

    val responses: Set<RESPONSE> by lazy {
        (posResponses.values + negResponses.values + globalNegResponses.values).toSet()
    }

    val posResponses: Map<String, POSRESPONSE> by lazy {
        basevariants.values
            .flatMap { it.posresponses?.posresponse ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.posresponses?.posresponse ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.posresponses?.posresponse ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.posresponses?.posresponse ?: emptyList() }
                    .associateBy { it.id }
    }

    val globalNegResponses: Map<String, GLOBALNEGRESPONSE> by lazy {
        basevariants.values
            .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.globalnegresponses?.globalnegresponse ?: emptyList() }
                    .associateBy { it.id }
    }

    val negResponses: Map<String, NEGRESPONSE> by lazy {
        basevariants.values
            .flatMap { it.negresponses?.negresponse ?: emptyList() }
            .associateBy { it.id } +
                ecuvariants.values
                    .flatMap { it.negresponses?.negresponse ?: emptyList() }
                    .associateBy { it.id } +
                functionalGroups.values
                    .flatMap { it.negresponses?.negresponse ?: emptyList() }
                    .associateBy { it.id } +
                ecuSharedDatas.values
                    .flatMap { it.negresponses?.negresponse ?: emptyList() }
                    .associateBy { it.id }
    }

    val comparams: Map<String, COMPARAM> by lazy {
        comParamSubSets.values
            .flatMap { it.comparams?.comparam ?: emptyList() }
            .associateBy { it.id } +
                complexComparams.values
                    .flatMap { it.comparamOrCOMPLEXCOMPARAM ?: emptyList() }
                    .filterIsInstance<COMPARAM>()
                    .associateBy { it.id }
    }

    val complexComparams: Map<String, COMPLEXCOMPARAM> by lazy {
        comParamSubSets.values.flatMap { it.complexcomparams?.complexcomparam ?: emptyList() }
            .associateBy { it.id }
    }

    val comParamSubSets: Map<String, COMPARAMSUBSET> by lazy {
        val data = data.values.flatMap { listOf(it.comparamsubset) }.filterNotNull()
        data.associateBy { it.id }
    }

    val diagDataDictionaries: List<DIAGDATADICTIONARYSPEC> by lazy {
        basevariants.values.mapNotNull { it.diagdatadictionaryspec } +
                ecuvariants.values.mapNotNull { it.diagdatadictionaryspec } +
                functionalGroups.values.mapNotNull { it.diagdatadictionaryspec } +
                ecuSharedDatas.values.mapNotNull { it.diagdatadictionaryspec }
    }

    val diagCodedTypes: Set<DIAGCODEDTYPE> by lazy {
        val data = dataObjectProps.values.flatMap { listOf(it.diagcodedtype) } +
                params.filterIsInstance<CODEDCONST>().flatMap { listOf(it.diagcodedtype) } +
                params.filterIsInstance<NRCCONST>().flatMap { listOf(it.diagcodedtype) } +
                dtcDops.values.flatMap { listOf(it.diagcodedtype) }

        data.filterNotNull().toSet()
    }

    val combinedDataObjectProps: Map<String, DOPBASE> by lazy {
        dataObjectProps + dtcDops + structures + staticfields + endofpdufields + dynLengthFields +
                dynEndMarkerFields + muxs + envDatas + envDataDescs
    }

    val dataObjectProps: Map<String, DATAOBJECTPROP> by lazy {
        val data = diagDataDictionaries
            .flatMap { it.dataobjectprops?.dataobjectprop ?: emptyList() } +
                comParamSubSets.values
                    .flatMap { it.dataobjectprops?.dataobjectprop ?: emptyList() }

        data.associateBy { it.id }
    }

    val dtcDops: Map<String, DTCDOP> by lazy {
        diagDataDictionaries
            .flatMap { it.dtcdops?.dtcdop ?: emptyList() }
            .associateBy { it.id }
    }

    val envDatas: Map<String, ENVDATA> by lazy {
        diagDataDictionaries
            .flatMap { it.envdatas?.envdata ?: emptyList() }
            .associateBy { it.id }
    }

    val envDataDescs: Map<String, ENVDATADESC> by lazy {
        diagDataDictionaries
            .flatMap { it.envdatadescs?.envdatadesc ?: emptyList() }
            .associateBy { it.id }
    }

    val structures: Map<String, STRUCTURE> by lazy {
        diagDataDictionaries
            .flatMap { it.structures?.structure ?: emptyList() }
            .associateBy { it.id }
    }

    val tables: Map<String, TABLE> by lazy {
        diagDataDictionaries
            .flatMap { it.tables?.table ?: emptyList() }
            .associateBy { it.id }
    }

    val tableRows: Map<String, TABLEROW> by lazy {
        diagDataDictionaries
            .flatMap { it.tables?.table ?: emptyList() }
            .flatMap { it.rowwrapper }
            .map {
                if (it is TABLEROW) {
                    it
                } else {
                    error("Unexpected type: ${it::class.java}")
                }
            }.associateBy { it.id }
    }

    val endofpdufields: Map<String, ENDOFPDUFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.endofpdufields?.endofpdufield ?: emptyList() }
            .associateBy { it.id }
    }

    val staticfields: Map<String, STATICFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.staticfields?.staticfield ?: emptyList() }
            .associateBy { it.id }
    }

    val dynLengthFields: Map<String, DYNAMICLENGTHFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.dynamiclengthfields?.dynamiclengthfield ?: emptyList() }
            .associateBy { it.id }
    }

    val dynEndMarkerFields: Map<String, DYNAMICENDMARKERFIELD> by lazy {
        diagDataDictionaries
            .flatMap { it.dynamicendmarkerfields?.dynamicendmarkerfield ?: emptyList() }
            .associateBy { it.id }
    }

    val muxs: Map<String, MUX> by lazy {
        diagDataDictionaries
            .flatMap { it.muxs?.mux ?: emptyList() }
            .associateBy { it.id }

    }

    val units: Map<String, UNIT> by lazy {
        diagDataDictionaries
            .flatMap { it.unitspec?.units?.unit ?: emptyList() }
            .associateBy { it.id } +
                data.values
                    .flatMap { it.comparamsubset?.unitspec?.units?.unit ?: emptyList() }
                    .associateBy { it.id }
    }

    val sds: Set<SD> by lazy {
        sdgss.flatMap { it.sdg }.flatMap { it.sdgOrSD.filterIsInstance<SD>() }.toSet()
    }

    val sdgs: Set<SDG> by lazy {
        sdgss.flatMap { it.sdg }.toSet()
    }

    val sdgss: List<SDGS> by lazy {
        val data =
            diagDataDictionaries.flatMap { listOf(it.sdgs) } +
                    diagServices.flatMap { listOf(it.value.sdgs) } +
                    singleEcuJobs.flatMap { listOf(it.value.sdgs) } +
                    diagLayerContainer.values.flatMap { listOf(it.sdgs) } +
                    basevariants.values.flatMap { listOf(it.sdgs) } +
                    ecuvariants.values.flatMap { listOf(it.sdgs) } +
                    functionalGroups.values.flatMap { listOf(it.sdgs) } +
                    requests.values.flatMap { listOf(it.sdgs) } +
                    posResponses.values.flatMap { listOf(it.sdgs) } +
                    negResponses.values.flatMap { listOf(it.sdgs) } +
                    globalNegResponses.values.flatMap { listOf(it.sdgs) } +
                    params.flatMap { listOf(it.sdgs) } +
                    combinedDataObjectProps.values.flatMap { listOf(it.sdgs) } +
                    dtcs.values.flatMap { listOf(it.sdgs) } +
                    tables.values.flatMap { listOf(it.sdgs) } +
                    tableRows.values.flatMap { listOf(it.sdgs) }


        data.filterNotNull()
    }

    val dtcs: Map<String, DTC> by lazy {
        dtcDops.values.flatMap { it.dtcs?.dtcproxy?.filterIsInstance<DTC>() ?: emptyList() }.associateBy { it.id }
    }

    val audiences: Set<AUDIENCE> by lazy {
        val data = diagServices.values.flatMap { listOf(it.audience) } +
                singleEcuJobs.values.flatMap { listOf(it.audience) } +
                tables.values.flatMap { it.rowwrapper.filterIsInstance<TABLEROW>() }.flatMap { listOf(it.audience) }
        data.filterNotNull().toSet()
    }

    val additionalAudiences: Map<String, ADDITIONALAUDIENCE> by lazy {
        val data = basevariants.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() } +
                ecuvariants.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() } +
                functionalGroups.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() } +
                ecuSharedDatas.values.flatMap { it.additionalaudiences?.additionalaudience ?: emptyList() }

        data.associateBy { it.id }
    }

    val stateCharts: Map<String, STATECHART> by lazy {
        val data = basevariants.values.flatMap { it.statecharts?.statechart ?: emptyList() } +
                ecuvariants.values.flatMap { it.statecharts?.statechart ?: emptyList() } +
                functionalGroups.values.flatMap { it.statecharts?.statechart ?: emptyList() } +
                ecuSharedDatas.values.flatMap { it.statecharts?.statechart ?: emptyList() }
        data.associateBy { it.id }
    }

    val states: Map<String, STATE> by lazy {
        stateCharts.values.flatMap { it.states?.state ?: emptyList() }.associateBy { it.id }
    }

    val stateTransitions: Map<String, STATETRANSITION> by lazy {
        stateCharts.values.flatMap { it.statetransitions?.statetransition ?: emptyList() }
            .associateBy { it.id }
    }

    val stateTransitionsRefs: Set<STATETRANSITIONREF> by lazy {
        val data = basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<DIAGSERVICE>()
            .flatMap { it.statetransitionrefs?.statetransitionref ?: emptyList() } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .flatMap { it.statetransitionrefs?.statetransitionref ?: emptyList() } +
                tableRows.values.flatMap { it.statetransitionrefs?.statetransitionref ?: emptyList() }

        data.toSet()
    }

    val preConditionstateRefs: Set<PRECONDITIONSTATEREF> by lazy {
        val data = basevariants.values
            .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
            .filterIsInstance<DIAGSERVICE>()
            .flatMap { it.preconditionstaterefs?.preconditionstateref ?: emptyList() } +
                ecuvariants.values
                    .flatMap { it.diagcomms?.diagcommproxy ?: emptyList() }
                    .filterIsInstance<DIAGSERVICE>()
                    .flatMap { it.preconditionstaterefs?.preconditionstateref ?: emptyList() } +
                tableRows.values.flatMap { it.preconditionstaterefs?.preconditionstateref ?: emptyList() }

        data.toSet()
    }

    val unitSpecs: Set<UNITSPEC> by lazy {
        val data = comParamSubSets.values.flatMap { listOf(it.unitspec) } +
                diagDataDictionaries.flatMap { listOf(it.unitspec) }

        data.filterNotNull().toSet()
    }

    val protocols: Map<String, PROTOCOL> by lazy {
        diagLayerContainer.values.flatMap { it.protocols?.protocol ?: emptyList() }
            .associateBy { it.id }
    }

    val comparamSpecs: Map<String, COMPARAMSPEC> by lazy {
        data.values.flatMap { listOf(it.comparamspec) }
            .filterNotNull()
            .associateBy { it.id }
    }

    val physDimensions: Map<String, PHYSICALDIMENSION> by lazy {
        unitSpecs.flatMap { it.physicaldimensions?.physicaldimension ?: emptyList() }
            .associateBy { it.id }
    }

    val protStacks: Map<String, PROTSTACK> by lazy {
        comparamSpecs.values
            .flatMap { it.protstacks?.protstack ?: emptyList() }
            .associateBy { it.id }
    }

    val libraries: Map<String, LIBRARY> by lazy {
        val data = basevariants.values.flatMap { it.librarys?.library ?: emptyList() } +
                ecuvariants.values.flatMap { it.librarys?.library ?: emptyList() } +
                functionalGroups.values.flatMap { it.librarys?.library ?: emptyList() }

        data.associateBy { it.id }
    }
}

private val context: JAXBContext = org.eclipse.persistence.jaxb.JAXBContextFactory
    .createContext(arrayOf(ODX::class.java), null)

fun convert(inputFile: File, outputFile: File, logger: Logger, options: ConverterOptions) {
    logger.info("Converting ${inputFile.name} to mdd")
    val unmarshaller = context.createUnmarshaller()

    val odxData = mutableMapOf<String, ODX>()

    val inputFileData = mutableMapOf<String, ByteArray>()

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
            val odx = unmarshaller.unmarshal(
                XMLInputFactory.newFactory().createXMLEventReader(ByteArrayInputStream(entry.value)),
                ODX::class.java
            ).value
            odxData[entry.key] = odx
        }
    }
    logger.fine("Reading and parsing into objects took $readParseFileDuration")

    val col = ODXCollection(odxData)

    var sizeUncompressed: Long

    var compressionDuration: Duration
    val writingDuration = measureTime {
        val dw = DatabaseWriter(logger = logger, odx = col, options = options)

        val ecuData = dw.createEcuData()

        val mddFile = MDDFile.newBuilder()
        mddFile.version = "2025-05-21"
        mddFile.ecuName = ecuData.ecuName
        mddFile.revision = ecuData.revision

        mddFile.putMetadata("created", Instant.now().toString())
        mddFile.putMetadata("source", inputFile.name)
        mddFile.putMetadata("options", Json.Default.encodeToString(options))
        // additional metadata?

        compressionDuration = measureTime {
            val chunk = createEcuDataChunk(ecuData)
            mddFile.addChunks(chunk)
            sizeUncompressed = chunk.uncompressedSize
        }
        mddFile.addAllChunks(createJobsChunks(logger, inputFileData, ecuData, options))
        mddFile.addAllChunks(createPartialChunks(logger, inputFileData, ecuData, options))

        val mddFileOut = mddFile.build()
        BufferedOutputStream(outputFile.outputStream()).use {
            it.write(FILE_MAGIC)
            mddFileOut.writeTo(it)
        }
    }


    val sizeCompressed = outputFile.toPath().fileSize()
    logger.info("Writing database took $writingDuration total (compression: $compressionDuration) - size uncompressed: ${sizeUncompressed.format()} bytes, compressed: ${sizeCompressed.format()} bytes - ratio: ${(sizeUncompressed.toFloat() / sizeCompressed).format()} ")
}

fun createJobsChunks(
    logger: Logger,
    inputData: Map<String, ByteArray>,
    ecuData: EcuData,
    options: ConverterOptions
): List<Chunk> {
    if (!options.includeJobFiles) {
        return emptyList()
    }
    val jobFiles = ecuData.singleEcuJobsList.flatMap { it.progCodesList }.map { it.codeFile }
    val libraries = ecuData.librariesList.map { it.codeFile }
    val files = (jobFiles + libraries).toSet()
    return files.mapNotNull { fileName ->
        val data = inputData[fileName]
        if (data == null) {
            throw IllegalStateException("File $fileName is not included in PDX")
        } else {
            logger.info("Including $fileName (${data.size} bytes)")
            Chunk.newBuilder()
                .setName(fileName)
                .setType(Chunk.DataType.JAR_FILE)
                .setUncompressedSize(data.size.toLong())
                .setData(ByteString.copyFrom(data))
                .build()
        }
    }
}

fun createPartialChunks(
    logger: Logger,
    inputData: Map<String, ByteArray>,
    ecuData: EcuData,
    options: ConverterOptions
): List<Chunk> {
    if (options.partialJobFiles.isEmpty()) {
        return emptyList()
    }
    val jobFiles = ecuData.singleEcuJobsList.flatMap { it.progCodesList }.map { it.codeFile }
    val libraries = ecuData.librariesList.map { it.codeFile }
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

                        Chunk.newBuilder()
                            .setName("$jobFileName::$filename")
                            .setType(Chunk.DataType.JAR_FILE_PARTIAL)
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

fun createEcuDataChunk(ecuData: EcuData): Chunk =
    ByteString.newOutput().use { out ->
        var sizeUncompressed: Long
        LZMACompressorOutputStream(out).use { outputStream ->
            val countingOutputStream = CountingOutputStream(outputStream)
            ecuData.writeTo(countingOutputStream)
            sizeUncompressed = countingOutputStream.byteCount
        }
        Chunk.newBuilder()
            .setType(Chunk.DataType.DIAGNOSTIC_DESCRIPTION)
            .setCompressionAlgorithm("lzma")
            .setUncompressedSize(sizeUncompressed)
            .setData(out.toByteString())
            .build()
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
                                convert(inputFile, outFile, logger, options)
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
    }
}

fun Number.format(): String =
    NumberFormat.getNumberInstance().format(this)

fun main(args: Array<String>) =
    Converter().main(args)

