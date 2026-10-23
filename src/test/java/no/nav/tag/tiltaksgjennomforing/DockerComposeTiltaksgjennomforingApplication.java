package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class DockerComposeTiltaksgjennomforingApplication extends TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
            .profiles(Miljø.LOCAL, Miljø.WIREMOCK, Miljø.TESTDATA, Miljø.DOCKER_COMPOSE)
            .build()
            .run();
    }
}
