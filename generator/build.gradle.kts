plugins {
    java
    id("net.fabricmc.fabric-loom") version "1.17.19"
}

val mcVersion = (findProperty("mcVersion") as String?) ?: "26.3"
val loaderVersion = (findProperty("loaderVersion") as String?) ?: "0.19.3"
val serverDir = layout.projectDirectory.dir("run")

val fabricApiVersion = "0.160.6+26.3"
val bluemapVersion = "5.27-fabric"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

loom {
    runs {
        named("server") {
            runDirectory = serverDir
            jvmArguments.add("-Xmx2G")
        }
        named("client") {
            runDirectory = layout.projectDirectory.dir("run-client")
        }
    }
}

repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    implementation("net.fabricmc:fabric-loader:$loaderVersion")

    localRuntime("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    localRuntime("maven.modrinth:bluemap:$bluemapVersion")
}

tasks.compileJava {
    options.encoding = "utf-8"
}

tasks.register<JavaExec>("generateEntityModels") {
    group = "bluemap"
    description = "Generates bluemap entity-models from the vanilla minecraft client-jar"
    dependsOn(tasks.compileJava)

    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "de.bluecolored.bluemap.entities.generator.Main"

    // -PoutDir=<path> writes the models somewhere else (dry-run), -Players=<regex> generates a subset only
    val outDir = findProperty("outDir") as String?
    val out = outDir ?: rootProject.file("src/main/resources/assets/minecraft/models/entity").absolutePath
    val index = if (outDir != null) layout.buildDirectory.file("generated-index.txt").get().asFile.absolutePath
    else file("generated-index.txt").absolutePath
    val clientJar = loom.namedMinecraftJars

    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "--client", clientJar.singleFile.absolutePath,
            "--mc-version", mcVersion,
            "--out", out,
            "--index", index,
            "--config", file("config").absolutePath,
            "--overrides", file("overrides").absolutePath,
            "--report", layout.buildDirectory.file("generation-report.txt").get().asFile.absolutePath
        ) + (findProperty("layers") as String?)?.let { listOf("--include", it) }.orEmpty()
    })
}

val installAddon = tasks.register<Copy>("installAddon") {
    group = "bluemap"
    description = "Copies the built addon into the packs-folder of the test-server"
    from(rootProject.tasks.named<Jar>("jar"))
    into(serverDir.dir("config/bluemap/packs"))
}

val installTestDatapack = tasks.register<Sync>("installTestDatapack") {
    group = "bluemap"
    description = "Copies the test-datapack into the world of the test-server"
    from(layout.projectDirectory.dir("datapack/bluemap-entity-test"))
    into(serverDir.dir("world/datapacks/bluemap-entity-test"))
}

tasks.named("runServer") {
    group = "bluemap"
    dependsOn(installAddon, installTestDatapack)
}
