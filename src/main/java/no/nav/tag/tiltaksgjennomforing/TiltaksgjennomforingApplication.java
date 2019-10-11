package no.nav.tag.tiltaksgjennomforing;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.SjekkAktiveProfilerInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = {
        "springfox.documentation.swagger.web.ApiResourceController",
        "org.springframework"
})
@EnableConfigurationProperties
@EnableJpaRepositories
public class TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
                .initializers(new SjekkAktiveProfilerInitializer())
                .build();
        application.run(args);
    }
}
