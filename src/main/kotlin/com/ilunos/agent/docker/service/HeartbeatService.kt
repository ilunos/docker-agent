package com.ilunos.agent.docker.service

import com.ilunos.agent.docker.config.OrchestratorConfig
import com.ilunos.agent.docker.domain.OrchestratorConnectRequest
import com.ilunos.agent.docker.domain.OrchestratorConnectResponse
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.annotation.Async
import io.micronaut.scheduling.annotation.Scheduled
import io.reactivex.Flowable
import org.slf4j.LoggerFactory
import java.net.InetAddress
import javax.inject.Singleton

@Singleton
@Requires(property = "agent.orchestrator.enabled", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
open class HeartbeatService(private val config: OrchestratorConfig, private val server: EmbeddedServer) {

    private val logger = LoggerFactory.getLogger(HeartbeatService::class.java)
    private val client = HttpClient.create(config.url.toURL())

    private var token: String? = null
    private var name: String = InetAddress.getLocalHost().hostName

    @Async
    @Scheduled(initialDelay = "2s")
    open fun connect() {
        val request = HttpRequest.PUT("/agents", OrchestratorConnectRequest(name, token, InetAddress.getLocalHost().hostName, server.port))
        request.basicAuth("agent", config.token ?: "none")

        Flowable.fromPublisher(client.exchange(request, OrchestratorConnectResponse::class.java)).subscribe({
            if (it.status != HttpStatus.OK) {
                logger.warn("Unexpected Server Response: ${it.status}")
                return@subscribe
            }

            val response = it.body() ?: throw IllegalStateException("OrchestratorConnectResponse Body is empty!?")
            if (response.connected) {
                this.token = response.token

                logger.info("Initial Heartbeat check successful. Orchestrator: ${config.url}")
                return@subscribe
            }

            logger.error("Failed Initial Heartbeat check: ${response.errorMessage}")

        }, {
            if (logger.isDebugEnabled)
                logger.error("Failed to connect to Orchestrator! Is it running?", it)
            else
                logger.error("Failed to connect to Orchestrator! Is it running?")
        })
    }

}