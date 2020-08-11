package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import jdk.jfr.BooleanFlag
import java.io.Serializable
import javax.validation.constraints.NotNull

@Introspected
@ConfigurationProperties("agent")
data class AgentConfig(

        @field:BooleanFlag
        @JsonProperty("auto-connect")
        var autoConnect: Boolean,

        @field:NotNull
        @JsonProperty("url")
        var hostname: String

) : Serializable {
    constructor(config: AgentConfig) : this(config.autoConnect, config.hostname)
    constructor() : this(true, "tcp://localhost:2375")
}