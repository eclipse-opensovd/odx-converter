# Vendor Integration

The ODX-to-MDD converter supports a plugin system based on the Java ServiceLoader SPI. You can create your own plugins to extend the conversion pipeline — for example, to add custom signing, encryption, or post-processing of chunks.

This guide describes how to set up an external project that wraps the converter and adds custom plugins.

## Overview

The recommended approach is:

1. Create a new Gradle project that includes the converter as a **git submodule**.
2. Place your proprietary ODX schema files in your project and copy them into the submodule at build time.
3. Implement one or more plugins against the `converter-plugin-api`.
4. Build a single fat JAR that merges both the upstream converter and your plugins.

## Project Structure

```
my-custom-converter/
├── build.gradle.kts                # Root build — assembles the final fat JAR
├── settings.gradle.kts             # Schema copy logic + subproject includes
├── gradle/libs.versions.toml       # Version catalog
├── .gitmodules                     # Points to the odx-converter repo
├── odx-converter/                  # Git submodule (this repository)
├── schema/                         # Your ODX schema files (odx_2_2_0.xsd, odx-xhtml.xsd)
├── src/main/kotlin/
│   └── CustomConverter.kt          # (Optional) Custom entry point
└── my-plugin/                      # Your plugin subproject
    ├── build.gradle.kts
    └── src/
        ├── main/kotlin/my/plugin/
        │   ├── MyPlugin.kt
        │   └── MyPluginProvider.kt
        └── main/resources/
            └── META-INF/services/
                └── converter.plugin.api.ConverterPluginProvider
```

## Step-by-step Setup

### 1. Add the converter as a git submodule

```shell
git submodule add https://github.com/eclipse-opensovd/odx-converter.git odx-converter
```

### 2. Place your schema files

Copy `odx_2_2_0.xsd` and `odx-xhtml.xsd` into a `schema/` directory at the root of your project. These will be copied into the submodule before the converter builds.

### 3. Configure `settings.gradle.kts`

The key trick is to copy your schema files into the submodule's expected location **at settings-evaluation time**, before any build script runs. This is necessary because the converter's build checks for the schema at configuration time.

```kotlin
rootProject.name = "my-custom-converter"

// Copy schema files into the submodule before it configures
val schemaSource = file("schema")
val schemaTarget = file("odx-converter/converter/src/main/resources/schema")
if (schemaSource.exists()) {
    schemaSource.listFiles()?.filter { it.extension == "xsd" }?.forEach {
        it.copyTo(File(schemaTarget, it.name), overwrite = true)
    }
}

// Include the submodule's subprojects
includeBuild("odx-converter")

// Include your plugin subproject(s)
include("my-plugin")
```

### 4. Implement a plugin

#### Plugin provider (`MyPluginProvider.kt`)

```kotlin
package my.plugin

import converter.plugin.api.ConverterPlugin
import converter.plugin.api.ConverterPluginProvider

class MyPluginProvider : ConverterPluginProvider {
    override fun getPlugins(): List<ConverterPlugin> = listOf(MyPlugin())
}
```

#### Plugin implementation (`MyPlugin.kt`)

```kotlin
package my.plugin

import converter.plugin.api.ConverterApi
import converter.plugin.api.ConverterPlugin
import converter.plugin.api.ChunkApi

class MyPlugin : ConverterPlugin {
    override fun getPluginIdentifier(): String = "my-plugin"
    override fun getPluginVersion(): String = "1.0.0"
    override fun getPluginDescription(): String = "Description of what this plugin does"
    override fun getPluginPriority(): Int = 100

    override fun beforeProcessing(api: ConverterApi) {
        // Called once before any chunks are processed
    }

    override fun processChunk(api: ConverterApi, initialData: ByteArray, chunkApi: ChunkApi) {
        // Called for each chunk. Use chunkApi to modify chunk metadata,
        // call keepChunk() to retain it, or removeChunk() to discard it.
        
        // Note: keepChunk() is also the default. 
        // If you don't call removeChunk(), the chunk will be kept by default, it'll only
        // reset a previously called removeChunk()
        chunkApi.keepChunk()
    }

    override fun afterProcessing(api: ConverterApi) {
        // Called once after all chunks are processed
    }
}
```

#### Plugin priority

Plugins run in ascending priority order (lower value = runs first). The built-in compression plugin uses priority `50`. Set your priority accordingly — for example, a signing plugin that needs compressed data should use a higher value like `100` or `1000`.

#### SPI registration

Create the file `my-plugin/src/main/resources/META-INF/services/converter.plugin.api.ConverterPluginProvider` with the fully qualified class name of your provider:

