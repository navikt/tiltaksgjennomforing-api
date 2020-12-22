package no.nav.tag.tiltaksgjennomforing.enhet;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.norg2")
public class Norg2Properties {
    private String url;
}
