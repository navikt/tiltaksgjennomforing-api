package no.nav.tag.tiltaksgjennomforing.avtale;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBruker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.exceptions.ErAlleredeVeilederException;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeAdminTilgangException;
import no.nav.tag.tiltaksgjennomforing.exceptions.IkkeTilgangTilDeltakerException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.logging.SecureLog;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;

@Slf4j
public class Veileder extends Avtalepart<NavIdent> implements InternBruker {
    private static final SecureLog secureLog = SecureLog.getLogger(log);

    private final TilgangskontrollService tilgangskontrollService;
    private final PersondataService persondataService;
    private final SlettemerkeProperties slettemerkeProperties;
    private final boolean harAdGruppeForBeslutter;
    private final Norg2Client norg2Client;
    private final Set<NavEnhet> navEnheter;
    private final VeilarboppfolgingService veilarboppfolgingService;
    private final UUID azureOid;
    private final FeatureToggleService featureToggleService;

    public Veileder(
            NavIdent identifikator,
            UUID azureOid,
            TilgangskontrollService tilgangskontrollService,
            PersondataService persondataService,
            Norg2Client norg2Client,
            Set<NavEnhet> navEnheter,
            SlettemerkeProperties slettemerkeProperties,
            boolean harAdGruppeForBeslutter,
            VeilarboppfolgingService veilarboppfolgingService,
            FeatureToggleService featureToggleService
    ) {

        super(identifikator);
        this.azureOid = azureOid;
        this.tilgangskontrollService = tilgangskontrollService;
        this.persondataService = persondataService;
        this.norg2Client = norg2Client;
        this.navEnheter = navEnheter;
        this.slettemerkeProperties = slettemerkeProperties;
        this.harAdGruppeForBeslutter = harAdGruppeForBeslutter;
        this.veilarboppfolgingService = veilarboppfolgingService;
        this.featureToggleService = featureToggleService;
    }

    @Deprecated
    public Veileder(
            NavIdent identifikator,
            TilgangskontrollService tilgangskontrollService,
            PersondataService persondataService,
            Norg2Client norg2Client,
            Set<NavEnhet> navEnheter,
            SlettemerkeProperties slettemerkeProperties,
            boolean harAdGruppeForBeslutter,
            VeilarboppfolgingService veilarboppfolgingService,
            FeatureToggleService featureToggleService
    ) {
        this(identifikator, null, tilgangskontrollService, persondataService, norg2Client, navEnheter, slettemerkeProperties, harAdGruppeForBeslutter, veilarboppfolgingService, featureToggleService);
    }

