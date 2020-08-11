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
                emitter.onSuccess(UserDetails("user", emptyList()))
            } else {
                emitter.onError(AuthenticationException(AuthenticationFailed()))
            }
        }.toFlowable()
    }
}