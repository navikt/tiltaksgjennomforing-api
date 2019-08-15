package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;

import static java.time.LocalDate.of;
import static no.bekk.bekkopen.person.FodselsnummerCalculator.getFodselsnummerForDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestData {
    
    public static final String GYLDIG_FNR = "12089512415";
    public static final String GYLDIG_BEDRIFTSNR = "156544825";

    public static Avtale enAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
    }

    public static Avtale enAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
        avtale.endreAvtale(avtale.getVersjon(), endringPaAlleFelt(), Avtalerolle.VEILEDER);
        return avtale;
    }

    public static Avtale enAvtaleMedAltUtfyltGodkjentAvVeileder() {
        Avtale avtale = enAvtaleMedAltUtfylt();
        avtale.setGodkjentAvArbeidsgiver(LocalDateTime.now());
        return avtale;
    }

    private static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr(GYLDIG_FNR);
        BedriftNr bedriftNr = new BedriftNr(GYLDIG_BEDRIFTSNR);
        return new OpprettAvtale(deltakerFnr, bedriftNr);
    }

    public static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    static EndreAvtale endringPaAlleFelt() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Fornavn");
        endreAvtale.setDeltakerEtternavn("Etternavn");
        endreAvtale.setDeltakerTlf("22334455");
        endreAvtale.setBedriftNavn("Bedriftnavn");
        endreAvtale.setBedriftNr(new BedriftNr(GYLDIG_BEDRIFTSNR));
        endreAvtale.setArbeidsgiverFornavn("AG fornavn");
        endreAvtale.setArbeidsgiverEtternavn("AG etternavn");
        endreAvtale.setArbeidsgiverTlf("AG tlf");
        endreAvtale.setVeilederFornavn("Veilederfornavn");
        endreAvtale.setVeilederEtternavn("Veilederetternavn");
        endreAvtale.setVeilederTlf("Veiledertlf");
        endreAvtale.setOppfolging("Oppfolging");
        endreAvtale.setTilrettelegging("Tilrettelegging");
        endreAvtale.setStartDato(LocalDate.now());
        endreAvtale.setArbeidstreningLengde(2);
        endreAvtale.setArbeidstreningStillingprosent(50);
        endreAvtale.setMaal(List.of(TestData.etMaal(), TestData.etMaal()));
        endreAvtale.setOppgaver(List.of(TestData.enOppgave(), TestData.enOppgave()));
        return endreAvtale;
    }

    static Deltaker enDeltaker() {
        return new Deltaker(etFodselsnummer(), enAvtale());
    }

    public static Deltaker enDeltaker(Avtale avtale) {
        return new Deltaker(avtale.getDeltakerFnr(), avtale);
    }

    public static InnloggetSelvbetjeningBruker enSelvbetjeningBruker() {
        return new InnloggetSelvbetjeningBruker(etFodselsnummer());
    }

    public static InnloggetNavAnsatt enNavAnsatt() {
        return new InnloggetNavAnsatt(new NavIdent("F8888888"));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        return new Arbeidsgiver(etFodselsnummer(), enAvtale());
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(TestData.etFodselsnummer(), avtale);
    }

    public static Fnr etFodselsnummer() {
        return etFodselsnummerForDato(1992, 9, 17);
    }

    public static Fnr etFodselsnummerForDato(int year, int month, int dayOfMonth) {
        return new Fnr(getFodselsnummerForDate(new Date((of(year, month, dayOfMonth).toEpochSecond(LocalTime.now(), ZoneOffset.UTC)*1000))).toString());
    }
    public static Veileder enVeileder() {
        return new Veileder(new NavIdent("X123456"), enAvtale());
    }

    public static Veileder enVeileder(Avtale avtale) {
        return new Veileder(avtale.getVeilederNavIdent(), avtale);
    }

    public static Oppgave enOppgave() {
        return new Oppgave();
    }

    public static Maal etMaal() {
        return new Maal();
    }

    public static GodkjentPaVegneGrunn enGodkjentPaVegneGrunn() {
        GodkjentPaVegneGrunn paVegneGrunn = new GodkjentPaVegneGrunn();
        paVegneGrunn.setIkkeBankId(true);
        return paVegneGrunn;
    }

    public static InnloggetSelvbetjeningBruker innloggetSelvbetjeningBrukerMedOrganisasjon(Avtalepart<Fnr> avtalepartMedFnr) {
        InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = new InnloggetSelvbetjeningBruker(avtalepartMedFnr.getIdentifikator());
        Organisasjon organisasjon = new Organisasjon(avtalepartMedFnr.getAvtale().getBedriftNr(), avtalepartMedFnr.getAvtale().getBedriftNavn());
        innloggetSelvbetjeningBruker.setOrganisasjoner(Arrays.asList(organisasjon));
        return innloggetSelvbetjeningBruker;
    }

    public static InnloggetSelvbetjeningBruker innloggetSelvbetjeningBrukerUtenOrganisasjon(Avtalepart<Fnr> avtalepartMedFnr) {
        InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker = new InnloggetSelvbetjeningBruker(avtalepartMedFnr.getIdentifikator());
        return innloggetSelvbetjeningBruker;
    }

    public static InnloggetNavAnsatt innloggetNavAnsatt(Avtalepart<NavIdent> avtalepartMedNavIdent) {
        return new InnloggetNavAnsatt(avtalepartMedNavIdent.getIdentifikator());
    }

    public static Identifikator enIdentifikator() {
        return new Identifikator() {
            @Override
            public String asString() {
                return "test-id";
            }
        };
    }

}
