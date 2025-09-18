plugins {
    application
    antlr
}

group = "klaxon.klaxon"
version = "0.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")     // Log4j2 API
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")    // Log4j2 Core impl
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") // SLF4J to Log4j2 bridge
}

val vectorArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")

application {
    mainClass = "klaxon.klaxon.scribe.Scribe"
    applicationDefaultJvmArgs += vectorArgs
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(vectorArgs)
}