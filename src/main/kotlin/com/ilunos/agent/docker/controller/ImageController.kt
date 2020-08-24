package com.ilunos.agent.docker.controller

import com.github.dockerjava.api.command.InspectImageResponse
import com.github.dockerjava.api.model.Image
import com.ilunos.agent.docker.service.docker.DockerContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import java.util.*
import javax.inject.Inject

@Controller("/images")
@Secured("DOCKER_AGENT_USER")
class ImageController {

    @Inject
    private lateinit var docker: DockerContext

    @Get("{?dangling,name,labels}")
    fun list(dangling: Optional<Boolean>, name: Optional<String>, labels: Optional<List<String>>): HttpResponse<List<Image>> {
        return HttpResponse.ok(docker.listImages(dangling, name, labels))
    }

    @Get("/{id}")
    fun inspect(id: String): HttpResponse<InspectImageResponse> {
        return HttpResponse.ok(docker.inspectImage(id))
    }

    @Delete("{id}{?force,no-prune}")
    @Secured("DOCKER_AGENT_ADMIN")
    fun delete(id: String, @QueryValue("force") force: Optional<Boolean>, @QueryValue("no-prune") noPrune: Optional<Boolean>): HttpResponse<Nothing> {
        docker.deleteImage(id, force, noPrune)

        return HttpResponse.ok()
    }
}