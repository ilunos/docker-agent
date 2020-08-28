package com.ilunos.agent.docker.config

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.context.annotation.ConfigurationProperties
import java.nio.file.Path

@ConfigurationProperties("agent.ssl")
class AgentSSLConfig {

    var type: AgentSSLType = AgentSSLType.NONE

    @JsonProperty("keystore-file")
    var keystoreFile: Path? = null

    @JsonProperty("keystore-pass")
    var keystorePass: String? = null

    @JsonProperty("pem-directory")
    var pemDirectory: Path? = null

    enum class AgentSSLType {
        NONE,
        KEYSTORE,
        PEM_FILES
    }
}