package com.ilunos.agent.docker.util

import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

object FileUtils {

    fun exists(path: String): Boolean {
        return try {
            exists(Path.of(path))
        } catch (e: InvalidPathException) {
            false
        }
    }

    fun exists(path: Path): Boolean = Files.exists(path)

    fun copyTemplate(resourcePath: String, targetPath: String) {
        val target = Path.of(targetPath)
        val stream = {}.javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalStateException("Unable to find '$resourcePath'!")

        Files.createDirectories(target.parent)
        Files.copy(stream, target)
    }
}