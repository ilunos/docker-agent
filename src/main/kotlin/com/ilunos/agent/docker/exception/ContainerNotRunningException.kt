package com.ilunos.agent.docker.exception

import com.github.dockerjava.api.exception.DockerException

class ContainerNotRunningException(val containerId: String) : DockerException("Container $containerId is not running!", 409) {
}