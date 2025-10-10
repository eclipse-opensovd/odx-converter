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

plugins {
    kotlin("jvm")
    application
    id("com.gradleup.shadow") version libs.versions.shadow
    id("com.google.protobuf") version libs.versions.protobuf
    kotlin("plugin.serialization") version libs.versions.kt.plugins.serialization
}

dependencies {
    implementation(project(":database"))
    implementation(libs.apache.compress)
    implementation(libs.tukaani.xz)
    implementation(libs.clikt)
    implementation(libs.protobuf.java)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    application {
        mainClass.set("ViewerKt")
    }
}
