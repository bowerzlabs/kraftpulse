plugins {
    `java-library`
    `maven-publish`
}

group = "com.bowerzlabs.kraftpulse"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":shared"))

    implementation("io.opentelemetry:opentelemetry-api:1.62.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
            artifactId = "core"
        }
    }

    repositories {
        mavenLocal()
    }
}