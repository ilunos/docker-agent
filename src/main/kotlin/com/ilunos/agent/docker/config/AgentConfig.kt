package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.ilunos.agent.docker.Ilunos
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import jdk.jfr.BooleanFlag
import java.io.Serializable
import javax.annotation.Nullable
import javax.validation.constraints.NotNull

@Introspected
@ConfigurationProperties("agent")
class AgentConfig : Serializable {

    @BooleanFlag
    @JsonProperty("auto-connect")
    var autoConnect: Boolean = false

    @NotNull
    @JsonProperty("url")
    var url: String = "tcp://localhost:2375"

    @Nullable
    @JsonProperty("orchestrator-url")
    var orchestratorUrl: String? = null

    @Nullable
    @JsonProperty("token")
    var token: String? = null

    init {
        if (Ilunos.runningInDocker) {
            autoConnect = true
            url = "unix:///var/run/docker.sock"
        }
    }
}