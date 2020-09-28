package com.ilunos.agent.docker.domain

data class OrchestratorConnectRequest(
        val name: String,
        val token: String?,
        val hostname: String,
        val port: Int
) {
}