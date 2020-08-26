package com.ilunos.agent.docker.security

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter
import org.reactivestreams.Publisher
import javax.inject.Singleton


@Singleton
class UserPasswordAuthenticationProvider : AuthenticationProvider {

    override fun authenticate(httpRequest: HttpRequest<*>?, authenticationRequest: AuthenticationRequest<*, *>): Publisher<AuthenticationResponse> {
        return Maybe.create { emitter: MaybeEmitter<AuthenticationResponse> ->
            if (authenticationRequest.identity == "admin" && authenticationRequest.secret == "admin") {
                emitter.onSuccess(UserDetails("admin", listOf("DOCKER_AGENT_ADMIN", "DOCKER_AGENT_MANAGER", "DOCKER_AGENT_USER")))
            } else if (authenticationRequest.identity == "manager" && authenticationRequest.secret == "manager") {
                emitter.onSuccess(UserDetails("manager", listOf("DOCKER_AGENT_MANAGER", "DOCKER_AGENT_USER")))
            } else if (authenticationRequest.identity == "user" && authenticationRequest.secret == "user") {
                emitter.onSuccess(UserDetails("user", listOf("DOCKER_AGENT_USER")))
            } else {
                emitter.onError(AuthenticationException(AuthenticationFailed()))
            }
        }.toFlowable()
    }
}