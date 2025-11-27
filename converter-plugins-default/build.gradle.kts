plugins {
    id("kotlin")
}

dependencies {
    implementation(project(":converter-plugin-api"))

    implementation(libs.apache.compress)
    implementation(libs.tukaani.xz)
}

tasks.test {
    useJUnitPlatform()
}
