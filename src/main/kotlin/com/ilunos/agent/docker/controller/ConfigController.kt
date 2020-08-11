package com.ilunos.agent.docker.controller

import com.ilunos.agent.docker.config.AgentConfig
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/config")
class ConfigController(private val agentConfig: AgentConfig) {

    @Get
    fun get(): HttpResponse<AgentConfig> {
        return HttpResponse.ok(agentConfig)
    }

    // TODO: Return this in the future with a better solution
/*    @Post
    fun set(config: AgentConfig): HttpResponse<AgentConfig> {
        provider.save(config)
        return HttpResponse.ok(config)
    }*/
}