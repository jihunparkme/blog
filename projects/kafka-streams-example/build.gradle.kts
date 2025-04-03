plugins {
    kotlin("jvm") version "2.1.10"
}

group = "kafka-streams"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-streams:4.0.0")
    implementation("org.rocksdb:rocksdbjni:9.10.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.google.code.gson:gson:2.12.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}