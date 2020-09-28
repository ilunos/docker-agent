package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.ilunos.agent.docker.Ilunos
import edu.umd.cs.findbugs.annotations.NonNull
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import java.io.Serializable

@Introspected
@ConfigurationProperties("agent")
class AgentConfig : Serializable {

    @JsonProperty("auto-connect")
    var autoConnect: Boolean = true

    @NonNull
    @JsonProperty("url")
    var url: String = when {
        Ilunos.isDocker -> "unix:///var/run/docker.sock"
        Ilunos.isWindows -> "npipe:////./pipe/docker_engine"
        else -> "unix:///var/run/docker.sock"
    }
}