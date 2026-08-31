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
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import converter.plugin.api.SigningPlugin
import converter.plugin.api.SigningPluginProvider
import java.io.File
import java.util.ServiceLoader
import java.util.logging.Logger
import kotlin.system.exitProcess

/**
 * `sign` subcommand: adds signatures to already-converted `.mdd` files, in-place, by delegating
 * the actual cryptographic operation to OEM/vendor-provided [SigningPlugin]s discovered on the
 * classpath via [SigningPluginProvider].
 */
class SignCommand : CliktCommand(name = "sign") {
    val mddFiles: List<File> by argument(name = "mdd-files")
        .file(mustExist = true, mustBeReadable = true, mustBeWritable = true, canBeFile = true)
        .help("mdd files to sign (modified in-place)")
        .multiple()

    val scope: String by option("--scope")
        .help("Whether to sign each chunk individually, the whole file, or both (default: both)")
        .choice("chunk", "file", "both")
        .default("both")

    val algorithm: String? by option("--algorithm")
        .help("Only use signing plugins that support this algorithm (also used to disambiguate whole-file signing)")

    val pluginOptions: List<Pair<String, String>> by option("--plugin-option")
        .help(
            "Plugin-specific option, in the format: <key> <value>. Can be repeated. Passed through " +
                "unchanged to every signing plugin invoked.",
        ).pair()
        .multiple()

    private fun retrievePlugins(): List<SigningPlugin> =
        ServiceLoader
            .load(SigningPluginProvider::class.java)
            .flatMap { it.getPlugins() }
            .filter { plugin -> algorithm == null || algorithm in plugin.getSupportedAlgorithms() }

    override fun run() {
        val plugins = retrievePlugins()
        if (plugins.isEmpty()) {
            System.err.println(
                "No signing plugins found on the classpath" +
                    (algorithm?.let { " supporting algorithm '$it'" } ?: "") +
                    ". Signing plugins need to be provided by an OEM/vendor-specific plugin jar.",
            )
            exitProcess(1)
        }

        val options = pluginOptions.toMap()
        var hadErrors = false

        mddFiles.forEach { file ->
            try {
                val mddFile = MddFileIO.read(file)
                val builder = mddFile.toBuilder()
                val logger = Logger.getLogger(file.name)

                val errors =
                    SigningExecutor.apply(
                        builder,
                        plugins,
                        scope,
                        options,
                        logger,
                        onChunkSigned = { chunk, plugin, count ->
                            println(
                                "Added $count signature(s) to chunk '${chunk.name}' (${chunk.type}) " +
                                    "in '${file.name}' via plugin '${plugin.getPluginIdentifier()}'",
                            )
                        },
                        onFileSigned = { signature ->
                            println("Set whole-file signature for '${file.name}' (algorithm: ${signature.algorithm})")
                        },
                    )
                if (errors.isNotEmpty()) {
                    errors.forEach { System.err.println("Error while signing '${file.name}': $it") }
                    hadErrors = true
                    return@forEach
                }

                MddFileIO.write(file, builder.build())
                println("Signed '${file.name}'")
            } catch (e: Exception) {
                hadErrors = true
                System.err.println("Error while signing '${file.name}': ${e.message}")
            }
        }

        if (hadErrors) {
            exitProcess(1)
        }
    }
}
