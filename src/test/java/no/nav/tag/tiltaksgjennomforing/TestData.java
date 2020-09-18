package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.veilarbabac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.avtale.*;
import no.nav.tag.tiltaksgjennomforing.orgenhet.ArbeidsgiverOrganisasjon;
import no.nav.tag.tiltaksgjennomforing.varsel.BjelleVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.SmsVarsel;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelse;
import no.nav.tag.tiltaksgjennomforing.varsel.VarslbarHendelseType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;

public class TestData {
    public static Avtale enAvtale(Tiltakstype tiltakstype) {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(tiltakstype), veilderNavIdent);
    }

    public static Avtale enArbeidstreningAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), veilderNavIdent);
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

    public static Avtale enLonnstilskuddAvtaleMedAltUtfylt() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.setDeltakerFornavn("Lilly");
        avtale.setDeltakerEtternavn("Lønning");
        avtale.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        avtale.setArbeidsgiverKontonummer("22222222222");
        avtale.setLonnstilskuddProsent(60);
        avtale.setManedslonn(20000);
        avtale.setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.setVersjon(1);
        avtale.setJournalpostId(null);
        avtale.setMaal(Arrays.asList());
        avtale.setOppgaver(Arrays.asList());
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleGodkjentAvVeileder() {
        Avtale avtale = enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        avtale.setArbeidsgiverKontonummer("22222222222");
        avtale.setLonnstilskuddProsent(60);
        avtale.setManedslonn(20000);
        avtale.setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.setVersjon(1);
        avtale.setJournalpostId(null);
        avtale.setMaal(Arrays.asList());
        avtale.setOppgaver(Arrays.asList());
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
        avtale.setMaal(Arrays.asList());
        avtale.setOppgaver(Arrays.asList());
        return avtale;
    }

    private static OpprettAvtale lagOpprettAvtale(Tiltakstype tiltakstype) {
        Fnr deltakerFnr = new Fnr("00000000000");
        BedriftNr bedriftNr = new BedriftNr("999999999");
        return new OpprettAvtale(deltakerFnr, bedriftNr, Tiltakstype.ARBEIDSTRENING);
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

        // Gjør endring begge steder i en overgangsfase
        endreAvtale.setOppgaver(List.of(TestData.enOppgave(), TestData.enOppgave(), TestData.enOppgave()));
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
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(2));
        endreAvtale.setStillingprosent(50);
        endreAvtale.setMaal(List.of(TestData.etMaal()));
        endreAvtale.setOppgaver(List.of(TestData.enOppgave(), TestData.enOppgave(), TestData.enOppgave()));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Stilling");
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
        endreAvtale.setFeriepengerBelop(2400);
        endreAvtale.setOtpBelop(448);
        endreAvtale.setArbeidsgiveravgiftBelop(3222);
        endreAvtale.setSumLonnsutgifter(26070);
        endreAvtale.setSumLonnstilskudd(15642);
        return endreAvtale;
    }

    static Deltaker enDeltaker() {
        return new Deltaker(new Fnr("01234567890"), enArbeidstreningAvtale());
    }

    public static Deltaker enDeltaker(Avtale avtale) {
        return new Deltaker(avtale.getDeltakerFnr(), avtale);
    }

    public static InnloggetDeltaker enInnloggetDeltaker() {
        return new InnloggetDeltaker(new Fnr("99999999999"));
    }

    public static InnloggetArbeidsgiver enInnloggetArbeidsgiver() {
        return new InnloggetArbeidsgiver(new Fnr("99999999999"), emptyList());
    }

    public static InnloggetVeileder enInnloggetVeileder() {
        return new InnloggetVeileder(new NavIdent("F888888"), mock(TilgangskontrollService.class));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        return new Arbeidsgiver(new Fnr("12345678901"), enArbeidstreningAvtale());
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(TestData.etFodselsnummer(), avtale);
    }

    public static Fnr etFodselsnummer() {
        return new Fnr("00000000000");
    }

    public static Veileder enVeileder() {
        return new Veileder(new NavIdent("X123456"), enArbeidstreningAvtale());
    }

    public static Veileder enVeileder(Avtale avtale) {
        return new Veileder(avtale.getVeilederNavIdent(), avtale);
    }

    public static Oppgave enOppgave() {
        Oppgave oppgave = new Oppgave();
        oppgave.setTittel("Lagerarbeid");
        oppgave.setBeskrivelse("Rydde på lageret");
        oppgave.setOpplaering("Ryddekurs");
        return oppgave;
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

    public static InnloggetArbeidsgiver innloggetArbeidsgiver(Avtalepart<Fnr> avtalepartMedFnr) {
        ArbeidsgiverOrganisasjon organisasjon = new ArbeidsgiverOrganisasjon(avtalepartMedFnr.getAvtale().getBedriftNr(), avtalepartMedFnr.getAvtale().getBedriftNavn());
        organisasjon.getTilgangstyper().addAll(List.of(Tiltakstype.values()));
        return new InnloggetArbeidsgiver(avtalepartMedFnr.getIdentifikator(), List.of(organisasjon));
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

    public static BjelleVarsel etBjelleVarsel(Avtale avtale) {
        return BjelleVarsel.nyttVarsel(TestData.enIdentifikator(), TestData.enHendelse(avtale));
    }

    public static SmsVarsel etSmsVarsel(Avtale avtale) {
        return SmsVarsel.nyttVarsel("tlf", TestData.enIdentifikator(), "", null);
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
}
