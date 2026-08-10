import groovy.json.JsonSlurper
import java.net.URI

plugins {
    java
}

val mcVersion = (findProperty("mcVersion") as String?) ?: "26.2"
val mcDir = layout.buildDirectory.dir("minecraft/$mcVersion")

// the generator is a tool, not part of the addon
val requested = gradle.startParameter.taskNames.any { it.startsWith(":generator") || it == "generateEntityModels" }
val available = { requested || mcDir.get().file("client.jar").asFile.isFile }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

@Suppress("UNCHECKED_CAST")
val downloadMinecraft by tasks.registering {
    description = "Downloads the vanilla minecraft client-jar and its libraries for version $mcVersion"
    val target = mcDir
    outputs.dir(target)
    onlyIf { available() }
    doLast {
        val dir = target.get().asFile
        val libs = dir.resolve("libs").apply { mkdirs() }

        fun fetch(url: String, file: File, size: Long) {
            if (file.isFile && file.length() == size) return
            logger.lifecycle("downloading ${file.name}")
            URI(url).toURL().openStream().use { input -> file.outputStream().use { input.copyTo(it) } }
        }

        val manifest = JsonSlurper()
            .parse(URI("https://launchermeta.mojang.com/mc/game/version_manifest_v2.json").toURL()) as Map<String, Any>
        val entry = (manifest["versions"] as List<Map<String, Any>>)
            .firstOrNull { it["id"] == mcVersion }
            ?: throw GradleException("unknown minecraft version: $mcVersion")

        val version = JsonSlurper().parse(URI(entry["url"] as String).toURL()) as Map<String, Any>
        val client = (version["downloads"] as Map<String, Any>)["client"] as Map<String, Any>
        fetch(client["url"] as String, dir.resolve("client.jar"), (client["size"] as Number).toLong())

        val wanted = mutableSetOf("client.jar")
        (version["libraries"] as List<Map<String, Any>>).forEach { lib ->
            val name = lib["name"] as String
            if (name.contains("natives") || name.startsWith("org.lwjgl")) return@forEach
            val artifact = (lib["downloads"] as Map<String, Any>?)?.get("artifact") as Map<String, Any>? ?: return@forEach
            val file = libs.resolve(File(artifact["path"] as String).name)
            fetch(artifact["url"] as String, file, (artifact["size"] as Number).toLong())
            wanted += file.name
        }

        libs.listFiles()?.forEach { if (it.name !in wanted) it.delete() }
    }
}

val minecraftFiles = files({
    val dir = mcDir.get().asFile
    listOf(dir.resolve("client.jar")) + (dir.resolve("libs").listFiles()?.sorted() ?: emptyList())
})

dependencies {
    compileOnly(minecraftFiles)
}

tasks.compileJava {
    dependsOn(downloadMinecraft)
    onlyIf { available() }
    options.encoding = "utf-8"
}

tasks.register<JavaExec>("generateEntityModels") {
    group = "bluemap"
    description = "Generates bluemap entity-models from the vanilla minecraft client-jar"
    dependsOn(tasks.compileJava)

    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
    classpath = sourceSets.main.get().runtimeClasspath + minecraftFiles
    mainClass = "de.bluecolored.bluemap.entities.generator.Main"

    // -PoutDir=<path> writes the models somewhere else (dry-run), -Players=<regex> generates a subset only
    val outDir = findProperty("outDir") as String?
    val out = outDir ?: rootProject.file("src/main/resources/assets/minecraft/models/entity").absolutePath
    val index = if (outDir != null) layout.buildDirectory.file("generated-index.txt").get().asFile.absolutePath
    else file("generated-index.txt").absolutePath

    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "--client", mcDir.get().file("client.jar").asFile.absolutePath,
            "--mc-version", mcVersion,
            "--out", out,
            "--index", index,
            "--config", file("config").absolutePath,
            "--overrides", file("overrides").absolutePath,
            "--report", layout.buildDirectory.file("generation-report.txt").get().asFile.absolutePath
        ) + (findProperty("layers") as String?)?.let { listOf("--include", it) }.orEmpty()
    })
}
