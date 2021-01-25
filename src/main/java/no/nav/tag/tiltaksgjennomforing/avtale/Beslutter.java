package no.nav.tag.tiltaksgjennomforing.avtale;


import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import org.apache.commons.lang3.NotImplementedException;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Beslutter extends Avtalepart<NavIdent> {

    private TilgangskontrollService tilgangskontrollService;
    private AxsysService axsysService;

    public Beslutter(NavIdent identifikator, TilgangskontrollService tilgangskontrollService, AxsysService axsysService) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
        this.axsysService = axsysService;
    }

    public void godkjennTilskuddsperiode(Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.godkjennTilskuddsperiode(getIdentifikator());
    }

    public void avslåTilskuddsperiode(Avtale avtale, EnumSet<Avslagsårsak> avslagsårsaker, String avslagsforklaring) {
        sjekkTilgang(avtale);
        avtale.avslåTilskuddsperiode(getIdentifikator(), avslagsårsaker, avslagsforklaring);
    }

    @Override
    public boolean harTilgang(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        Set<String> navEnheter = hentNavEnheter();
        if (navEnheter.isEmpty()) {
            throw new NavEnhetIkkeFunnetException();
        }
        TilskuddPeriodeStatus status = queryParametre.getTilskuddPeriodeStatus();
        if (status == null) {
            status = TilskuddPeriodeStatus.UBEHANDLET;
        }
        return avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(status.name(), navEnheter);
    }

    private Set<String> hentNavEnheter() {
        return axsysService.hentEnheterNavAnsattHarTilgangTil(getIdentifikator())
                .stream()
                .map(NavEnhet::getVerdi)
                .collect(Collectors.toSet());
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        throw new TilgangskontrollException("Beslutter kan ikke godkjenne avtaler");
    }

    @Override
    public boolean kanEndreAvtale() {
        return false;
    }

    @Override
    public AvtaleStatusDetaljer statusDetaljerForAvtale(Avtale avtale) {
        throw new NotImplementedException();
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
        throw new TilgangskontrollException("Beslutter kan ikke oppheve godkjenninger av avtaler");
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.BESLUTTER;
    }

    @Override
    public void låsOppAvtale(Avtale avtale) {
        throw new TilgangskontrollException("Beslutter kan ikke låse opp avtaler");
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator());
    }
}
