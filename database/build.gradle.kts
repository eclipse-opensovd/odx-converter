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

import com.google.protobuf.gradle.GenerateProtoTask

plugins {
    kotlin("jvm")
    id("com.google.protobuf") version libs.versions.protobuf
    publishing
}

dependencies {
    api(libs.protobuf.java)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.javax.annotation.api)

    implementation(libs.apache.compress)
    implementation(libs.tukaani.xz)
    api(files("lib/flatbuffers-java-25.9.23.jar", "lib/flatbuffers-kotlin-jvm-2.0.0-SNAPSHOT.jar"))

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

protobuf {
    // Configure the protoc executable
    protoc {
        // Download from repositories
        artifact =
            libs.protobuf.protoc
                .get()
                .toString()
    }
}

tasks.withType<GenerateProtoTask>().configureEach {
}

tasks.register<Exec>("generateFbs") {
    val cmd =
        listOf(
            "flatc",
            "--kotlin",
            "-o",
            "${projectDir.absolutePath}/src/main/kotlin",
            "${projectDir.absolutePath}/src/main/fbs/diagnostic_description.fbs",
        )
    logger.info("Executing ${cmd.joinToString(" ")}")
    commandLine(cmd)
}
