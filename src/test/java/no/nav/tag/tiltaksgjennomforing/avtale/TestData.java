package no.nav.tag.tiltaksgjennomforing.avtale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.VeilarbArenaClient;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.AxsysService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.okonomi.KontoregisterService;
import no.nav.tag.tiltaksgjennomforing.persondata.Adressebeskyttelse;
import no.nav.tag.tiltaksgjennomforing.persondata.Data;
import no.nav.tag.tiltaksgjennomforing.persondata.HentGeografiskTilknytning;
import no.nav.tag.tiltaksgjennomforing.persondata.HentPerson;
import no.nav.tag.tiltaksgjennomforing.persondata.Navn;
import no.nav.tag.tiltaksgjennomforing.persondata.PdlRespons;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.sporingslogg.Sporingslogg;
import no.nav.tag.tiltaksgjennomforing.utils.Now;

public class TestData {

    public static NavEnhet ENHET_OPPFØLGING = new NavEnhet("0906", "Oslo gamlebyen");
    public static NavEnhet ENHET_GEOGRAFISK = new NavEnhet("0904", "Vinstra");
    public static Integer ET_AVTALENR = 10;

    public static Avtale enArbeidstreningAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), veilderNavIdent);
    }

    public static Avtale enInkluderingstilskuddAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.INKLUDERINGSTILSKUDD), veilderNavIdent);
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());

        return avtale;
    }

    public static Avtale enInkluderingstilskuddAvtaleUtfyltOgGodkjentAvArbeidsgiver() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.INKLUDERINGSTILSKUDD), veilderNavIdent);
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        return avtale;
    }

    public static Avtale enSommerjobbAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.SOMMERJOBB), veilderNavIdent);
    }

    public static Avtale enMidlertidigLonnstilskuddsjobbAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), veilderNavIdent);
    }

    public static Avtale enVarigLonnstilskuddsjobbAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.VARIG_LONNSTILSKUDD), veilderNavIdent);
    }

    public static Avtale enMentorAvtaleUsignert() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.MENTOR), veilderNavIdent);
        avtale.setMentorFnr(new Fnr("00000000000"));
        avtale.getGjeldendeInnhold().setBedriftNavn("Donald Duck Co..");
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Donal");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Duck");
        avtale.getGjeldendeInnhold().setDeltakerTlf("12312323");
        avtale.getGjeldendeInnhold().setArbeidsgiverFornavn("Onkel");
        avtale.getGjeldendeInnhold().setArbeidsgiverEtternavn("Skrue");
        avtale.getGjeldendeInnhold().setArbeidsgiverTlf("12312345");
        avtale.getGjeldendeInnhold().setVeilederFornavn("Minni");
        avtale.getGjeldendeInnhold().setVeilederEtternavn("Mus");
        avtale.getGjeldendeInnhold().setVeilederTlf("12312367");
        avtale.getGjeldendeInnhold().setMentorFornavn("Petter");
        avtale.getGjeldendeInnhold().setMentorEtternavn("Samrt");
        avtale.getGjeldendeInnhold().setMentorTlf("12312389");
        avtale.getGjeldendeInnhold().setMentorOppgaver("Hepp Hepp");
        avtale.getGjeldendeInnhold().setMentorAntallTimer(20);
        avtale.getGjeldendeInnhold().setMentorTimelonn(300);
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate());
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusWeeks(2).minusDays(1));
        avtale.getGjeldendeInnhold().setOppfolging("Oppfølging");
        avtale.getGjeldendeInnhold().setTilrettelegging("Tilrettelegging");
         return avtale;
    }

    public static Avtale enMentorAvtaleSignert() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.MENTOR), veilderNavIdent);
        avtale.setMentorFnr(new Fnr("00000000000"));
        avtale.setBedriftNr(new BedriftNr("999999999"));
        EndreAvtale endreAvtale = endringPåAlleFelter();
        endreAvtale.setDeltakerFornavn("Solfrid");
        endreAvtale.setBedriftNavn("Donald Duck Co..");
        endreAvtale.setDeltakerEtternavn("Sommerfeldt");
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(Now.localDate().plusWeeks(4).minusDays(1));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.godkjennForMentor(new Fnr("00000000000"));
        avtale.godkjentAvArbeidsgiver();
        avtale.godkjennForDeltaker(new Fnr("00000000000"));
         return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt() {
        return Avtale.arbeidsgiverOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING));
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhetOgGeografiskEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        avtale.setEnhetGeografisk(ENHET_GEOGRAFISK.getVerdi());
        avtale.setEnhetsnavnGeografisk(ENHET_GEOGRAFISK.getNavn());
        return avtale;
    }

    public static Avtale enArbeidstreningsAvtaleMedGittAvtaleNr() {
        Avtale avtale = enArbeidstreningAvtale();
        avtale.setAvtaleNr(ET_AVTALENR);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        avtale.setEnhetGeografisk(ENHET_GEOGRAFISK.getVerdi());
        avtale.setEnhetsnavnGeografisk(ENHET_GEOGRAFISK.getNavn());
        return avtale;
    }


    public static Avtale enAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), veilderNavIdent);
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        return avtale;
    }

    public static Avtale enAvtaleMedAltUtfyltGodkjentAvVeileder() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        avtale.getGjeldendeInnhold().setRefusjonKontaktperson(new RefusjonKontaktperson("Donald","Duck","55555123", true));
        return avtale;
    }

    public static Avtale enLønnstilskuddsAvtaleMedStartOgSlutt(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setEnhetGeografisk(ENHET_GEOGRAFISK.getVerdi());
        avtale.setEnhetsnavnGeografisk(ENHET_GEOGRAFISK.getNavn());
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        EndreAvtale endring = TestData.endringPåAlleFelter();
        endring.setStartDato(startDato);
        endring.setSluttDato(sluttDato);
        avtale.endreAvtale(Now.instant(), endring, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        return avtale;
    }

    public static Avtale enLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setEnhetGeografisk(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnGeografisk(ENHET_GEOGRAFISK.getNavn());
        avtale.setEnhetOppfolging(ENHET_GEOGRAFISK.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endring = TestData.endringPåAlleFelter();
        endring.setStartDato(startDato);
        endring.setSluttDato(sluttDato);
        avtale.endreAvtale(Now.instant(), endring, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent(), List.of());
        return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleMedAltUtfylt() {
        return enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    public static Avtale enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype tiltakstype) {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.veilederOppretterAvtale(lagOpprettAvtale(tiltakstype), veilderNavIdent);
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.setTiltakstype(tiltakstype);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(60);
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleGodkjentAvVeileder() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleGodkjentAvVeilederTilbakeITid() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Geir");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Geirsen");
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusMonths(3));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusMonths(1));
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }

    public static Avtale enSommerjobbAvtaleGodkjentAvVeileder() {
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), new NavIdent("Z123456"));
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        EndreAvtale endreAvtale = endringPåAlleFelter();
        endreAvtale.setDeltakerFornavn("Solveig");
        endreAvtale.setDeltakerEtternavn("Sommerfeldt");
        endreAvtale.setLonnstilskuddProsent(50);
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        NavIdent veileder = TestData.enNavIdent();
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(veileder);
        avtale.setVeilederNavIdent(veileder);
        return avtale;
    }

    public static Avtale enSommerjobbAvtaleGodkjentAvBeslutter() {
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), new NavIdent("Z123456"));
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        avtale.setAvtaleNr(1);
        EndreAvtale endreAvtale = endringPåAlleFelter();
        endreAvtale.setDeltakerFornavn("Solbe");
        endreAvtale.setDeltakerEtternavn("Sommerfeldt");
        endreAvtale.setLonnstilskuddProsent(50);
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvBeslutter(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setRefusjonKontaktperson(null);
        return avtale;
    }

    public static Avtale enSommerjobbAvtaleGodkjentAvArbeidsgiver() {
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), new NavIdent("Z123456"));
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        EndreAvtale endreAvtale = endringPåAlleFelter();
        endreAvtale.setDeltakerFornavn("Solfrid");
        endreAvtale.setDeltakerEtternavn("Sommerfeldt");
        endreAvtale.setLonnstilskuddProsent(50);
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(Now.instant(), endreAvtale, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        return avtale;
    }

    public static Avtale enMentorAvtaleUtenGodkjenninger() {
        Avtale avtale = Avtale.veilederOppretterAvtale(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), new NavIdent("Z123456"));
        avtale.setTiltakstype(Tiltakstype.MENTOR);
        avtale.setMentorFnr(new Fnr("00000000000"));
        avtale.getGjeldendeInnhold().setMentorFornavn("Jo");
        avtale.getGjeldendeInnhold().setMentorEtternavn("Å");
        avtale.getGjeldendeInnhold().setMentorOppgaver("Spise lunch med deltaker");
        avtale.getGjeldendeInnhold().setMentorAntallTimer(30);
        avtale.getGjeldendeInnhold().setMentorTlf("12345678");
        avtale.getGjeldendeInnhold().setMentorTimelonn(500);
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }

    public static Avtale enMentorAvtaleMedMedAltUtfylt() {
        Avtale avtale = enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.setTiltakstype(Tiltakstype.MENTOR);
        avtale.setMentorFnr(new Fnr("00000000000"));
        avtale.getGjeldendeInnhold().setMentorFornavn("Jo");
        avtale.getGjeldendeInnhold().setMentorEtternavn("Å");
        avtale.getGjeldendeInnhold().setMentorOppgaver("Spise lunch med deltaker");
        avtale.getGjeldendeInnhold().setMentorAntallTimer(30);
        avtale.getGjeldendeInnhold().setMentorTlf("12345678");
        avtale.getGjeldendeInnhold().setMentorTimelonn(500);
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleMedAltUtfyltMedGodkjentForEtterregistrering(LocalDate avtaleStart, LocalDate avtaleSlutt){
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endring = TestData.endringPåAlleFelter();
        endring.setStartDato(avtaleStart);
        endring.setSluttDato(avtaleSlutt);
        avtale.setGodkjentForEtterregistrering(true);
        avtale.endreAvtale(Now.instant(), endring, Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent(), List.of());
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

    public static EndreAvtale endringPåAlleInkluderingstilskuddFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Geirulf");
        endreAvtale.setDeltakerEtternavn("Persen");
        endreAvtale.setDeltakerTlf("22334455");
        endreAvtale.setBedriftNavn("Inkluderende Butikk AS");
        endreAvtale.setArbeidsgiverFornavn("Per");
        endreAvtale.setArbeidsgiverEtternavn("Larsen");
        endreAvtale.setArbeidsgiverTlf("33333333");
        endreAvtale.setVeilederFornavn("Vera");
        endreAvtale.setVeilederEtternavn("Veiledersen");
        endreAvtale.setVeilederTlf("44444444");
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(2));
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(13337, InkluderingstilskuddsutgiftType.PROGRAMVARE));
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(25697, InkluderingstilskuddsutgiftType.OPPLÆRING));
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(7195, InkluderingstilskuddsutgiftType.UTSTYR));
        endreAvtale.setInkluderingstilskuddBegrunnelse("Behov for tilskudd til dyre programvarelisenser og opplæring i disse.");
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("En middels god forklaring");
        return endreAvtale;
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
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(2));
        endreAvtale.setStillingprosent(50);
        endreAvtale.setStillingStyrk08(500);
        endreAvtale.setStillingKonseptId(50000);
        endreAvtale.setAntallDagerPerUke(4);
        endreAvtale.setMaal(List.of(TestData.etMaal()));
        endreAvtale.setStillingstittel("Butikksjef");
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Dagny");
        endreAvtale.setDeltakerEtternavn("Deltaker");
        endreAvtale.setDeltakerTlf("92334455");
        endreAvtale.setBedriftNavn("Pers butikk");
        endreAvtale.setArbeidsgiverFornavn("Per");
        endreAvtale.setArbeidsgiverEtternavn("Kremmer");
        endreAvtale.setArbeidsgiverTlf("43333333");
        endreAvtale.setVeilederFornavn("Vera");
        endreAvtale.setVeilederEtternavn("Veileder");
        endreAvtale.setVeilederTlf("44444444");
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(12).minusDays(1));
        endreAvtale.setStillingprosent(50);
        endreAvtale.getMaal().add(TestData.etMaal());
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(25000, InkluderingstilskuddsutgiftType.PROGRAMVARE));
        endreAvtale.setInkluderingstilskuddBegrunnelse("Behov for inkluderingstilskudd");
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
        endreAvtale.setMentorTlf("12348594837");
        endreAvtale.setMentorTimelonn(1000);
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("En middels god forklaring");
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(5);
        endreAvtale.setRefusjonKontaktperson(new RefusjonKontaktperson("Ola", "Olsen", "12345678", true));
        return endreAvtale;
    }

    public static Deltaker enDeltaker(Avtale avtale) {
        return new Deltaker(avtale.getDeltakerFnr());
    }

    public static InnloggetDeltaker enInnloggetDeltaker() {
        return new InnloggetDeltaker(new Fnr("99999999999"));
    }

    public static InnloggetMentor enInnloggetMentor() {
        return new InnloggetMentor(new Fnr("99999999999"));
    }

    public static InnloggetArbeidsgiver enInnloggetArbeidsgiver() {
        return new InnloggetArbeidsgiver(new Fnr("99999999999"), Collections.emptySet(), Map.of());
    }

    public static InnloggetVeileder enInnloggetVeileder() {
        return new InnloggetVeileder(new NavIdent("F888888"), Set.of(ENHET_OPPFØLGING), false);
    }

    public static InnloggetBeslutter enInnloggetBeslutter() {
        return new InnloggetBeslutter(new NavIdent("F888888"));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        return new Arbeidsgiver(new Fnr("01234567890"), Set.of(), Map.of(), null, null,  null);
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(new AltinnReportee("Bedriftnavn", "", null, avtale.getBedriftNr().asString(), "", ""))
                , Map.of(avtale.getBedriftNr(),
                List.of(Tiltakstype.values())),
                null,
                null,
                null);
    }

    public static Fnr etFodselsnummer() {
        return new Fnr("00000000000");
    }

    public static Veileder enVeileder(Avtale avtale) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        VeilarbArenaClient veilarbArenaClient = mock(VeilarbArenaClient.class);
        KontoregisterService kontoregisterService = mock(KontoregisterService.class);
        AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
        lenient().when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(avtale.getVeilederNavIdent()), eq(avtale.getDeltakerFnr()))).thenReturn(true);
        lenient().when(veilarbArenaClient.sjekkOgHentOppfølgingStatus(any()))
                .thenReturn(new Oppfølgingsstatus(Formidlingsgruppe.ARBEIDSSOKER, Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, "0906"));
        return new Veileder(avtale.getVeilederNavIdent(), tilgangskontrollService, mock(PersondataService.class), mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Oslo gamlebyen")), new SlettemerkeProperties(), new TilskuddsperiodeConfig(), false, veilarbArenaClient);
    }

    public static Beslutter enBeslutter(Avtale avtale) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        AxsysService axsysService = mock(AxsysService.class);
        NavIdent navIdent = new NavIdent("B999999");
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(navIdent), eq(avtale.getDeltakerFnr()))).thenReturn(true);
        return new Beslutter(navIdent, tilgangskontrollService, axsysService);
    }

    public static Maal etMaal() {
        Maal maal = new Maal();
        maal.setKategori(MaalKategori.FÅ_JOBB_I_BEDRIFTEN);
        maal.setBeskrivelse("Lære butikkarbeid");
        return maal;
    }

    public static Inkluderingstilskuddsutgift enInkluderingstilskuddsutgift(Integer beløp, InkluderingstilskuddsutgiftType type) {
        Inkluderingstilskuddsutgift i = new Inkluderingstilskuddsutgift();
        i.setBeløp(beløp);
        i.setType(type);
        return i;
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

    public static Sporingslogg enHendelse(Avtale avtale) {
        return Sporingslogg.nyHendelse(avtale, HendelseType.OPPRETTET);
    }

    public static Avtale enAvtaleMedFlereVersjoner() {
        Avtale avtale = TestData.enAvtaleMedAltUtfyltGodkjentAvVeileder();
        avtale.endreKontaktInformasjon(new EndreKontaktInformasjon("Atle",
                "Jørgensen",
                avtale.getGjeldendeInnhold().getDeltakerTlf(),
                avtale.getGjeldendeInnhold().getVeilederFornavn(),
                avtale.getGjeldendeInnhold().getVeilederEtternavn(),
                avtale.getGjeldendeInnhold().getVeilederTlf(),
                avtale.getGjeldendeInnhold().getArbeidsgiverFornavn(),
                avtale.getGjeldendeInnhold().getArbeidsgiverEtternavn(),
                avtale.getGjeldendeInnhold().getArbeidsgiverTlf(),
            new RefusjonKontaktperson("Atle", "Jørgensen", "12345678",true)),
            TestData.enNavIdent());
        return avtale;
    }

    public static Avtale enAvtaleKlarForOppstart() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().plusDays(7));
        avtale.getGjeldendeInnhold().setSluttDato(avtale.getGjeldendeInnhold().getStartDato().plusMonths(1));
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }

    public static BedriftNr etBedriftNr() {
        return new BedriftNr("777777777");
    }

    public static NavIdent enNavIdent() {
        return new NavIdent("Q987654");
    }

    public static NavIdent enNavIdent2() {
        return new NavIdent("B987654");
    }

    public static Veileder enVeileder(NavIdent navIdent) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        KontoregisterService kontoregisterService = mock(KontoregisterService.class);
        AvtaleRepository avtaleRepository = mock(AvtaleRepository.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(navIdent), any())).thenReturn(true);
        return new Veileder(navIdent, tilgangskontrollService, mock(PersondataService.class), mock(Norg2Client.class),
                Set.of(ENHET_OPPFØLGING), new SlettemerkeProperties(), new TilskuddsperiodeConfig(), false, mock(VeilarbArenaClient.class));
    }

    public static Veileder enVeileder(Avtale avtale, PersondataService persondataService) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        KontoregisterService kontoregisterService = mock(KontoregisterService.class);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(avtale.getVeilederNavIdent(), avtale.getDeltakerFnr())).thenReturn(true);
        return new Veileder(avtale.getVeilederNavIdent(), tilgangskontrollService, persondataService, mock(Norg2Client.class),
                Set.of(ENHET_OPPFØLGING), new SlettemerkeProperties(), new TilskuddsperiodeConfig(), false, mock(VeilarbArenaClient.class));
    }

    public static PdlRespons enPdlrespons(boolean harKode6) {
        Adressebeskyttelse[] adressebeskyttelser = new Adressebeskyttelse[1];
        if (harKode6) {
            adressebeskyttelser[0] = new Adressebeskyttelse("STRENGT_FORTROLIG");
        }

        HentPerson hentPerson = new HentPerson(adressebeskyttelser, new Navn[]{ new Navn("Donald", null, "Duck") });
        return new PdlRespons(new Data(hentPerson, null, new HentGeografiskTilknytning(null, "030101", null, null)));
    }

    public static TilskuddPeriode enTilskuddPeriode() {
        return TestData.enLonnstilskuddAvtaleGodkjentAvVeileder().getTilskuddPeriode().first();
    }

    public static EndreTilskuddsberegning enEndreTilskuddsberegning() {
        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;
        return EndreTilskuddsberegning.builder().otpSats(otpSats).feriepengesats(feriepengesats).arbeidsgiveravgift(arbeidsgiveravgift).manedslonn(manedslonn).build();
    }

    public static Avtale enArbeidstreningAvtaleGodkjentAvVeileder() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.endreAvtale(avtale.getSistEndret(), endringPåAlleFelter(), Avtalerolle.VEILEDER, EnumSet.of(avtale.getTiltakstype()), List.of());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }


}
