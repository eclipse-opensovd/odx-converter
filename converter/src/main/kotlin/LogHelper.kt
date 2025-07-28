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
import java.util.logging.*

fun Logger.severe(msg: String, e: Throwable? = null) =
    this.log(Level.SEVERE, msg, e)


class WriteToFileHandler(level: Level, file: File): StreamHandler(FileOutputStream(file), FileFormatter()), AutoCloseable {
    init {
        setLevel(level)
    }
}

class FileFormatter : Formatter() {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'")
    override fun format(record: LogRecord): String {
        val dateTime = ZonedDateTime.now(UTC).format(formatter)
        return "[${dateTime}] [${record.level.name.padEnd(7)}] ${formatMessage(record)}\n"
    }
}