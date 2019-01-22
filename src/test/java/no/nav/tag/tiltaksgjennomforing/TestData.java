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

    static Veileder veileder() {
        return new Veileder("X123456");
    }

    static Oppgave minimalOppgave() {
        return new Oppgave();
    }

    static Maal minimaltMaal() {
        return new Maal();
    }
}
