package com.ilunos.agent.docker.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.ilunos.agent.docker.Application
import io.micronaut.core.version.VersionUtils
import java.io.Serializable

@Suppress("unused")
data class Status(

        @JsonProperty("Connected")
        val connected: Boolean,

        @JsonProperty("Images")
        val images: Int,

        @JsonProperty("Containers")
        val containers: Int,

        @JsonProperty("Containers-Running")
        val runningContainers: Int

) : Serializable {

    @JsonProperty("Version")
    val version: String = Application.VERSION

    @JsonProperty("Micronaut-Version")
    val micronautVersion: String = VersionUtils.MICRONAUT_VERSION
}