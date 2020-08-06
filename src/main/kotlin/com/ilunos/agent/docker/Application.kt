package com.ilunos.agent.docker

import io.micronaut.runtime.Micronaut.build
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


fun main(args: Array<String>) {
    build()
            .args(*args)
            .packages("com.ilunos.agent.docker")
            .start()
}

class Application {
    companion object {
        const val VERSION: String = "1.0.0"
        val isDocker: Boolean = isRunningInsideDocker()

        private fun isRunningInsideDocker(): Boolean {
            try {
                Files.lines(Paths.get("/proc/1/cgroup")).use { stream -> return stream.anyMatch { line: String -> line.contains("/docker").also {
                    LoggerFactory.getLogger(Application::class.java).info("Detected Docker Environment. Applying Docker specific settings.")
                } } }
            } catch (e: IOException) {
                return false
            }
        }
    }
}

