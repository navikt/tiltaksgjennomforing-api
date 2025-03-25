package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.ad-grupper")
public class AdGruppeProperties {
    private UUID beslutter;
    private UUID fortroligAdresse;
    private UUID strengtFortroligAdresse;
}
