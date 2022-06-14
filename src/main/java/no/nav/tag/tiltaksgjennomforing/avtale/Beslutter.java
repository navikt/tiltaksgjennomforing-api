package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;

@Slf4j
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

    public void setOmAvtalenKanEtterregistreres(Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.togglegodkjennEtterregistrering(getIdentifikator());
    }

    private List<Avtale> filtrereUbehandletPerioder(List<Avtale> avtaler, String sorteringskolonne) {
        return avtaler.stream().
                filter(avtale -> avtale.gjeldendeTilskuddsperiode().getStatus() != TilskuddPeriodeStatus.AVSLÅTT &&
                        avtale.getTilskuddPeriode().stream().anyMatch(tilskuddPeriode ->{
                            int val = LocalDate.now().compareTo(tilskuddPeriode.getStartDato().minusMonths(3));
                            return val >= 0 && tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET;
                        }))
                .sorted(AvtaleSorterer.comparatorForAvtale(sorteringskolonne)).collect(Collectors.toList());
    }

    private List<Avtale> filtrereGodkjentPerioder(List<Avtale> avtaler, String sorteringskolonne) {
        return avtaler.stream()
                .filter(avtale -> avtale.gjeldendeTilskuddsperiode().getStatus() != TilskuddPeriodeStatus.AVSLÅTT &&
                        avtale.getTilskuddPeriode().stream().allMatch(tilskuddPeriode ->{
                            int val = LocalDate.now().compareTo(tilskuddPeriode.getStartDato().minusMonths(3));
                            return !(val >= 0 && tilskuddPeriode.getStatus() == TilskuddPeriodeStatus.UBEHANDLET);
                        }))
                .sorted(AvtaleSorterer.comparatorForAvtale(sorteringskolonne)).collect(Collectors.toList());
    }

    private List<Avtale> filtrereAvslattPerioder(List<Avtale> avtaler, String sorteringskolonne) {
        return avtaler.stream()
                .filter(avtale -> avtale.gjeldendeTilskuddsperiode().getStatus() == TilskuddPeriodeStatus.AVSLÅTT)
                .sorted(AvtaleSorterer.comparatorForAvtale(sorteringskolonne))
                .collect(Collectors.toList());
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(getIdentifikator(), avtale.getDeltakerFnr());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return avtaleRepository.findAllByAvtaleNr(queryParametre.getAvtaleNr());
    }

    List<Avtale> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheter(
            AvtaleRepository avtaleRepository,
            AvtalePredicate queryParametre,
            String sorteringskolonne) {

        Set<String> navEnheter = hentNavEnheter();
        if (navEnheter.isEmpty()) {
            throw new NavEnhetIkkeFunnetException();
        }

        TilskuddPeriodeStatus status = queryParametre.getTilskuddPeriodeStatus();
        Tiltakstype tiltakstype = null;

        if(queryParametre.getTiltakstype() != null) {
            tiltakstype = queryParametre.getTiltakstype();
        }
        if (status == null) {
            status = TilskuddPeriodeStatus.UBEHANDLET;
        }

       return switch (status) {
           case GODKJENT -> filtrereGodkjentPerioder(
                   avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjent(
                   status.name(),
                   navEnheter,
                   tiltakstype), sorteringskolonne);
            case AVSLÅTT -> filtrereAvslattPerioder(avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterAvslatt(
                            status.name(),
                            navEnheter,
                            tiltakstype), sorteringskolonne);
            default -> filtrereUbehandletPerioder(
                    avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandlet(
                            status.name(),
                            navEnheter), sorteringskolonne);
        };
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
