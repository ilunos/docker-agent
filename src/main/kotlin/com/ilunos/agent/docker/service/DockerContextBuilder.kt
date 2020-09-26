package com.ilunos.agent.docker.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.LocalDirectorySSLConfig
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.ilunos.agent.docker.config.AgentConfig
import com.ilunos.agent.docker.config.AgentSSLConfig
import org.slf4j.LoggerFactory
import java.nio.file.Files
import javax.naming.ConfigurationException

class DockerContextBuilder(private val agentConfig: AgentConfig, private val agentSSLConfig: AgentSSLConfig) {

    private val logger = LoggerFactory.getLogger(DockerContextBuilder::class.java)

    fun build(): DockerClient {
        logger.debug("Start building new DockerClient...")

        val configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(agentConfig.url)
        val clientConfig = loadSSlConfig(configBuilder)

        val httpClient = ApacheDockerHttpClient.Builder()
                .dockerHost(clientConfig.dockerHost)
                .sslConfig(clientConfig.sslConfig)
                .build()

        return DockerClientBuilder.getInstance(clientConfig).withDockerHttpClient(httpClient).build()
    }


    private fun loadSSlConfig(builder: DefaultDockerClientConfig.Builder): DockerClientConfig {
        return when (agentSSLConfig.type) {
            AgentSSLConfig.AgentSSLType.NONE -> loadNoSSL(builder)
            AgentSSLConfig.AgentSSLType.KEYSTORE -> loadKeystoreSSl(builder)
            AgentSSLConfig.AgentSSLType.PEM_FILES -> loadPemSSL(builder)
        }
    }

    private fun loadNoSSL(builder: DefaultDockerClientConfig.Builder): DockerClientConfig {
        logger.debug("Building DockerClient without SSL Config")

        return builder.build()
    }

    private fun loadKeystoreSSl(builder: DefaultDockerClientConfig.Builder): DockerClientConfig {
        logger.debug("Building DockerClient with Keystore SSL Config")

        TODO("Keystore SSL Config is not yet implemented")
    }

    private fun loadPemSSL(builder: DefaultDockerClientConfig.Builder): DockerClientConfig {
        logger.debug("Building DockerClient with PemDirectory SSL Config")

        val directory = agentSSLConfig.pemDirectory
                ?: throw ConfigurationException("Property 'agent.ssl.pem-directory' is required when using SSL Type 'PEM_FILES'!")

        if (!Files.isDirectory(directory))
            throw ConfigurationException("Property 'agent.ssl.pem-directory' does not exist or is not a directory!")

        logger.debug("Using PemDirectory at '$directory'")
        return builder.withCustomSslConfig(LocalDirectorySSLConfig(directory.toAbsolutePath().toString())).build()
    }


}