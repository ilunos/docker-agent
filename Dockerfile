FROM openjdk:14-alpine
COPY build/libs/docker-agent-*-all.jar docker-agent.jar
COPY config/templates/application.yml config/application.yml
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "docker-agent.jar"]