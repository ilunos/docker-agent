package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.ilunos.agent.docker.Ilunos
import edu.umd.cs.findbugs.annotations.NonNull
import edu.umd.cs.findbugs.annotations.Nullable
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import jdk.jfr.BooleanFlag
import java.io.Serializable

@Introspected
@ConfigurationProperties("agent")
class AgentConfig : Serializable {

    @BooleanFlag
    @JsonProperty("auto-connect")
    var autoConnect: Boolean = false

    @NonNull
    @JsonProperty("url")
    var url: String = "tcp://localhost:2375"

    init {
        if (Ilunos.runningInDocker) {
            autoConnect = true
            url = "unix:///var/run/docker.sock"
        }
    }
}