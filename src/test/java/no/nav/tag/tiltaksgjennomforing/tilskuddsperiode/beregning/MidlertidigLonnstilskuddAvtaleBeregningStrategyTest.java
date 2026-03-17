package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.enhet.Kvalifiseringsgruppe;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT;
import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_REDUKSJONSFAKTOR;
import static no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.MidlertidigLonnstilskuddAvtaleBeregningStrategy.TILSKUDDSPROSENT_TILPASSET;
import static org.assertj.core.api.Assertions.assertThat;

class MidlertidigLonnstilskuddAvtaleBeregningStrategyTest {

    private final MidlertidigLonnstilskuddAvtaleBeregningStrategy strategy =
            new MidlertidigLonnstilskuddAvtaleBeregningStrategy();

    @BeforeEach
    void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    private Avtale lagAvtaleMedDatoer(Kvalifiseringsgruppe kvalifiseringsgruppe, LocalDate startDato, LocalDate sluttDato) {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD);
        avtale.setKvalifiseringsgruppe(kvalifiseringsgruppe);
        avtale.getGjeldendeInnhold().setStartDato(startDato);
        avtale.getGjeldendeInnhold().setSluttDato(sluttDato);
        return avtale;
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_startdato_er_null() {
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, null, LocalDate.of(2025, 1, 1));

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_sluttdato_er_null() {
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, LocalDate.of(2024, 1, 1), null);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__situasjonsbestemt_innsats_gir_reduksjon_etter_6_maaneder() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2024, 12, 31);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusMonths(6));
    }

    @Test
    void getDatoerForReduksjon__spesielt_tilpasset_innsats_gir_reduksjon_etter_1_aar() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS, startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusYears(1));
    }

    @Test
    void getDatoerForReduksjon__varig_tilpasset_innsats_gir_reduksjon_etter_1_aar() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusYears(1));
    }

    @Test
    void getDatoerForReduksjon__returnerer_tom_liste_naar_reduksjonsdato_er_etter_sluttdato() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        // Sluttdato er bare 3 måneder frem, men reduksjon for SITUASJONSBESTEMT er etter 6 måneder
        LocalDate sluttDato = LocalDate.of(2024, 3, 31);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).isEmpty();
    }

    @Test
    void getDatoerForReduksjon__gir_reduksjonsdato_naar_reduksjonsdato_er_lik_sluttdato() {
        // datoForReduksjon.isAfter(sluttDato) is false when they are equal,
        // so the date IS returned (the reduksjon starts on the very last day of the avtale).
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = startDato.plusMonths(6); // Reduksjonsdato for SITUASJONSBESTEMT = 2024-07-01
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, startDato, sluttDato);

        List<LocalDate> result = strategy.getDatoerForReduksjon(avtale);

        assertThat(result).containsExactly(startDato.plusMonths(6));
    }

    @Test
    void getDatoerForReduksjon_handterer_skuddaar() {
        LocalDate varigTilpassetStart = LocalDate.of(2024, 2, 29);
        Avtale varigTilpassetAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, varigTilpassetStart, LocalDate.of(2026, 2, 28));
        List<LocalDate> reduksjon = strategy.getDatoerForReduksjon(varigTilpassetAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2025, 2, 28));

        LocalDate situasjonsbestemtStart = LocalDate.of(2024, 2, 29);
        Avtale situasjonsbestemtAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, situasjonsbestemtStart, LocalDate.of(2026, 2, 28));
        reduksjon = strategy.getDatoerForReduksjon(situasjonsbestemtAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2024, 8, 29));
    }

    @Test
    void getDatoerForReduksjon_handterer_skuddaar_i_midten_av_trinn() {
        LocalDate varigTilpassetStart = LocalDate.of(2023, 3, 1);
        Avtale varigTilpassetAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, varigTilpassetStart, LocalDate.of(2025, 2, 28));
        List<LocalDate> reduksjon = strategy.getDatoerForReduksjon(varigTilpassetAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2024, 3, 1)); // 2024 = skuddaar

        LocalDate situasjonsbestemtStart = LocalDate.of(2023, 8, 29);
        Avtale situasjonsbestemtAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, situasjonsbestemtStart, LocalDate.of(2025, 2, 28));
        reduksjon = strategy.getDatoerForReduksjon(situasjonsbestemtAvtale);

        assertThat(reduksjon).containsExactly(LocalDate.of(2024, 2, 29)); // 2024 = skuddaar

        LocalDate situasjonsbestemtStart2 = LocalDate.of(2023, 8, 31);
        Avtale situasjonsbestemtAvtale2 = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, situasjonsbestemtStart2, LocalDate.of(2025, 2, 28));
        reduksjon = strategy.getDatoerForReduksjon(situasjonsbestemtAvtale2);

        assertThat(reduksjon).containsExactly(LocalDate.of(2024, 2, 29)); // 2024 = skuddaar
    }

    @Test
    void getProsentForPeriode__situasjonsbestemt_innsats_gir_40_prosent_foer_reduksjon() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2024, 12, 31);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, startDato, sluttDato);
        Periode periode = new Periode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(TILSKUDDSPROSENT);
    }

    @Test
    void getProsentForPeriode__situasjonsbestemt_innsats_gir_redusert_prosent_etter_6_maaneder() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2024, 12, 31);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, startDato, sluttDato);
        // Perioden starter etter reduksjonsdato (2024-07-01)
        Periode periode = new Periode(LocalDate.of(2024, 8, 1), LocalDate.of(2024, 8, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(
                TILSKUDDSPROSENT
                        - TILSKUDDSPROSENT_REDUKSJONSFAKTOR
        );
    }

    @Test
    void getProsentForPeriode__spesielt_tilpasset_innsats_gir_60_prosent_foer_reduksjon() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS, startDato, sluttDato);
        Periode periode = new Periode(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(TILSKUDDSPROSENT_TILPASSET);
    }

    @Test
    void getProsentForPeriode__spesielt_tilpasset_innsats_gir_redusert_prosent_etter_1_aar() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2026, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SPESIELT_TILPASSET_INNSATS, startDato, sluttDato);
        // Perioden starter etter reduksjonsdato (2025-01-01)
        Periode periode = new Periode(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(
                TILSKUDDSPROSENT_TILPASSET
                        - TILSKUDDSPROSENT_REDUKSJONSFAKTOR
        );
    }

    @Test
    void getProsentForPeriode__ukjent_kvalifiseringsgruppe_gir_null() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2024, 12, 31);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.STANDARD_INNSATS, startDato, sluttDato);
        Periode periode = new Periode(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isNull();
    }

    @Test
    void getProsentForPeriode__periode_slutter_paa_reduksjonsdato_er_redusert() {
        LocalDate startDato = LocalDate.of(2024, 1, 1);
        LocalDate sluttDato = LocalDate.of(2025, 1, 1);
        Avtale avtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, startDato, sluttDato);
        // Perioden slutter akkurat på reduksjonsdatoen
        Periode periode = new Periode(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 7, 1));

        Integer prosent = strategy.getProsentForPeriode(avtale, periode);

        assertThat(prosent).isEqualTo(TILSKUDDSPROSENT - TILSKUDDSPROSENT_REDUKSJONSFAKTOR);
    }

    @Test
    void getProsentForPeriode_handterer_skuddaar() {
        LocalDate varigTilpassetStart = LocalDate.of(2024, 2, 29);
        Avtale varigTilpassetAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, varigTilpassetStart, LocalDate.of(2026, 2, 28));

        Integer forstePeriode = strategy.getProsentForPeriode(
            varigTilpassetAvtale,
            Periode.av(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 27))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT_TILPASSET);

        Integer andrePeriode = strategy.getProsentForPeriode(
            varigTilpassetAvtale,
            Periode.av(LocalDate.of(2025, 2, 28), LocalDate.of(2026, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT_TILPASSET - TILSKUDDSPROSENT_REDUKSJONSFAKTOR);

        LocalDate situasjonsbestemtStart = LocalDate.of(2024, 2, 29);
        Avtale situasjonsbestemtAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, situasjonsbestemtStart, LocalDate.of(2026, 2, 28));
        forstePeriode = strategy.getProsentForPeriode(
            situasjonsbestemtAvtale,
            Periode.av(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 8, 28))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT);

        andrePeriode = strategy.getProsentForPeriode(
            situasjonsbestemtAvtale,
            Periode.av(LocalDate.of(2024, 8, 29), LocalDate.of(2026, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT - TILSKUDDSPROSENT_REDUKSJONSFAKTOR);
    }

    @Test
    void getProsentForPeriode_handterer_skuddaar_i_midten_av_trinn() {
        LocalDate varigTilpassetStart = LocalDate.of(2023, 3, 1);
        Avtale varigTilpassetAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.VARIG_TILPASSET_INNSATS, varigTilpassetStart, LocalDate.of(2025, 2, 28));

        Integer forstePeriode = strategy.getProsentForPeriode(
            varigTilpassetAvtale,
            Periode.av(LocalDate.of(2023, 3, 1), LocalDate.of(2024, 2, 29))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT_TILPASSET);

        Integer andrePeriode = strategy.getProsentForPeriode(
            varigTilpassetAvtale,
            Periode.av(LocalDate.of(2024, 3, 1), LocalDate.of(2025, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT_TILPASSET - TILSKUDDSPROSENT_REDUKSJONSFAKTOR);

        LocalDate situasjonsbestemtStart = LocalDate.of(2023, 8, 29);
        Avtale situasjonsbestemtAvtale = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, situasjonsbestemtStart, LocalDate.of(2025, 2, 28));
        forstePeriode = strategy.getProsentForPeriode(
            situasjonsbestemtAvtale,
            Periode.av(LocalDate.of(2023, 8, 29), LocalDate.of(2024, 2, 28))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT);

        andrePeriode = strategy.getProsentForPeriode(
            situasjonsbestemtAvtale,
            Periode.av(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT - TILSKUDDSPROSENT_REDUKSJONSFAKTOR);

        LocalDate situasjonsbestemtStart2 = LocalDate.of(2023, 8, 31);
        Avtale situasjonsbestemtAvtale2 = lagAvtaleMedDatoer(Kvalifiseringsgruppe.SITUASJONSBESTEMT_INNSATS, situasjonsbestemtStart2, LocalDate.of(2025, 2, 28));
        forstePeriode = strategy.getProsentForPeriode(
            situasjonsbestemtAvtale2,
            Periode.av(LocalDate.of(2023, 8, 31), LocalDate.of(2024, 2, 28))
        );
        assertThat(forstePeriode).isEqualTo(TILSKUDDSPROSENT);

        andrePeriode = strategy.getProsentForPeriode(
            situasjonsbestemtAvtale2,
            Periode.av(LocalDate.of(2024, 2, 29), LocalDate.of(2025, 2, 28))
        );
        assertThat(andrePeriode).isEqualTo(TILSKUDDSPROSENT - TILSKUDDSPROSENT_REDUKSJONSFAKTOR);
    }
}
