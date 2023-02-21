package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
public class LokalTiltaksgjennomforingApplication extends TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
            .profiles(Miljø.LOCAL, "wiremock", "testdata")
            .build()
            .run();
    }
}
