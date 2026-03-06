FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

COPY import-vault-token.sh /init-scripts
COPY /target/tiltaksgjennomforing-api-1.0.0-SNAPSHOT.jar app.jar

ENV TZ="Europe/Oslo"
EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar"]
