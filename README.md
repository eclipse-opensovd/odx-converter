# 🚗 ODX to MDD Converter 🚀

## Introduction 🌟

This is the repository of the ODX to MDD Converter! This tool transforms packed ODX files (.pdx) into a custom format called "Marvelous Diagnostic Description" (MDD).

[OpenSOVDs Classic Diagnostic Adapter (CDA)](https://github.com/eclipse-opensovd/classic-diagnostic-adapter) solution is designed for embedded systems, and needs manageable file sizes with simple processing. Enter the MDD format, developed to tackle these challenges.

PDX files are essentially compressed archives of multiple ODX files, which follow the ISO-22091 standard in the automotive industry for exchanging diagnostic descriptions of electronic control units (ECUs). These XML-based files can be quite large, often reaching hundreds of megabytes, making their use impractical due to their size and processing complexity. This is also why many diagnostic testers opt for proprietary formats.

Compression sizes vary, but here are some typical values:

| ODX   | PDX   | MDD  |
|-------|-------|------|
| 66MB  | 3.6MB | 300k |
| 154MB | 7.5MB | 653k |
| 42MB  | 2.7MB | 202k |

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

The MDD format is defined using two protobuf files, ensuring compatibility across various programming languages.

- **Container Format**: Defined in [file_format.proto](database/src/main/proto/file_format.proto), it includes metadata like versioning and a collection of chunks. Each chunk is a byte stream with chunk-specific metadata, including optional encryption, signatures, compression algorithms, and vendor-specific metadata in a key-value map.
- **Diagnostic Description Format**: Derived from the ODX-schema, and defined in [data_format.proto](database/src/main/proto/diagnostic_description.proto), it efficiently represents the ODX structures in a binary format, with simplifications and minor omissions to minimize size and maximize usability.

### Limitations/Changes Compared to ODX 🚧

- Data types (e.g., END-OF-PDU, LEADING-LENGTH-FIELD, STRUCTURE, MUX, DTC, etc.) are combined into a single message with a type and fields for the different data types, and composition is used instead of inheritance.
- No support for cross-file references outside the pdx and runtime resolution
- Most data is referenced via integer identifiers, the original text identifiers are discarded

### Language 💻

The converter is built in Kotlin, chosen for its mature XML tooling through the Java ecosystem. Kotlin's features like garbage collection, object generation through XML schema, extension functions and extended streams api enabled efficient development. Plus, it's a favorite of the author!

### Contributors ✨
See [CONTRIBUTORS](CONTRIBUTORS)