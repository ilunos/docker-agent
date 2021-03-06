FROM gradle:jdk14 as build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:14-alpine
COPY --from=build /home/gradle/src/build/libs/docker-agent-*-all.jar docker-agent.jar
EXPOSE 8885
CMD ["java", "-Dcom.sun.management.jmxremote", "-Xmx128m", "-jar", "docker-agent.jar"]