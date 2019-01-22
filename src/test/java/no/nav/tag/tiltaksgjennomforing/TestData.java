package no.nav.tag.tiltaksgjennomforing;

import no.nav.tag.tiltaksgjennomforing.domene.*;

public class TestData {
    // TODO: Rename
    static Avtale minimalAvtale() {
        NavIdent veilderNavIdent = new NavIdent("X123456");
        return Avtale.nyAvtale(lagOpprettAvtale(), veilderNavIdent);
    }

    static OpprettAvtale lagOpprettAvtale() {
        Fnr deltakerFnr = new Fnr("12345678901");
        Fnr arbeidsgiverFnr = new Fnr("01234567890");
        return new OpprettAvtale(deltakerFnr, arbeidsgiverFnr);
    }

    static EndreAvtale ingenEndring() {
        return new EndreAvtale();
    }

    public static Bruker deltaker() {
        return new Bruker("01234567890");
    }

    static Bruker deltaker(Avtale avtale) {
        return new Bruker(avtale.getDeltakerFnr());
    }

    static Bruker arbeidsgiver() {
        return new Bruker("12345678901");
    }

    static Bruker arbeidsgiver(Avtale avtale) {
        return new Bruker(avtale.getArbeidsgiverFnr());
    }

    public static Veileder veileder() {
        return new Veileder("X123456");
    }

    static Veileder veileder(Avtale avtale) {
        return new Veileder(avtale.getVeilederNavIdent());
    }

    static Oppgave minimalOppgave() {
        return new Oppgave();
    }

    static Maal minimaltMaal() {
        return new Maal();
    }
}
