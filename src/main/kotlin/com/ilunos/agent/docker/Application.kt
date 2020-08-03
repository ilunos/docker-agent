package com.ilunos.agent.docker

import io.micronaut.runtime.Micronaut.*

fun main(args: Array<String>) {
    build()
            .args(*args)
            .packages("com.nanabell.comp.agent.docker")
            .start()
}

class Application {
    companion object {
        const val VERSION: String = "1.0.0"
    }
}

