
plugins {
    `java-library`
    kotlin("jvm") version "2.2.20"
    `maven-publish`
}

group = "com.bowerzlabs.kraftpulse"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":shared"))

    // Exposed core, JDBC and DAO
    implementation("org.jetbrains.exposed:exposed-core:1.6.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.6.0")
    implementation("org.jetbrains.exposed:exposed-dao:1.6.0")
    implementation("org.jetbrains.exposed:exposed-java-time:1.6.0")

// Additional Exposed modules
    implementation("org.jetbrains.exposed:exposed-crypt:1.6.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:1.6.0")
    implementation("org.jetbrains.exposed:exposed-money:1.6.0")

    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

    api("org.flywaydb:flyway-core:10.17.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "kraftpulse-sqlite"

            pom {
                name.set("KraftPulse SQLite")
                description.set(
                    "Lightweight SQLite event storage adapter for KraftPulse"
                )
            }
        }
    }

    repositories {
        // Configure your Maven repository when ready to publish.
    }
}
