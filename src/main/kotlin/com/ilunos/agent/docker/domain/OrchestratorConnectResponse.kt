package com.ilunos.agent.docker.domain

data class OrchestratorConnectResponse(
        val token: String?,
        val connected: Boolean,
        val errorMessage: String?
)