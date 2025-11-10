# 🚗 ODX to MDD Converter 🚀

## Introduction 🌟

This is the repository of the ODX to MDD Converter! This tool transforms packed ODX files (.pdx) into a custom format called "Marvelous Diagnostic Description" (MDD).

[OpenSOVDs Classic Diagnostic Adapter (CDA)](https://github.com/eclipse-opensovd/classic-diagnostic-adapter) solution is designed for embedded systems, and needs manageable file sizes with simple processing. Enter the MDD format, developed to tackle these challenges.

PDX files are essentially compressed archives of multiple ODX files, which follow the ISO-22091 standard in the automotive industry for exchanging diagnostic descriptions of electronic control units (ECUs). These XML-based files can be quite large, often reaching hundreds of megabytes, making their use impractical due to their size and processing complexity. This is also why many diagnostic testers opt for proprietary formats.

Compression sizes vary, but here are some typical values:

| Raw ODX | PDX    | MDD    |
|---------|--------|--------|
| 5.7 MB  | 1.4 MB | 111 kB |
| 41 MB   | 2.7 MB | 470 kB |
| 132 MB  | 5 MB   | 1.5 MB |

## Converter 🛠️

### Notes 
- __This software is in early development, the output format may change in incompatible ways, until the first release (1.0.0)__
- __Due to copyright, we can't provide the required odx-schema at the moment. You'll have to provide it yourself.__   
 Please read the schema [NOTICE](converter/src/main/resources/schema/NOTICE.md).

### Usage 📜


#### After building from source

```shell
java -jar converter/build/libs/converter-all.jar --help
```

Output:
```
Usage: converter [<options>] [<pdx-files>]...

Options:
  -O, --output-directory=<path>  output directory for files (default: same as pdx-file)
  -L, --lenient
  --include-job-files            Include job files & libraries referenced in single ecu jobs
  --partial-job-files=<text>...  Include job files partially, and spread the contents as individual chunks. Argument can be repeated, and are in the format: <regex for job-file-name pattern> <regex for content file-name pattern>.
  -h, --help                     Show this message and exit

Arguments:
  <pdx-files>  pdx files to convert
```

### Building 🏗️

**Prerequisites**:
- Installed JDK 21 — we recommend [Eclipse Temurin Java JDK 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any)

Provide ODX schema:
Place the files odx_2_2_0.xsd and odx-xhtml.xsd in converter/src/main/resources/schema/

Execute Gradle:
```shell
./gradlew clean build shadowJar
```
This will create `converter/build/libs/converter-all.jar`, which can be run using the `java` executable.

Example:
```shell
java -jar converter/build/libs/converter-all.jar ECU.pdx GATEWAY.pdx 
```

This will convert the given pdx files into mdd. 

# development

## File Format 📂

The MDD format itself is a container format defined using a [protobuf file](database/src/main/proto/file_format.proto), 
ensuring compatibility across various programming languages. It includes metadata like versioning and a 
collection of chunks. Each chunk is a byte stream with chunk-specific metadata, including optional encryption, 
signatures, compression algorithms, and vendor-specific metadata in a key-value map.

For the diagnostic description, a compressed chunk within that container format is used, whose contents are defined using a
[flatbuffers schema](database/src/main/fbs/diagnostic_description.fbs), to reduce the memory footprint and access times
at runtime.

## flatbuffers

To regenerate the flatbuffers schema, the flatbuffers compiler in the version [25.9.23](https://github.com/google/flatbuffers/releases/tag/v25.9.23) is required. Please install it according to the instructions in
the flatbuffers documentation, then you can run the gradle `generateFbs` task.

Please note, that changing the flatbuffers version will make resulting
mdd files incompatible with other flatbuffers versions, which will 
cause issues when it isn't updated in the CDA as well.

### Limitations/Changes Compared to ODX 🚧

- Data types (e.g., END-OF-PDU, LEADING-LENGTH-FIELD, STRUCTURE, MUX, DTC, etc.) are combined into a single message with 
  a type and fields for the different data types, and composition is used instead of inheritance.
- No support for cross-file references outside the pdx and runtime resolution

### Language 💻

The converter is built in Kotlin, chosen for its mature XML tooling through the Java ecosystem. Kotlin's features like garbage collection, object generation through XML schema, extension functions and extended streams api enabled efficient development. Plus, it's a favorite of the author!

### Contributors ✨
See [CONTRIBUTORS](CONTRIBUTORS)
