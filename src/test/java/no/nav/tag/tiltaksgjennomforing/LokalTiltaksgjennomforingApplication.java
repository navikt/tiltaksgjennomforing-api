package no.nav.tag.tiltaksgjennomforing;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.builder.SpringApplicationBuilder;

@OpenAPIDefinition
public class LokalTiltaksgjennomforingApplication extends TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
            .profiles(Miljø.LOCAL, "wiremock", "testdata")
            .build()
            .run();
    }
}
