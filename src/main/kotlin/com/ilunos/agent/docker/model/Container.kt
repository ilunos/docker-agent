package com.ilunos.agent.docker.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.dockerjava.api.model.Container

data class Container(

        @JsonProperty("Id")
        val id: String,

        @JsonProperty("Name")
        val name: String,

        @JsonProperty("State")
        val state: String,

        @JsonProperty("Status")
        val status: String,

        @JsonProperty("Labels")
        val labels: Map<String, String>

) {
    constructor(container: Container) :
            this(container.id,
                    container.names.first().removePrefix("/"),
                    container.state,
                    container.status,
                    container.labels
            )
}