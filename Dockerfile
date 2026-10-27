FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27
COPY /target/tiltaksgjennomforing-api-1.0.0-SNAPSHOT.jar app.jar
ENV TZ="Europe/Oslo"
EXPOSE 8080
CMD ["-jar", "app.jar"]
