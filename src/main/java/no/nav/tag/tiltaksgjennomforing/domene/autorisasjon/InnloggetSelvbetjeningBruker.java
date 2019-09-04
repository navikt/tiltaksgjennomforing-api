package no.nav.tag.tiltaksgjennomforing.domene.autorisasjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.domene.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        } else if (arbeidsgiverIdentifikatorer().contains(avtale.getBedriftNr())) {
            return new Arbeidsgiver(getIdentifikator(), avtale);
        } else {
            return null;
        }
    }

    @Override
    public List<Identifikator> identifikatorer() {
        var identifikatorer = new ArrayList<Identifikator>();
        identifikatorer.addAll(super.identifikatorer());
        identifikatorer.addAll(arbeidsgiverIdentifikatorer());
        return identifikatorer;
    }

    private List<BedriftNr> arbeidsgiverIdentifikatorer() {
        return organisasjoner.stream().map(Organisasjon::getBedriftNr).collect(Collectors.toList());
    }
}
