package no.nav.tag.tiltaksgjennomforing;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = {
        "springfox.documentation.swagger.web.ApiResourceController",
        "org.springframework"
})
@EnableConfigurationProperties
@EnableJpaRepositories
public class TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        String clusterName = System.getenv("NAIS_CLUSTER_NAME");
        if (clusterName == null) {
            System.out.println("Kan ikke startes uten miljøvariabel NAIS_CLUSTER_NAME. Lokalt kan LokalTiltaksgjennomforingApplication kjøres.");
            System.exit(1);
        }
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
                .profiles(clusterName)
                .build()
                .run();
    }
}
