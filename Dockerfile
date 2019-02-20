FROM navikt/java:11
COPY import-vault-token.sh /init-scripts
COPY /target/tiltaksgjennomforing-api-1.0.0-SNAPSHOT.jar app.jar