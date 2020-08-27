package com.ilunos.agent.docker.config

import edu.umd.cs.findbugs.annotations.Nullable
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected
import java.net.URI

@Introspected
@ConfigurationProperties("agent.orchestrator")
class OrchestratorConfig {

    lateinit var url: URI

    @Nullable
    var token: String? = null
}

