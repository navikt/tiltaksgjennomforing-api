package no.nav.tag.tiltaksgjennomforing.arena.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.arena.aktivitet-acl")
public class AktivitetArenaAclProperties {
    private String url;
}
