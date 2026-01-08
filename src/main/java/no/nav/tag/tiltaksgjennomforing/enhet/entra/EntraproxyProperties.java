package no.nav.tag.tiltaksgjennomforing.enhet.entra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.entraproxy")
public class EntraproxyProperties {
    private URI uri;
}
