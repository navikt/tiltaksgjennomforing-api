package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.pilot")
public class PilotProperties {

    private boolean enabled;
    private List<NavEnhet> enheter = new ArrayList<>();
    private List<NavIdent> identer = new ArrayList<>();

}