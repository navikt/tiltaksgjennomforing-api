package no.nav.tag.tiltaksgjennomforing.domene;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import no.nav.tag.tiltaksgjennomforing.domene.exceptions.TiltaksgjennomforingException;

public class BedriftNrTest {

    @Test(expected = TiltaksgjennomforingException.class)
    public void bedriftNrSkalIkkeVaereTomt() {
        new BedriftNr("");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void bedriftNrSkalIkkeHaMindreEnn9Siffer() {
        new BedriftNr("123");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void bedriftNrSkalIkkeHaMerEnn9Siffer() {
        new BedriftNr("1234567890123");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void bedriftNrSkalIkkeInneholdeBokstaver() {
        new BedriftNr("12345678a");
    }

    @Test(expected = TiltaksgjennomforingException.class)
    public void bedriftNrSkalIkkeInneholdeAndreTingEnnTall() {
        new BedriftNr("12345678 ");
    }

    @Test
    public void gyldigBedriftNr() {
        assertThat(new BedriftNr(TestData.GYLDIG_BEDRIFTSNR).getBedriftNr()).isEqualTo(TestData.GYLDIG_BEDRIFTSNR);
    }
}
