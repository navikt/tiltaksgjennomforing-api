package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreAvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.InkluderingstilskuddsutgiftDTO;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType.OPPLÆRING;
import static no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType.PROGRAMVARE;
import static no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType.UTSTYR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
class InkluderingstilskuddAvtaleBeregningStrategyTest {
    private static final LocalDate AVTALE_START = LocalDate.of(2026, 3, 1);
    private static final LocalDate AVTALE_SLUTT = LocalDate.of(2026, 8, 31);

    @BeforeEach
    void setup() {
        Now.fixedDate(AVTALE_START);
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        new FeatureToggleHolder(featureToggleService);
    }

    @AfterEach
    void tearDown() {
        Now.resetClock();
        FeatureToggleHolder.reset();
    }

    @Test
    void lager_en_tilskuddsperiode_per_utgift_med_beløp_og_datoer_fra_utgiften() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            utgift(20_000, OPPLÆRING, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31))
        );

        assertThat(avtale.getTilskuddPeriode())
            .extracting(TilskuddPeriode::getLøpenummer, TilskuddPeriode::getBeløp, TilskuddPeriode::getStartDato, TilskuddPeriode::getSluttDato)
            .containsExactlyInAnyOrder(
                tuple(1, 10_000, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
                tuple(2, 20_000, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 31))
            );
        assertThat(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift())
            .allSatisfy(utgift -> assertThat(utgift.getTilskuddperiode()).isNotNull());
    }

    @Test
    void genererer_ikke_nye_perioder_når_alle_utgifter_allerede_har_tilskuddsperiode() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );
        TilskuddPeriode eksisterendePeriode = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().getFirst().getTilskuddperiode();

        List<TilskuddPeriode> perioder = new InkluderingstilskuddAvtaleBeregningStrategy(avtale).genererNyeTilskuddsperioder();

        assertThat(perioder).singleElement().isSameAs(eksisterendePeriode);
        assertThat(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().getFirst().getTilskuddperiode()).isSameAs(eksisterendePeriode);
    }

    @Test
    void nye_utgifter_får_løpenummer_etter_høyeste_eksisterende() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1)),
            utgift(20_000, OPPLÆRING, AVTALE_START, AVTALE_START.plusMonths(1))
        );
        Inkluderingstilskuddsutgift nyUtgift = new Inkluderingstilskuddsutgift(
            utgift(5_000, UTSTYR, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31))
        );

        List<TilskuddPeriode> perioder = new InkluderingstilskuddAvtaleBeregningStrategy(avtale)
            .genererTilskuddsperioder(GenererTilskuddsperioder.fra(List.of(nyUtgift)));

        assertThat(perioder)
            .extracting(TilskuddPeriode::getLøpenummer, TilskuddPeriode::getBeløp)
            .containsExactly(tuple(3, 5_000));
        assertThat(nyUtgift.getTilskuddperiode()).isSameAs(perioder.getFirst());
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "2026-02-28, 2026-03-31, INKLUDERINGSTILSKUDD_PERIODE_FOR_TIDLIG_STARTDATO",
        "2026-08-01, 2026-09-01, INKLUDERINGSTILSKUDD_PERIODE_FOR_SEN_SLUTTDATO",
        "2026-06-01, 2026-03-01, START_ETTER_SLUTT",
    })
    void avviser_utgift_med_ugyldig_periode(LocalDate start, LocalDate slutt, Feilkode feilkode) {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );

        assertThatThrownBy(() -> genererFor(avtale, utgift(5_000, UTSTYR, start, slutt)))
            .isInstanceOf(FeilkodeException.class)
            .extracting("feilkode")
            .isEqualTo(feilkode);
    }

    @ParameterizedTest(name = "{0} - {1}")
    @CsvSource({
        "2026-03-01, 2026-05-31",
        "2026-06-30, 2026-09-29",
        "2026-08-31, 2026-11-29",
    })
    void godtar_periode_på_maks_3_mnd(LocalDate start, LocalDate slutt) {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            LocalDate.of(2026, 12, 31),
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );

        assertThat(genererFor(avtale, utgift(5_000, UTSTYR, start, slutt)))
            .extracting(TilskuddPeriode::getStartDato, TilskuddPeriode::getSluttDato)
            .containsExactly(tuple(start, slutt));
    }

    @ParameterizedTest(name = "{0} - {1}")
    @CsvSource({
        "2026-03-01, 2026-06-01",
        "2026-06-30, 2026-09-30",
        "2026-08-31, 2026-11-30",
    })
    void avviser_periode_over_3_mnd(LocalDate start, LocalDate slutt) {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            LocalDate.of(2026, 12, 31),
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );

        assertThatThrownBy(() -> genererFor(avtale, utgift(5_000, UTSTYR, start, slutt)))
            .isInstanceOf(FeilkodeException.class)
            .extracting("feilkode")
            .isEqualTo(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_FOR_LANG);
    }

    @Test
    void avviser_utgift_som_går_på_tvers_av_år() {
        Now.fixedDate(LocalDate.of(2026, 11, 1));
        Avtale avtale = avtaleMedUtgifter(
            LocalDate.of(2026, 11, 1),
            LocalDate.of(2027, 4, 30),
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30))
        );

        assertThatThrownBy(() -> genererFor(avtale, utgift(5_000, UTSTYR, LocalDate.of(2026, 12, 15), LocalDate.of(2027, 1, 15))))
            .isInstanceOf(FeilkodeException.class)
            .extracting("feilkode")
            .isEqualTo(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_PAA_TVERS_AV_AAR);
    }

    @Test
    void feiler_når_utgift_mangler_datoer() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );

        assertThatThrownBy(() -> genererFor(avtale, utgift(5_000, UTSTYR, null, null)))
            .isInstanceOf(FeilkodeException.class)
            .extracting("feilkode")
            .isEqualTo(Feilkode.INKLUDERINGSTILSKUDD_UTGIFT_MANGLER_DATO);
    }

    @Test
    void avviser_utgift_med_datoer_når_avtalen_mangler_datoer() {
        assertThatThrownBy(() -> avtaleMedUtgifter(null, null, utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))))
            .isInstanceOf(FeilkodeException.class)
            .extracting("feilkode")
            .isEqualTo(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_AVTALE_MANGLER_DATO);
    }

    @Test
    void feiler_når_både_avtalen_og_utgiften_mangler_datoer() {
        assertThatThrownBy(() -> avtaleMedUtgifter(null, null, utgift(10_000, PROGRAMVARE, null, null)))
            .isInstanceOf(FeilkodeException.class)
            .extracting("feilkode")
            .isEqualTo(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_AVTALE_MANGLER_DATO);
    }

    @Test
    void beholder_eksisterende_perioder_når_utgiftene_er_lastet_fra_databasen() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1)),
            utgift(20_000, OPPLÆRING, AVTALE_START, AVTALE_START.plusMonths(2))
        );
        Set<TilskuddPeriode> eksisterende = Set.copyOf(avtale.getTilskuddPeriode());
        avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().forEach(utgift -> {
            utgift.setGenererTilskuddsperiodeData(new Inkluderingstilskuddsutgift.GenererTilskuddsperiodeData(null, null));
        });

        InkluderingstilskuddAvtaleBeregningStrategy strategi = new InkluderingstilskuddAvtaleBeregningStrategy(avtale);
        List<TilskuddPeriode> perioder = strategi.genererNyeTilskuddsperioder();

        assertThat(perioder).hasSize(2).allMatch(eksisterende::contains);
        assertThat(strategi.harNyeTilskuddsperioder(avtale.getTilskuddPeriode(), perioder)).isFalse();
    }

    @Test
    void ny_utgift_får_periode_selv_om_andre_utgifter_er_lastet_fra_databasen() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );
        Inkluderingstilskuddsutgift lastetUtgift = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().getFirst();
        TilskuddPeriode eksisterendePeriode = lastetUtgift.getTilskuddperiode();
        lastetUtgift.setGenererTilskuddsperiodeData(new Inkluderingstilskuddsutgift.GenererTilskuddsperiodeData(null, null));
        Inkluderingstilskuddsutgift nyUtgift = new Inkluderingstilskuddsutgift(
            utgift(5_000, UTSTYR, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31))
        );
        avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().add(nyUtgift);

        List<TilskuddPeriode> perioder = new InkluderingstilskuddAvtaleBeregningStrategy(avtale).genererNyeTilskuddsperioder();

        assertThat(perioder).hasSize(2).first().isSameAs(eksisterendePeriode);
        assertThat(nyUtgift.getTilskuddperiode())
            .isSameAs(perioder.get(1))
            .extracting(TilskuddPeriode::getLøpenummer, TilskuddPeriode::getBeløp)
            .containsExactly(2, 5_000);
    }

    @Test
    void nye_utgifter_fra_endring_erstatter_eksisterende_perioder() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, AVTALE_START, AVTALE_START.plusMonths(1))
        );
        TilskuddPeriode gammelPeriode = avtale.getTilskuddPeriode().iterator().next();

        EndreAvtaleDTO endring = TestData.endringPåAlleInkluderingstilskuddFelter();
        endring.setStartDato(AVTALE_START);
        endring.setSluttDato(AVTALE_SLUTT);
        endring.getInkluderingstilskuddsutgift().clear();
        endring.getInkluderingstilskuddsutgift().add(utgift(15_000, UTSTYR, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30)));
        avtale.endreAvtale(endring, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode())
            .doesNotContain(gammelPeriode)
            .extracting(TilskuddPeriode::getBeløp)
            .containsExactly(15_000);
    }

    private static List<TilskuddPeriode> genererFor(Avtale avtale, InkluderingstilskuddsutgiftDTO utgift) {
        return new InkluderingstilskuddAvtaleBeregningStrategy(avtale)
            .genererTilskuddsperioder(GenererTilskuddsperioder.fra(List.of(new Inkluderingstilskuddsutgift(utgift))));
    }

    private static Avtale avtaleMedUtgifter(LocalDate start, LocalDate slutt, InkluderingstilskuddsutgiftDTO... utgifter) {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleInkluderingstilskuddFelter();
        endreAvtale.setStartDato(start);
        endreAvtale.setSluttDato(slutt);
        endreAvtale.getInkluderingstilskuddsutgift().clear();
        endreAvtale.getInkluderingstilskuddsutgift().addAll(List.of(utgifter));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        return avtale;
    }

    private static InkluderingstilskuddsutgiftDTO utgift(int beløp, InkluderingstilskuddsutgiftType type, LocalDate start, LocalDate slutt) {
        return TestData.enInkluderingstilskuddsutgiftDto(beløp, type, start, slutt);
    }
}
