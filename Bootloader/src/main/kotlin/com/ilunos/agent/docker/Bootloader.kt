package com.ilunos.agent.docker

import io.micronaut.configuration.picocli.PicocliRunner
import org.slf4j.LoggerFactory
import picocli.CommandLine.*
import java.io.File
import kotlin.system.exitProcess

@Command(name = "Docker-Agent-Bootloader", mixinStandardHelpOptions = true)
class Bootloader : Runnable {

    private val logger = LoggerFactory.getLogger(Bootloader::class.java)

    @Parameters(index = "0", description = ["File File that will be loaded"], arity = "1")
    lateinit var file: File

    @Option(names = ["-a, --arguments"], description = ["Arguments passed to the child process"])
    var arguments: String? = null

    @Option(names = ["-e", "--exit-code"], description = ["The exit code on which the application will be restarted"], defaultValue = "302")
    var exitCode: Int = 302

    @Option(names = ["-d", "--directory"], description = ["Sets the working directory of the child process", "Default: Current working directory"])
    var workingDirectory: File = File(".")

    private var recentBoots = 0
    private var lastBoot = 0L

    private var process: Process? = null

    override fun run() {
        logger.info("Initializing Bootloader...")

        if (!file.exists()) {
            throw IllegalArgumentException("File: $file does not exist!")
        }

        if (file.extension != "jar") {
            throw IllegalArgumentException("Bootloader can only manage jar files!")
        }

        logger.info("Configuration loaded:")
        logger.info("File      : ${file.absolutePath}")
        logger.info("Directory : ${workingDirectory.absolutePath}")
        logger.info("Arguments : $arguments")
        logger.info("Exit-Code : $exitCode")

        logger.info("Bootloader Initialization done. Starting child process...")

        logger.debug("Registering ShutdownHook to kill ChildProcess on exit")
        Runtime.getRuntime().addShutdownHook(Thread {
            val process = getProcess() ?: return@Thread
            process.destroy()
        })

        loop@ while (true) {
            process = boot()
            logger.debug("Started Child Process with PID: ${process?.pid()}")

            process?.waitFor()
            when (process?.exitValue()) {
                exitCode -> logger.info("Child Process exited with Exit-Code $exitCode. Restarting...")
                else -> {
                    logger.info("Child Process exited with Unknown or Unspecified Exit-Code ${process?.exitValue()}. Exiting...")
                    break@loop
                }
            }
        }

        logger.info("Shutting down Bootloader...")
    }

    private fun boot(): Process {
        if (System.currentTimeMillis() - lastBoot > 3600 * 1000) {
            logger.debug("Recent reboots more than 60 min ago. resetting recent reboots...")
            recentBoots = 0
        }

        recentBoots++
        lastBoot = System.currentTimeMillis()
        logger.debug("Increased recentReboots by 1, is now: $recentBoots")

        if (recentBoots >= 4) {
            logger.error("Failed to Boot 3 times, Assuming broken State. Exiting...")
            exitProcess(78)
        }

        val pb = ProcessBuilder().inheritIO()
        pb.command(command())
        pb.directory(workingDirectory)

        return pb.start()
    }

    private fun command(): List<String> {
        val list = mutableListOf<String>()
        list.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java")
        list.add("-jar")

        val args = arguments
        if (args != null)
            list.add(args)

        list.add("-Dbootloader.loaded=true") // Indicate to the child it has been loaded by a bootloader
        list.add(file.absolutePath)

        return list
    }

    private fun getProcess(): Process? {
        return process
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PicocliRunner.execute(Bootloader::class.java, *args)
        }
    }
}
