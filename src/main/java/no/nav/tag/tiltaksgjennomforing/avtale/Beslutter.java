package no.nav.tag.tiltaksgjennomforing.avtale;

import java.util.List;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;

public class Beslutter extends Avtalepart<NavIdent> {

    private TilgangskontrollService tilgangskontrollService;


    public Beslutter(NavIdent identifikator, TilgangskontrollService tilgangskontrollService) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
    }

    public Beslutter(NavIdent identifikator) {
        super(identifikator);
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        tilgangskontrollService.sjekkSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
        return avtale.getDeltakerFnr().equals(getIdentifikator());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
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
