package no.nav.tag.tiltaksgjennomforing.enhet;

import java.net.URI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.veilarbarena")
public class VeilarbArenaProperties {

    private URI url;
}
