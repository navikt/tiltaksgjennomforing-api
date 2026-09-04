package no.nav.tag.tiltaksgjennomforing.avtale;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FnrTest {

    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    @Test
    public void fnrKanIkkeVæreNull(){
        assertThatThrownBy(() -> new Fnr(null)).isExactlyInstanceOf(FeilkodeException.class)
                .hasMessage(Feilkode.FØDSELSNUMMER_IKKE_GYLDIG.name());
        assertThatThrownBy(() -> Fnr.fraDb(null)).isExactlyInstanceOf(FeilkodeException.class)
            .hasMessage(Feilkode.FØDSELSNUMMER_IKKE_GYLDIG.name());
    }

    @Test
    public void fnrSkalIkkeVaereTomt() {
        assertThatThrownBy(() -> new Fnr("")).isExactlyInstanceOf(FeilkodeException.class);
        assertThatThrownBy(() -> Fnr.fraDb("")).isExactlyInstanceOf(FeilkodeException.class);
    }

    @Test
    public void fnrSkalIkkeHaMindreEnn11Siffer() {
        assertThatThrownBy(() -> new Fnr("123")).isExactlyInstanceOf(FeilkodeException.class);
        assertThatThrownBy(() -> Fnr.fraDb("123")).isExactlyInstanceOf(FeilkodeException.class);
    }

    @Test
    public void fnrSkalIkkeHaMerEnn11Siffer() {
        assertThatThrownBy(() -> new Fnr("1234567890123")).isExactlyInstanceOf(FeilkodeException.class);
        assertThatThrownBy(() -> Fnr.fraDb("1234567890123")).isExactlyInstanceOf(FeilkodeException.class);
    }

    @Test
    public void fnrSkalIkkeInneholdeBokstaver() {
        assertThatThrownBy(() -> new Fnr("1234567890a")).isExactlyInstanceOf(FeilkodeException.class);
        assertThatThrownBy(() -> Fnr.fraDb("1234567890a")).isExactlyInstanceOf(FeilkodeException.class);
    }

    @Test
    public void fnrSkalIkkeInneholdeAndreTingEnnTall() {
        assertThatThrownBy(() -> new Fnr("12345678900 ")).isExactlyInstanceOf(FeilkodeException.class);
        assertThatThrownBy(() -> Fnr.fraDb("12345678900 ")).isExactlyInstanceOf(FeilkodeException.class);
    }

    @Test
    public void fnrSkalInneholde11Tall() {
        String gyldigFnr = "00000000000";
        assertThat(new Fnr(gyldigFnr).asString()).isEqualTo(gyldigFnr);
        assertThat(Fnr.fraDb(gyldigFnr).asString()).isEqualTo(gyldigFnr);
    }

    @Test
    public void testFnr1() {
        Now.fixedDate(LocalDate.of(2013, 12, 20));
        Fnr fnrOver16 = new Fnr("13459803674");
        assertThat(fnrOver16.erUnder16år()).isTrue();
        assertThat(fnrOver16.erOver30år()).isFalse();

        fnrOver16 = Fnr.fraDb("13459803674");
        assertThat(fnrOver16.erUnder16år()).isTrue();
        assertThat(fnrOver16.erOver30år()).isFalse();

        Now.resetClock();
    }

    @Test
    public void testFnr2() {
        Now.fixedDate(LocalDate.of(2021, 12, 20));
        Fnr fnr = new Fnr("27479635477");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();

        fnr = Fnr.fraDb("27479635477");
        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();

        Now.resetClock();
    }

    @Test
    public void testFnr3() {
        Now.fixedDate(LocalDate.of(2026, 12, 20));
        Fnr fnr = new Fnr("27479635477");
        assertThat(fnr.erOver30år()).isTrue();
        assertThat(fnr.erUnder16år()).isFalse();

        fnr = Fnr.fraDb("27479635477");
        assertThat(fnr.erOver30år()).isTrue();
        assertThat(fnr.erUnder16år()).isFalse();

        Now.resetClock();
    }

    @Test
    public void testFnr4() {
        Now.fixedDate(LocalDate.of(1984, 12, 20));
        Fnr fnr = new Fnr("25506715337");
        assertThat(fnr.erOver30årFørsteJanuar()).isFalse();
        assertThat(fnr.erUnder16år()).isFalse();

        fnr = Fnr.fraDb("25506715337");
        assertThat(fnr.erOver30årFørsteJanuar()).isFalse();
        assertThat(fnr.erUnder16år()).isFalse();

        Now.resetClock();
    }

    @Test
    public void testFnr5() {
        Now.fixedDate(LocalDate.of(2024, 12, 20));
        LocalDate startDato = LocalDate.of(2025, 1, 5);

        Fnr fnr = new Fnr("17509432406");
        assertThat(fnr.erOver30årFørsteJanuar()).isFalse();
        assertThat(fnr.erOver30årFraOppstartDato(startDato)).isTrue();

        fnr = Fnr.fraDb("17509432406");
        assertThat(fnr.erOver30årFørsteJanuar()).isFalse();
        assertThat(fnr.erOver30årFraOppstartDato(startDato)).isTrue();

        Now.resetClock();
    }

    @Test
    public void testDnr1() {
        Now.fixedDate(LocalDate.of(2023, 11, 1));
        Fnr fnr = new Fnr("52512536379");

        assertThat(fnr.erUnder16år()).isTrue();
        assertThat(fnr.erOver30år()).isFalse();

        fnr = Fnr.fraDb("52512536379");
        assertThat(fnr.erUnder16år()).isTrue();
        assertThat(fnr.erOver30år()).isFalse();

        Now.resetClock();
    }

    @Test
    public void testDnr2() {
        Now.fixedDate(LocalDate.of(2023, 12, 1));
        Fnr fnr = new Fnr("58519743742");

        assertThat(fnr.erUnder16år()).isFalse();
        assertThat(fnr.erOver30år()).isFalse();

        fnr = Fnr.fraDb("58519743742");
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

        fnr = Fnr.fraDb("07459742977");
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
        assertThat(Fnr.fraDb("00000000000").equals(new Fnr("00000000000"))).isTrue();
        assertThat(new Fnr("12345678910").equals(Fnr.fraDb("12345678910"))).isTrue();

        assertThat(new Fnr("00000000000").hashCode()).isEqualTo(new Fnr("00000000000").hashCode());
        assertThat(new Fnr("12345678910").hashCode()).isEqualTo(new Fnr("12345678910").hashCode());
        assertThat(Fnr.fraDb("00000000000").hashCode()).isEqualTo(new Fnr("00000000000").hashCode());
        assertThat(new Fnr("12345678910").hashCode()).isEqualTo(Fnr.fraDb("12345678910").hashCode());

        assertThat(Fnr.fraDb("29462114573")).isEqualTo(new Fnr("29462114573"));
    }

    @Test
    public void er67Aar() {
        Now.fixedDate(LocalDate.of(2025, 1, 1));

        Fnr fnr = Fnr.generer(1958, 1, 1);
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate())).isTrue();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().plusDays(1))).isTrue();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().minusDays(1))).isFalse();

        Now.fixedDate(LocalDate.of(1997, 7, 13));
        fnr = Fnr.fraDb("13473036007");
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate())).isTrue();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().plusDays(1))).isTrue();
        assertThat(fnr.erOver67ÅrFraSluttDato(Now.localDate().minusDays(1))).isFalse();

        Now.resetClock();
    }

    @Test
    public void mocking_av_fnr_funker_ikke_uten_ALLOW_SYNTHETIC_NUMBERS() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;

        assertThatThrownBy(() -> new Fnr("00000000000")).isExactlyInstanceOf(FeilkodeException.class)
            .hasMessage(Feilkode.FØDSELSNUMMER_IKKE_GYLDIG.name());

        assertThatThrownBy(() -> new Fnr("12345678910")).isExactlyInstanceOf(FeilkodeException.class)
            .hasMessage(Feilkode.FØDSELSNUMMER_IKKE_GYLDIG.name());

        assertThatThrownBy(() -> Fnr.generer(1988, 12, 10))
            .isExactlyInstanceOf(NotImplementedException.class);

        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }


}
