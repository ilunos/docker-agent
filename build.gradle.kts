object Dependency {
    const val KOTLIN = "1.4.10"
    const val MICRONAUT = "2.0.3"
}

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
    kotlin("plugin.allopen") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    application
}

version = "0.1"
group = "com.ilunos.agent.docker"

repositories {
    maven("https://dl.bintray.com/nanabell/ilunos")
    mavenCentral()
    jcenter()
}

val developmentOnly = configurations.create("developmentOnly")


dependencies {
    kapt(platform("io.micronaut:micronaut-bom:${Dependency.MICRONAUT}"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kapt("io.micronaut.security:micronaut-security-annotations")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    implementation("com.ilunos.common:common:1.0.1")

    implementation(platform("io.micronaut:micronaut-bom:${Dependency.MICRONAUT}"))
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")

    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.security:micronaut-security-oauth2")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("javax.annotation:javax.annotation-api")

    implementation("com.github.docker-java:docker-java:3.2.5")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.2.5")

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    kaptTest(enforcedPlatform("io.micronaut:micronaut-bom:${Dependency.MICRONAUT}"))
    kaptTest("io.micronaut:micronaut-inject-java")

    testImplementation(enforcedPlatform("io.micronaut:micronaut-bom:${Dependency.MICRONAUT}"))
    testImplementation("io.micronaut.test:micronaut-test-kotlintest")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
}

allOpen {
    annotations("io.micronaut.aop.Around", "io.micronaut.scheduling.annotation.Scheduled")
}

kapt {
    arguments {
        arg("micronaut.processing.incremental", true)
        arg("micronaut.processing.annotations", "com.ilunos.agent.docker.*")
        arg("micronaut.processing.group", "com.ilunos.agent.docker")
        arg("micronaut.processing.module", "Docker-Agent")
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "13"
            javaParameters = true
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "13"
            javaParameters = true
        }
    }

    shadowJar {
        mergeServiceFiles()
    }

    // use JUnit 5 platform
    test {
        useJUnitPlatform()
    }
}


tasks.withType<JavaCompile> {
    classpath += developmentOnly
    options.compilerArgs.addAll(arrayOf("-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote"))
}