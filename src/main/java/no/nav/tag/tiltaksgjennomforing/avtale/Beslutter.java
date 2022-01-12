package no.nav.tag.tiltaksgjennomforing.avtale;


import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;

public class Beslutter extends Avtalepart<NavIdent> {

    private TilgangskontrollService tilgangskontrollService;
    private AxsysService axsysService;

    public Beslutter(NavIdent identifikator, TilgangskontrollService tilgangskontrollService, AxsysService axsysService) {
        super(identifikator);
        this.tilgangskontrollService = tilgangskontrollService;
        this.axsysService = axsysService;
    }

    public void godkjennTilskuddsperiode(Avtale avtale, String enhet) {
        sjekkTilgang(avtale);
        avtale.godkjennTilskuddsperiode(getIdentifikator(), enhet);
    }

    public void avslåTilskuddsperiode(Avtale avtale, EnumSet<Avslagsårsak> avslagsårsaker, String avslagsforklaring) {
        sjekkTilgang(avtale);
        avtale.avslåTilskuddsperiode(getIdentifikator(), avslagsårsaker, avslagsforklaring);
    }

    public void setOmAvtalenKanEtterregistreres(Avtale avtale){
        sjekkTilgang(avtale);
        avtale.togglegodkjennEtterregistrering(getIdentifikator());
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return avtaleRepository.findAllByAvtaleNr(queryParametre.getAvtaleNr());
    }

    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
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
    public InnloggetBruker innloggetBruker() {
        return new InnloggetBeslutter(getIdentifikator());
    }
}
