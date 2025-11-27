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

import com.github.jk1.license.filter.SpdxLicenseBundleNormalizer
import com.github.jk1.license.render.InventoryMarkdownReportRenderer
import com.github.jk1.license.render.JsonReportRenderer
import com.github.jk1.license.render.ReportRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version libs.versions.kotlin
    id("com.github.jk1.dependency-license-report") version "2.9"
}

group = "org.eclipse.opensovd.cda.mdd"
version = "0.1.0-SNAPSHOT"

if (System.getenv("GITHUB_REF")?.contains("refs/tags/") == true) {
    // When we build a tag from the pipeline, override the version with the tag name
    version = System.getenv("GITHUB_REF_NAME")
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    toolchain {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

licenseReport {
    configurations = arrayOf("runtimeClasspath")
    allowedLicensesFile = file("config/allowed-licenses.json")
    filters = arrayOf(SpdxLicenseBundleNormalizer())
    renderers = arrayOf<ReportRenderer>(InventoryMarkdownReportRenderer(), JsonReportRenderer())
}
