package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.altinn-varsel")
public class AltinnVarselProperties {
    private URI uri;
    private Integer serviceCode;
    private Integer serviceEdition;
}
