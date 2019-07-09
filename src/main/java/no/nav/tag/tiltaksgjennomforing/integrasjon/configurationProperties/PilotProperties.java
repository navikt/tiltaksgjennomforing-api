package no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties;

import lombok.Data;
import no.nav.tag.tiltaksgjennomforing.domene.NavIdent;
import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.integrasjon.axsys.AxsysService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
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

            for (String enhet : enheter) {
                List<NavIdent> kontorIdenter = axsysService.hentIdenter(enhet);
                identer.addAll(kontorIdenter);
                if (identer.contains(ident)) {
                    break;
                }
            }


        if (enabled && !identer.contains(ident)) {
            throw new TilgangskontrollException("Ident " + ident.asString() + " er ikke lagt til i lista over brukere med tilgang.");
        }
    }
}