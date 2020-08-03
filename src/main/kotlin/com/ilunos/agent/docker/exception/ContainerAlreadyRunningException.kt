package com.ilunos.agent.docker.exception

import com.github.dockerjava.api.exception.DockerException

class ContainerAlreadyRunningException(val containerId: String) : DockerException("Container $containerId is already running!", 409) {
}