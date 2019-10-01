package no.nav.tag.tiltaksgjennomforing.varsel.altinnvarsel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.altinn-varsel")
public class AltinnVarselProperties {
    private URI uri;
    private String systemBruker;
    private String systemPassord;
}
