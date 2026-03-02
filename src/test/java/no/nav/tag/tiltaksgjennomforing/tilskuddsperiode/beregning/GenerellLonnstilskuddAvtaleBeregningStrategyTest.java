package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenerellLonnstilskuddAvtaleBeregningStrategyTest {

    private static final int SUM_LONNSTILSKUDD_PER_MANED = 10_000;
    private static final int SUM_LONNSTILSKUDD_REDUSERT_PER_MANED = 8_000;
    private static final int PROSENT = 60;

    private final GenerellLonnstilskuddAvtaleBeregningStrategy strategy =
            new GenerellLonnstilskuddAvtaleBeregningStrategy();

    private Avtale lagAvtale(Tiltakstype tiltakstype, LocalDate datoForRedusertProsent) {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(tiltakstype);
        avtale.getGjeldendeInnhold().setSumLonnstilskudd(SUM_LONNSTILSKUDD_PER_MANED);
        avtale.getGjeldendeInnhold().setSumLønnstilskuddRedusert(SUM_LONNSTILSKUDD_REDUSERT_PER_MANED);
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(PROSENT);
        avtale.getGjeldendeInnhold().setDatoForRedusertProsent(datoForRedusertProsent);
        return avtale;
    }


    @BeforeEach
    public void setup() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true;
    }

    @AfterEach
    public void tearDown() {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = false;
    }

    // -----------------------------------------------------------------------
    // Ingen reduksjon
    // -----------------------------------------------------------------------

    @Test
    void ingen_reduksjon__en_maned_gir_en_periode_med_riktig_prosent() {
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 1, 31);
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, null);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getLonnstilskuddProsent()).isEqualTo(PROSENT);
        assertThat(perioder.get(0).getStartDato()).isEqualTo(fra);
        assertThat(perioder.get(0).getSluttDato()).isEqualTo(til);
    }

    @Test
    void ingen_reduksjon__hele_maneder_gir_riktig_belop_per_maned() {
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 3, 31);
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, null);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        assertThat(perioder).hasSize(3);
        assertThat(perioder).allMatch(p -> p.getLonnstilskuddProsent() == PROSENT);
        assertThat(perioder).allMatch(p -> p.getBeløp() == SUM_LONNSTILSKUDD_PER_MANED);
    }

    @Test
    void ingen_reduksjon__perioden_starter_midt_i_maneden_gir_proratert_belop() {
        LocalDate fra = LocalDate.of(2024, 1, 15);
        LocalDate til = LocalDate.of(2024, 1, 31);
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, null);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        assertThat(perioder).hasSize(1);
        assertThat(perioder.get(0).getBeløp()).isLessThan(SUM_LONNSTILSKUDD_PER_MANED);
    }

    // -----------------------------------------------------------------------
    // Kun reduserte perioder (datoFraOgMed er etter datoForRedusertProsent)
    // -----------------------------------------------------------------------

    @Test
    void kun_redusert__alle_perioder_bruker_redusert_prosent_og_belop() {
        LocalDate fra = LocalDate.of(2024, 3, 1);
        LocalDate til = LocalDate.of(2024, 4, 30);
        LocalDate datoForRedusertProsent = LocalDate.of(2024, 1, 1); // allerede passert
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, datoForRedusertProsent);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        assertThat(perioder).hasSize(2);
        int redusertProsent = BeregningStrategy.getRedusertLonnstilskuddprosent(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, PROSENT);
        assertThat(perioder).allMatch(p -> p.getLonnstilskuddProsent() == redusertProsent);
        assertThat(perioder).allMatch(p -> p.getBeløp() == SUM_LONNSTILSKUDD_REDUSERT_PER_MANED);
    }

    // -----------------------------------------------------------------------
    // Både fulle og reduserte perioder (datoForRedusertProsent er inne i perioden)
    // -----------------------------------------------------------------------

    @Test
    void baade_full_og_redusert__splitter_paa_reduksjonsdatoen() {
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 4, 30);
        LocalDate datoForRedusertProsent = LocalDate.of(2024, 3, 1);
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, datoForRedusertProsent);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        // Jan + Feb → full prosent; Mar + Apr → redusert prosent
        assertThat(perioder).hasSize(4);

        List<TilskuddPeriode> forReduksjon = perioder.stream()
                .filter(p -> p.getSluttDato().isBefore(datoForRedusertProsent))
                .toList();
        List<TilskuddPeriode> etterReduksjon = perioder.stream()
                .filter(p -> !p.getStartDato().isBefore(datoForRedusertProsent))
                .toList();

        assertThat(forReduksjon).hasSize(2);
        assertThat(forReduksjon).allMatch(p -> p.getLonnstilskuddProsent() == PROSENT);
        assertThat(forReduksjon).allMatch(p -> p.getBeløp() == SUM_LONNSTILSKUDD_PER_MANED);

        int redusertProsent = BeregningStrategy.getRedusertLonnstilskuddprosent(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, PROSENT);
        assertThat(etterReduksjon).hasSize(2);
        assertThat(etterReduksjon).allMatch(p -> p.getLonnstilskuddProsent() == redusertProsent);
        assertThat(etterReduksjon).allMatch(p -> p.getBeløp() == SUM_LONNSTILSKUDD_REDUSERT_PER_MANED);
    }

    @Test
    void baade_full_og_redusert__ingen_perioder_foer_reduksjonsdato_naar_fra_er_lik_reduksjonsdato() {
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 3, 31);
        LocalDate datoForRedusertProsent = LocalDate.of(2024, 1, 1); // lik startdato
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, datoForRedusertProsent);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        int redusertProsent = BeregningStrategy.getRedusertLonnstilskuddprosent(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, PROSENT);
        assertThat(perioder).allMatch(p -> p.getLonnstilskuddProsent() == redusertProsent);
    }

    @Test
    void baade_full_og_redusert__reduksjonsdato_siste_dag_gir_fulle_perioder_foer_og_en_redusert_en_dags_periode() {
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 2, 29);
        LocalDate datoForRedusertProsent = LocalDate.of(2024, 2, 29); // siste dag
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, datoForRedusertProsent);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        // Jan 1–31 og Feb 1–28 → full prosent (2 perioder), Feb 29–29 → redusert (1 periode)
        assertThat(perioder).hasSize(3);

        int redusertProsent = BeregningStrategy.getRedusertLonnstilskuddprosent(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, PROSENT);

        List<TilskuddPeriode> fullePerioder = perioder.stream()
                .filter(p -> p.getSluttDato().isBefore(datoForRedusertProsent))
                .toList();
        assertThat(fullePerioder).hasSize(2);
        assertThat(fullePerioder).allMatch(p -> p.getLonnstilskuddProsent() == PROSENT);

        TilskuddPeriode redusertPeriode = perioder.getLast();
        assertThat(redusertPeriode.getStartDato()).isEqualTo(datoForRedusertProsent);
        assertThat(redusertPeriode.getSluttDato()).isEqualTo(til);
        assertThat(redusertPeriode.getLonnstilskuddProsent()).isEqualTo(redusertProsent);
    }

    // -----------------------------------------------------------------------
    // Varig lønnstilskudd – getRedusertLonnstilskuddprosent har annen logikk
    // -----------------------------------------------------------------------

    @Test
    void varig_lonnstilskudd__redusert_prosent_beholdes_naar_under_grenseverdi() {
        int prosent = 50; // under grensen på 68
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 3, 31);
        LocalDate datoForRedusertProsent = LocalDate.of(2024, 2, 1);
        Avtale avtale = lagAvtale(Tiltakstype.VARIG_LONNSTILSKUDD, datoForRedusertProsent);
        avtale.getGjeldendeInnhold().setLonnstilskuddProsent(prosent);

        List<TilskuddPeriode> perioder = strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til);

        List<TilskuddPeriode> etterReduksjon = perioder.stream()
                .filter(p -> !p.getStartDato().isBefore(datoForRedusertProsent))
                .toList();

        // For VARIG_LONNSTILSKUDD under grensen beholdes prosenten uendret
        assertThat(etterReduksjon).allMatch(p -> p.getLonnstilskuddProsent() == prosent);
    }

    // -----------------------------------------------------------------------
    // Ugyldig tilstand
    // -----------------------------------------------------------------------

    @Test
    void ugyldig_tilstand__kaster_feilkode_exception_naar_til_er_foer_reduksjonsdato() {
        LocalDate fra = LocalDate.of(2024, 1, 1);
        LocalDate til = LocalDate.of(2024, 1, 31);
        LocalDate datoForRedusertProsent = LocalDate.of(2024, 6, 1); // etter perioden
        Avtale avtale = lagAvtale(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, datoForRedusertProsent);

        assertThatThrownBy(() -> strategy.beregnTilskuddsperioderForAvtale(avtale, fra, til))
                .isInstanceOf(FeilkodeException.class);
    }
}
