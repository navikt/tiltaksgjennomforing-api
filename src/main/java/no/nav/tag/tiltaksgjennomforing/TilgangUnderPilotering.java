package no.nav.tag.tiltaksgjennomforing;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.pilot")
@Validated
@Data
public class TilgangUnderPilotering {
    private boolean enabled;
    private List<NavIdent> identer = new ArrayList<>();

    public void sjekkTilgang(NavIdent ident) {
        if (enabled && !identer.contains(ident)) {
            throw new TilgangskontrollException("Ident " + ident.asString() + " er ikke lagt til i lista over brukere med tilgang.");
        }
    }
}
