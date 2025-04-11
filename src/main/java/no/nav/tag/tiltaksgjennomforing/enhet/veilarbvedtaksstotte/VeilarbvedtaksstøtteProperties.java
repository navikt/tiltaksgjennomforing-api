package no.nav.tag.tiltaksgjennomforing.enhet.veilarbvedtaksstotte;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Data
@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.veilarbvedtaksstotte")
public class VeilarbvedtaksstøtteProperties {
    private URI url;
}
