package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.List;
import java.util.UUID;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.exceptions.RessursFinnesIkkeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;

public class Mentor extends Avtalepart<Fnr> {

    public Mentor(Fnr identifikator) {
        super(identifikator);
    }

    //TODO: Test tilgang om erIkke godkjent
    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return avtale.getMentorFnr().equals(getIdentifikator().asString());
    }

    @Override
    public Avtale hentAvtale(AvtaleRepository avtaleRepository, UUID avtaleId) {
        //TODO: kjør en super.hentAvtale()....også sjekk erGodkjentAvMentor
        Avtale avtale = avtaleRepository.findById(avtaleId)
            .orElseThrow(RessursFinnesIkkeException::new);
        sjekkTilgang(avtale);
        if(!avtale.erGodkjentAvMentor()) throw new TilgangskontrollException("Ikke tilgang til avtale");
        return avtale;
    }

    //TODO Test visning av avtale liste med tom gjeldende innhold
    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return avtaleRepository.findAllByMentorFnr(getIdentifikator().asString()).stream()
                .map(avtale -> {
                    if(!avtale.erGodkjentAvMentor()) {
                        //Todo flytte den til egen metode i avtale
                        AvtaleInnhold innhold = AvtaleInnhold.nyttTomtInnhold(avtale.getTiltakstype());
                        innhold.setBedriftNavn(avtale.getGjeldendeInnhold().getBedriftNavn());
                        innhold.setAvtale(avtale);
                        avtale.setGjeldendeInnhold(innhold);
                        avtale.setDeltakerFnr(null);
                        avtale.setVeilederNavIdent(null);
                        return avtale;
                    }
                    return avtale;
                }).toList();
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
