package com.ilunos.agent.docker.controller

import com.github.dockerjava.api.command.InspectContainerResponse
import com.ilunos.agent.docker.exception.ContainerAlreadyRunningException
import com.ilunos.agent.docker.exception.ContainerNotRunningException
import com.ilunos.agent.docker.model.Container
import com.ilunos.agent.docker.service.DockerContext
import io.micronaut.core.version.annotation.Version
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.security.annotation.Secured
import java.util.*
import javax.inject.Inject
import com.github.dockerjava.api.model.Container as RawContainer

@Controller("/containers")
@Secured("DOCKER_AGENT_USER")
class ContainerController {

    @Inject
    private lateinit var docker: DockerContext

    @Version("1")
    @Get("/{?all,limit,states,exit-code,before,since,labels}")
    fun list(all: Optional<Boolean>,
             limit: Optional<Int>,
             states: Optional<List<String>>,
             @QueryValue("exit-code") exitCode: Optional<Int>,
             before: Optional<String>,
             since: Optional<String>,
             labels: Optional<List<String>>
    ): HttpResponse<List<Container>> {
        return HttpResponse.ok(docker.listContainers(all, limit, states, exitCode, before, since, labels).map { Container(it) })
    }

    @Version("0")
    @Get("/{?all}")
    fun listRaw(all: Optional<Boolean>): HttpResponse<List<RawContainer>> {
        return HttpResponse.ok(docker.listContainers(all))
    }

    @Get("/{id}")
    fun info(id: String): HttpResponse<InspectContainerResponse> {
        return HttpResponse.ok(docker.inspectContainer(id))
    }

    @Get("/{id}/start")
    @Secured("DOCKER_AGENT_MANAGER")
    fun start(id: String): HttpResponse<Nothing> {
        if (docker.inspectContainer(id).state.running == true) throw ContainerAlreadyRunningException(id)

        docker.startContainer(id)
        return HttpResponse.ok()
    }

    @Get("/{id}/stop")
    @Secured("DOCKER_AGENT_MANAGER")
    fun stop(id: String): HttpResponse<Nothing> {
        if (docker.inspectContainer(id).state.running != true) throw ContainerAlreadyRunningException(id)

        docker.stopContainer(id)
        return HttpResponse.ok()
    }

    @Delete("/{id}{?force,volumes}")
    @Secured("DOCKER_AGENT_ADMIN")
    fun delete(id: String, force: Optional<Boolean>, volumes: Optional<Boolean>): HttpResponse<Nothing> {
        docker.deleteContainer(id, force, volumes)

        return HttpResponse.ok()
    }

    @Error(ContainerAlreadyRunningException::class)
    fun errorAlreadyRunning(e: ContainerAlreadyRunningException): HttpResponse<Any> {
        return HttpResponse.status<Any>(HttpStatus.CONFLICT).body(JsonError(e.message).link(Link.SELF, "/containers/${e.containerId}/start"))
    }

    @Error(ContainerNotRunningException::class)
    fun errorNotRunning(e: ContainerNotRunningException): HttpResponse<Any> {
        return HttpResponse.status<Any>(HttpStatus.CONFLICT).body(JsonError(e.message).link(Link.SELF, "/containers/${e.containerId}/stop"))
    }
}