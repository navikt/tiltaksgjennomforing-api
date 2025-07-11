package no.nav.tag.tiltaksgjennomforing;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = {
        "org.springdoc",
        "springfox.documentation.swagger.web.ApiResourceController",
        "no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleController",
        "org.springframework"
})
@EnableConfigurationProperties
@EnableJpaRepositories
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@OpenAPIDefinition
@EnableRetry
public class TiltaksgjennomforingApplication {
    public static void main(String[] args) {
        String clusterName = System.getenv("MILJO");
        if (clusterName == null) {
            System.out.println("Kan ikke startes uten miljøvariabel MILJO. Lokalt kan LokalTiltaksgjennomforingApplication kjøres.");
            System.exit(1);
        }
        new SpringApplicationBuilder(TiltaksgjennomforingApplication.class)
                .profiles(clusterName)
                .build()
                .run();
    }
}
