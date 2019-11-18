package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class LokalTiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
                .profiles("dev", "wiremock", "testdata")
                .build()
                .run()
        ;
    }
}
