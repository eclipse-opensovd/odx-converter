plugins {
    id("kotlin")
    publishing
}

dependencies {
    api(project(":database"))
}

tasks.test {
    useJUnitPlatform()
}
