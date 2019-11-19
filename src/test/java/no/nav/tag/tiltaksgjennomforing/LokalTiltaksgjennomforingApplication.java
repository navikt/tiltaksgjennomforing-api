package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class LokalTiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
                .profiles("dev", "wiremock", "testdata")
                .build()
                .run()
        ;
    }
}
