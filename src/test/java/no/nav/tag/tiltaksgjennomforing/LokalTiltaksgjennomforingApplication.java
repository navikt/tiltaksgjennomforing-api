package no.nav.tag.tiltaksgjennomforing;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Profile;

@Profile("dev")
public class LokalTiltaksgjennomforingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TiltaksgjennomforingApplication.class);
    }
}
