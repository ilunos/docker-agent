package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.ilunos.agent.docker.Ilunos
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import jdk.jfr.BooleanFlag
import java.io.Serializable
import javax.validation.constraints.NotNull

@Introspected
@ConfigurationProperties("agent")
data class AgentConfig(

        @BooleanFlag
        @JsonProperty("auto-connect")
        var autoConnect: Boolean,

        @NotNull
        @JsonProperty("url")
        var hostname: String

) : Serializable {
    constructor(config: AgentConfig) : this(config.autoConnect, config.hostname)
    constructor() : this(true, "tcp://localhost:2375")

    init {
        if (Ilunos.runningInDocker) {
            autoConnect = true
            hostname = "unix:///var/run/docker.sock"
        }
    }
}