package no.nav.tag.tiltaksgjennomforing.avtale;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.NavEnhetIkkeFunnetException;
import no.nav.tag.tiltaksgjennomforing.exceptions.TilgangskontrollException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;

@Slf4j
public class Beslutter extends Avtalepart<NavIdent> implements InternBruker {
    private AxsysService axsysService;
    private Norg2Client norg2Client;
    private TilgangskontrollService tilgangskontrollService;

    private UUID azureOid;

    public Beslutter(NavIdent identifikator, UUID azureOid, TilgangskontrollService tilgangskontrollService, AxsysService axsysService, Norg2Client norg2Client) {
        super(identifikator);
        this.azureOid = azureOid;
        this.tilgangskontrollService = tilgangskontrollService;
        this.axsysService = axsysService;
        this.norg2Client = norg2Client;
    }

    @Deprecated
    public Beslutter(NavIdent identifikator, TilgangskontrollService tilgangskontrollService, AxsysService axsysService, Norg2Client norg2Client) {
        this(identifikator, null, tilgangskontrollService, axsysService, norg2Client);
    }

    public void godkjennTilskuddsperiode(Avtale avtale, String enhet) {
        sjekkTilgang(avtale);
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhetsnavn(enhet);

        if (response == null) {
            throw new FeilkodeException(Feilkode.ENHET_FINNES_IKKE);
        }
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

    private List<Avtale> filtrereVekkAvslattPerioder(List<Avtale> avtaler, Tiltakstype tiltakstype, String sorteringskolonne) {
        return avtaler.stream()
                .filter(avtale -> avtale.gjeldendeTilskuddsperiode().getStatus() != TilskuddPeriodeStatus.AVSLÅTT &&
                        (avtale.getTiltakstype() == tiltakstype || tiltakstype == null))
                .sorted(AvtaleSorterer.comparatorForAvtale(sorteringskolonne))
                .collect(Collectors.toList());
    }

    private List<Avtale> filtrereOgFinAvslattPerioder(List<Avtale> avtaler, Tiltakstype tiltakstype, String sorteringskolonne) {
        return avtaler.stream()
                .filter(avtale -> avtale.gjeldendeTilskuddsperiode().getStatus() == TilskuddPeriodeStatus.AVSLÅTT &&
                        (avtale.getTiltakstype() == tiltakstype || tiltakstype == null))
                .sorted(AvtaleSorterer.comparatorForAvtale(sorteringskolonne))
                .collect(Collectors.toList());
    }


    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(this, avtale.getDeltakerFnr());
    }

    public boolean harTilgangTilFnr(Fnr fnr) {
        return tilgangskontrollService.harSkrivetilgangTilKandidat(this, fnr);
    }

    public Set<Fnr> harTilgangTilFnr(Set<Fnr> fnrSet) {
        return tilgangskontrollService.skriveTilganger(this, fnrSet).entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    List<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtalePredicate queryParametre) {
        return avtaleRepository.findAllByAvtaleNr(queryParametre.getAvtaleNr());
    }

    private Integer getPlussdato() {
        return ((int) ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.now().plusMonths(3)));
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
        Tiltakstype tiltakstype = queryParametre.getTiltakstype();
        Integer plussDato = getPlussdato();

        if (status == null) {
            status = TilskuddPeriodeStatus.UBEHANDLET;
        }

        return switch (status) {
            case GODKJENT -> filtrereVekkAvslattPerioder(
                    avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjent(
                            status.name(),
                            navEnheter,
                            plussDato),
                    tiltakstype,
                    sorteringskolonne);
            case AVSLÅTT -> filtrereOgFinAvslattPerioder(
                    avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterAvslatt(
                            status.name(),
                            navEnheter,
                            plussDato),
                    tiltakstype,
                    sorteringskolonne);
            default -> filtrereVekkAvslattPerioder(
                    avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandlet(
                            status.name(),
                            navEnheter,
                            plussDato),
                    tiltakstype,
                    sorteringskolonne);
        };
    }

    List<AvtaleMinimal> finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterListe(
            AvtaleRepository avtaleRepository,
            AvtalePredicate queryParametre,
            String sorteringskolonne) {

        Set<String> navEnheter = hentNavEnheter();
        if (navEnheter.isEmpty()) {
            throw new NavEnhetIkkeFunnetException();
        }

        TilskuddPeriodeStatus status = queryParametre.getTilskuddPeriodeStatus();
        Tiltakstype tiltakstype = queryParametre.getTiltakstype();
        BedriftNr bedriftNr = queryParametre.getBedriftNr();
        Integer plussDato = getPlussdato();

        if (status == null) {
            status = TilskuddPeriodeStatus.UBEHANDLET;
        }

        Set<String> tiltakstyper = new HashSet<>();
        if(tiltakstype != null) {
            tiltakstyper.add(tiltakstype.name());
        } else {
            tiltakstyper.add(Tiltakstype.SOMMERJOBB.name());
            tiltakstyper.add(Tiltakstype.VARIG_LONNSTILSKUDD.name());
            tiltakstyper.add(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD.name());
        }
        List<AvtaleMinimal> resultat = new ArrayList<>();
        if(status == TilskuddPeriodeStatus.GODKJENT || status == TilskuddPeriodeStatus.AVSLÅTT) {
            resultat = avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterGodkjentEllerAvslåttMinimal(
                    status.name(),
                    navEnheter,
                    tiltakstyper);
        } else {
            resultat = avtaleRepository.finnGodkjenteAvtalerMedTilskuddsperiodestatusOgNavEnheterUbehandletMinimal(
                    status.name(),
                    navEnheter,
                    plussDato,
                    tiltakstyper);
        }

        if(bedriftNr != null) {
            resultat = resultat.stream().filter(avtaleMinimal -> {
                if(avtaleMinimal.getBedriftNr().equals(bedriftNr.asString())) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }

        return resultat;
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

    @Override
    public UUID getAzureOid() {
        return azureOid;
    }

    @Override
    public NavIdent getNavIdent() {
        return getIdentifikator();
    }
}
