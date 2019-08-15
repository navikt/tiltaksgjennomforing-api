package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FnrTest {

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeVaereTomt() {
        new Fnr("");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeHaMindreEnn11Siffer() {
        new Fnr("123");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeHaMerEnn11Siffer() {
        new Fnr("1234567890123");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeInneholdeBokstaver() {
        new Fnr("1234567890a");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeInneholdeAndreTingEnnTall() {
        new Fnr("12345678900 ");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrKanIkkeHaUgyldigMnd() {
        new Fnr("01234567890");
    }
    
    @Test
    public void gyldigFnr() {
        assertThat(new Fnr(TestData.GYLDIG_FNR).getFnr()).isEqualTo(TestData.GYLDIG_FNR);
    }
}