    @Override
    public boolean harTilgangTilAvtale(Avtale avtale) {
        secureLog.info("Sjekker tilgang for veileder {} til avtale {}", getIdentifikator(), avtale.getId());
        boolean harTilgang = tilgangskontrollService.harSkrivetilgangTilKandidat(this, avtale.getDeltakerFnr());
        if (!harTilgang) {
            log.info("Har ikke tilgang til avtalenr {}, id: {}", avtale.getAvtaleNr(), avtale.getId());
        }
        return harTilgang;
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
    Page<Avtale> hentAlleAvtalerMedMuligTilgang(AvtaleRepository avtaleRepository, AvtaleQueryParameter queryParametre, Pageable pageable) {
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

    @Override
    AvtaleMinimalListevisning skjulData(AvtaleMinimalListevisning avtaleMinimalListevisning) {
        return avtaleMinimalListevisning;
    }

    public void annullerAvtale(Instant sistEndret, String annullerGrunn, Avtale avtale) {
        avtale.sjekkSistEndret(sistEndret);
        avtale.annuller(this, annullerGrunn);
    }

    @Override
    public boolean kanEndreAvtale() {
        return true;
    }

    @Override
    void godkjennForAvtalepart(Avtale avtale) {
        this.sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        this.sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        avtale.godkjennForVeileder(getIdentifikator());
    }

    @Override
    boolean kanOppheveGodkjenninger(Avtale avtale) {
        return true;
    }

    public void godkjennForVeilederOgDeltaker(GodkjentPaVegneGrunn paVegneAvGrunn, Avtale avtale) {
        sjekkTilgang(avtale);
        sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        this.sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        avtale.godkjennForVeilederOgDeltaker(getIdentifikator(), paVegneAvGrunn);
    }

    public void godkjennForVeilederOgArbeidsgiver(
            GodkjentPaVegneAvArbeidsgiverGrunn paVegneAvArbeidsgiverGrunn,
            Avtale avtale
    ) {
        super.sjekkTilgang(avtale);
        this.sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        this.sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
        avtale.godkjennForVeilederOgArbeidsgiver(getIdentifikator(), paVegneAvArbeidsgiverGrunn);
    }

    public void godkjennForVeilederOgDeltakerOgArbeidsgiver(
            GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn,
            Avtale avtale
    ) {
        super.sjekkTilgang(avtale);
        this.sjekkOgBlokkereKode6(avtale.getDeltakerFnr());
        this.sjekkOgOppdaterOppfølgningsstatusForAvtale(avtale);
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
        return new InnloggetVeileder(getIdentifikator(), navEnheter, harAdGruppeForBeslutter);
    }

    public void delAvtaleMedAvtalepart(Avtalerolle avtalerolle, Avtale avtale) {
        avtale.delMedAvtalepart(avtalerolle);
    }

    public void overtaAvtale(Avtale avtale) {
        super.sjekkTilgang(avtale);
        if (this.getIdentifikator().equals(avtale.getVeilederNavIdent())) {
            throw new ErAlleredeVeilederException();
        }
        avtale.overtaAvtale(this.getIdentifikator());
    }

    @Override
    public void endreAvtale(
            Instant sistEndret,
            EndreAvtale endreAvtale,
            Avtale avtale,
            EnumSet<Tiltakstype> tiltakstyperMedTilskuddsperioder
    ) {
        super.sjekkTilgangOgEndreAvtale(
                sistEndret,
                endreAvtale,
                avtale,
                tiltakstyperMedTilskuddsperioder
        );
        this.oppdatereEnheterVedEndreAvtale(avtale);
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
        if (norg2GeoResponse == null) return;
        avtale.setEnhetGeografisk(norg2GeoResponse.getEnhetNr());
        avtale.setEnhetsnavnGeografisk(norg2GeoResponse.getNavn());
    }

    private void oppdatereOppfølgingEnhetsnavnVedEndreAvtale(Avtale avtale) {
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhetFraCacheNorg2(
                avtale.getEnhetOppfolging()
        );
        if (response == null) return;
        avtale.setEnhetsnavnOppfolging(response.getNavn());
    }

    public void oppdatereOppfølgingStatusVedEndreAvtale(Avtale avtale) {
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOppfolgingsstatus(
                avtale.getDeltakerFnr().asString()
        );
        if (oppfølgingsstatus == null) return;
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
    }

    private void sjekkOgBlokkereKode6(Fnr fnr) {
        if (!featureToggleService.isEnabled(FeatureToggle.KODE_6_SPERRE)) {
            return;
        }
        if (!persondataService.hentDiskresjonskode(fnr).erKode6()){
            return;
        }
        throw new IkkeTilgangTilDeltakerException(fnr);
    }

    public Avtale opprettAvtale(OpprettAvtale opprettAvtale) {
        this.sjekkTilgangskontroll(opprettAvtale.getDeltakerFnr());
        this.sjekkOgBlokkereKode6(opprettAvtale.getDeltakerFnr());
        Avtale avtale = Avtale.opprett(opprettAvtale, Avtaleopphav.VEILEDER, getIdentifikator());
        avtale.leggTilDeltakerNavn(persondataService.hentNavn(avtale.getDeltakerFnr()));
        leggTilEnheter(avtale);
        return avtale;
    }

    private void sjekkTilgangskontroll(Fnr deltakerFnr) {
        if (!tilgangskontrollService.harSkrivetilgangTilKandidat(this, deltakerFnr)) {
            throw new IkkeTilgangTilDeltakerException(deltakerFnr);
        }
    }

    protected void leggTilEnheter(Avtale avtale) {
        super.hentGeoEnhetFraNorg2(avtale, norg2Client, persondataService);
        this.hentOppfølgingFraArena(avtale, veilarboppfolgingService);
        this.hentOppfolgingEnhetsnavnFraNorg2(avtale, norg2Client);
    }

    public void hentOppfolgingEnhetsnavnFraNorg2(Avtale avtale, Norg2Client norg2Client) {
        final Norg2OppfølgingResponse response = norg2Client.hentOppfølgingsEnhet(avtale.getEnhetOppfolging());
        if (response == null) return;
        avtale.setEnhetsnavnOppfolging(response.getNavn());
    }

    public void hentOppfølgingFraArena(
            Avtale avtale,
            VeilarboppfolgingService veilarboppfolgingService
    ) {
        if (avtale.harOppfølgingsStatus()) return;
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale);
        if (oppfølgingsstatus == null) return;
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
        this.settLonntilskuddProsentsats(avtale);
    }

    public void sjekkOgOppdaterOppfølgningsstatusForAvtale(Avtale avtale) {
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOgSjekkOppfolgingstatus(avtale);
        if (oppfølgingsstatus == null) return;
        this.settOppfølgingsStatus(avtale, oppfølgingsstatus);
    }

    protected void settOppfølgingsStatus(Avtale avtale, Oppfølgingsstatus oppfølgingsstatus) {
        avtale.setEnhetOppfolging(oppfølgingsstatus.getOppfolgingsenhet());
        avtale.setKvalifiseringsgruppe(oppfølgingsstatus.getKvalifiseringsgruppe());
        avtale.setFormidlingsgruppe(oppfølgingsstatus.getFormidlingsgruppe());
    }

    public void slettemerk(Avtale avtale) {
        super.sjekkTilgang(avtale);
        List<NavIdent> identer = slettemerkeProperties.getIdent();
        if (!identer.contains(this.getIdentifikator())) {
            throw new IkkeAdminTilgangException();
        }
        avtale.slettemerk(this.getIdentifikator());
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

    protected void oppdaterOppfølgingOgGeoEnhetEtterForespørsel(Avtale avtale) {
        // Geo enhet
        super.hentGeoEnhetFraNorg2(avtale, norg2Client, persondataService);
        // Oppfølgingsenhet
        Oppfølgingsstatus oppfølgingsstatus = veilarboppfolgingService.hentOppfolgingsstatus(avtale.getDeltakerFnr().asString());
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

    public void sendTilbakeTilBeslutter(Avtale avtale) {
        super.sjekkTilgang(avtale);
        avtale.sendTilbakeTilBeslutter();
    }

    protected void oppdatereKostnadssted(Avtale avtale, Norg2Client norg2Client, String enhet) {
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
