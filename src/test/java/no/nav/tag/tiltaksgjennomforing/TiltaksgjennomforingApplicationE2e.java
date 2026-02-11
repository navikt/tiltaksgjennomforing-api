package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class TiltaksgjennomforingApplicationE2e extends TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
            .profiles(Miljø.LOCAL, Miljø.TESTDATA)
            .build()
            .run();
    }
}
