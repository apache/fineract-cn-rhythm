FROM openjdk:8-jdk-alpine

ARG rhythm_port=2022

ENV server.max-http-header-size=16384 \
    cassandra.clusterName="Test Cluster" \
    rhythm.beatCheckRate=10 \
    rhythm.user=imhotep \
    server.port=$rhythm_port

WORKDIR /tmp
COPY rhythm-service-boot-0.1.0-BUILD-SNAPSHOT.jar .

CMD ["java", "-jar", "rhythm-service-boot-0.1.0-BUILD-SNAPSHOT.jar"]
