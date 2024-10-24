package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Data
@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.veilarboppfolging")
public class VeilarboppfolgingProperties {
    private URI url;
}
