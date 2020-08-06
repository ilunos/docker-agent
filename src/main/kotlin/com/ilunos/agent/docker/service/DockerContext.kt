package com.ilunos.agent.docker.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.api.command.InspectImageResponse
import com.github.dockerjava.api.exception.DockerException
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Info
import com.github.dockerjava.api.model.Version
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.ilunos.agent.docker.config.ConfigProvider
import com.ilunos.agent.docker.exception.AgentNotConnectedException
import com.ilunos.agent.docker.model.ConnectionStatus
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

@Context
@Infrastructure
class DockerContext(private val provider: ConfigProvider) {

    private val logger: Logger = LoggerFactory.getLogger(DockerContext::class.java)

    private lateinit var client: DockerClient

    var status: ConnectionStatus = ConnectionStatus.UNKNOWN

    init {
        if (provider.load().autoConnect) {
            logger.info("Auto-Connect is enabled. Attempting to Connect to Docker System")
            GlobalScope.launch {
                connect()
            }
        }
    }

    fun info(): Info {
        requireConnection()

        return client.infoCmd().exec()
    }

    fun ping(): Boolean {
        if (status != ConnectionStatus.CONNECTED) return false

        return try {
            client.pingCmd().exec().let { true }
        } catch (e: DockerException) {
            false
        }
    }

    fun listContainers(all: Optional<Boolean> = Optional.empty(),
                       limit: Optional<Int> = Optional.empty(),
                       states: Optional<List<String>> = Optional.empty(),
                       exitCode: Optional<Int> = Optional.empty(),
                       before: Optional<String> = Optional.empty(),
                       since: Optional<String> = Optional.empty(),
                       labels: Optional<List<String>> = Optional.empty()

    ): List<Container> {
        requireConnection()

        val cmd = client.listContainersCmd()

        all.ifPresent { cmd.withShowAll(it) }
        limit.ifPresent { cmd.withLimit(it) }
        states.ifPresent { cmd.withStatusFilter(it) }
        exitCode.ifPresent { cmd.withExitedFilter(it) }
        before.ifPresent { cmd.withBefore(it) }
        since.ifPresent { cmd.withSince(it) }
        labels.ifPresent { cmd.withLabelFilter(it) }

        return cmd.exec()
    }

    fun inspectContainer(id: String): InspectContainerResponse {
        requireConnection()

        return client.inspectContainerCmd(id).exec()
    }

    fun startContainer(id: String) {
        requireConnection()

        client.startContainerCmd(id).exec()
    }

    fun stopContainer(id: String) {
        requireConnection()

        client.stopContainerCmd(id).exec()
    }

    fun deleteContainer(id: String,
                        force: Optional<Boolean> = Optional.empty(),
                        volumes: Optional<Boolean> = Optional.empty()
    ) {
        requireConnection()

        val cmd = client.removeContainerCmd(id)

        force.ifPresent { cmd.withForce(it) }
        volumes.ifPresent { cmd.withRemoveVolumes(it) }

        cmd.exec()
    }

    fun listImages(dangling: Optional<Boolean> = Optional.empty(),
                   name: Optional<String> = Optional.empty(),
                   labels: Optional<List<String>> = Optional.empty()
    ): List<Image> {
        requireConnection()

        val cmd = client.listImagesCmd()

        dangling.ifPresent { cmd.withDanglingFilter(it) }
        name.ifPresent { cmd.withImageNameFilter(it) }
        labels.ifPresent { cmd.withLabelFilter(*it.toTypedArray()) }

        return cmd.exec()
    }

    fun inspectImage(imageId: String): InspectImageResponse {
        requireConnection()

        return client.inspectImageCmd(imageId).exec()
    }

    fun deleteImage(id: String,
                    force: Optional<Boolean> = Optional.empty(),
                    noPrune: Optional<Boolean> = Optional.empty()
    ) {
        requireConnection()

        val cmd = client.removeImageCmd(id)

        force.ifPresent { cmd.withForce(it) }
        noPrune.ifPresent { cmd.withNoPrune(it) }

        cmd.exec()
    }

    fun version(): Version {
        requireConnection()

        return client.versionCmd().exec()
    }

    fun connect() {
        disconnect()

        val clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(provider.load().hostname)
                .build()

        val httpClient = ApacheDockerHttpClient.Builder()
                .dockerHost(clientConfig.dockerHost)
                .sslConfig(clientConfig.sslConfig)
                .build()

        this.status = ConnectionStatus.BUILDING

        try {
            this.client = DockerClientBuilder.getInstance(clientConfig).withDockerHttpClient(httpClient).build()
            this.status = ConnectionStatus.CONNECTED
            logger.info("Connected to Docker System at ${provider.load().hostname}")

        } catch (e: DockerException) {
            status = ConnectionStatus.FAILED
            logger.error("Failed to Connect to Docker System", e)
        }
    }

    fun disconnect() {
        if (::client.isInitialized) {
            this.client.close()
            logger.info("Disconnected from Docker System")
        }

        this.status = ConnectionStatus.DISCONNECTED
    }

    private fun requireConnection() {
        if (status != ConnectionStatus.CONNECTED)
            throw AgentNotConnectedException()
    }
}