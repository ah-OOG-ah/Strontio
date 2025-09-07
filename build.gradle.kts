plugins {
    id("application")
    id("me.champeau.jmh") version "0.7.3"
}

group = "klaxon.klaxon"
version = "0.0.1"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")
    implementation("org.ejml:ejml-all:0.44.0")

    // Elmo deps
    implementation("com.formdev:flatlaf:3.6")
    implementation("com.formdev:flatlaf-extras:3.6")

    implementation("org.apache.logging.log4j:log4j-api:2.20.0")     // Log4j2 API
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")    // Log4j2 Core impl
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0") // SLF4J to Log4j2 bridge

    implementation("org.jfree:jfreechart:1.5.6")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

val vectorArgs = listOf("--enable-preview", "--add-modules", "jdk.incubator.vector")

application {
    mainClass = "klaxon.klaxon.elmo.core.Hand"
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

tasks.test {
    useJUnitPlatform()
    allJvmArgs = allJvmArgs.toMutableList() + vectorArgs
}

tasks.jmh {
    jvmArgsAppend = vectorArgs + listOf("-XX:+AdjustStackSizeForTLS")
}