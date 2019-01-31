package no.nav.tag.tiltaksgjennomforing.domene;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test(expected = TiltaksgjennomforingException.class)
    public void fnrSkalIkkeInneholdeAndreTingEnnTall() {
        Fnr fnr = new Fnr("12345678900 ");
    }

    @Test
    public void fnrSkalInneholde11Tall() {
        String gyldigFnr = "01234567890";
        assertThat(new Fnr(gyldigFnr).getFnr()).isEqualTo(gyldigFnr);
    }
}