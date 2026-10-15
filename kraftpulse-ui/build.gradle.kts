plugins {
    `java`
    `maven-publish`
}

group = "com.bowerzlabs.kraftpulse"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val isWindows = org.apache.tools.ant.taskdefs.condition.Os.isFamily(
    org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS
)
val npmCommand = if (isWindows) "npm.cmd" else "npm"

// Svelte project lives in ./ui so it never collides with src/main
val uiDir = projectDir.resolve("ui")
val uiDistDir = uiDir.resolve("dist")

// Built assets are embedded here and packaged into the jar
val embeddedAssetsDir =
    projectDir.resolve("src/main/resources/META-INF/resources/kraftpulse")

val npmInstall by tasks.registering(Exec::class) {
    group = "build"
    description = "Installs KraftPulse UI dependencies"

    workingDir(uiDir)
    commandLine(npmCommand, "ci")

    inputs.file(uiDir.resolve("package.json"))
    inputs.file(uiDir.resolve("package-lock.json"))
    outputs.dir(uiDir.resolve("node_modules"))
}

val buildUi by tasks.registering(Exec::class) {
    group = "build"
    description = "Builds the KraftPulse Svelte UI"

    dependsOn(npmInstall)

    workingDir(uiDir)
    commandLine(npmCommand, "run", "build")

    // Track every file that affects the build output, minus generated folders
    inputs.files(fileTree(uiDir) {
        include("src/**", "public/**", "index.html", "package*.json", "*.config.*")
        exclude("node_modules/**", "dist/**")
    })

    outputs.dir(uiDistDir)
}

val embedUi by tasks.registering(Sync::class) {
    group = "build"
    description = "Embeds the built KraftPulse UI into the jar resources"

    dependsOn(buildUi)

    from(uiDistDir)
    into(embeddedAssetsDir)
}

tasks.processResources {
    dependsOn(embedUi)
}

tasks.clean {
    delete(embeddedAssetsDir, uiDistDir)
}

tasks.jar {
    archiveBaseName.set("kraftpulse-ui")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "kraftpulse-ui"

            pom {
                name.set("KraftPulse UI")
                description.set("Compiled Svelte and Tailwind assets for KraftPulse")
            }
        }
    }

    repositories {
        mavenLocal()
    }
}