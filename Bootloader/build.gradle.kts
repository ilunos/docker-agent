plugins {
    kotlin("jvm") version "1.4.0"
    kotlin("kapt") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

group = "com.ilunos.agent.docker"
version = "1.0"

application {
    mainClassName = "com.ilunos.agent.docker.Bootloader"
}

repositories {
    mavenCentral()
}

dependencies {
    kapt("info.picocli:picocli-codegen:4.5.0")
    implementation(kotlin("stdlib"))
    implementation("info.picocli:picocli")
    implementation("io.micronaut.picocli:micronaut-picocli:2.2.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
}

tasks {
    kapt {
        arguments {
            arg("project", "${project.group}/${project.name}")
        }
    }
}
