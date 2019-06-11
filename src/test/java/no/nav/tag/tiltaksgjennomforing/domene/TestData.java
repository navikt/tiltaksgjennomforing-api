package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestData {
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
        Fnr deltakerFnr = new Fnr("88888899999");
        BedriftNr bedriftNr = new BedriftNr("12345678");
        return new OpprettAvtale(deltakerFnr, bedriftNr);
    }

    public static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    static EndreAvtale endringPaAlleFelt() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Fornavn");
        endreAvtale.setDeltakerEtternavn("Etternavn");
        endreAvtale.setBedriftNavn("Bedriftnavn");
        endreAvtale.setBedriftNr(new BedriftNr("12345678"));
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
        return new Deltaker(new Fnr("01234567890"), enAvtale());
    }

    public static Deltaker enDeltaker(Avtale avtale) {
        return new Deltaker(avtale.getDeltakerFnr(), avtale);
    }

    public static InnloggetSelvbetjeningBruker enSelvbetjeningBruker() {
        return new InnloggetSelvbetjeningBruker(new Fnr("99999999999"));
    }

    public static InnloggetNavAnsatt enNavAnsatt() {
        return new InnloggetNavAnsatt(new NavIdent("F8888888"));
    }

    public static Arbeidsgiver enArbeidsgiver() {
        return new Arbeidsgiver(new Fnr("12345678901"), enAvtale());
    }

    public static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(TestData.etFodselsnummer(), avtale);
    }

    public static Fnr etFodselsnummer() {
        return new Fnr("00000000000");
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
