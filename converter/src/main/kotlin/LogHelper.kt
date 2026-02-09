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

import java.io.File
import java.io.FileOutputStream
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.logging.StreamHandler

fun Logger.severe(
    msg: String,
    e: Throwable? = null,
) = this.log(Level.SEVERE, msg, e)

class WriteToFileHandler(
    level: Level,
    file: File,
) : StreamHandler(FileOutputStream(file), DefaultFormatter()),
    AutoCloseable {
    init {
        setLevel(level)
    }
}

class ConsoleHandlerWithFile(
    level: Level,
    filename: String? = null,
) : StreamHandler(System.out, DefaultFormatter(filename)),
    AutoCloseable {
    init {
        setLevel(level)
    }
}

class DefaultFormatter(
    val filename: String? = null,
) : Formatter() {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'")

    override fun format(record: LogRecord): String {
        val dateTime = ZonedDateTime.now(UTC).format(formatter)
        val sb = StringBuilder("[$dateTime] [${record.level.name.padEnd(7)}] ${formatMessage(record)}")
        if (filename != null) {
            sb.insert(0, "[$filename] ")
        }
        record.thrown?.let {
            sb.append(":\n")
            sb.append(it.stackTraceToString())
        }
        sb.append("\n")
        return sb.toString()
    }
}
