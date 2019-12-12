package no.nav.tag.tiltaksgjennomforing.autorisasjon;

import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class InnloggetSelvbetjeningBruker extends InnloggetBruker<Fnr> {

    private final List<Organisasjon> organisasjoner;

    public InnloggetSelvbetjeningBruker(Fnr identifikator, List<Organisasjon> organisasjoner) {
        super(identifikator);
        this.organisasjoner = organisasjoner != null ? organisasjoner : new ArrayList<Organisasjon>();
    }

    @Override
    public Avtalepart<Fnr> avtalepart(Avtale avtale) {
        if (avtale.getDeltakerFnr().equals(getIdentifikator())) {
            return new Deltaker(getIdentifikator(), avtale);
        } else if (arbeidsgiverIdentifikatorer().contains(avtale.getBedriftNr())) {
            return new Arbeidsgiver(getIdentifikator(), avtale);
        } else {
            return null;
        }
    }

    @Override
    public boolean harLeseTilgang(Avtale avtale) {
        return avtalepart(avtale) != null;
    }

    @Override
    public boolean harSkriveTilgang(Avtale avtale) {
        return avtalepart(avtale) != null;
    }

    @Override
    public List<Identifikator> identifikatorer() {
        var identifikatorer = new ArrayList<Identifikator>();
        identifikatorer.addAll(super.identifikatorer());
        identifikatorer.addAll(arbeidsgiverIdentifikatorer());
        return identifikatorer;
    }

    @Override
    public boolean erNavAnsatt() {
        return false;
    }

    private List<BedriftNr> arbeidsgiverIdentifikatorer() {
        return organisasjoner.stream().map(Organisasjon::getBedriftNr).collect(Collectors.toList());
    }


}
