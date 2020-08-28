package com.ilunos.agent.docker.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.InspectContainerResponse
import com.github.dockerjava.api.command.InspectImageResponse
import com.github.dockerjava.api.exception.DockerException
import com.github.dockerjava.api.model.Container
import com.github.dockerjava.api.model.Image
import com.github.dockerjava.api.model.Info
import com.github.dockerjava.api.model.Version
import com.github.dockerjava.core.*
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.ilunos.agent.docker.config.AgentConfig
import com.ilunos.agent.docker.config.AgentSSLConfig
import com.ilunos.agent.docker.exception.AgentNotConnectedException
import com.ilunos.agent.docker.model.ConnectionStatus
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Infrastructure
import io.micronaut.context.exceptions.ConfigurationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.security.KeyStore
import java.util.*

@Context
@Infrastructure
class DockerContext(private val agentConfig: AgentConfig, private val agentSSLConfig: AgentSSLConfig) {

    private val logger: Logger = LoggerFactory.getLogger(DockerContext::class.java)

    private lateinit var client: DockerClient
    private var status: ConnectionStatus = ConnectionStatus.UNKNOWN

    init {
        if (agentConfig.autoConnect) {
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

        val configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(agentConfig.url)
        val clientConfig = loadSSLConfig(configBuilder)

        val httpClient = ApacheDockerHttpClient.Builder()
                .dockerHost(clientConfig.dockerHost)
                .sslConfig(clientConfig.sslConfig)
                .build()

        this.status = ConnectionStatus.BUILDING

        try {
            this.client = DockerClientBuilder.getInstance(clientConfig).withDockerHttpClient(httpClient).build()
            client.pingCmd().exec()

            this.status = ConnectionStatus.CONNECTED
            logger.info("Connected to Docker System at ${agentConfig.url}")

        } catch (e: Exception) {
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

    private fun loadSSLConfig(clientConfigBuilder: DefaultDockerClientConfig.Builder): DockerClientConfig {
        if (agentSSLConfig.type == AgentSSLConfig.AgentSSLType.KEYSTORE) {
            val keystoreFile = agentSSLConfig.keystoreFile
            val keystorePass = agentSSLConfig.keystorePass

            if (keystoreFile == null || !Files.exists(keystoreFile))
                throw ConfigurationException("Required Property 'agent.ssl.keystore-file' is not set for type KEYSTORE!")

            if (!Files.isRegularFile(keystoreFile))
                throw ConfigurationException("Property 'agent.ssl.keystore-file': $keystoreFile is not a regular file!")

            if (keystorePass == null)
                throw ConfigurationException("Required Property 'agent.ssl.keystore-pass' is not set for type KEYSTORE!")

            when (FilenameUtils.getExtension(keystoreFile.toString()).toLowerCase()) {
                "jks" -> {
                    val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
                    keystore.load(Files.newInputStream(keystoreFile), keystorePass.toCharArray())
                    clientConfigBuilder.withCustomSslConfig(KeystoreSSLConfig(keystore, keystorePass))
                }
                "pkcs12" -> {
                    clientConfigBuilder.withCustomSslConfig(KeystoreSSLConfig(keystoreFile.toFile(), keystorePass))
                }
                else -> throw ConfigurationException("Unknown Keystore FileExtension: ${FilenameUtils.getExtension(keystoreFile.toString())}, Supported: jks, pkcs12")
            }

        } else if (agentSSLConfig.type == AgentSSLConfig.AgentSSLType.PEM_FILES) {
            val directory = agentSSLConfig.pemDirectory
            if (directory == null || !Files.exists(directory))
                throw ConfigurationException("Required Property 'agent.ssl.pem-directory' is not set for type PEM_FILES!")

            if (!Files.isDirectory(directory))
                throw ConfigurationException("Property 'agent.ssl.pem-directory': $directory is not a directory!")

            clientConfigBuilder.withCustomSslConfig(LocalDirectorySSLConfig(directory.normalize().toAbsolutePath().toString()))
        }

        return clientConfigBuilder.build()
    }

    private fun requireConnection() {
        if (status != ConnectionStatus.CONNECTED)
            throw AgentNotConnectedException()
    }
}