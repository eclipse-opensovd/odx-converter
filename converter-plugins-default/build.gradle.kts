plugins {
    id("kotlin")
}

dependencies {
    implementation(project(":converter-plugin-api"))

    implementation(libs.apache.compress)
    implementation(libs.tukaani.xz)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.assertk)
}

tasks.test {
    useJUnitPlatform()
}
