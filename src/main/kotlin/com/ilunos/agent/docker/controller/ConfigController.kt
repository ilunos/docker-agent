package com.ilunos.agent.docker.controller

import com.ilunos.agent.docker.config.ConfigProvider
import com.ilunos.agent.docker.config.DockerConfig
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post

@Controller("/config")
class ConfigController(private val provider: ConfigProvider) {

    @Get
    fun get(): HttpResponse<DockerConfig> {
        return HttpResponse.ok(provider.load())
    }

    @Post
    fun set(config: DockerConfig): HttpResponse<DockerConfig> {
        provider.save(config)

        return HttpResponse.ok(config)
    }

    @Post("/hostname")
    fun setHost(hostname: String): HttpResponse<DockerConfig> {
        val config = provider.load()
        config.hostname = hostname

        provider.save(config)
        return HttpResponse.ok(config)
    }
}