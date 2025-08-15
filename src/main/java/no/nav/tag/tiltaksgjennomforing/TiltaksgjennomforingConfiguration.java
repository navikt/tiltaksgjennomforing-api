package no.nav.tag.tiltaksgjennomforing;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Slf4j
@Configuration
class TiltaksgjennomforingConfiguration {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        if (environment.matchesProfiles(Miljø.PROD_FSS)) {
            log.info("Syntetiske fødselsnumre er skrudd av");
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
        } else {
            log.info("Syntetiske fødselsnumre er skrudd på");
            FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        }
    }

    @Bean
    public RestTemplate noAuthRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

}
