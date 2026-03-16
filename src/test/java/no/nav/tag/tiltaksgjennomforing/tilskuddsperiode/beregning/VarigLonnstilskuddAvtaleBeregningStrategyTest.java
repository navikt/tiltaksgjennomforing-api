package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.VarigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_MAKS;
import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.VarigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_REDUSERT_MAKS;
import static org.assertj.core.api.Assertions.assertThat;

class VarigLonnstilskuddAvtaleBeregningStrategyTest {

    private final VarigLonnstilskuddAvtaleBeregningStrategy strategy =
            new VarigLonnstilskuddAvtaleBeregningStrategy();

    @BeforeEach
    void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    private Avtale lagAvtaleMedDatoerOgProsent(LocalDate startDato, LocalDate sluttDato, Integer lonnstilskuddProsent) {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(lonnstilskuddProsent);
        avtale.getGjeldendeInnhold().setStartDato(startDato);
        avtale.getGjeldendeInnhold().setSluttDato(sluttDato);
        return avtale;
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_startdato_er_null() {
        Avtale avtale = lagAvtaleMedDatoerOgProsent(null, LocalDate.of(2025, 1, 1), 75);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_sluttdato_er_null() {
        Avtale avtale = lagAvtaleMedDatoerOgProsent(LocalDate.of(2024, 1, 1), null, 75);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__gir_reduksjonsdato_naar_prosent_er_null_og_sluttdato_er_etter_et_aar() {
        // When prosent is null the "lonnstilskuddProsent <= REDUSERT_MAKS" guard is skipped,
        // so the code falls through and returns the date 1 year after start.
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(startDato, LocalDate.of(2026, 1, 1), null);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusYears(1));
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_prosent_er_under_eller_lik_redusert_maks() {
        // TILSKUDDSPROSENT_REDUSERT_MAKS = 67
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2026, 1, 1),
                TILSKUDDSPROSENT_REDUSERT_MAKS
        );

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__gir_reduksjonsdato_etter_1_aar_naar_prosent_er_over_redusert_maks() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
                startDato,
                LocalDate.of(2026, 1, 1),
                TILSKUDDSPROSENT_MAKS
        );

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusYears(1));
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_reduksjonsdato_er_etter_sluttdato() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        // Sluttdato er bare 6 måneder frem, men reduksjon skjer etter 1 år
        LocalDate sluttDato = LocalDate.of(2024, 6, 30);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
                startDato,
                sluttDato,
                TILSKUDDSPROSENT_MAKS
        );

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__gir_reduksjonsdato_naar_reduksjonsdato_er_lik_sluttdato() {
        // datoForReduksjon.isAfter(sluttDato) is false when they are equal,
        // so the date IS returned (the reduksjon starts on the very last day of the avtale).
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2025, 1, 1);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
                startDato,
                sluttDato,
                TILSKUDDSPROSENT_MAKS
        );

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(LocalDate.of(2025, 1, 1));
    }

    @Test
    void getDatoerForReduksjon_handterer_skuddaar() {
        LocalDate varigTilpassetStart = LocalDate.of(2024, 2, 29);
        Avtale varigTilpassetAvtale = lagAvtaleMedDatoerOgProsent(varigTilpassetStart, LocalDate.of(2030, 2, 28), TILSKUDDSPROSENT_MAKS);
        List<LocalDate> reduksjon = strategy.getDatoerForReduksjon(varigTilpassetAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2025, 2, 28));
    }

    @Test
    void getDatoerForReduksjon_handterer_skuddaar_i_midten_av_trinn() {
        LocalDate varigTilpassetStart = LocalDate.of(2023, 2, 28);
        Avtale varigTilpassetAvtale = lagAvtaleMedDatoerOgProsent(varigTilpassetStart, LocalDate.of(2025, 2, 28), TILSKUDDSPROSENT_MAKS);
        List<LocalDate> reduksjon = strategy.getDatoerForReduksjon(varigTilpassetAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2024, 2, 28)); // 2024 = skuddaar

        varigTilpassetStart = LocalDate.of(2023, 3, 1);
        varigTilpassetAvtale = lagAvtaleMedDatoerOgProsent(varigTilpassetStart, LocalDate.of(2025, 2, 28), TILSKUDDSPROSENT_MAKS);
        reduksjon = strategy.getDatoerForReduksjon(varigTilpassetAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2024, 3, 1)); // 2024 = skuddaar
    }

    @Test
    void getProsentForPeriode__returnerer_lonnstilskuddprosent_naar_ingen_reduksjon() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        // Sluttdato kort slik at ingen reduksjon
        LocalDate sluttDato = LocalDate.of(2024, 6, 30);
        int lonnstilskuddProsent = TILSKUDDSPROSENT_MAKS;
        Avtale avtale = lagAvtaleMedDatoerOgProsent(startDato, sluttDato, lonnstilskuddProsent);
        Periode periode = new Periode(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(lonnstilskuddProsent);
    }

    @Test
    void getProsentForPeriode__begrenser_til_maks_tilskuddsprosent() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2024, 6, 30);
        // Sett en for høy prosent – skal begrenses til TILSKUDDSPROSENT_MAKS (75)
        Avtale avtale = lagAvtaleMedDatoerOgProsent(startDato, sluttDato, 80);
        Periode periode = new Periode(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(TILSKUDDSPROSENT_MAKS);
    }

    @Test
    void getProsentForPeriode__returnerer_redusert_maks_etter_reduksjonsdato() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        // Prosenten er over TILSKUDDSPROSENT_REDUSERT_MAKS, så reduksjon skal skje etter 1 år
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
                startDato,
                sluttDato,
                TILSKUDDSPROSENT_MAKS
        );
        // Periode er etter reduksjonsdato (2025-01-01)
        Periode periode = new Periode(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(TILSKUDDSPROSENT_REDUSERT_MAKS);
    }

    @Test
    void getProsentForPeriode__ingen_reduksjon_naar_prosent_allerede_er_under_redusert_maks() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        int lonnstilskuddProsent = 50; // under TILSKUDDSPROSENT_REDUSERT_MAKS (67)
        Avtale avtale = lagAvtaleMedDatoerOgProsent(startDato, sluttDato, lonnstilskuddProsent);
        // Periode langt etter startdato – men getDatoerForReduksjon returnerer tom liste
        Periode periode = new Periode(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(lonnstilskuddProsent);
    }

    @Test
    void getProsentForPeriode__returnerer_0_naar_lonnstilskuddprosent_er_null() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2024, 6, 30);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(startDato, sluttDato, null);
        Periode periode = new Periode(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isZero();
    }

    @Test
    void getProsentForPeriode__periode_slutter_paa_reduksjonsdato_er_redusert() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
                startDato,
                sluttDato,
                TILSKUDDSPROSENT_MAKS
        );

        Periode periode = new Periode(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);
        assertThat(prosent).isEqualTo(TILSKUDDSPROSENT_REDUSERT_MAKS);
    }

    @Test
    void getProsentForPeriode_handterer_skuddaar() {
        LocalDate startDato = LocalDate.of(2024, 2, 29);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
            startDato,
            LocalDate.of(2026, 2, 28),
            TILSKUDDSPROSENT_MAKS
        );

        Integer forstePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 27))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT_MAKS);

        Integer andrePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2025, 2, 28), LocalDate.of(2026, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT_REDUSERT_MAKS);
    }

    @Test
    void getProsentForPeriode_handterer_skuddaar_i_midten_av_trinn() {
        LocalDate startDato = LocalDate.of(2023, 3, 1);
        Avtale avtale = lagAvtaleMedDatoerOgProsent(
            startDato,
            LocalDate.of(2025, 2, 28),
            TILSKUDDSPROSENT_MAKS
        );

        Integer forstePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2023, 3, 1), LocalDate.of(2024, 2, 29))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT_MAKS);

        Integer andrePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2024, 3, 1), LocalDate.of(2025, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT_REDUSERT_MAKS);
    }
}
