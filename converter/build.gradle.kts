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
    id("com.github.bjornvester.xjc") version libs.versions.xjc
    application
    id("com.gradleup.shadow") version libs.versions.shadow
    id("com.google.protobuf") version libs.versions.protobuf
    kotlin("plugin.serialization") version libs.versions.kt.plugins.serialization
}

val odxSchema = file("$projectDir/src/main/resources/schema/odx_2_2_0.xsd")

dependencies {
    implementation(project(":database"))
    implementation(libs.jakarta.xml.bind.api)
    implementation(libs.eclipse.persistence.moxy)
    implementation(libs.jaxb2.basics)
    implementation(libs.jaxb.api)
    implementation(libs.jaxb.impl)
    implementation(libs.apache.compress)
    implementation(libs.tukaani.xz)
    implementation(libs.clikt)
    implementation(libs.protobuf.java)
    implementation(libs.kotlinx.serialization.json)

    if (!odxSchema.exists()) {
        // You need to provide your own schema as src/main/resources/schema/odx_2_2_0.xsd
        //
	// Alternatively it might be possible to provide the class files
        // taken from a different project like ODX-Commander, move them into
        // the schema.odx package, and provide them as a library,
        // including them with a statement like
        // implementation(file("lib/odx-schema-2.2.0.jar"))
        error("Schema is missing, aborting build")
    }

    xjcPlugins(libs.jaxb2.basics)

    xjcPlugins(libs.jaxb.core)
    xjcPlugins(libs.jaxb.api)
    xjcPlugins(libs.jaxb.impl)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

xjc {
    xsdDir.set(file("src/main/resources/schema"))
    defaultPackage.set("schema.odx")
    useJakarta.set(true)
    options.add("-Xequals")
    options.add("-XhashCode")
    options.add("-XtoString")
    addCompilationDependencies.set(true)
}

tasks {
    application {
        mainClass.set("ConverterKt")
    }
}

tasks.shadowJar {
    exclude("**/schema/NOTICE.txt")
    exclude("**/odx*.xsd*")
}