plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

group = "de.bluecolored.bluemap.entities"
version = "1.5"

repositories {
    mavenCentral()
    maven ( "https://repo.bluecolored.de/releases" )
    maven ( "https://maven.neoforged.net/releases" )
    maven ( "https://repo.papermc.io/repository/maven-public/" )
}

dependencies {
    compileOnly ( "de.bluecolored:bluemap-core:5.23" )
    compileOnly ( "org.projectlombok:lombok:1.18.46" )
    annotationProcessor ( "org.projectlombok:lombok:1.18.46" )

    // only for the @Mod annotation (neo)
    compileOnly ( "net.neoforged.fancymodloader:loader:11.0.17" ) { isTransitive = false }
    compileOnly ( "net.neoforged:mergetool:2.0.0:api" ) { isTransitive = false }

    // only to warn if the addon is loaded as a paper-plugin
    compileOnly ( "io.papermc.paper:paper-api:26.1.2.build.+" )
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "utf-8"
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml", "plugin.yml")) { expand(props) }
}

tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
