package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;

public class FnrTest {

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeVaereTomt() {
        Fnr fnr = new Fnr("");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeHaMindreEnn11Siffer() {
        Fnr fnr = new Fnr("123");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeHaMerEnn11Siffer() {
        Fnr fnr = new Fnr("1234567890123");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeInneholdeBokstaver() {
        Fnr fnr = new Fnr("1234567890a");
    }
}