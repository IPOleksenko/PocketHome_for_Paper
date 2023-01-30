plugins {
    id("java")
}

group = project.properties["maven_group"].toString()
version = project.properties["plugin_version"].toString()+getVersionMetadata()

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

fun getVersionMetadata(): String {
    val buildId = System.getenv("GITHUB_RUN_NUMBER")

    if (buildId != null)
        return "+build.${buildId}"
    return ""
}