package no.nav.tag.tiltaksgjennomforing.autorisasjon.pilottilgang;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.avtale.NavIdent;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
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
