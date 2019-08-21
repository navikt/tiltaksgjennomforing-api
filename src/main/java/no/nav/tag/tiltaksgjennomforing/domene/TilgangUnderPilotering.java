package no.nav.tag.tiltaksgjennomforing.domene;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.axsys.AxsysService;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.PilotProperties;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.Collections.disjoint;


@Data
@Component
public class TilgangUnderPilotering {

    private final PilotProperties pilotProperties;
    private final AxsysService axsysService;

    public void sjekkTilgang(NavIdent ident) {
        if (pilotProperties.isEnabled() && !pilotProperties.getIdenter().contains(ident)) {
            List<NavEnhet> enheterInnloggetVeilederHarTilgangTil = axsysService.hentEnheterVeilederHarTilgangTil(ident);
            if (disjoint(pilotProperties.getEnheter(), enheterInnloggetVeilederHarTilgangTil)) {
                throw new TilgangskontrollException("Ident " + ident.asString() + " er ikke lagt til i lista over brukere med tilgang.");
            }
        }
    }

}
