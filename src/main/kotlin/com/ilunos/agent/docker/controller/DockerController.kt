package com.ilunos.agent.docker.controller

import com.ilunos.agent.docker.model.DockerInfo
import com.ilunos.agent.docker.service.DockerContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import javax.inject.Inject

@Controller("/docker")
class DockerController {

    @Inject
    private lateinit var docker: DockerContext

    @Get
    fun info(): HttpResponse<DockerInfo> {
        return HttpResponse.ok(DockerInfo(docker.info(), docker.version()))
    }

    @Get("/connect")
    fun connect(): HttpResponse<Nothing> {
        docker.connect()

        return HttpResponse.ok()
    }

    @Get("/disconnect")
    fun disconnect(): HttpResponse<Nothing> {
        docker.disconnect()

        return HttpResponse.ok()
    }
}