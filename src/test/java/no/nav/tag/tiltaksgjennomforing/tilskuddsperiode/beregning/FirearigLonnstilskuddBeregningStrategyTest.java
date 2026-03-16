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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FirearigLonnstilskuddBeregningStrategyTest {

    private static final int PROSENT_AAR_1 = 70;
    private static final int PROSENT_AAR_2 = 60;
    private static final int PROSENT_AAR_3 = 50;
    private static final int PROSENT_AAR_4 = 35;

    private final FirearigLonnstilskuddBeregningStrategy strategy =
            new FirearigLonnstilskuddBeregningStrategy();

    @BeforeEach
    void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    private Avtale lagAvtaleMedDatoer(LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.FIREARIG_LONNSTILSKUDD);
        avtale.getGjeldendeInnhold().setStartDato(startDato);
        avtale.getGjeldendeInnhold().setSluttDato(sluttDato);
        return avtale;
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_startdato_er_null() {
        Avtale avtale = lagAvtaleMedDatoer(null, LocalDate.of(2028, 1, 1));

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_sluttdato_er_null() {
        Avtale avtale = lagAvtaleMedDatoer(LocalDate.of(2027, 1, 1), null);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__returnerer_alle_tre_reduksjonsdatoer_for_fireaarig_avtale() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        LocalDate sluttDato = LocalDate.of(2031, 12, 31);
        Avtale avtale = lagAvtaleMedDatoer(startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(
                startDato.plusYears(1),
                startDato.plusYears(2),
                startDato.plusYears(3)
        );
    }

    @Test
    void getDatoerForReduksjon__filtrerer_bort_reduksjonsdatoer_etter_sluttdato() {
        LocalDate startDato = LocalDate.of(2027, 1, 1);
        // Sluttdato er innenfor år 2 – bare første reduksjonsdato skal inkluderes
        LocalDate sluttDato = LocalDate.of(2028, 6, 30);
        Avtale avtale = lagAvtaleMedDatoer(startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusYears(1));
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_sluttdato_er_i_forste_aar() {
        LocalDate startDato = LocalDate.of(2027, 1, 1);
        // Sluttdato er før første reduksjonsdato
        LocalDate sluttDato = LocalDate.of(2027, 6, 30);
        Avtale avtale = lagAvtaleMedDatoer(startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__inkluderer_reduksjonsdato_lik_sluttdato() {
        LocalDate startDato = LocalDate.of(2027, 1, 1);
        // Sluttdato er eksakt lik første reduksjonsdato
        LocalDate sluttDato = startDato.plusYears(1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        // !dato.isAfter(sluttDato) → includes the date when it equals sluttDato
        assertThat(result).containsExactly(startDato.plusYears(1));
    }

    @Test
    void getDatoerForReduksjon__returnerer_to_datoer_naar_sluttdato_er_i_tredje_aar() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        LocalDate sluttDato = LocalDate.of(2030, 6, 30);
        Avtale avtale = lagAvtaleMedDatoer(startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(
                startDato.plusYears(1),
                startDato.plusYears(2)
        );
    }

    @Test
    void getDatoerForReduksjon_handterer_skuddaar() {
        LocalDate startDato = LocalDate.of(2028, 2, 29);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2032, 2, 27));
        List<LocalDate> reduksjon = strategy.getDatoerForReduksjon(avtale);

        assertThat(reduksjon).containsExactly(
            LocalDate.of(2029, 2, 28),
            LocalDate.of(2030, 2, 28),
            LocalDate.of(2031, 2, 28)
        );
    }

    @Test
    void getDatoerForReduksjon_handterer_skuddaar_i_midten_av_trinn() {
        // Start 28.02 året før skuddår
        LocalDate startDato = LocalDate.of(2027, 2, 28);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 2, 27));
        List<LocalDate> reduksjon = strategy.getDatoerForReduksjon(avtale);

        assertThat(reduksjon).containsExactly(
            LocalDate.of(2028, 2, 28), // 2028 = skuddaar
            LocalDate.of(2029, 2, 28),
            LocalDate.of(2030, 2, 28)
        );

        // Start 01.03 året før skuddår
        startDato = LocalDate.of(2027, 3, 1);
        avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 2, 27));
        reduksjon = strategy.getDatoerForReduksjon(avtale);

        assertThat(reduksjon).containsExactly(
            LocalDate.of(2028, 3, 1), // 2028 = skuddaar
            LocalDate.of(2029, 3, 1),
            LocalDate.of(2030, 3, 1)
        );
    }

    @Test
    void getProsentForPeriode__forste_aar_gir_70_prosent() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 12, 31));
        // Perioden slutter innenfor første år
        Periode periode = new Periode(LocalDate.of(2028, 6, 1), LocalDate.of(2028, 6, 30));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(PROSENT_AAR_1);
    }

    @Test
    void getProsentForPeriode__andre_aar_gir_60_prosent() {
        LocalDate startDato = LocalDate.of(2027, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2030, 12, 31));
        // Perioden slutter i andre år (1 år etter start)
        Periode periode = new Periode(LocalDate.of(2028, 1, 1), LocalDate.of(2028, 1, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(PROSENT_AAR_2);
    }

    @Test
    void getProsentForPeriode__tredje_aar_gir_50_prosent() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 12, 31));
        // Perioden slutter i tredje år (2 år etter start)
        Periode periode = new Periode(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(PROSENT_AAR_3);
    }

    @Test
    void getProsentForPeriode__fjerde_aar_gir_35_prosent() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 12, 31));
        // Perioden slutter i fjerde år (3 år etter start)
        Periode periode = new Periode(LocalDate.of(2031, 1, 1), LocalDate.of(2031, 1, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(PROSENT_AAR_4);
    }

    @Test
    void getProsentForPeriode__periode_slutter_paa_aarsskifte_bruker_neste_aars_prosent() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 12, 31));
        // Perioden slutter eksakt på 1-årsmerket → Period.between gir getYears() == 1 → år 2
        Periode periode = new Periode(LocalDate.of(2028, 12, 1), LocalDate.of(2029, 1, 1));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(PROSENT_AAR_2);
    }

    @Test
    void getProsentForPeriode__kaster_exception_naar_periode_slutter_mer_enn_4_aar_etter_start() {
        LocalDate startDato = LocalDate.of(2028, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 12, 31));
        // Perioden slutter mer enn 4 år etter startdato
        Periode periode = new Periode(LocalDate.of(2031, 6, 1), LocalDate.of(2032, 6, 30));

        assertThatThrownBy(() -> strategy.getProsentForPeriode(avtale, periode))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tilskuddsperiode avsluttes mer enn 4 år etter startdato");
    }

    @Test
    void getProsentForPeriode_handterer_skuddaar() {
        LocalDate startDato = LocalDate.of(2028, 2, 29);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2032, 2, 27));

        Integer forstePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2028, 2, 29), LocalDate.of(2029, 2, 27))
        );
        assertThat(forstePeriode).isEqualTo(PROSENT_AAR_1);

        Integer andrePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2029, 2, 28), LocalDate.of(2030, 2, 27))
        );
        assertThat(andrePeriode).isEqualTo(PROSENT_AAR_2);

        Integer tredjePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2030, 2, 28), LocalDate.of(2031, 2, 27))
        );
        assertThat(tredjePeriode).isEqualTo(PROSENT_AAR_3);

        Integer fjerdePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2031, 2, 28), LocalDate.of(2032, 2, 27))
        );
        assertThat(fjerdePeriode).isEqualTo(PROSENT_AAR_4);
    }

    @Test
    void getProsentForPeriode_handterer_skuddaar_i_midten_av_trinn() {
        // Start 28.02 året før skuddår
        LocalDate startDato = LocalDate.of(2027, 2, 28);
        Avtale avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 2, 27));

        Integer forstePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2027, 2, 28), LocalDate.of(2028, 2, 27))
        );
        assertThat(forstePeriode).isEqualTo(PROSENT_AAR_1);

        Integer andrePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2028, 2, 28), LocalDate.of(2029, 2, 27)) // 2028 = skuddaar
        );
        assertThat(andrePeriode).isEqualTo(PROSENT_AAR_2);

        Integer tredjePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2029, 2, 28), LocalDate.of(2030, 2, 27))
        );
        assertThat(tredjePeriode).isEqualTo(PROSENT_AAR_3);

        Integer fjerdePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2030, 2, 28), LocalDate.of(2031, 2, 27))
        );
        assertThat(fjerdePeriode).isEqualTo(PROSENT_AAR_4);

        // Start 01.03 året før skuddår
        startDato = LocalDate.of(2027, 3, 1);
        avtale = lagAvtaleMedDatoer(startDato, LocalDate.of(2031, 2, 28));

        forstePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2027, 3, 1), LocalDate.of(2028, 2, 29))
        );
        assertThat(forstePeriode).isEqualTo(PROSENT_AAR_1);

        andrePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2028, 3, 1), LocalDate.of(2029, 2, 28)) // 2028 = skuddaar
        );
        assertThat(andrePeriode).isEqualTo(PROSENT_AAR_2);

        tredjePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2029, 3, 1), LocalDate.of(2030, 2, 28))
        );
        assertThat(tredjePeriode).isEqualTo(PROSENT_AAR_3);

        fjerdePeriode = strategy.getProsentForPeriode(
            avtale,
            Periode.av(LocalDate.of(2030, 3, 1), LocalDate.of(2031, 2, 28))
        );
        assertThat(fjerdePeriode).isEqualTo(PROSENT_AAR_4);
    }

}
