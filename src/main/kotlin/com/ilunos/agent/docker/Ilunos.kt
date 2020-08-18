package com.ilunos.agent.docker

import com.ilunos.agent.docker.util.FileUtils
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.env.Environment
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.runtime.Micronaut.build
import io.micronaut.security.oauth2.client.DefaultOpenIdProviderMetadata
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val propertiesPath = "config/docker-agent.properties"
    if (!FileUtils.exists(propertiesPath)) {
        FileUtils.copyTemplate("docker-agent.properties", propertiesPath)
    }

    System.setProperty(Environment.PROPERTY_SOURCES_KEY, "file:$propertiesPath")
    build().args(*args)
            .packages("com.ilunos.agent.docker")
            .start()
}

@Context
class Ilunos(context: ApplicationContext, environment: Environment) {

    init {
        val security = environment.getProperty("micronaut.security.enabled", Boolean::class.java, true)!!
        val basicAuth = environment.getProperty("micronaut.security.basic-auth.enabled", Boolean::class.java, true)!!
        val oauthAuth = environment.getProperty("micronaut.security.oauth2.enabled", Boolean::class.java, false)!!

        if (security) {
            if (basicAuth) {
                logger.info("Basic Auth is enabled, registering default accounts admin:admin, manager:manager & user:user")
            }

            if (oauthAuth) {
                logger.info("OAuth2 Auth is enabled, verifying Connectivity...")
                try {
                    context.getBean(DefaultOpenIdProviderMetadata::class.java)
                } catch (e: BeanInstantiationException) {
                    logger.error("Unable to communicate with OpenId Provider: ${e.cause?.cause?.message}")
                    exitProcess(406)
                }
            }

            if (!basicAuth && !oauthAuth) {
                logger.error("Neither Basic Auth or OAuth2 is enabled, At least one required! Please enable one AuthProvider or disable Security.")
                exitProcess(405)
            }
        } else {
            if (basicAuth || oauthAuth) {
                logger.warn("Detected enabled AuthProvider but security is disabled. You need to enable security for Authentication to work.")
            }

            logger.warn("!!! ⚠⚠ Running Agent without Security ⚠⚠ !!! This is not recommended as everyone with access to the agent will be able to control docker!")
        }
    }

    companion object {
        const val VERSION: String = "1.0.0"
        private val logger = LoggerFactory.getLogger(Ilunos::class.java)
        val runningInDocker = isRunningInsideDocker()

        private fun isRunningInsideDocker(): Boolean {
            try {
                Files.lines(Paths.get("/proc/1/cgroup")).use { stream ->
                    return stream.anyMatch { line: String ->
                        line.contains("/docker")
                    }.also {
                        if (it)
                            logger.info("Detected Docker Environment. Applying Docker specific settings.")
                        else
                            logger.info("Running Docker-Agent in default mode")
                    }
                }
            } catch (e: IOException) {
                logger.info("Running Docker-Agent in default mode")
                return false
            }
        }
    }
}

