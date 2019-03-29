package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.domene.Avtale;
import no.nav.tag.tiltaksgjennomforing.domene.Fnr;
import no.nav.tag.tiltaksgjennomforing.domene.Organisasjon;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class InnloggetSelvbetjeningBruker extends InnloggetBruker<Fnr> {
    private List<Organisasjon> organisasjoner = new ArrayList<>();

    public InnloggetSelvbetjeningBruker(Fnr identifikator) {
        super(identifikator);
    }

    public boolean harTilgang(Avtale avtale) {
        if (avtale.getDeltakerFnr().equals(getIdentifikator())
                || avtale.getArbeidsgiverFnr().equals(getIdentifikator())
                || organisasjoner.stream().anyMatch(o -> avtale.getBedriftNr().equals(o.getBedriftNr()))) {
            return true;
        }
        return false;
    }
}
