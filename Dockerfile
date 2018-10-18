FROM maven:3.5.4-jdk-11 as builder
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
RUN mvn package

FROM navikt/java:11
COPY --from=builder /target/tiltaksgjennomforing-1.0.0-SNAPSHOT.jar app.jar