```
my.plugin.MyPluginProvider
```

### 5. Configure the plugin's `build.gradle.kts`

Your plugin needs to compile against the `converter-plugin-api` and `database` modules from the submodule:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Build the submodule's plugin-api and database JARs first
val buildConverterApi by tasks.registering(Exec::class) {
    workingDir = file("../odx-converter")
    commandLine("./gradlew", ":converter-plugin-api:jar", ":database:jar")
}

dependencies {
    compileOnly(files("../odx-converter/converter-plugin-api/build/libs").asFileTree)
    compileOnly(files("../odx-converter/database/build/libs").asFileTree)

    // Add any additional dependencies your plugin needs
}

tasks.compileKotlin {
    dependsOn(buildConverterApi)
}
```

### 6. Configure the root `build.gradle.kts`

The root project assembles everything into a single fat JAR:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
}

// Build the converter's shadow JAR
val buildConverter by tasks.registering(Exec::class) {
    workingDir = file("odx-converter")
    commandLine("./gradlew", ":converter:shadowJar")
}

dependencies {
    // The upstream converter fat JAR
    implementation(files("odx-converter/converter/build/libs").asFileTree.matching {
        include("*-all.jar")
    })

    // Your plugin(s)
    implementation(project(":my-plugin"))
}

tasks.compileKotlin {
    dependsOn(buildConverter)
}

tasks.shadowJar {
    // Customize the output JAR name
    archiveBaseName.set("my-custom-converter")
    archiveClassifier.set("")

    // Merge SPI service files so both default and custom plugins are discovered
    mergeServiceFiles()

    manifest {
        attributes(
            // Set your custom main class, or use the upstream entry point ("ConverterKt")
            "Main-Class" to "ConverterKt",

            // Custom naming and versioning — the converter reads these at runtime
            // to display its name and version in CLI output and logs
            "Implementation-Title" to "my-custom-converter",
            "Implementation-Version" to project.version,
            "Implementation-Commit" to resolveCommitHash(),
            "Implementation-BuildDate" to Instant.ofEpochSecond(
                System.getenv("SOURCE_DATE_EPOCH")?.toLong() ?: Instant.now().epochSecond
            ).toString(),
        )
    }
}

// Helper to resolve the current git commit hash for the manifest
fun resolveCommitHash(): String =
    providers.exec { commandLine("git", "rev-parse", "--short", "HEAD") }
        .standardOutput.asText.get().trim()
```

### 7. (Optional) Custom entry point

If you want to override default CLI arguments or add custom logic, create a custom main class:

```kotlin
fun main(args: Array<String>) {
    val customArgs = listOf("--lenient") + args.toList()

    // Reflectively call the upstream main
    val converterClass = Class.forName("ConverterKt")
    val mainMethod = converterClass.getMethod("main", Array<String>::class.java)
    mainMethod.invoke(null, customArgs.toTypedArray())
}
```

Update the `Main-Class` in your shadow JAR manifest accordingly.

### 8. Build and run

```shell
./gradlew clean build shadowJar
java -jar build/libs/my-custom-converter-all.jar input.pdx
```

## Custom Naming and Versioning

The converter reads JAR manifest attributes at runtime to display its identity in CLI output and logs. By setting these in your shadow JAR manifest, you can brand the output as your own distribution:

| Manifest Attribute | Purpose | Default (if missing) |
|--------------------|---------|----------------------|
| `Implementation-Title` | Application name shown in CLI output | `odx-converter` |
| `Implementation-Version` | Version string shown in CLI output | `development` |
| `Implementation-BuildDate` | Build timestamp | `unknown` |
| `Implementation-Commit` | Git commit hash | `unknown` |

Setting `project.version` in your root `build.gradle.kts` (or `gradle.properties`) controls the value of `Implementation-Version`. The `SOURCE_DATE_EPOCH` environment variable can be used for reproducible builds — if set, it is used as the build timestamp instead of the current time.

## Plugin API Reference

| Interface | Purpose |
|-----------|---------|
| `ConverterPluginProvider` | Entry point discovered by ServiceLoader; returns a list of `ConverterPlugin` instances |
| `ConverterPlugin` | Defines the plugin lifecycle: `beforeProcessing`, `processChunk`, `afterProcessing` |
| `ConverterApi` | Provides access to the `MDDFile.Builder`, a `Logger`, and `addChunk()` |
| `ChunkApi` | Provides access to the `Chunk.Builder` for the current chunk, plus `keepChunk()`/`removeChunk()` |

All interfaces are in the `converter.plugin.api` package, provided by the `converter-plugin-api` module.
