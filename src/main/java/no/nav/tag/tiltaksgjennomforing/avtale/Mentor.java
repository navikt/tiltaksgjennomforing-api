package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.ArrayList;
import java.util.List;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import org.apache.commons.lang3.NotImplementedException;

public class Mentor extends Avtalepart<Fnr> {

    public Mentor(Fnr identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return avtale.getMentorFnr().equals(getIdentifikator().asString());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return new ArrayList<Avtale>(avtaleRepository.findAllByMentorFnr(getIdentifikator().asString()));
    }

    @Override
    public void godkjennForAvtalepart(Avtale avtale) {
        avtale.godkjennForMentor(getIdentifikator());
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return avtale.getMentorFnr().equals(getIdentifikator().asString()) && avtale.erGodkjentAvMentor();
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
        return Avtalerolle.MENTOR;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetMentor(getIdentifikator());
    }
}