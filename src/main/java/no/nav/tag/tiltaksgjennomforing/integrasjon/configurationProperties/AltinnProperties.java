package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;

@Component
@Data
@ConfigurationProperties(prefix = "tiltaksgjennomforing.altinn")
public class AltinnProperties {
    private URI altinnUrl;
    private String altinnApiKey;
    private String apiGwApiKey;
}
