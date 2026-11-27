plugins {
    java
    id("org.springframework.boot") version "4.0.10"
    id("io.spring.dependency-management") version "1.1.7"
    id("gg.jte.gradle") version "3.2.5"
}

group = "com.bowerzlabs.kraftpulse"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.10"))
    implementation("gg.jte:jte-spring-boot-starter-4:3.2.5")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("com.bowerzlabs.kraftpulse:kraftpulse-springboot-starter:0.1.0-SNAPSHOT")
    implementation("com.bowerzlabs.kraftpulse:kraftpulse-sqlite:0.1.0-SNAPSHOT")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-opentelemetry-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val jteSourceDir = layout.projectDirectory.dir("src/main/jte").asFile.toPath()
val jteGeneratedJavaDir = layout.buildDirectory.dir("generated-sources/jte").get().asFile.toPath()

jte {
    sourceDirectory.set(jteSourceDir)
    targetDirectory.set(jteGeneratedJavaDir)
    binaryStaticContent = true
    generate()
}

sourceSets {
    main {
        java.srcDir(jteGeneratedJavaDir)
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("generateJte"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}