package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetNavAnsatt;
import no.nav.tag.tiltaksgjennomforing.domene.autorisasjon.InnloggetSelvbetjeningBruker;

import java.time.LocalDate;
import java.util.List;

public class TestData {
    public static Avtale enAvtale() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        return Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
    }

    static Avtale enAvtaleMedAltUtfylt() {
        NavIdent veilderNavIdent = new NavIdent("Z123456");
        Avtale avtale = Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
        avtale.endreAvtale(avtale.getVersjon(), endringPaAlleFelt());
        return avtale;
    }

    private static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("88888899999");
        Fnr arbeidsgiverFnr = new Fnr("77777788888");
        return new OpprettAvtale(deltakerFnr, arbeidsgiverFnr, "");
    }

    public static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    static EndreAvtale endringPaAlleFelt() {
        EndreAvtale endreAvtale = new EndreAvtale();
        endreAvtale.setDeltakerFornavn("Fornavn");
        endreAvtale.setDeltakerEtternavn("Etternavn");
        endreAvtale.setBedriftNavn("Bedriftnavn");
        endreAvtale.setBedriftNr("12345678");
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

    static Arbeidsgiver enArbeidsgiver(Avtale avtale) {
        return new Arbeidsgiver(avtale.getArbeidsgiverFnr(), avtale);
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

    public static InnloggetSelvbetjeningBruker innloggetSelvbetjeningBruker(Avtalepart<Fnr> avtalepartMedFnr) {
        return new InnloggetSelvbetjeningBruker(avtalepartMedFnr.getIdentifikator());
    }

    public static InnloggetNavAnsatt innloggetNavAnsatt(Avtalepart<NavIdent> avtalepartMedNavIdent) {
        return new InnloggetNavAnsatt(avtalepartMedNavIdent.getIdentifikator());
    }
}
