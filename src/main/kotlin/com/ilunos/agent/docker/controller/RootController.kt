package com.ilunos.agent.docker.controller

import com.github.dockerjava.api.exception.ConflictException
import com.github.dockerjava.api.exception.NotFoundException
import com.ilunos.agent.docker.Ilunos
import com.ilunos.agent.docker.exception.AgentNotConnectedException
import com.ilunos.agent.docker.model.Status
import com.ilunos.agent.docker.service.docker.DockerContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.*
import javax.inject.Inject

@Controller
@Secured("DOCKER_AGENT_USER")
class RootController(private val ilunos: Ilunos) {

    @Inject
    private lateinit var docker: DockerContext

    @Get
    fun get(): HttpResponse<Status> {
        val ping = docker.ping()
        val images = if (ping) docker.listImages() else emptyList()
        val containers = if (ping) docker.listContainers(Optional.of(true)) else emptyList()

        return HttpResponse.ok(Status(ping, images.size, containers.size, containers.count { it.state == "running" }))
    }

    @Get("/shutdown")
    @Secured("DOCKER_AGENT_ADMIN")
    fun shutdown(): HttpResponse<Any> {
        return HttpResponse.ok<Any>().also { ilunos.shutdown() }
    }


    @Get("/reboot")
    @Secured("DOCKER_AGENT_ADMIN")
    fun reboot(): HttpResponse<Any> {
        if (!ilunos.canReboot())
            return HttpResponse.status<Any>(HttpStatus.SERVICE_UNAVAILABLE).body(JsonError("Cannot reboot, Agent not started with a Bootloader!"))

        return HttpResponse.ok<Any>().also { ilunos.tryReboot() }
    }

    @Get("/heartbeat")
    @Secured(SecurityRule.IS_ANONYMOUS)
    fun heartbeat(): HttpResponse<Any> {
        return HttpResponse.ok()
    }

    @Error(AgentNotConnectedException::class, global = true)
    fun notConnected(request: HttpRequest<Any>): HttpResponse<Any> {
        return HttpResponse.status<Any>(HttpStatus.PRECONDITION_FAILED).body(JsonError("Agent is not connected to any Docker Service!").link(Link.HREF, "/docker/connect"))
    }

    @Error(ConflictException::class, global = true)
    fun conflict(exception: ConflictException): HttpResponse<Any> {
        return HttpResponse.status<Any>(HttpStatus.CONFLICT).body(exception.message?.removeRange(0..11))
    }

    @Error(NotFoundException::class, global = true)
    fun notFound(exception: NotFoundException): HttpResponse<Any> {
        return HttpResponse.notFound(exception.message?.removeRange(0..11))
    }
}