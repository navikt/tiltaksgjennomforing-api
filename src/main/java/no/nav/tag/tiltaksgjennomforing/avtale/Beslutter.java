package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.List;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;

public class Beslutter extends Avtalepart<NavIdent> {

    public Beslutter(NavIdent identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        //TODO: Skriv om, oppslag i abac for å sjekke om man kan lese deltaker fnr
        return avtale.getDeltakerFnr().equals(getIdentifikator());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        // TODO: Hent nav enhet fra axsys evt. fagområde
        // TODO: sjekke at nav enhet for avtale er samme enhet for beslutter
        // TODO: hente alle godkjente avtaler
        return avtaleRepository.findAllByDeltakerFnr(null);
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {

    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale) {
        return null;
    }

    @Override
    public boolean erGodkjentAvInnloggetBruker(Avtale avtale) {
        return false;
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return false;
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {

    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.BESLUTTER;
    }

    @Override
    public void låsOppAvtale(Avtale avtale) {

    }


    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator());
    }
}
