package com.ilunos.agent.docker.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.dockerjava.api.model.Info
import com.github.dockerjava.api.model.Version

data class DockerInfo(

        @JsonProperty("Info")
        val info: Info,

        @JsonProperty("Version")
        val version: Version

)