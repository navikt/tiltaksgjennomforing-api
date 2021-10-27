package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import org.junit.Test;

import java.time.LocalDate;

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
        assertThat(new Fnr(gyldigFnr).asString()).isEqualTo(gyldigFnr);
    }

    @Test
    public void testFnr1() {
        Fnr fnrOver16 = new Fnr("29110976648");
        assertThat(fnrOver16.erUnder16år()).isTrue();
        int year = LocalDate.now().getYear();
        assertThat(fnrOver16.erOver30år()).isFalse();
    }

    @Test
    public void testFnr2() {
        Fnr fnr = new Fnr("19109613897");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();
    }

    @Test
    public void testFnr3() {
        Fnr fnr = new Fnr("25128626630");
        assertThat(fnr.erOver30år()).isTrue();
        assertThat(fnr.erUnder16år()).isFalse();
    }

    @Test
    public void testFnr4() {
        Fnr fnr = new Fnr("23029149054");
        assertThat(fnr.erOVer30årFørsteJanuar()).isFalse();
        assertThat(fnr.erUnder16år()).isFalse();
    }

    @Test
    public void testFnr5() {
        final Fnr fnr = new Fnr("23029149054");
        final Avtale avtale = TestData.enSommerjobbAvtaleGodkjentAvArbeidsgiver();
        AvtaleInnhold avtaleInnhold = avtale.gjeldendeInnhold();
        avtaleInnhold.setStartDato(LocalDate.parse("2021-06-01"));
        assertThat(fnr.erOVer30årFørsteJanuar()).isFalse();
        assertThat(fnr.erOver30årFraOppstartDato(avtaleInnhold.getStartDato())).isTrue();
    }


    @Test
    public void testDnr1() {
        Fnr fnr = new Fnr("49120799125");
        assertThat(fnr.erUnder16år()).isTrue();
        assertThat(fnr.erOver30år()).isFalse();
    }
    @Test
    public void testDnr2() {
        Fnr fnr = new Fnr("64090099076");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();
    }


}