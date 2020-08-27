# Docker Agent

Agent used to connect a Docker environment into the Ilunos eco system.

## Building 
#### Requirements
- JDK 13 or higher

Building on Unix:
```shell script
./gradlew build
```

Building on Windows:
```shell script
gradlew build
```

### Building Docker Image *(Optional)*
```shell script
docker build -t ilunos/docker-agent:[VERSION] .
```

## Running
There multiple ways to use the Docker Agent, currently the Agent only supports **one** Docker system only

Running on Host:
```shell script
java -jar build/libs/docker-agent-[VERSION]-all.jar
```

Running in Docker *(unix only)*:
```shell script
docker run --name ilunos_docker-agent --publish 8885:8885 --volume /opt/ilunos/docker-agent/config:/config --volume /var/run/docker.sock:/var/run/docker.sock ilunos/docker-agent:[VERSION]
```
***Note:*** Except for very specific use-cases it is generally a **very** bad idea to mount the socket file into a container.
The container will have **full** access to the Docker environment.

## Configuring
When first starting the Agent will generate a default configuration in `config/application.yml`.
Read through the file as it contains comments about the individual settings you can change.

#### Important: Agent-Orchestrator
While you can directly talk to an Agent, it is recommended to use an Agent-Orchestrator.  
To connect an Agent to the Orchestrator set the `agent.orchestrator.url` in the application.yml file.
```yaml
agent:
  orchestrator:

    # Base Url of the Orchestrator, The orchestrator needs to live somewhere from where it can reach the agent
    url: <orchestrator-url>

    # Set to the value of micronaut.security.register-token from your orchestrator configuration
    # !If token is not being used remove or leave commented out!
    # token: my-super-secure-registration-password
```
