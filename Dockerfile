FROM openjdk:14-alpine
COPY build/libs/docker-agent-*-all.jar docker-agent.jar
EXPOSE 8080
CMD ["java", "-Dcom.sun.management.jmxremote", "-Ddocker.container.id=${head -1 /proc/self/cgroup|cut -d/ -f3}", "-Xmx128m", "-jar", "docker-agent.jar"]