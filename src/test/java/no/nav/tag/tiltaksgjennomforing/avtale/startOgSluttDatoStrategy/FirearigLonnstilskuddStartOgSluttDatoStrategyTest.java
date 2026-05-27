package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Stillingstype;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.FIREARIG_LONNSTILSKUDD;
import static no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy.FirearigLonnstilskuddStartOgSluttdatoStrategy.erForLangVarighet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class FirearigLonnstilskuddStartOgSluttDatoStrategyTest {

    private static final LocalDate OPPSTARTSDATO = LocalDate.of(2026, 8, 1);
    private static final LocalDate GYLDIG_START = OPPSTARTSDATO;
    private static final LocalDate GYLDIG_SLUTT = GYLDIG_START.plusYears(1);

    private Avtale avtale;

    @BeforeEach
    void setUp() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
        FirearigLonnstilskuddProperties props = new FirearigLonnstilskuddProperties();
        props.setDato(OPPSTARTSDATO);
        props.init();
        avtale = Avtale.opprett(
                new OpprettAvtale(Fnr.generer(25), TestData.etBedriftNr(), FIREARIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                TestData.enNavIdent()
        );
        avtale.setGodkjentForEtterregistrering(true);
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    private FirearigLonnstilskuddStartOgSluttdatoStrategy strategy() {
        return new FirearigLonnstilskuddStartOgSluttdatoStrategy(avtale);
    }

    @Test
    void null_startDato_kaster_ikke_exception() {
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(null, GYLDIG_SLUTT));
    }

    @Test
    void startDato_før_oppstartsdato_kaster_feilkode() {
        LocalDate forTidlig = OPPSTARTSDATO.minusDays(1);
        assertFeilkode(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_TIDLIG_OPPSTART,
                () -> strategy().sjekkStartOgSluttdato(forTidlig, GYLDIG_SLUTT));
    }

    @Test
    void startDato_lik_oppstartsdato_kaster_ikke_exception() {
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(OPPSTARTSDATO, null));
    }

    @Test
    void deltaker_over_30_på_startdato_kaster_feilkode() {
        LocalDate fødselsdato = GYLDIG_START.minusYears(31);
        avtale = Avtale.opprett(
                new OpprettAvtale(Fnr.generer(fødselsdato), TestData.etBedriftNr(), FIREARIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                TestData.enNavIdent()
        );
        avtale.setGodkjentForEtterregistrering(true);
        assertFeilkode(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_GAMMEL_FRA_OPPSTARTDATO,
                () -> strategy().sjekkStartOgSluttdato(GYLDIG_START, GYLDIG_SLUTT));
    }

    @Test
    void deltaker_nøyaktig_30_på_startdato_kaster_ikke_exception() {
        LocalDate fødselsdato = GYLDIG_START.minusYears(30);
        avtale = Avtale.opprett(
                new OpprettAvtale(Fnr.generer(fødselsdato), TestData.etBedriftNr(), FIREARIG_LONNSTILSKUDD),
                Avtaleopphav.VEILEDER,
                TestData.enNavIdent()
        );
        avtale.setGodkjentForEtterregistrering(true);
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(GYLDIG_START, GYLDIG_SLUTT));
    }

    @Test
    void null_sluttDato_kaster_ikke_exception() {
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(GYLDIG_START, null));
    }

    @Test
    void sluttDato_etter_2032_kaster_feilkode() {
        LocalDate forSen = LocalDate.of(2033, 1, 1);
        assertFeilkode(Feilkode.FIREARIG_LONNSTILSKUDD_FOR_SEN_SLUTTDATO,
                () -> strategy().sjekkStartOgSluttdato(GYLDIG_START, forSen));
    }

    @Test
    void sluttDato_nøyaktig_2032_12_31_kaster_ikke_exception() {
        // Start within 4 years of 2032-12-31 to avoid duration-check interference
        LocalDate start = LocalDate.of(2030, 1, 1);
        LocalDate sluttdatoGrenseverdi = LocalDate.of(2032, 12, 31);
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(start, sluttdatoGrenseverdi));
    }

    @Test
    void midlertidig_stilling_varighet_nøyaktig_2_år_kaster_feilkode() {
        avtale.getGjeldendeInnhold().setStillingstype(Stillingstype.MIDLERTIDIG);
        LocalDate slutt = GYLDIG_START.plusYears(2);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_FIREARIG_LONNSTILSKUDD_2_AAR,
                () -> strategy().sjekkStartOgSluttdato(GYLDIG_START, slutt));
    }

    @Test
    void midlertidig_stilling_varighet_under_2_år_kaster_ikke_exception() {
        avtale.getGjeldendeInnhold().setStillingstype(Stillingstype.MIDLERTIDIG);
        LocalDate slutt = GYLDIG_START.plusYears(2).minusDays(1);
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(GYLDIG_START, slutt));
    }

    @Test
    void fast_stilling_varighet_nøyaktig_4_år_kaster_feilkode() {
        avtale.getGjeldendeInnhold().setStillingstype(Stillingstype.FAST);
        LocalDate slutt = GYLDIG_START.plusYears(4);
        assertFeilkode(Feilkode.VARIGHET_FOR_LANG_FIREARIG_LONNSTILSKUDD_4_AAR,
                () -> strategy().sjekkStartOgSluttdato(GYLDIG_START, slutt));
    }

    @Test
    void fast_stilling_varighet_under_4_år_kaster_ikke_exception() {
        avtale.getGjeldendeInnhold().setStillingstype(Stillingstype.FAST);
        LocalDate slutt = GYLDIG_START.plusYears(4).minusDays(1);
        assertThatNoException().isThrownBy(() -> strategy().sjekkStartOgSluttdato(GYLDIG_START, slutt));
    }

    @Test
    void start_etter_slutt_kaster_feilkode() {
        assertFeilkode(Feilkode.START_ETTER_SLUTT,
                () -> strategy().sjekkStartOgSluttdato(GYLDIG_SLUTT, GYLDIG_START));
    }

    @Nested
    class ErForLangVarighetTest {

        private static final LocalDate START = LocalDate.of(2026, 8, 1);

        @Test
        void null_startDato_returnerer_false() {
            assertThat(erForLangVarighet(Stillingstype.FAST, null, START.plusYears(1))).isFalse();
        }

        @Test
        void null_sluttDato_returnerer_false() {
            assertThat(erForLangVarighet(Stillingstype.FAST, START, null)).isFalse();
        }

        @Test
        void begge_datoer_null_returnerer_false() {
            assertThat(erForLangVarighet(Stillingstype.FAST, null, null)).isFalse();
        }

        @Test
        void midlertidig_stilling_varighet_under_2_år_returnerer_false() {
            assertThat(erForLangVarighet(Stillingstype.MIDLERTIDIG, START, START.plusYears(2).minusDays(1))).isFalse();
        }

        @Test
        void midlertidig_stilling_varighet_nøyaktig_2_år_returnerer_true() {
            assertThat(erForLangVarighet(Stillingstype.MIDLERTIDIG, START, START.plusYears(2))).isTrue();
        }

        @Test
        void midlertidig_stilling_varighet_over_2_år_returnerer_true() {
            assertThat(erForLangVarighet(Stillingstype.MIDLERTIDIG, START, START.plusYears(2).plusDays(1))).isTrue();
        }

        @Test
        void fast_stilling_varighet_under_4_år_returnerer_false() {
            assertThat(erForLangVarighet(Stillingstype.FAST, START, START.plusYears(4).minusDays(1))).isFalse();
        }

        @Test
        void fast_stilling_varighet_nøyaktig_4_år_returnerer_true() {
            assertThat(erForLangVarighet(Stillingstype.FAST, START, START.plusYears(4))).isTrue();
        }

        @Test
        void fast_stilling_varighet_over_4_år_returnerer_true() {
            assertThat(erForLangVarighet(Stillingstype.FAST, START, START.plusYears(4).plusDays(1))).isTrue();
        }

        @Test
        void null_stillingstype_behandles_som_fast_stilling() {
            // null is not MIDLERTIDIG, so maks varighet = 4 år
            assertThat(erForLangVarighet(null, START, START.plusYears(3))).isFalse();
            assertThat(erForLangVarighet(null, START, START.plusYears(4))).isTrue();
        }
    }
}
