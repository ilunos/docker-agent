package com.ilunos.agent.docker.config

import com.ilunos.agent.docker.Ilunos
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import javax.inject.Singleton

@Singleton
class ConfigProvider {

    private val path = File("").toPath().resolve("config.yml")
    private var config: DockerConfig? = null

    private val constructor = Constructor(DockerConfig::class.java)
    private val representer = Representer()
    private val dumperOptions = DumperOptions()

    init {
        representer.addClassTag(DockerConfig::class.java, Tag.MAP)

        dumperOptions.isPrettyFlow = true
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    }

    fun load(): DockerConfig {
        synchronized(this) {
            if (config != null) {
                return config!!
            }

            if (!exists()) {
                if (Ilunos.isDocker) {
                    save(DockerConfig((System.getenv("ILUNOS_AUTO_CONNECT") ?: "true").toBoolean(),
                            System.getenv("ILUNOS_HOST") ?: "unix:///var/run/docker.sock"))
                } else {
                    save(DockerConfig())
                }
            }

            return Yaml(constructor, representer, dumperOptions).load<DockerConfig>(FileReader(path.toFile()))
        }
    }

    fun save(config: DockerConfig) {
        synchronized(this) {
            this.config = config

            Yaml(representer, dumperOptions).dump(config, FileWriter(path.toFile()))
        }
    }

    private fun exists(): Boolean {
        return Files.exists(path)
    }

}