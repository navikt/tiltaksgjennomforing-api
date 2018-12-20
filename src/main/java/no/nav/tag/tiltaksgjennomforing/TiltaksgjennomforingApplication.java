package no.nav.tag.tiltaksgjennomforing;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableOIDCTokenValidation
public class TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TiltaksgjennomforingApplication.class, args);
    }
}
