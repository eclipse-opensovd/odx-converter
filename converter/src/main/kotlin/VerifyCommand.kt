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
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import converter.plugin.api.VerificationPlugin
import converter.plugin.api.VerificationPluginProvider
import converter.plugin.api.VerificationResult
import org.eclipse.opensovd.cda.mdd.Signature
import java.io.File
import java.util.ServiceLoader
import java.util.logging.Logger
import kotlin.system.exitProcess

/**
 * `verify` subcommand: checks signatures present in `.mdd` files by delegating the actual
 * cryptographic verification to OEM/vendor-provided [VerificationPlugin]s discovered on the
 * classpath via [VerificationPluginProvider].
 *
 * Signatures with an algorithm no plugin supports only produce a warning. Any signature that a
 * plugin reports as [VerificationResult.INVALID] causes the command to fail (non-zero exit code).
 */
class VerifyCommand : CliktCommand(name = "verify") {
    val mddFiles: List<File> by argument(name = "mdd-files")
        .file(mustExist = true, mustBeReadable = true, canBeFile = true)
        .help("mdd files to verify")
        .multiple()

    val algorithm: String? by option("--algorithm")
        .help("Only verify signatures using this algorithm")

    private fun pluginsFor(
        plugins: List<VerificationPlugin>,
        signatureAlgorithm: String,
    ): List<VerificationPlugin> = plugins.filter { signatureAlgorithm in it.getSupportedAlgorithms() }

    override fun run() {
        val plugins = ServiceLoader.load(VerificationPluginProvider::class.java).flatMap { it.getPlugins() }
        if (plugins.isEmpty()) {
            System.err.println(
                "No verification plugins found on the classpath. Verification plugins need to be provided " +
                    "by an OEM/vendor-specific plugin jar.",
            )
        }

        var hadInvalid = false

        mddFiles.forEach { file ->
            println("Verifying '${file.name}'")
            val logger = Logger.getLogger(file.name)
            val mddFile =
                try {
                    MddFileIO.read(file)
                } catch (e: Exception) {
                    System.err.println("Error while reading '${file.name}': ${e.message}")
                    hadInvalid = true
                    return@forEach
                }
            val api = VerificationApiHandler(mddFile, logger)

            mddFile.chunksList.forEach { chunk ->
                if (chunk.signaturesCount == 0) {
                    return@forEach
                }
                val data by lazy { chunk.data.toByteArray() }
                chunk.signaturesList.forEach { signature ->
                    if (algorithm != null && signature.algorithm != algorithm) {
                        return@forEach
                    }
                    if (!verifySignature(
                            plugins,
                            signature,
                            scopeLabel = "chunk '${chunk.name}' (${chunk.type})",
                        ) { plugin -> plugin.verifyChunkSignature(api, chunk, signature, data) }
                    ) {
                        hadInvalid = true
                    }
                }
            }

            if (mddFile.hasChunksSignature()) {
                val signature = mddFile.chunksSignature
                if (algorithm == null || signature.algorithm == algorithm) {
                    if (!verifySignature(
                            plugins,
                            signature,
                            scopeLabel = "whole-file",
                        ) { plugin -> plugin.verifyFileSignature(api, signature) }
                    ) {
                        hadInvalid = true
                    }
                }
            }
        }

        if (hadInvalid) {
            exitProcess(1)
        }
    }

    /** Returns `false` if any plugin reported [VerificationResult.INVALID]. */
    private fun verifySignature(
        plugins: List<VerificationPlugin>,
        signature: Signature,
        scopeLabel: String,
        verify: (VerificationPlugin) -> VerificationResult,
    ): Boolean {
        val candidates = pluginsFor(plugins, signature.algorithm)
        if (candidates.isEmpty()) {
            System.err.println(
                "WARN: $scopeLabel has a signature with unsupported algorithm '${signature.algorithm}', skipping",
            )
            return true
        }
        var valid = true
        candidates.forEach { plugin ->
            when (verify(plugin)) {
                VerificationResult.VALID ->
                    println(
                        "OK: $scopeLabel signature '${signature.algorithm}' " +
                            "(plugin '${plugin.getPluginIdentifier()}') is valid",
                    )

                VerificationResult.INVALID -> {
                    System.err.println(
                        "FAIL: $scopeLabel signature '${signature.algorithm}' " +
                            "(plugin '${plugin.getPluginIdentifier()}') is INVALID",
                    )
                    valid = false
                }

                VerificationResult.UNSUPPORTED_ALGORITHM ->
                    System.err.println(
                        "WARN: plugin '${plugin.getPluginIdentifier()}' reported unsupported algorithm " +
                            "'${signature.algorithm}' for $scopeLabel",
                    )
            }
        }
        return valid
    }
}
