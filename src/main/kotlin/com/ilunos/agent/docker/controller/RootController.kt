package com.ilunos.agent.docker.controller

import com.github.dockerjava.api.exception.ConflictException
import com.github.dockerjava.api.exception.NotFoundException
import com.ilunos.agent.docker.exception.AgentNotConnectedException
import com.ilunos.agent.docker.model.Status
import com.ilunos.agent.docker.service.DockerContext
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import java.util.*
import javax.inject.Inject

@Controller
class RootController {

    @Inject
    private lateinit var docker: DockerContext

    @Get
    fun get(): HttpResponse<Status> {
        val ping = docker.ping()
        val images = if (ping) docker.listImages() else emptyList()
        val containers = if (ping) docker.listContainers(Optional.of(true)) else emptyList()

        return HttpResponse.ok(Status(docker.ping(), images.size, containers.size, containers.count { it.state == "running" }))
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