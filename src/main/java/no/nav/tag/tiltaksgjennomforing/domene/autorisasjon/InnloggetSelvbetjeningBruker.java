package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.domene.*;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class InnloggetSelvbetjeningBruker extends InnloggetBruker<Fnr> {
    private List<Organisasjon> organisasjoner = new ArrayList<>();

    public InnloggetSelvbetjeningBruker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public Avtalepart avtalepart(Avtale avtale) {
        if (avtale.getDeltakerFnr().equals(getIdentifikator())) {
            return new Deltaker(getIdentifikator(), avtale);
        } else if (avtale.getArbeidsgiverFnr().equals(getIdentifikator())
                || organisasjoner.stream().anyMatch(o -> o.getBedriftNr().equals(avtale.getBedriftNr()))) {
            return new Arbeidsgiver(getIdentifikator(), avtale);
        } else {
            return null;
        }
    }
}
