plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.http4k:http4k-core:5.9.0.0")
    implementation("org.http4k:http4k-server-jetty:5.9.0.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("io.strikt:strikt-core:0.34.1")
    testImplementation("org.http4k:http4k-client-jetty:5.9.0.0")
    testImplementation("com.ubertob.pesticide:pesticide-core:1.6.6")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
