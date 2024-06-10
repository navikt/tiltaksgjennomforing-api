package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Map;

public class LokalTiltaksgjennomforingApplication extends TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
            .properties(Map.of("spring.embedded.kafka.brokers", "localhost:3333"))
            .profiles(Miljø.LOCAL, Miljø.WIREMOCK, Miljø.TESTDATA)
            .build()
            .run();
    }
}
