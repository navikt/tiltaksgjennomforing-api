package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Data
@Configuration
@ConfigurationProperties(prefix = "tiltaksgjennomforing.ao-oppfolgingskontor")
public class AoOppfolgingskontorProperties {
    private URI url;
}
