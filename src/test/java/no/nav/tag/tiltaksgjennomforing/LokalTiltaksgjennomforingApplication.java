package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class LokalTiltaksgjennomforingApplication extends TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
            .profiles(Miljø.LOCAL, Miljø.WIREMOCK, Miljø.TESTDATA)
            .build()
            .run();
    }
}
