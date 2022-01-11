package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

import java.util.ArrayList;
import java.util.List;

public class Deltaker extends Avtalepart<Fnr> {

    public Deltaker(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return avtale.getDeltakerFnr().equals(getIdentifikator());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return new ArrayList<Avtale>(avtaleRepository.findAllByDeltakerFnr(getIdentifikator()));
    }

    @Override
    public void godkjennForAvtalepart(Avtale avtale) {
        avtale.godkjennForDeltaker(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return avtale.erGodkjentAvDeltaker();
    }


    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return false;
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        throw new TilgangskontrollException("Deltaker kan ikke oppheve godkjenninger");
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.DELTAKER;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetDeltaker(getIdentifikator());
    }
}
