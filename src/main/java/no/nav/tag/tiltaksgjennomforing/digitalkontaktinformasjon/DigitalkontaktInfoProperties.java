package no.nav.tag.tiltaksgjennomforing.digitalkontaktinformasjon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * Konfigurasjon for Kontakt- og reservasjonsregister (KRR).
 */
@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.krr")
public class DigitalkontaktInfoProperties {
    private URI uri;
}
