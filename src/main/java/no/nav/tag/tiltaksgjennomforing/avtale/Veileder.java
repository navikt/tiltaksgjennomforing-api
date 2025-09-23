package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.AdGruppeTilganger;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Kode6SperretForOpprettelseOgEndringException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.logging.TeamLogs;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class Veileder extends Avtalepart<NavIdent> implements InternBruker {
    private static final TeamLogs teamLogs = TeamLogs.getLogger(log);

    private final TilgangskontrollService tilgangskontrollService;
    private final PersondataService persondataService;
    private final AdGruppeTilganger adGruppeTilganger;
    private final Norg2Client norg2Client;
    private final Set<NavEnhet> navEnheter;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final UUID azureOid;
    private final FeatureToggleService featureToggleService;
    private final EregService eregService;

    public Veileder(
        NavIdent identifikator,
        UUID azureOid,
        TilgangskontrollService tilgangskontrollService,
        PersondataService persondataService,
        Norg2Client norg2Client,
        Set<NavEnhet> navEnheter,
        AdGruppeTilganger adGruppeTilganger,
        VeilarboppfolgingService veilarboppfolgingService,
        FeatureToggleService featureToggleService,
        EregService eregService
    ) {

        super(identifikator);
        this.azureOid = azureOid;
        this.tilgangskontrollService = tilgangskontrollService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.navEnheter = navEnheter;
        this.adGruppeTilganger = adGruppeTilganger;
        this.veilarboppfolgingService = veilarboppfolgingService;
        this.featureToggleService = featureToggleService;
        this.eregService = eregService;
    }

    @Override
    public Page<BegrensetAvtale> hentBegrensedeAvtalerMedLesetilgang(
        AvtaleRepository avtaleRepository,
        AvtaleQueryParameter queryParametre,
        Pageable pageable
    ) {
        Page<Avtale> avtaler = hentAvtalerMedLesetilgang(avtaleRepository, queryParametre, pageable);

        boolean isSkalHenteDiskresjonskoder = adGruppeTilganger.fortroligAdresse() || adGruppeTilganger.strengtFortroligAdresse();
        Map<Fnr, Diskresjonskode> diskresjon = isSkalHenteDiskresjonskoder ? persondataService.hentDiskresjonskoder(
            avtaler.getContent().stream().map(Avtale::getDeltakerFnr).collect(Collectors.toSet())
        ) : Map.of();

        List<BegrensetAvtale> begrensedeAvtaler = avtaler
            .getContent()
            .stream()
            .map(avtale -> BegrensetAvtale.fraAvtaleMedDiskresjonskode(avtale, diskresjon.get(avtale.getDeltakerFnr())))
            .toList();

        return new PageImpl<>(begrensedeAvtaler, pageable, avtaler.getTotalElements());
    }

    @Override
    public Tilgang harTilgangTilAvtale(Avtale avtale) {
        teamLogs.info(
            "Sjekker tilgang for veileder {} til deltaker {} på avtale {}",
            getIdentifikator().asString(),
            avtale.getDeltakerFnr().asString(),
            avtale.getId()
        );
        Tilgang tilgang = tilgangskontrollService.hentSkrivetilgang(this, avtale.getDeltakerFnr());
        boolean harTilgang = tilgang != null && tilgang.erTillat();
        if (!harTilgang) {
            log.info("Har ikke tilgang til avtalenr {}, id: {}", avtale.getAvtaleNr(), avtale.getId());
        }
        return tilgang;
    }

    @Override
    Predicate<Avtale> harTilgangTilAvtale(List<Avtale> avtaler) {
        Map<Fnr, Boolean> map = tilgangskontrollService.harSkrivetilgangTilAvtaler(this, avtaler);
        return avtale -> {
            boolean resultat = map.getOrDefault(avtale.getDeltakerFnr(), false);
            if (!resultat) {
                log.info("Har ikke tilgang til avtalenr {}, id: {}", avtale.getAvtaleNr(), avtale.getId());
            }
            return resultat;
        };
    }

    @Override
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(
        AvtaleRepository avtaleRepository,
        AvtaleQueryParameter queryParametre,
        Pageable pageable
    ) {
        NavIdent veilederNavIdent = queryParametre.harFilterPaEnEntitet() ? queryParametre.getVeilederNavIdent() : getIdentifikator();

        return avtaleRepository.sokEtterAvtale(
            veilederNavIdent,
            queryParametre.getAvtaleNr(),
            queryParametre.getDeltakerFnr(),
            queryParametre.getBedriftNr(),
            queryParametre.getNavEnhet(),
            queryParametre.getTiltakstype(),
            queryParametre.getStatus(),
            queryParametre.erUfordelt(),
            pageable
        );
    }

    public void annullerAvtale(String annullerGrunn, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.annuller(this, annullerGrunn);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        sjekkOmBedriftErGyldigOgOppdaterNavn(avtale);
        avtale.godkjennForVeileder(getIdentifikator(), featureToggleService.isEnabled(FeatureToggle.MENTOR_TILSKUDD));
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return true;
    }

    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn, Avtale avtale) {
        super.sjekkTilgang(avtale);
        sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        sjekkOmBedriftErGyldigOgOppdaterNavn(avtale);
        avtale.godkjennForVeilederOgDeltaker(getIdentifikator(), paVegneAvGrunn);
    }

    public void godkjennForVeilederOgArbeidsgiver(
        GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvArbeidsgiverGrunn,
        Avtale avtale
    ) {
        super.sjekkTilgang(avtale);
        sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        sjekkOmBedriftErGyldigOgOppdaterNavn(avtale);
        avtale.godkjennForVeilederOgArbeidsgiver(getIdentifikator(), paVegneAvArbeidsgiverGrunn);
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(
        GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn,
        Avtale avtale
    ) {
        super.sjekkTilgang(avtale);
        sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        sjekkOmBedriftErGyldigOgOppdaterNavn(avtale);
        avtale.godkjennForVeilederOgDeltakerOgArbeidsgiver(getIdentifikator(), paVegneAvDeltakerOgArbeidsgiverGrunn);
    }

    @Override
    void opphevGodkjenningerSomAvtalepart(Avtale avtale) {
        avtale.opphevGodkjenningerSomVeileder();
    }

    @Override
    protected Avtalerolle rolle() {
        return Avtalerolle.VEILEDER;
    }

    @Override
    public InnloggetBruker innloggetBruker() {
        return new InnloggetVeileder(getIdentifikator(), navEnheter, adGruppeTilganger.beslutter());
    }

    public void delAvtaleMedAvtalepart(Avtalerolle avtalerolle, Avtale avtale) {
        sjekkTilgang(avtale);
        avtale.delMedAvtalepart(avtalerolle);
    }

    public void overtaAvtale(Avtale avtale) {
        super.sjekkTilgang(avtale);
        this.hentOppfølgingFraArena(avtale, veilarboppfolgingService);
        if (this.getIdentifikator().equals(avtale.getVeilederNavIdent())) {
            throw new ErAlleredeVeilederException();
        }
        avtale.overtaAvtale(this.getIdentifikator());
    }

    @Override
    public void endreAvtale(
        EndreAvtale endreAvtale,
        Avtale avtale
    ) {
        super.endreAvtale(
            endreAvtale,
            avtale,
            () -> oppdatereEnheterVedEndreAvtale(avtale), featureToggleService.isEnabled(FeatureToggle.MENTOR_TILSKUDD)
        );
    }

    protected void oppdatereEnheterVedEndreAvtale(Avtale avtale) {
        this.oppdatereOppfølgingStatusVedEndreAvtale(avtale);
        this.oppdatereGeoEnhetVedEndreAvtale(avtale);
        this.oppdatereOppfølgingEnhetsnavnVedEndreAvtale(avtale);

    }

    private void oppdatereGeoEnhetVedEndreAvtale(Avtale avtale) {
        Norg2GeoResponse norg2GeoResponse = persondataService.hentGeografiskTilknytning(avtale.getDeltakerFnr())
            .map(norg2Client::hentGeoEnhetFraCacheEllerNorg2)
            .orElse(null);
        if (norg2GeoResponse == null) {
            return;
        }
        avtale.setEnhetGeografisk(norg2GeoResponse.getEnhetNr());
        avtale.setEnhetsnavnGeografisk(norg2GeoResponse.getNavn());
    }

    private void oppdatereOppfølgingEnhetsnavnVedEndreAvtale(Avtale avtale) {
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhetFraCacheNorg2(
            avtale.getEnhetOppfolging()
        );
        if (response == null) {
            return;
        }
        avtale.setEnhetsnavnOppfolging(response.getNavn());
    }

    public void oppdatereOppfølgingStatusVedEndreAvtale(Avtale avtale) {
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOppfolgingsstatus(
            avtale.getDeltakerFnr().asString()
        );
        if (oppfølgingsstatus == null) {
            return;
        }
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
    }

    private void sjekkOgBlokkereKode6(Fnr fnr) {
        if (!featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)) {
            return;
        }
        if (!persondataService.hentDiskresjonskode(fnr).erKode6()) {
            return;
        }
        throw new Kode6SperretForOpprettelseOgEndringException();
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        this.sjekkTilgangskontroll(opprettAvtale.getDeltakerFnr());
        this.sjekkOgBlokkereKode6(opprettAvtale.getDeltakerFnr());
        Avtale avtale = Avtale.opprett(opprettAvtale, Avtaleopphav.VEILEDER, getIdentifikator());
        avtale.leggTilDeltakerNavn(persondataService.hentNavn(avtale.getDeltakerFnr()));
        this.sjekkOmBedriftErGyldigOgOppdaterNavn(avtale);
        this.leggTilEnheter(avtale);
        return avtale;
    }

    private void sjekkTilgangskontroll(Fnr deltakerFnr) {
        if (!tilgangskontrollService.harSkrivetilgangTilKandidat(this, deltakerFnr)) {
            throw new IkkeTilgangTilDeltakerException(deltakerFnr);
        }
    }

    private void leggTilEnheter(Avtale avtale) {
        super.hentGeoEnhetFraNorg2(avtale, norg2Client, persondataService);
        this.hentOppfølgingFraArena(avtale, veilarboppfolgingService);
        this.hentOppfolgingEnhetsnavnFraNorg2(avtale, norg2Client);
    }

    public void hentOppfolgingEnhetsnavnFraNorg2(Avtale avtale, Norg2Client norg2Client) {
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhet(avtale.getEnhetOppfolging());
        if (response == null) {
            return;
        }
        avtale.setEnhetsnavnOppfolging(response.getNavn());
    }

    void hentOppfølgingFraArena(
        Avtale avtale,
        VeilarboppfolgingService veilarboppfolgingService
    ) {
        if (avtale.harOppfølgingsStatus()) {
            return;
        }
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale);
        if (oppfølgingsstatus == null) {
            return;
        }
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
        this.settLonntilskuddProsentsats(avtale);
    }

    public void sjekkOgOppdaterOppfølgningsstatusForAvtale(Avtale avtale) {
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale);
        if (oppfølgingsstatus == null) {
            return;
        }
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
    }

    private void sjekkOmBedriftErGyldigOgOppdaterNavn(Avtale avtale) {
        Optional.ofNullable(eregService.hentVirksomhet(avtale.getBedriftNr()))
            .map(Organisasjon::getBedriftNavn)
            .ifPresent(avtale::leggTilBedriftNavn);
    }

    private void settOppfølgingsStatus(Avtale avtale, Oppfølgingsstatus oppfølgingsstatus) {
        avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
        avtale.setKvalifiseringsgruppe(oppfølgingsstatus.getKvalifiseringsgruppe());
        avtale.setFormidlingsgruppe(oppfølgingsstatus.getFormidlingsgruppe());
    }

    public void endreMål(EndreMål endreMål, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreMål(endreMål, getIdentifikator());
    }

    public void endreInkluderingstilskudd(EndreInkluderingstilskudd endreInkluderingstilskudd, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, getIdentifikator());
    }

    public void forkortAvtale(Avtale avtale, LocalDate sluttDato, ForkortetGrunn forkortetGrunn) {
        super.sjekkTilgang(avtale);
        avtale.forkortAvtale(sluttDato, forkortetGrunn, getIdentifikator());
    }

    public void forlengAvtale(LocalDate sluttDato, Avtale avtale) {
        super.sjekkTilgang(avtale);
        sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        avtale.forlengAvtale(sluttDato, getIdentifikator());
    }

    public void endreKidOgKontonummer(EndreKidOgKontonummer endreKidOgKontonummer, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreKidOgKontonummer(endreKidOgKontonummer, getIdentifikator());
    }

    protected void oppdaterOppfølgingOgGeoEnhetEtterForespørsel(Avtale avtale) {
        sjekkTilgang(avtale);
        // Geo enhet
        super.hentGeoEnhetFraNorg2(avtale, norg2Client, persondataService);
        // Oppfølgingsenhet
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOppfolgingsstatus(avtale.getDeltakerFnr()
            .asString());
        avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
        this.hentOppfolgingEnhetsnavnFraNorg2(avtale, norg2Client);
    }

    public void endreStillingbeskrivelse(EndreStillingsbeskrivelse endreStillingsbeskrivelse, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreStillingsbeskrivelse(endreStillingsbeskrivelse, getIdentifikator());
    }

    public void endreOppfølgingOgTilrettelegging(
        EndreOppfølgingOgTilrettelegging endreOppfølgingOgTilrettelegging,
        Avtale avtale
    ) {
        super.sjekkTilgang(avtale);
        avtale.endreOppfølgingOgTilrettelegging(endreOppfølgingOgTilrettelegging, getIdentifikator());
    }

    public void endreOmMentor(EndreOmMentor endreOmMentor, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreOmMentor(endreOmMentor, getIdentifikator());
    }

    public void endreKontaktinfo(EndreKontaktInformasjon endreKontaktInformasjon, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreKontaktInformasjon(endreKontaktInformasjon, getIdentifikator());
    }

    public void oppfolgingAvAvtale(Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.godkjennOppfolgingAvAvtale(getIdentifikator());
    }

    public void endreTilskuddsberegning(EndreTilskuddsberegning endreTilskuddsberegning, Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, getIdentifikator());
    }

    public void reaktiverTilskuddsperiodeOgsendTilbakeTilBeslutter(Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.reaktiverTilskuddsperiodeOgSendTilbakeTilBeslutter();
    }

    protected void oppdatereKostnadssted(Avtale avtale, Norg2Client norg2Client, String enhet) {
        sjekkTilgang(avtale);
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhet(enhet);

        if (response == null) {
            throw new FeilkodeException(Feilkode.ENHET_FINNES_IKKE);
        }
        NyttKostnadssted nyttKostnadssted = new NyttKostnadssted(enhet, response.getNavn());
        TreeSet<TilskuddPeriode> tilskuddPerioder = avtale.finnTilskuddsperioderIkkeLukketForEndring();

        if (tilskuddPerioder == null) {
            throw new FeilkodeException(Feilkode.TILSKUDDSPERIODE_ER_IKKE_SATT);
        }
        avtale.oppdatereKostnadsstedForTilskuddsperioder(nyttKostnadssted);
    }

    private LocalDate settStartDato(LocalDate startdato) {
        return startdato != null ? startdato : Now.localDate();
    }

    protected List<AlleredeRegistrertAvtale> hentAvtaleDeltakerAlleredeErRegistrertPaa(
        Fnr deltakerFnr,
        Tiltakstype tiltakstype,
        UUID avtaleId,
        LocalDate startDato,
        LocalDate sluttDato,
        AvtaleRepository avtaleRepository
    ) {
        if (avtaleId != null && startDato != null && sluttDato != null) {
            return AlleredeRegistrertAvtale.filtrerAvtaleDeltakerAlleredeErRegistrertPaa(
                avtaleRepository.finnAvtalerSomOverlapperForDeltakerVedGodkjenningAvAvtale(
                    deltakerFnr.asString(),
                    avtaleId.toString(),
                    Date.valueOf(settStartDato(startDato)),
                    Date.valueOf(sluttDato)
                ),
                tiltakstype
            );
        }
        return AlleredeRegistrertAvtale.filtrerAvtaleDeltakerAlleredeErRegistrertPaa(
            avtaleRepository.finnAvtalerSomOverlapperForDeltakerVedOpprettelseAvAvtale(
                deltakerFnr.asString(),
                Date.valueOf(settStartDato(startDato))

            ),
            tiltakstype
        );
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
