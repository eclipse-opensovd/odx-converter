/*
 * SPDX-FileCopyrightText: 2025 The Eclipse OpenSOVD contributors
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.gradle.kotlin.dsl.version

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.1.21")
            version("kt-plugins-serialization", "2.1.21")
            version("protobuf", "0.9.4")
            version("shadow", "9.0.0-rc1")
            version("xjc", "1.8.2")

            library("kotlinx-serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

            library("protobuf-protoc", "com.google.protobuf:protoc:4.31.0")

            library("protobuf-java", "com.google.protobuf:protobuf-java:4.31.0")
            library("grpc-stub", "io.grpc:grpc-stub:1.72.0")
            library("grpc-protobuf", "io.grpc:grpc-protobuf:1.72.0")
            library("javax-annotation-api", "javax.annotation:javax.annotation-api:1.3.2")
            library("tukaani-xz", "org.tukaani:xz:1.10")
            library("clikt", "com.github.ajalt.clikt:clikt:5.0.3")
            library("apache-compress", "org.apache.commons:commons-compress:1.27.1")

            library("jakarta-xml-bind-api", "jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
            library("eclipse-persistence-moxy", "org.eclipse.persistence:org.eclipse.persistence.moxy:4.0.6")

            library("jaxb-api", "javax.xml.bind:jaxb-api:2.3.1")
            library("jaxb-impl", "com.sun.xml.bind:jaxb-impl:2.3.1")
            library("jaxb-core", "com.sun.xml.bind:jaxb-core:2.3.0.1")
            library("jaxb2-basics", "org.jvnet.jaxb2_commons:jaxb2-basics:1.11.1")
        }
    }
}

rootProject.name = "odx_converter"
include("database")
include("converter")
include("converter-plugin-api")
include("converter-plugins-default")

if (File(rootProject.projectDir, "viewer").exists()) {
    include("viewer")
}
