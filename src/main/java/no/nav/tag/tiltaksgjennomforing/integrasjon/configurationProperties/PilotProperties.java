package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.axsys.AxsysService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "tiltaksgjennomforing.pilot")
public class PilotProperties {
    private boolean enabled;
    private List<String> enheter = new ArrayList<>();
    private List<NavIdent> identer = new ArrayList<>();
    private final AxsysService axsysService;

    public void sjekkTilgang(NavIdent ident) {
        if (enabled && !identer.contains(ident)) {
            List<NavEnhet> enheterInnloggetVeilederHarTilgangTil = axsysService.hentEnheterVeilederHarTilgangTil(ident);
            if (!enheter.contains(enheterInnloggetVeilederHarTilgangTil)) {
                throw new TilgangskontrollException("Ident " + ident.asString() + " er ikke lagt til i lista over brukere med tilgang.");
            }

        }
    }
}