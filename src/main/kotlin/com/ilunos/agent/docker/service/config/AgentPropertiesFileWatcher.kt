package com.ilunos.agent.docker.service.config

import com.ilunos.agent.docker.Ilunos
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds

@Context
@Requires(property = "bootloader.loaded")
class AgentPropertiesFileWatcher(private val ilunos: Ilunos) {

    private val logger = LoggerFactory.getLogger(AgentPropertiesFileWatcher::class.java)

    private val watchService = FileSystems.getDefault().newWatchService()
    private val directory: Path = Path.of("config").toAbsolutePath()
    private val file = directory.resolve("docker-agent.properties")

    private var running = true

    init {
        directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)
        logger.info("Registered $directory to FileWatchService")

        logger.debug("Launching FileWatch Service")
        GlobalScope.launch { run() }
    }


    private fun run() {
        while (running) {
            val key = watchService.take()
            for (pollEvent in key.pollEvents()) {
                logger.debug("File: '${pollEvent.context()}' ${pollEvent.kind()}")

                if (pollEvent.context().toString() == file.fileName.toString()) {
                    logger.info("$file has changed! Restarting Agent to reload changes...")
                    running = false
                    break
                }
            }

            key.reset()
        }

        ilunos.tryReboot()
    }
}