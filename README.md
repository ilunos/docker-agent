# Docker Agent

Agent used to connect a Docker environment into the Ilunos eco system.

## Building
You will need JDK 14 then just run `gradlew build` on windows and `./gradlew build` on linux  
The result will be in `build/libs/docker-agent-[VERSION]-all.jar`

## Running
There multiple ways to use the Docker Agent, currently the Agent only supports **one** Docker system only

By default, the Docker Agent expects a Keycloak Server used for Authentication & Authorization.
In the default configuration file the keycloak is not configured and will crash the application.   
To disable this you can set `micronaut.security.oauth2.clients.keycloak.enabled` to false in the config.

### Docker Container
You can run the Agent directly inside of the Docker environment you wish to manage.
For this to work you will need to mount the `docker.sock` into the agent container.  
***Note:*** Unless you are mounting an Agent to manage your Docker it is generally a **very** bad idea to mount the socket file into a container.
The container will have **full** access to the Docker environment.

When running the Agent in a container it will automatically pick up on this, and the default configuration will look for the socket file at `/var/run/docker.sock`.
If your mounting path is different you will have to change the config.

If you have built the application locally you can simply run: `docker build -f Dockerfile -t ilunos/docker-agent .`   
or `docker build -f Build.Dockerfile -t ilunos/docker-agent .`
if you do not have java 14 installed and cannot build the application locally

Running the application `docker run -v /var/run/docker.sock:/var/run/docker.sock ilunos/docker-agent`  

To edit the config from the defaults either mount a config file `-v myConfigDir:config`   
or set the individual configs with environment Variables e.g: `-e MICRONAUT_SERVER_PORT=80` for `micronaut.server.port`

Upsides:
- Easy deployment

Downsides:
- Host System must be the same as Docker Environment
- Dies with Docker
- Need to mount docker.sock file into container

### Local Host (unix connect)
TODO: Running on the Host as Docker.

Upsides: 
- unaffected by Docker, should docker die

Downsides:
- need Java 14 on the Host
- Needs to run as a User that has access to docker.sock file (if using unix connect) 

### Local Host (tcp connect)
TODO: Running Agent on Host connecting via tcp.

Upsides:
- Unaffected by Docker, should daemon die
- Does not need access to docker.sock file

Downsides:
- Does not need access to docker.sock file
- exposing tcp gives everyone access unless secured by certificates

### Remote Host (tcp connect)
TODO: Running Agent on a Remote Machine connecting via tcp.

Upsides:
- Unaffected by Docker, should daemon die
- Unaffected by Host Status
- Does not need access to docker.sock file

Downsides:
- Does not need access to docker.sock file
- exposing tcp gives everyone access unless secured by certificates
    - This should never be done without tls enabled, as it gives everyone with access to the machine access to Docker

### Local/Remote Host (tcp & tls connect)
TODO: Running Agent on Local or Remote Most connecting via tcp secured by tls

Upsides:
- Best of both worlds, secure access and control with mutual certification verification
- Unaffected by Host or Docker Status

Downsides:
- Currently not supported by the Agent

# Internal Documentation
Reference Links used for Development
## Feature security documentation

- [Micronaut Micronaut Security documentation](https://micronaut-projects.github.io/micronaut-security/latest/guide/index.html)

## Feature http-client documentation

- [Micronaut Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#httpClient)

