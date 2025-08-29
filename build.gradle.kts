import org.gradle.kotlin.dsl.accessors.runtime.extensionOf

plugins {
    id("application")
}

group = "klaxon.klaxon"
version = "0.0.0"

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

    implementation("org.jfree:jfreechart:1.5.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "klaxon.klaxon.elmo.strontio.Strontio"
    // applicationDefaultJvmArgs += "--add-exports=java.desktop/sun.awt.image=ALL-UNNAMED"
}

tasks.test {
    useJUnitPlatform()
}