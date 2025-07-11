package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.AdGruppeTilganger;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetArbeidsgiver;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetBeslutter;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetDeltaker;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetMentor;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.InnloggetVeileder;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.SlettemerkeProperties;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.Tilgang;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.abac.TilgangskontrollService;
import no.nav.tag.tiltaksgjennomforing.enhet.Formidlingsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2Client;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2EnhetStatus;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2GeoResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Norg2OppfølgingResponse;
import no.nav.tag.tiltaksgjennomforing.enhet.Oppfølgingsstatus;
import no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging.VeilarboppfolgingService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.enhet.NavEnhet;
import no.nav.tag.tiltaksgjennomforing.orgenhet.EregService;
import no.nav.tag.tiltaksgjennomforing.orgenhet.Organisasjon;
import no.nav.tag.tiltaksgjennomforing.persondata.PersondataService;
import no.nav.tag.tiltaksgjennomforing.sporingslogg.Sporingslogg;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode;
import no.nav.team_tiltak.felles.persondata.pdl.domene.Navn;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestData {
    public static NavEnhet ENHET_OPPFØLGING = new NavEnhet("0906", "Oslo gamlebyen");
    public static NavEnhet ENHET_GEOGRAFISK = new NavEnhet("0904", "Vinstra");
    public static Integer ET_AVTALENR = 10;
    public static AdGruppeTilganger INGEN_AD_GRUPPER = new AdGruppeTilganger(false, false, false);

    public static FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

    public static Avtale enAvtale(Tiltakstype tiltakstype){
        switch (tiltakstype){
            case ARBEIDSTRENING:
                return enArbeidstreningAvtale();
            case INKLUDERINGSTILSKUDD:
                return enInkluderingstilskuddAvtale();
            case SOMMERJOBB:
                return enSommerjobbAvtale();
            case MIDLERTIDIG_LONNSTILSKUDD:
                return enMidlertidigLonnstilskuddsjobbAvtale();
            case VARIG_LONNSTILSKUDD:
                return enVarigLonnstilskuddsjobbAvtale();
            case MENTOR:
                return enMentorAvtale();
            default:
                throw new IllegalArgumentException("Ukjent tiltakstype");

        }
    }

    public static Avtale enArbeidstreningAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.opprett(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, veilderNavIdent);
    }

    public static Avtale enInkluderingstilskuddAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(Tiltakstype.INKLUDERINGSTILSKUDD), Avtaleopphav.VEILEDER, veilderNavIdent);
        avtale.endreAvtale(endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enInkluderingstilskuddAvtaleUtfyltOgGodkjentAvArbeidsgiver() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(Tiltakstype.INKLUDERINGSTILSKUDD), Avtaleopphav.VEILEDER, veilderNavIdent);
        avtale.endreAvtale(endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        return avtale;
    }

    public static Avtale enSommerjobbAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.opprett(lagOpprettAvtale(Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, veilderNavIdent);
    }

    public static Avtale enMidlertidigLonnstilskuddsjobbAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.opprett(lagOpprettAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, veilderNavIdent);
    }

    public static Avtale enVarigLonnstilskuddsjobbAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.opprett(lagOpprettAvtale(Tiltakstype.VARIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, veilderNavIdent);
    }

    public static Avtale enMentorAvtale() {
        NavIdent veilederNavIdent = new NavIdent("Z123456");
        return Avtale.opprett(lagOpprettMentorAvtale(Tiltakstype.MENTOR), Avtaleopphav.VEILEDER, veilederNavIdent);
    }

    public static Avtale enMentorAvtaleUsignert() {
        Avtale avtale = enMentorAvtale();
        EndreAvtale endreAvtale = endringPåAlleMentorFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enMentorAvtaleSignert() {
        Avtale avtale = enMentorAvtale();
        EndreAvtale endreAvtale = endringPåAlleMentorFelter();
        avtale.godkjennForMentor(avtale.getMentorFnr());
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt() {
        return Avtale.opprett(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), Avtaleopphav.ARBEIDSGIVER);
    }

    public static Avtale enAvtaleOpprettetAvArbeidsgiver(Tiltakstype tiltakstype) {
        return Avtale.opprett(lagOpprettAvtale(tiltakstype), Avtaleopphav.ARBEIDSGIVER);
    }

    public static Avtale setOppfølgingPåAvtale(Avtale avtale) {
        avtale.setEnhetOppfolging(ENHET_OPPFØLGING.getVerdi());
        avtale.setEnhetsnavnOppfolging(ENHET_OPPFØLGING.getNavn());
        return avtale;
    }

    public static Avtale setGeografiskPåAvtale(Avtale avtale) {
        avtale.setEnhetGeografisk(ENHET_GEOGRAFISK.getVerdi());
        avtale.setEnhetsnavnGeografisk(ENHET_GEOGRAFISK.getNavn());
        return avtale;
    }

    public static Avtale setOppfølgingOgGeografiskPåAvtale(Avtale avtale) {
        setOppfølgingPåAvtale(avtale);
        setGeografiskPåAvtale(avtale);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedOppfølgningsEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        setOppfølgingPåAvtale(avtale);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordeltMedGeografiskEnhet() {
        Avtale avtale = enArbeidstreningAvtaleOpprettetAvArbeidsgiverOgErUfordelt();
        setGeografiskPåAvtale(avtale);
        return avtale;
    }

    public static Avtale enAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(Tiltakstype.ARBEIDSTRENING), Avtaleopphav.VEILEDER, veilderNavIdent);
        avtale.endreAvtale(endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enVtaoArenaAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(Tiltakstype.VTAO), Avtaleopphav.ARENA, veilderNavIdent);
        avtale.endreAvtale(endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enAvtaleMedAltUtfyltGodkjentAvVeileder() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentTaushetserklæringAvMentor(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        avtale.getGjeldendeInnhold().setRefusjonKontaktperson(new RefusjonKontaktperson("Donald", "Duck", "55555123", true));
        return avtale;
    }

    public static Avtale enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        setOppfølgingOgGeografiskPåAvtale(avtale);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endring = TestData.endringPåAlleLønnstilskuddFelter();
        endring.setStartDato(startDato);
        endring.setSluttDato(sluttDato);
        avtale.setGodkjentForEtterregistrering(true);
        avtale.endreAvtale(endring, Avtalerolle.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        return avtale;
    }

    public static Avtale enMidlertidigLønnstilskuddsRyddeAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setArenaRyddeAvtale(new ArenaRyddeAvtale());
        setOppfølgingOgGeografiskPåAvtale(avtale);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endring = TestData.endringPåAlleLønnstilskuddFelter();
        endring.setStartDato(startDato);
        endring.setSluttDato(sluttDato);
        avtale.setGodkjentForEtterregistrering(true);
        avtale.endreAvtale(endring, Avtalerolle.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        return avtale;
    }

    public static Avtale enVtaoRyddeAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enVtaoAvtaleMedAltUtfylt();
        avtale.setArenaRyddeAvtale(new ArenaRyddeAvtale());
        setOppfølgingOgGeografiskPåAvtale(avtale);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endring = TestData.endringPåAlleLønnstilskuddFelter();
        endring.setStartDato(startDato);
        endring.setSluttDato(sluttDato);
        avtale.setGodkjentForEtterregistrering(true);
        avtale.endreAvtale(endring, Avtalerolle.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleMedAltUtfylt() {
        return enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    public static Avtale enVtaoAvtaleMedAltUtfylt() {
        return enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VTAO);
    }

    public static Avtale enSommerjobbLonnstilskuddAvtaleMedAltUtfylt(int lonnstilskuddProsent) {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, veilderNavIdent);
        setOppfølgingPåAvtale(avtale);
        avtale.endreAvtale(endringPåAlleLønnstilskuddFelterForSommerjobb(lonnstilskuddProsent), Avtalerolle.VEILEDER);
        avtale.setTiltakstype(Tiltakstype.SOMMERJOBB);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }

    public static Avtale enVarigLonnstilskuddAvtaleMedAltUtfylt() {
        Avtale avtale = enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusYears(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusYears(1));
        return avtale;
    }

    public static Avtale enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjent() {
        Avtale avtale = enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusYears(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusYears(1));

        // Godkjenning
        Arbeidsgiver arbeidsgiver = enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        Veileder veileder = enVeileder(avtale);
        veileder.godkjennForVeilederOgDeltaker(enGodkjentPaVegneGrunn(), avtale);
        return avtale;
    }
   public static Avtale enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomMåFølgesOpp() {
        Avtale avtale = enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjent();
        avtale.getGjeldendeTilskuddsperiode().avslå(TestData.enNavIdent2(),EnumSet.of(Avslagsårsak.FEIL_I_PROSENTSATS), "Feil i prosentsats");
        return avtale;
    }

   public static Avtale enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomHarBlittRettetAvVeileder() {
       Avtale avtale = enVarigLonnstilskuddAvtaleMedAltUtfyltOgGodkjentAvslåttePerioderSomMåFølgesOpp();
       Veileder veileder = enVeileder(avtale);
       veileder.reaktiverTilskuddsperiodeOgsendTilbakeTilBeslutter(avtale);
       return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleMedAltUtfyltUtenTilskuddsperioder() {
        return enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
    }

    public static Avtale enVarigLonnstilskuddAvtaleMedBehandletIArenaPerioder() {
        Avtale avtale = enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setStartDato(Now.localDate().minusYears(1));
        avtale.getGjeldendeInnhold().setSluttDato(Now.localDate().plusYears(1));
        avtale.nyeTilskuddsperioderEtterMigreringFraArena(LocalDate.of(2023, 2, 1));

        // Godkjenning
        Arbeidsgiver arbeidsgiver = enArbeidsgiver(avtale);
        arbeidsgiver.godkjennAvtale(avtale);
        Veileder veileder = enVeileder(avtale);
        veileder.godkjennForVeilederOgDeltaker(enGodkjentPaVegneGrunn(), avtale);

        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype tiltakstype) {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(tiltakstype), Avtaleopphav.VEILEDER, veilderNavIdent);
        setOppfølgingPåAvtale(avtale);
        if (tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD) {
            avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        } else {
            avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS);
        }
        avtale.endreAvtale(endringPåAlleLønnstilskuddFelter(), Avtalerolle.VEILEDER);
        avtale.setTiltakstype(tiltakstype);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }

    public static Avtale enMidLonnstilskuddAvtaleMedVarigTilpassetSatsMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD), Avtaleopphav.VEILEDER, veilderNavIdent);
        setOppfølgingPåAvtale(avtale);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelter();
        endreAvtale.setSluttDato(Now.localDate().plusMonths(23).minusDays(2));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.setTiltakstype(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsOgAltUtfylt(Tiltakstype tiltakstype) {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.opprett(lagOpprettAvtale(tiltakstype), Avtaleopphav.VEILEDER, veilderNavIdent);
        setOppfølgingPåAvtale(avtale);
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS);
        avtale.endreAvtale(endringPåAlleLønnstilskuddFelter(), Avtalerolle.VEILEDER);
        avtale.setTiltakstype(tiltakstype);
        avtale.getGjeldendeInnhold().setDeltakerFornavn("Lilly");
        avtale.getGjeldendeInnhold().setDeltakerEtternavn("Lønning");
        avtale.getGjeldendeInnhold().setArbeidsgiverKontonummer("22222222222");
        avtale.getGjeldendeInnhold().setManedslonn(20000);
        avtale.getGjeldendeInnhold().setFeriepengesats(BigDecimal.valueOf(0.12));
        avtale.getGjeldendeInnhold().setArbeidsgiveravgift(BigDecimal.valueOf(0.141));
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        avtale.getGjeldendeInnhold().setMaal(List.of());
        return avtale;
    }


    public static Avtale enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.getGjeldendeTilskuddsperiode().avslå(TestData.enNavIdent2(),EnumSet.of(Avslagsårsak.FEIL_I_PROSENTSATS), "Feil i prosentsats");
        return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomHarBlittRettetAvVeileder() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp();
        Veileder veileder = enVeileder(avtale);
        veileder.reaktiverTilskuddsperiodeOgsendTilbakeTilBeslutter(avtale);
        return avtale;
    }

    public static Avtale enSommerjobbLonnstilskuddAvtaleGodkjentAvVeileder(int lonnstilskuddProsent) {
        Avtale avtale = enSommerjobbLonnstilskuddAvtaleMedAltUtfylt(lonnstilskuddProsent);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }

    public static Avtale enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsGodkjentAvVeileder() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleMedSpesieltTilpassetInnsatsOgAltUtfylt(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(TestData.enNavIdent());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }

    public static Avtale enLonnstilskuddAvtaleGodkjentAvVeilederUtenTilskuddsperioder() {
        Avtale avtale = enMidlertidigLonnstilskuddAvtaleMedAltUtfyltUtenTilskuddsperioder();
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
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(50);
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        NavIdent veileder = TestData.enNavIdent();
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(veileder);
        avtale.setVeilederNavIdent(veileder);
        return avtale;
    }

    public static Avtale enSommerjobbAvtaleGodkjentAvBeslutter() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        avtale.setAvtaleNr(1);
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(50);
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
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
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.SOMMERJOBB), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(50);
        endreAvtale.setFeriepengesats(new BigDecimal("0.12"));
        endreAvtale.setArbeidsgiveravgift(new BigDecimal("0.141"));
        endreAvtale.setStartDato(LocalDate.of(2021, 6, 1));
        endreAvtale.setSluttDato(LocalDate.of(2021, 6, 1).plusWeeks(4).minusDays(1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        return avtale;
    }

    public static Avtale enMentorAvtaleMedMedAltUtfylt() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.setTiltakstype(Tiltakstype.MENTOR);
        avtale.setMentorFnr(new Fnr("00000000000"));
        EndreAvtale endreAvtale = new EndreAvtale();
        endreMentorInfo(endreAvtale);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setVersjon(1);
        avtale.getGjeldendeInnhold().setJournalpostId(null);
        return avtale;
    }

    public static Avtale enVtaoAvtaleGodkjentAvVeileder(){
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.VTAO), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleVTAOFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        return avtale;
    }

    public static Avtale enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp(){
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.VTAO), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleVTAOFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeTilskuddsperiode().avslå(TestData.enNavIdent2(),EnumSet.of(Avslagsårsak.FEIL_I_PROSENTSATS), "Feil i prosentsats");
        return avtale;
    }

    public static Avtale enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomHarBlittRettetAvVeileder(){
        Avtale avtale = enVtaoAvtaleGodkjentAvVeilederAvslåttePerioderSomMåFølgesOpp();
        Veileder veileder = enVeileder(avtale);
        veileder.reaktiverTilskuddsperiodeOgsendTilbakeTilBeslutter(avtale);
        return avtale;
    }

    public static Avtale enVtaoAvtaleGodkjentAvVeilederFraAnnentOmråde(){
        Avtale avtale = Avtale.opprett(new OpprettAvtale(new Fnr("11111111111"), new BedriftNr("999999999"), Tiltakstype.VTAO), Avtaleopphav.VEILEDER, new NavIdent("A123456"));
        avtale.setEnhetOppfolging("0904");
        avtale.setEnhetsnavnOppfolging("Vinstra");
        EndreAvtale endreAvtale = endringPåAlleVTAOFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(avtale.getVeilederNavIdent());
        return avtale;
    }

    public static Avtale enEtterRegistrerdVtaoAvtaleGodkjentAvVeileder(){
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.VTAO), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        avtale.setGodkjentForEtterregistrering(true);
        EndreAvtale endreAvtale = endringPåAlleVTAOFelter();
        endreAvtale.setDeltakerFornavn("Test");
        endreAvtale.setDeltakerEtternavn("Testesen");
        endreAvtale.setStartDato(Now.localDate().minusMonths(12));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvNavIdent(avtale.getVeilederNavIdent());
        return avtale;
    }

    public static Avtale enVtaoAvtaleGodkjentAvArbeidsgiver() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.VTAO), Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleVTAOFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        return avtale;
    }

    public static Avtale enVtaoAvtaleIKKEGodkjentAvArbeidsgiver() {
        Avtale avtale = Avtale.opprett(new OpprettAvtale(TestData.etFodselsnummer(), new BedriftNr("999999999"), Tiltakstype.VTAO),Avtaleopphav.VEILEDER, new NavIdent("Z123456"));
        setOppfølgingPåAvtale(avtale);
        EndreAvtale endreAvtale = endringPåAlleVTAOFelter();
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        return avtale;
    }

    public static EndreAvtale endringPåAlleVTAOFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreKontaktInfo(endreAvtale);
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(12).minusDays(1));
        endreAvtale.setStillingprosent(BigDecimal.valueOf(50.0));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setManedslonn(6808);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.setHarFamilietilknytning(false);
        return endreAvtale;
    }

    public static Avtale enLonnstilskuddAvtaleMedAltUtfyltMedGodkjentForEtterregistrering(LocalDate avtaleStart, LocalDate avtaleSlutt) {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setKvalifiseringsgruppe(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS);
        EndreAvtale endring = TestData.endringPåAlleLønnstilskuddFelter();
        endring.setStartDato(avtaleStart);
        endring.setSluttDato(avtaleSlutt);
        avtale.setGodkjentForEtterregistrering(true);
        avtale.endreAvtale(endring, Avtalerolle.VEILEDER);
        avtale.godkjennForArbeidsgiver(TestData.enIdentifikator());
        avtale.godkjennForDeltaker(TestData.enIdentifikator());
        avtale.godkjennForVeileder(TestData.enNavIdent());
        return avtale;
    }

    private static OpprettAvtale lagOpprettAvtale(Tiltakstype tiltakstype) {
        Fnr deltakerFnr = new Fnr("00000000000");
        BedriftNr bedriftNr = new BedriftNr("999999999");
        return new OpprettAvtale(deltakerFnr, bedriftNr, tiltakstype);
    }

    private static OpprettMentorAvtale lagOpprettMentorAvtale(Tiltakstype tiltakstype) {
        Fnr deltakerFnr = new Fnr("00000000000");
        BedriftNr bedriftNr = new BedriftNr("999999999");
        Fnr mentorFnr = new Fnr("23090170716");
        return new OpprettMentorAvtale(deltakerFnr, mentorFnr, bedriftNr, tiltakstype, Avtalerolle.VEILEDER);
    }

    public static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    public static EndreInkluderingstilskudd endringMedNyeInkluderingstilskudd(List<Inkluderingstilskuddsutgift> eksisterendeUtgifter) {
        EndreInkluderingstilskudd endreInkluderingstilskudd = new EndreInkluderingstilskudd();
        endreInkluderingstilskudd.getInkluderingstilskuddsutgift().addAll(eksisterendeUtgifter);
        endreInkluderingstilskudd.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(13337, InkluderingstilskuddsutgiftType.PROGRAMVARE));
        return endreInkluderingstilskudd;
    }

    public static EndreAvtale endreInkluderingstilskuddInfo(EndreAvtale endreAvtale) {
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(13337, InkluderingstilskuddsutgiftType.PROGRAMVARE));
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(25697, InkluderingstilskuddsutgiftType.OPPLÆRING));
        endreAvtale.getInkluderingstilskuddsutgift().add(TestData.enInkluderingstilskuddsutgift(7195, InkluderingstilskuddsutgiftType.UTSTYR));
        endreAvtale.setInkluderingstilskuddBegrunnelse("Behov for tilskudd til dyre programvarelisenser og opplæring i disse.");
        return endreAvtale;
    }

    public static EndreAvtale endreKontaktInfo(EndreAvtale endreAvtale) {
        endreAvtale.setDeltakerFornavn("Dagny");
        endreAvtale.setDeltakerEtternavn("Deltaker");
        endreAvtale.setDeltakerTlf("40000000");
        endreAvtale.setBedriftNavn("Pers butikk");
        endreAvtale.setArbeidsgiverFornavn("Per");
        endreAvtale.setArbeidsgiverEtternavn("Kremmer");
        endreAvtale.setArbeidsgiverTlf("99999999");
        endreAvtale.setVeilederFornavn("Vera");
        endreAvtale.setVeilederEtternavn("Veileder");
        endreAvtale.setVeilederTlf("44444444");
        endreAvtale.setHarFamilietilknytning(true);
        endreAvtale.setFamilietilknytningForklaring("En middels god forklaring");
        return endreAvtale;
    }

    public static EndreAvtale endreMaalInfo(EndreAvtale endreAvtale) {
        endreAvtale.getMaal().add(TestData.etMaal());
        return endreAvtale;
    }

    public static EndreAvtale endreMentorInfo(EndreAvtale endreAvtale) {
        endreAvtale.setMentorFornavn("Mentor");
        endreAvtale.setMentorEtternavn("Mentorsen");
        endreAvtale.setMentorOppgaver("Mentoroppgaver");
        endreAvtale.setMentorAntallTimer(10.0);
        endreAvtale.setMentorTlf("44444444");
        endreAvtale.setMentorTimelonn(1000);
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleArbeidstreningFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreKontaktInfo(endreAvtale);
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setStillingprosent(BigDecimal.valueOf(50.5));
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.getMaal().add(TestData.etMaal());
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(12).minusDays(1));
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setOppfolging("Telefon hver uke");
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleInkluderingstilskuddFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreKontaktInfo(endreAvtale);
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(2));
        endreInkluderingstilskuddInfo(endreAvtale);
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleLønnstilskuddFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreKontaktInfo(endreAvtale);
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(12).minusDays(1));
        endreAvtale.setStillingprosent(BigDecimal.valueOf(50.7));
        endreAvtale.setArbeidsoppgaver("Butikkarbeid");
        endreAvtale.setArbeidsgiverKontonummer("000111222");
        endreAvtale.setStillingstittel("Butikkbetjent");
        endreAvtale.setStillingStyrk08(5223);
        endreAvtale.setStillingKonseptId(112968);
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setManedslonn(10000);
        endreAvtale.setFeriepengesats(BigDecimal.ONE);
        endreAvtale.setArbeidsgiveravgift(BigDecimal.ONE);
        endreAvtale.setOtpSats(0.02);
        endreAvtale.setStillingstype(Stillingstype.FAST);
        endreAvtale.setAntallDagerPerUke(BigDecimal.valueOf(5.0));
        endreAvtale.setRefusjonKontaktperson(new RefusjonKontaktperson("Ola", "Olsen", "12345678", true));
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleLønnstilskuddFelterForSommerjobb(int prosentLonnstilskudd) {
        EndreAvtale endreAvtale = endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(prosentLonnstilskudd);
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusWeeks(4).minusDays(1));
        return endreAvtale;
    }

    public static EndreAvtale endringPåAlleMentorFelter() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreKontaktInfo(endreAvtale);
        endreAvtale.setOppfolging("Telefon hver uke");
        endreAvtale.setTilrettelegging("Ingen");
        endreAvtale.setStartDato(Now.localDate());
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(6).minusDays(1));
        endreMentorInfo(endreAvtale);
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
        return new InnloggetBeslutter(new NavIdent("F888888"), Set.of(ENHET_OPPFØLGING));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        return new Arbeidsgiver(new Fnr("01234567890"), Set.of(), Map.of(), List.of(), persondataService, null, null, null);
    }

    public static Mentor enMentor(Avtale avtale) {
        return new Mentor(avtale.getMentorFnr());
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        PersondataService persondataService = mock(PersondataService.class);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(persondataService.hentNavn(any())).thenReturn(new Navn("Donald", "", "Duck"));

        return new Arbeidsgiver(
                TestData.etFodselsnummer(),
                Set.of(new AltinnReportee("Bedriftnavn", "", null, avtale.getBedriftNr().asString(), "", "", null))
                , Map.of(avtale.getBedriftNr(),
                List.of(Tiltakstype.values())),
                List.of(),
                persondataService,
                null,
                null,
                null
        );
    }

    public static Fnr etFodselsnummer() {
        return new Fnr("00000000000");
    }

    public static void setGeoNavEnhet(Avtale avtale, NavEnhet geoNavEnhet) {
        if (avtale.getEnhetGeografisk() == null) {
            avtale.setEnhetGeografisk(geoNavEnhet.getVerdi());
            avtale.setEnhetsnavnGeografisk(geoNavEnhet.getNavn());
        }
    }

    public static void setOppfolgingNavEnhet(Avtale avtale, NavEnhet oppfolgingNavEnhet) {
        if (avtale.getEnhetOppfolging() == null) {
            avtale.setEnhetOppfolging(oppfolgingNavEnhet.getVerdi());
            avtale.setEnhetsnavnOppfolging(oppfolgingNavEnhet.getNavn());
        }
    }

    public static void setupVeilederMock(
            Avtale avtale,
            Avtalepart<NavIdent> veileder,
            TilgangskontrollService tilgangskontrollService,
            VeilarboppfolgingService veilarboppfolgingService,
            PersondataService persondataService,
            Norg2Client norg2Client
    ) {
        lenient().when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                eq((Veileder) veileder),
                eq(avtale.getDeltakerFnr())
        )).thenReturn(true);

        lenient().when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(any()))
                .thenReturn(
                        new Oppfølgingsstatus(
                                Formidlingsgruppe.ARBEIDSSOKER,
                                Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                                avtale.getEnhetOppfolging()
                        )
                );
        when(persondataService.hentDiskresjonskode(avtale.getDeltakerFnr())).thenReturn(Diskresjonskode.UGRADERT);

        when(norg2Client.hentGeografiskEnhet(any()))
                .thenReturn(new Norg2GeoResponse(
                        avtale.getEnhetsnavnOppfolging(),
                        avtale.getEnhetOppfolging()
                ));
    }

    public static Veileder enVeileder(Avtale avtale) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        VeilarboppfolgingService veilarboppfolgingService = mock(VeilarboppfolgingService.class);
        PersondataService persondataService  = mock(PersondataService.class);
        EregService eregService  = mock(EregService.class);

        var veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(new NavEnhet("4802", "Oslo gamlebyen")),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService,
                eregService
        );
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        when(persondataService.hentNavn(any(Fnr.class))).thenReturn(Navn.TOMT_NAVN);
        when(eregService.hentVirksomhet(any())).thenReturn(new Organisasjon(TestData.etBedriftNr(), "Arbeidsplass AS"));

        when(
            tilgangskontrollService.harSkrivetilgangTilKandidat(
                eq(veileder),
                eq(avtale.getDeltakerFnr())
            )
        ).thenReturn(true);

        when(veilarboppfolgingService.hentOgSjekkOppfolgingstatus(any()))
            .thenReturn(
                new Oppfølgingsstatus(
                    Formidlingsgruppe.ARBEIDSSOKER,
                    Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                    "0906"
                )
            );
        when(veilarboppfolgingService.hentOppfolgingsstatus(anyString()))
            .thenReturn(
                new Oppfølgingsstatus(
                    Formidlingsgruppe.ARBEIDSSOKER,
                    Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS,
                    "0906"
                )
            );

        return veileder;
    }

    public static Beslutter enBeslutter(Avtale avtale) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        Norg2Client norg2Client = mock(Norg2Client.class);
        PersondataService persondataService = mock(PersondataService.class);
        NavIdent navIdent = new NavIdent("B999999");
        var beslutter = new Beslutter(navIdent, UUID.randomUUID(), Set.of(), tilgangskontrollService, norg2Client, persondataService, TestData.INGEN_AD_GRUPPER);
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(beslutter, avtale.getDeltakerFnr())).thenReturn(true);
        when(norg2Client.hentOppfølgingsEnhet(eq("0000"))).thenReturn(new Norg2OppfølgingResponse(0, "0000", "Oslo", Norg2EnhetStatus.AKTIV));
        when(norg2Client.hentOppfølgingsEnhet(eq("0906"))).thenReturn(new Norg2OppfølgingResponse(906, "0906", "Oslo", Norg2EnhetStatus.AKTIV));
        return beslutter;
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

    public static GodkjentPaVegneAvArbeidsgiverGrunn enGodkjentPaVegneAvArbeidsgiverGrunn() {
        GodkjentPaVegneAvArbeidsgiverGrunn godkjentPaVegneAvArbeidsgiverGrunn = new GodkjentPaVegneAvArbeidsgiverGrunn();
        godkjentPaVegneAvArbeidsgiverGrunn.setKlarerIkkeGiFaTilgang(true);
        return  godkjentPaVegneAvArbeidsgiverGrunn;
    }

    public static GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn enGodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn() {
        GodkjentPaVegneAvArbeidsgiverGrunn arbeidsgiverGrunn = new GodkjentPaVegneAvArbeidsgiverGrunn();
        arbeidsgiverGrunn.setArenaMigreringArbeidsgiver(true);
        GodkjentPaVegneGrunn paVegneGrunn = new GodkjentPaVegneGrunn();
        paVegneGrunn.setIkkeBankId(true);
        GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn paVegneAvDeltakerOgArbeidsgiverGrunn = new GodkjentPaVegneAvDeltakerOgArbeidsgiverGrunn(arbeidsgiverGrunn, paVegneGrunn);
        return paVegneAvDeltakerOgArbeidsgiverGrunn;
    }

    public static InnloggetArbeidsgiver innloggetArbeidsgiver(Avtalepart<Fnr> avtalepartMedFnr, BedriftNr bedriftNr) {
        Map<BedriftNr, Collection<Tiltakstype>> tilganger = Map.of(bedriftNr, Set.of(Tiltakstype.values()));
        AltinnReportee altinnOrganisasjon = new AltinnReportee("Bedriften AS", "Business", bedriftNr.asString(), "BEDR", "Active", null, null);
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
                        new RefusjonKontaktperson("Atle", "Jørgensen", "12345678", true)
                ),
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
        return enVeileder(navIdent, mock(VeilarboppfolgingService.class));
    }

    public static Veileder enVeileder(NavIdent navIdent, VeilarboppfolgingService veilarboppfolgingService) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        PersondataService persondataService = mock(PersondataService.class);
        var veileder = new Veileder(
                navIdent,
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(ENHET_OPPFØLGING),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                veilarboppfolgingService,
                featureToggleService,
                mock(EregService.class)
        );
        when(tilgangskontrollService.hentSkrivetilgang(any(Veileder.class), any(Fnr.class))).thenReturn(new Tilgang.Tillat());
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(eq(veileder), any())).thenReturn(true);
        when(persondataService.hentDiskresjonskode(any(Fnr.class))).thenReturn(Diskresjonskode.UGRADERT);
        return veileder;
    }

    public static Veileder enVeileder(Avtale avtale, PersondataService persondataService) {
        TilgangskontrollService tilgangskontrollService = mock(TilgangskontrollService.class);
        var veileder = new Veileder(
                avtale.getVeilederNavIdent(),
                null,
                tilgangskontrollService,
                persondataService,
                mock(Norg2Client.class),
                Set.of(ENHET_OPPFØLGING),
                new SlettemerkeProperties(),
                TestData.INGEN_AD_GRUPPER,
                mock(VeilarboppfolgingService.class),
                featureToggleService,
                mock(EregService.class)
        );
        when(tilgangskontrollService.harSkrivetilgangTilKandidat(
                veileder,
                avtale.getDeltakerFnr())
        ).thenReturn(true);
        return veileder;
    }

    public static TilskuddPeriode enTilskuddPeriode() {
        return TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder().getTilskuddPeriode().first();
    }

    public static EndreTilskuddsberegning enEndreTilskuddsberegning() {
        double otpSats = 0.048;
        BigDecimal feriepengesats = new BigDecimal("0.166");
        BigDecimal arbeidsgiveravgift = BigDecimal.ZERO;
        int manedslonn = 44444;
        return EndreTilskuddsberegning.builder().otpSats(otpSats).feriepengesats(feriepengesats).arbeidsgiveravgift(arbeidsgiveravgift).manedslonn(manedslonn).build();
    }

    public static Avtale enArbeidstreningAvtaleMedAltUtfylt() {
        Avtale avtale = TestData.enArbeidstreningAvtale();
        avtale.endreAvtale(endringPåAlleArbeidstreningFelter(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enArbeidstreningAvtaleGodkjentAvVeileder() {
        Avtale avtale = enArbeidstreningAvtaleMedAltUtfylt();
        avtale.getGjeldendeInnhold().setGodkjentAvArbeidsgiver(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvDeltaker(Now.localDateTime());
        avtale.getGjeldendeInnhold().setGodkjentAvVeileder(Now.localDateTime());
        avtale.getGjeldendeInnhold().setAvtaleInngått(Now.localDateTime());
        avtale.getGjeldendeInnhold().setIkrafttredelsestidspunkt(Now.localDateTime());
        avtale.getGjeldendeInnhold().setJournalpostId("1");
        return avtale;
    }
}
