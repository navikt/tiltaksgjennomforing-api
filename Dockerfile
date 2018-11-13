FROM maven:3.5.4-jdk-11-slim as builder
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn clean package

FROM navikt/java:11
ENV spring.config.location /var/vault/djssdkløask
COPY --from=builder /target/tiltaksgjennomforing-1.0.0-SNAPSHOT.jar app.jar