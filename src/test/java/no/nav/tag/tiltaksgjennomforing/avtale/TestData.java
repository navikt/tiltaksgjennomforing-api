package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.persondata.Adressebeskyttelse;
import no.nav.tag.tiltaksgjennomforing.persondata.Data;
import no.nav.tag.tiltaksgjennomforing.persondata.HentGeografiskTilknytning;
import no.nav.tag.tiltaksgjennomforing.persondata.HentPerson;
import no.nav.tag.tiltaksgjennomforing.persondata.Navn;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;

public class TestData {

    public static String ENHET_OPPFØLGING = "0906";
    public static String ENHET_GEOGRAFISK = "0904";

    public static Avtale enArbeidstreningAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), veilderNavIdent);
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt() {
        return Avtale.arbeidsgiverOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING));
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhetOgGeografiskEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING);
        avtale.setEnhetGeografisk(ENHET_GEOGRAFISK);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        avtale.setEnhetGeografisk(ENHET_GEOGRAFISK);
        return avtale;
    }


    public static Avtale enAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), veilderNavIdent);
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enAvtaleMedAltUtfyltGodkjentAvVeileder() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setJournalpostId("1");
        return avtale;
    }

    public static Avtale enLønnstilskuddsAvtaleMedStartOgSlutt(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setEnhetGeografisk(ENHET_OPPFØLGING);
        avtale.setEnhetOppfolging(ENHET_GEOGRAFISK);
        EndreAvtale endring = TestData.endringPåAlleFelter();
        endring.setStartDato(startDato);
        endring.setSluttDato(sluttDato);
        avtale.endreAvtale(Instant.now(), endring, Avtalerolle.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleMedAltUtfylt() {
        return enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    public static Avtale enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype tiltakstype) {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(tiltakstype), veilderNavIdent);
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING);
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleFelter(), Avtalerolle.VEILEDER);
        avtale.setDeltakerFornavn("Lilly");
        avtale.setDeltakerEtternavn("Lønning");
        avtale.setTiltakstype(tiltakstype);
        avtale.setArbeidsgiverKontonummer("22222222222");
        avtale.setLonnstilskuddProsent(60);
        avtale.setManedslonn(20000);
        avtale.setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.setVersjon(1);
        avtale.setJournalpostId(null);
        avtale.setMaal(List.of());
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleGodkjentAvVeileder() {
        Avtale avtale = enLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setJournalpostId("1");
        return avtale;
    }

    public static Avtale enMentorAvtaleMedMedAltUtfylt() {
        Avtale avtale = enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setTiltakstype(Tiltakstype.MENTOR);
        avtale.setMentorFornavn("Jo");
        avtale.setMentorEtternavn("Å");
        avtale.setMentorOppgaver("Spise lunch med deltaker");
        avtale.setMentorAntallTimer(30);
        avtale.setMentorTimelonn(500);
        avtale.setVersjon(1);
        avtale.setJournalpostId(null);
        avtale.setMaal(List.of());
        return avtale;
    }

    private static OpprettAvtale lagOpprettAvtale(Tiltakstype tiltakstype) {
        Fnr deltakerFnr = new Fnr("00000000000");
        BedriftNr bedriftNr = new BedriftNr("999999999");
        return new OpprettAvtale(deltakerFnr, bedriftNr, tiltakstype);
    }

    public static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    public static EndreAvtale endringPåAlleArbeidstreningFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Dagny");
        endreAvtale.setDeltakerEtternavn("Deltaker");
        endreAvtale.setDeltakerTlf("22334455");
        endreAvtale.setBedriftNavn("Pers butikk");
        endreAvtale.setArbeidsgiverFornavn("Per");
        endreAvtale.setArbeidsgiverEtternavn("Kremmer");
        endreAvtale.setArbeidsgiverTlf("33333333");
        endreAvtale.setVeilederFornavn("Vera");
        endreAvtale.setVeilederEtternavn("Veileder");
        endreAvtale.setVeilederTlf("44444444");
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(LocalDate.now());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(2));
        endreAvtale.setStillingprosent(50);
        endreAvtale.setMaal(List.of(TestData.etMaal()));
        endreAvtale.setStillingstittel("Butikksjef");
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Dagny");
        endreAvtale.setDeltakerEtternavn("Deltaker");
        endreAvtale.setDeltakerTlf("22334455");
        endreAvtale.setBedriftNavn("Pers butikk");
        endreAvtale.setArbeidsgiverFornavn("Per");
        endreAvtale.setArbeidsgiverEtternavn("Kremmer");
        endreAvtale.setArbeidsgiverTlf("33333333");
        endreAvtale.setVeilederFornavn("Vera");
        endreAvtale.setVeilederEtternavn("Veileder");
        endreAvtale.setVeilederTlf("44444444");
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(LocalDate.now());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(12));
        endreAvtale.setStillingprosent(50);
        endreAvtale.setMaal(List.of(TestData.etMaal()));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setManedslonn(10000);
        endreAvtale.setFeriepengesats(BigDecimal.ONE);
        endreAvtale.setArbeidsgiveravgift(BigDecimal.ONE);
        endreAvtale.setMentorFornavn("Mentor");
        endreAvtale.setMentorEtternavn("Mentorsen");
        endreAvtale.setMentorOppgaver("Mentoroppgaver");
        endreAvtale.setMentorAntallTimer(10);
        endreAvtale.setMentorTimelonn(1000);
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("En middels god forklaring");
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setOtpBelop(448);
        endreAvtale.setArbeidsgiveravgiftBelop(3222);
        endreAvtale.setSumLonnsutgifter(26070);
        endreAvtale.setSumLonnstilskudd(15642);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        return endreAvtale;
    }

    public static Deltaker enDeltaker(Avtale avtale) {
        return new Deltaker(avtale.getDeltakerFnr());
    }

    public static InnloggetDeltaker enInnloggetDeltaker() {
        return new InnloggetDeltaker(new Fnr("99999999999"));
    }

    public static InnloggetArbeidsgiver enInnloggetArbeidsgiver() {
        return new InnloggetArbeidsgiver(new Fnr("99999999999"), Collections.emptySet(), Map.of());
    }

    public static InnloggetVeileder enInnloggetVeileder() {
        return new InnloggetVeileder(new NavIdent("F888888"), Set.of(ENHET_OPPFØLGING));
    }

    public static InnloggetBeslutter enInnloggetBeslutter() {
        return new InnloggetBeslutter(new NavIdent("F888888"));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        return new Arbeidsgiver(new Fnr("01234567890"), Set.of(), Map.of(), null, null);
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(new AltinnReportee("Bedriftnavn", "", null, avtale.getBedriftNr().asString(), "", ""))
                , Map.of(avtale.getBedriftNr(),
                List.of(Tiltakstype.values())),
                null,
                null);
    }

    public static Fnr etFodselsnummer() {
        return new Fnr("00000000000");
    }

    public static Veileder enVeileder(Avtale avtale) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(avtale.getVeilederNavIdent()), eq(avtale.getDeltakerFnr()))).thenReturn(true);
        return new Veileder(avtale.getVeilederNavIdent(), avtale, tilgangskontrollService, mock(PersondataService.class), mock(Norg2Client.class),
            Set.of("4802"));
    }

    public static Beslutter enBeslutter(Avtale avtale) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        AxsysService axsysService = mock(AxsysService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(avtale.getVeilederNavIdent()), eq(avtale.getDeltakerFnr()))).thenReturn(true);
        return new Beslutter(avtale.getVeilederNavIdent(), tilgangskontrollService, axsysService);
    }

    public static Maal etMaal() {
        Maal maal = new Maal();
        maal.setKategori(MaalKategori.FÅ_JOBB_I_BEDRIFTEN);
        maal.setBeskrivelse("Lære butikkarbeid");
        return maal;
    }

    public static GodkjentPaVegneGrunn enGodkjentPaVegneGrunn() {
        GodkjentPaVegneGrunn paVegneGrunn = new GodkjentPaVegneGrunn();
        paVegneGrunn.setIkkeBankId(true);
        return paVegneGrunn;
    }

    public static InnloggetArbeidsgiver innloggetArbeidsgiver(Avtalepart<Fnr> avtalepartMedFnr, BedriftNr bedriftNr) {
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(bedriftNr, Set.of(Tiltakstype.values()));
        AltinnReportee altinnOrganisasjon = new AltinnReportee("Bedriften AS", "Business", bedriftNr.asString(), "BEDR", "Active", null);
        return new InnloggetArbeidsgiver(avtalepartMedFnr.getIdentifikator(), Set.of(altinnOrganisasjon), tilganger);
    }

    public static InnloggetDeltaker innloggetDeltaker(Avtalepart<Fnr> avtalepartMedFnr) {
        return new InnloggetDeltaker(avtalepartMedFnr.getIdentifikator());
    }

    public static Identifikator enIdentifikator() {
        return new Identifikator("test-id");
    }

    public static VarslbarHendelse enHendelse(Avtale avtale) {
        return VarslbarHendelse.nyHendelse(avtale, VarslbarHendelseType.OPPRETTET);
    }

    public static Avtale enAvtaleMedFlereVersjoner() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.låsOppAvtale();
        EndreAvtale endreAvtale = TestData.endringPåAlleArbeidstreningFelter();
        endreAvtale.setDeltakerFornavn("Atle");
        endreAvtale.setDeltakerEtternavn("Jørgensen");
        endreAvtale.setOppfolging("Trenger mer oppfølging");
        avtale.endreAvtale(Instant.now(), endreAvtale, Avtalerolle.VEILEDER);
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        return avtale;
    }

    public static Avtale enAvtaleKlarForOppstart() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.setStartDato(LocalDate.now().plusDays(7));
        avtale.setSluttDato(avtale.getStartDato().plusMonths(1));
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        avtale.setGodkjentAvDeltaker(LocalDateTime.now());
        avtale.setGodkjentAvVeileder(LocalDateTime.now());
        avtale.setJournalpostId("1");
        return avtale;
    }

    public static BedriftNr etBedriftNr() {
        return new BedriftNr("777777777");
    }

    public static NavIdent enNavIdent() {
        return new NavIdent("Q987654");
    }

    public static Veileder enVeileder(NavIdent navIdent) {
        return new Veileder(navIdent, mock(TilgangskontrollService.class), mock(PersondataService.class), mock(Norg2Client.class),
            Set.of(ENHET_OPPFØLGING));
    }

    public static Veileder enVeileder(Avtale avtale, PersondataService persondataService) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(avtale.getVeilederNavIdent(), avtale.getDeltakerFnr())).thenReturn(true);
        return new Veileder(avtale.getVeilederNavIdent(), tilgangskontrollService, persondataService, mock(Norg2Client.class),
            Set.of(ENHET_OPPFØLGING));
    }

    public static PdlRespons enPdlrespons(boolean harKode6eller7) {
        Adressebeskyttelse adressebeskyttelser[] = new Adressebeskyttelse[1];
        if (harKode6eller7) {
            adressebeskyttelser[0] = new Adressebeskyttelse("FORTROLIG");
        }

        HentPerson hentPerson = new HentPerson(adressebeskyttelser, new Navn[]{new Navn("Donald", null, "Duck")});
        return new PdlRespons(new Data(hentPerson, null, new HentGeografiskTilknytning(null, "030101", null, null)));
    }

    public static TilskuddPeriode enTilskuddPeriode() {
        return TestData.enLonnstilskuddAvtaleGodkjentAvVeileder().getTilskuddPeriode().get(0);
    }
}
