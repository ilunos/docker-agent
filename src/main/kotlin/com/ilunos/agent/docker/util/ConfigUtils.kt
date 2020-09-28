package com.ilunos.agent.docker.util

import java.nio.file.Files
import java.nio.file.Path

object ConfigUtils {

    private val config = System.getProperty("ilunos.configurationPath", "config")

    fun initialize() {
        val configPath = Path.of(config)

        // Ensure Config Path exists, create if necessary
        if (!Files.exists(configPath))
            Files.createDirectories(configPath)

        // Ensure Config/application.yml exists, copy template file of necessary
        val appConfig = configPath.resolve("application.yml")
        if (!Files.exists(appConfig))
            copyTemplateConfig(appConfig)

        // If config includes a custom logback.xml use that instead of the bundled
        val loggerConfig = configPath.resolve("logback.xml")
        if (Files.exists(loggerConfig))
            System.setProperty("logback.configurationFile", loggerConfig.toString())
    }

    private fun copyTemplateConfig(targetPath: Path) {
        val stream = {}.javaClass.classLoader.getResourceAsStream("templates/application.yml")
                ?: throw IllegalStateException("Unable to find 'templates/application.yml'!")

        Files.copy(stream, targetPath)
    }
}