plugins {
    java
    id("com.gradleup.shadow") version "9.2.2"
}

group = "de.bluecolored.bluemap.entities"
version = "1.1"

repositories {
    mavenCentral()
    maven ( "https://repo.bluecolored.de/releases" )
}

dependencies {
    compileOnly ( "de.bluecolored:bluemap-core:5.13" )
    compileOnly ( "org.projectlombok:lombok:1.18.32" )
    annotationProcessor ( "org.projectlombok:lombok:1.18.32" )
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "utf-8"
}

tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}
