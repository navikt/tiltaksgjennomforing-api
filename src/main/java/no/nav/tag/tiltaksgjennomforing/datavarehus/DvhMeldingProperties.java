package no.nav.tag.tiltaksgjennomforing.datavarehus;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.dvh-melding")
public class DvhMeldingProperties {
    private boolean featureEnabled;
    private UUID gruppeTilgang;
}
