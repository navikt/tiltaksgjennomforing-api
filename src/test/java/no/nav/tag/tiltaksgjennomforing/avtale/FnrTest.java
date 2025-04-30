package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.exceptions.TiltaksgjennomforingException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FnrTest {

    @Test
    public void fnrKanVæreNull(){
        assertThat(new Fnr(null)).isEqualTo(new Fnr(null));
    }
    @Test
    public void fnrSkalIkkeVaereTomt() {
        assertThatThrownBy(() -> new Fnr("")).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void fnrSkalIkkeHaMindreEnn11Siffer() {
        assertThatThrownBy(() -> new Fnr("123")).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void fnrSkalIkkeHaMerEnn11Siffer() {
        assertThatThrownBy(() -> new Fnr("1234567890123")).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void fnrSkalIkkeInneholdeBokstaver() {
        assertThatThrownBy(() -> new Fnr("1234567890a")).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void fnrSkalIkkeInneholdeAndreTingEnnTall() {
        assertThatThrownBy(() -> new Fnr("12345678900 ")).isExactlyInstanceOf(TiltaksgjennomforingException.class);
    }

    @Test
    public void fnrSkalInneholde11Tall() {
        String gyldigFnr = "01234567890";
        assertThat(new Fnr(gyldigFnr).asString()).isEqualTo(gyldigFnr);
    }

    @Test
    public void testFnr1() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Fnr fnrOver16 = new Fnr("29110976648");
        assertThat(fnrOver16.erUnder16år()).isTrue();
        assertThat(fnrOver16.erOver30år()).isFalse();
        Now.resetClock();
    }

    @Test
    public void testFnr2() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Fnr fnr = new Fnr("19109613897");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();
        Now.resetClock();
    }

    @Test
    public void testFnr3() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Fnr fnr = new Fnr("25128626630");
        assertThat(fnr.erOver30år()).isTrue();
        assertThat(fnr.erUnder16år()).isFalse();
        Now.resetClock();
    }

    @Test
    public void testFnr4() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Fnr fnr = new Fnr("23029149054");
        assertThat(fnr.erOver30årFørsteJanuar()).isFalse();
        assertThat(fnr.erUnder16år()).isFalse();
        Now.resetClock();
    }

    @Test
    public void testFnr5() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        final Fnr fnr = new Fnr("23029149054");
        LocalDate startDato = LocalDate.of(2022, 1, 5);
        assertThat(fnr.erOver30årFørsteJanuar()).isFalse();
        assertThat(fnr.erOver30årFraOppstartDato(startDato)).isTrue();
        Now.resetClock();
    }

    @Test
    public void testDnr1() {
        Now.fixedDate(LocalDate.of(2023, 11, 1));
        Fnr fnr = new Fnr("49120799125");
        assertThat(fnr.erUnder16år()).isTrue();
        assertThat(fnr.erOver30år()).isFalse();
        Now.resetClock();
    }

    @Test
    public void testDnr2() {
        Now.fixedDate(LocalDate.of(2023, 12, 1));
        Fnr fnr = new Fnr("64090099076");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();
        Now.resetClock();
    }

    @Test
    void testAtAldersjekkKanGjøresPåSyntetiskFnr() {
        Now.fixedDate(LocalDate.of(2023, 6, 1));
        Fnr fnr = new Fnr("07459742977");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();
        Now.resetClock();
    }

    @Test
    void testAtAldersjekkKanGjøresPåSyntetiskFnrFraSkatteEtaten() {
        Now.fixedDate(LocalDate.of(2023, 6, 1));
        Fnr fnr = new Fnr("21899797180");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();
        Now.resetClock();
    }

    @Test
    void equalsOgHashCode() {
        assertThat(new Fnr("00000000000").equals(new Fnr("00000000000"))).isTrue();
        assertThat(new Fnr("12345678910").equals(new Fnr("12345678910"))).isTrue();

        assertThat(new Fnr("00000000000").hashCode()).isEqualTo(new Fnr("00000000000").hashCode());
        assertThat(new Fnr("12345678910").hashCode()).isEqualTo(new Fnr("12345678910").hashCode());
    }

    @Test
    public void er67Aar() {
        Now.fixedDate(LocalDate.of(2025, 1, 1));
        Fnr fnr = new Fnr("01015826670");
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate())).isTrue();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().plusDays(1))).isTrue();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().minusYears(67))).isFalse();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().minusDays(1))).isFalse();
        Now.resetClock();
    }
}
