package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.oppgave")
class OppgaveProperties {
    private URI oppgaveUri;
}
