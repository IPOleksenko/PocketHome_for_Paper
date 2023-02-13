import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

import java.io.ByteArrayOutputStream

plugins {
	`java-library`
	id("io.papermc.paperweight.userdev") version "1.5.0"
	id("xyz.jpenilla.run-paper") version "2.0.1"
	id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "dev.ipoleksenko"
version = "git --no-pager describe --always".runCommand() + getVersionMetadata()
description = "A pocket dimension plugin"

java {
	toolchain.languageVersion.set(JavaLanguageVersion.of(17))
	withSourcesJar()
}

dependencies {
	paperweight.paperDevBundle("1.19.3-R0.1-SNAPSHOT")
}


tasks {
	assemble {
		dependsOn(reobfJar)
	}

	compileJava {
		options.encoding = Charsets.UTF_8.name()
		options.release.set(17)
	}
	javadoc {
		options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
	}
	processResources {
		filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
	}
}

bukkit {
	main = "dev.ipoleksenko.PocketHome.PocketHomePlugin"
	load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
	authors = listOf("IPOleksenko", "rvbsm")
	apiVersion = "1.19"
}

fun getVersionMetadata(): String {
	val buildId = System.getenv("GITHUB_RUN_NUMBER")

	if (buildId != null) return "+build.${buildId}"
	return ""
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
