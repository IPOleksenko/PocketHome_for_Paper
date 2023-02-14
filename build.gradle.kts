import java.io.ByteArrayOutputStream

plugins {
	id("java")
}

group = "dev.ipoleksenko"
version = "git --no-pager describe --always".runCommand()

repositories {
	mavenCentral()
	maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
	annotationProcessor("io.papermc.paper:paper-api:${project.properties["paper_version"]}")
}

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
	withSourcesJar()
}

tasks {
	processResources {
		val props = linkedMapOf("version" to version, "api_version" to project.properties["paper_api_version"])
		inputs.properties(props)
		filesMatching("plugin.yml") {
			expand(props)
		}
		version = getVersionMetadata()
	}
}

fun getVersionMetadata(): String {
	val buildId = System.getenv("GITHUB_RUN_NUMBER")

	if (buildId != null)
		return version.toString() + "+build.${buildId}"
	return version.toString()
}

fun String.runCommand(currentWorkingDir: File = file("./")): String {
	val byteOut = ByteArrayOutputStream()
	project.exec {
		workingDir = currentWorkingDir
		commandLine = this@runCommand.split("\\s".toRegex())
		standardOutput = byteOut
	}
	return String(byteOut.toByteArray()).trim()
}
