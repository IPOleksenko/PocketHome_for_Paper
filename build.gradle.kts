plugins {
    id("java")
}

group = "dev.ipoleksenko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.properties["paper_version"]}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    processResources {
        val props = linkedMapOf("version" to version, "api_version" to project.properties["paper_api_version"])
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
