package com.ilunos.agent.docker.service

import com.ilunos.agent.docker.config.OrchestratorConfig
import com.ilunos.agent.docker.domain.OrchestratorRequest
import com.ilunos.agent.docker.domain.OrchestratorResponse
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.server.util.HttpHostResolver
import io.reactivex.Flowable
import org.slf4j.LoggerFactory
import java.net.InetAddress

@Context
@Requires(property = "agent.orchestrator.url")
class OrchestratorRegister(
        private val config: OrchestratorConfig,
        private val resolver: HttpHostResolver
) {

    private val logger = LoggerFactory.getLogger(OrchestratorRegister::class.java)

    init {
        initialize()
    }

    private fun initialize() {
        val client = HttpClient.create(config.url.toURL())

        val hostname = InetAddress.getLocalHost().hostName
        val selfUrl = resolver.resolve(HttpRequest.GET<Any>("/"))

        val request = HttpRequest.PUT("/agents", OrchestratorRequest(hostname, selfUrl))
        request.basicAuth("agent", config.token ?: "none")

        Flowable.fromPublisher(client.exchange(request, OrchestratorResponse::class.java)).subscribe({
            if (it.status == HttpStatus.OK) {
                when (it.body()?.status) {
                    "CREATED" -> logger.info("Registered Agent at Orchestrator at ${config.url}")
                    "ALREADY_EXISTING" -> logger.info("Connected to Orchestrator at ${config.url}")
                    else -> logger.warn("Unexpected response from Orchestrator. Status: ${it.body()?.status}")
                }
            } else {
                logger.error("Failed to register at Orchestrator ${config.url}, Status: ${it.status}")
            }
        }, {
            logger.error("Failed to register at Orchestrator!", it)
        })
    }
}
