package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import jdk.jfr.BooleanFlag
import java.io.Serializable
import javax.validation.constraints.NotNull

@Introspected
data class DockerConfig(

        @field:BooleanFlag
        @JsonProperty("Auto-Connect")
        var autoConnect: Boolean,

        @field:NotNull
        @JsonProperty("Hostname")
        var hostname: String

) : Serializable {
    constructor(config: DockerConfig) : this(config.autoConnect, config.hostname)
    constructor() : this(true, "tcp://localhost:2375")
}