package no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtalerolle;
import no.nav.tag.tiltaksgjennomforing.avtale.Inkluderingstilskuddsutgift;
import no.nav.tag.tiltaksgjennomforing.avtale.InkluderingstilskuddsutgiftType;
import no.nav.tag.tiltaksgjennomforing.avtale.RefusjonStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriode;
import no.nav.tag.tiltaksgjennomforing.avtale.TilskuddPeriodeStatus;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeAnnullert;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeForkortet;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collection;
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
        "2026-06-01, 2026-03-01, INKLUDERINGSTILSKUDD_PERIODE_AVTALE_START_ETTER_SLUTT",
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
            .isEqualTo(Feilkode.INKLUDERINGSTILSKUDD_PERIODE_UTGIFT_MANGLER_DATO);
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

    @Test
    void forleng_rorer_ikke_tilskuddsperioder() {
        // forleng er bevisst ikke implementert for inkluderingstilskudd fordi veileder selv velger
        // datoer per utgift, så eksisterende tilskuddsperioder skal ikke endres når avtalen forlenges.
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31)),
            utgift(20_000, OPPLÆRING, LocalDate.of(2026, 6, 1), AVTALE_SLUTT)
        );
        TilskuddPeriode godkjentPeriode = avtale.tilskuddsperiode(0);
        TilskuddPeriode ubehandletPeriode = avtale.tilskuddsperiode(1);
        godkjentPeriode.setStatus(TilskuddPeriodeStatus.GODKJENT);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forleng(AVTALE_SLUTT, AVTALE_SLUTT.plusMonths(3));

        assertThat(avtale.getTilskuddPeriode())
            .containsExactly(godkjentPeriode, ubehandletPeriode)
            .extracting(
                TilskuddPeriode::getLøpenummer,
                TilskuddPeriode::getStartDato,
                TilskuddPeriode::getSluttDato,
                TilskuddPeriode::getBeløp,
                TilskuddPeriode::getStatus
            )
            .containsExactly(
                tuple(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 31), 10_000, TilskuddPeriodeStatus.GODKJENT),
                tuple(2, LocalDate.of(2026, 6, 1), AVTALE_SLUTT, 20_000, TilskuddPeriodeStatus.UBEHANDLET)
            );
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TilskuddPeriodeStatus.class, names = {"UBEHANDLET", "GODKJENT"})
    void forkort_beholder_perioder_som_slutter_for_eller_pa_ny_sluttdato(TilskuddPeriodeStatus status) {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            utgift(15_000, OPPLÆRING, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30))
        );
        TilskuddPeriode forNySlutt = avtale.tilskuddsperiode(0);
        TilskuddPeriode paNySlutt = avtale.tilskuddsperiode(1);
        forNySlutt.setStatus(status);
        paNySlutt.setStatus(status);
        LocalDate nySluttDato = LocalDate.of(2026, 4, 30);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactly(forNySlutt, paNySlutt);
        assertThat(forNySlutt.getSluttDato()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(forNySlutt.getBeløp()).isEqualTo(10_000);
        assertThat(forNySlutt.getStatus()).isEqualTo(status);
        assertThat(paNySlutt.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(paNySlutt.getBeløp()).isEqualTo(15_000);
        assertThat(paNySlutt.getStatus()).isEqualTo(status);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    void forkort_forkorter_kryssende_ubehandlet_periode_med_uendret_belop_uten_hendelse() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31))
        );
        TilskuddPeriode periode = avtale.tilskuddsperiode(0);
        LocalDate nySluttDato = LocalDate.of(2026, 4, 15);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactly(periode);
        assertThat(periode.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(periode.getBeløp()).isEqualTo(10_000);
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    void forkort_forkorter_kryssende_godkjent_periode_med_uendret_belop_og_hendelse() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31))
        );
        TilskuddPeriode periode = avtale.tilskuddsperiode(0);
        periode.setStatus(TilskuddPeriodeStatus.GODKJENT);
        LocalDate nySluttDato = LocalDate.of(2026, 4, 15);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactly(periode);
        assertThat(periode.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(periode.getBeløp()).isEqualTo(10_000);
        assertThat(periode.getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeForkortet(avtale, periode));
    }

    @ParameterizedTest
    @EnumSource(value = TilskuddPeriodeStatus.class, names = {"UBEHANDLET", "GODKJENT"})
    void forkort_til_startdato_av_periode_gir_endagsperiode_med_uendret_belop(TilskuddPeriodeStatus status) {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31))
        );
        TilskuddPeriode periode = avtale.tilskuddsperiode(0);
        periode.setStatus(status);
        LocalDate nySluttDato = LocalDate.of(2026, 4, 1);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactly(periode);
        assertThat(periode.getStartDato()).isEqualTo(nySluttDato);
        assertThat(periode.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(periode.getBeløp()).isEqualTo(10_000);
        assertThat(periode.getStatus()).isEqualTo(status);
        if (status == TilskuddPeriodeStatus.GODKJENT) {
            assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeForkortet(avtale, periode));
        } else {
            assertThat(hentHendelser(avtale)).isEmpty();
        }
    }

    @Test
    void forkort_fjerner_ubehandlet_periode_etter_ny_sluttdato_uten_hendelse() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            utgift(20_000, OPPLÆRING, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30))
        );
        TilskuddPeriode beholdes = avtale.tilskuddsperiode(0);
        TilskuddPeriode fjernes = avtale.tilskuddsperiode(1);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(LocalDate.of(2026, 4, 30));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(beholdes).doesNotContain(fjernes);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    void forkort_annullerer_godkjent_periode_etter_ny_sluttdato_med_hendelse() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            utgift(20_000, OPPLÆRING, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30))
        );
        TilskuddPeriode beholdes = avtale.tilskuddsperiode(0);
        TilskuddPeriode annulleres = avtale.tilskuddsperiode(1);
        annulleres.setStatus(TilskuddPeriodeStatus.GODKJENT);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(LocalDate.of(2026, 4, 30));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(beholdes, annulleres);
        assertThat(annulleres.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(annulleres.getSluttDato()).isEqualTo(LocalDate.of(2026, 6, 30));
        assertThat(annulleres.getBeløp()).isEqualTo(20_000);
        assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeAnnullert(avtale, annulleres));
    }

    @Test
    void forkort_annullerer_godkjent_periode_med_utgatt_refusjon_etter_ny_sluttdato_uten_hendelse() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            utgift(20_000, OPPLÆRING, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30))
        );
        TilskuddPeriode beholdes = avtale.tilskuddsperiode(0);
        TilskuddPeriode annulleresMedUtgattRefusjon = avtale.tilskuddsperiode(1);
        annulleresMedUtgattRefusjon.setStatus(TilskuddPeriodeStatus.GODKJENT);
        annulleresMedUtgattRefusjon.setRefusjonStatus(RefusjonStatus.UTGÅTT);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(LocalDate.of(2026, 4, 30));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(beholdes, annulleresMedUtgattRefusjon);
        assertThat(annulleresMedUtgattRefusjon.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TilskuddPeriodeStatus.class, names = {"AVSLÅTT", "ANNULLERT", "BEHANDLET_I_ARENA"})
    void forkort_lar_perioder_med_andre_statuser_vaere_urort(TilskuddPeriodeStatus status) {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(10_000, PROGRAMVARE, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31)),
            utgift(20_000, OPPLÆRING, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31))
        );
        TilskuddPeriode kryssende = avtale.tilskuddsperiode(0);
        TilskuddPeriode etter = avtale.tilskuddsperiode(1);
        kryssende.setStatus(status);
        etter.setStatus(status);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(LocalDate.of(2026, 4, 15));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(kryssende, etter);
        assertThat(kryssende.getStatus()).isEqualTo(status);
        assertThat(kryssende.getSluttDato()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(kryssende.getBeløp()).isEqualTo(10_000);
        assertThat(etter.getStatus()).isEqualTo(status);
        assertThat(etter.getSluttDato()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(etter.getBeløp()).isEqualTo(20_000);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    void forkort_handterer_flere_overlappende_og_senere_perioder_samtidig() {
        Avtale avtale = avtaleMedUtgifter(
            AVTALE_START,
            AVTALE_SLUTT,
            utgift(5_000, PROGRAMVARE, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)),
            utgift(10_000, OPPLÆRING, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31)),
            utgift(12_000, UTSTYR, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 15)),
            utgift(15_000, PROGRAMVARE, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30)),
            utgift(18_000, OPPLÆRING, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30)),
            utgift(20_000, UTSTYR, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31)),
            utgift(25_000, PROGRAMVARE, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 31))
        );
        TilskuddPeriode forNySlutt = avtale.tilskuddsperiode(0);
        TilskuddPeriode godkjentKryssende = avtale.tilskuddsperiode(1);
        TilskuddPeriode ubehandletKryssende = avtale.tilskuddsperiode(2);
        TilskuddPeriode godkjentEtter = avtale.tilskuddsperiode(3);
        TilskuddPeriode godkjentUtgattEtter = avtale.tilskuddsperiode(4);
        TilskuddPeriode ubehandletEtter = avtale.tilskuddsperiode(5);
        TilskuddPeriode avslattEtter = avtale.tilskuddsperiode(6);

        forNySlutt.setStatus(TilskuddPeriodeStatus.GODKJENT);
        godkjentKryssende.setStatus(TilskuddPeriodeStatus.GODKJENT);
        godkjentEtter.setStatus(TilskuddPeriodeStatus.GODKJENT);
        godkjentUtgattEtter.setStatus(TilskuddPeriodeStatus.GODKJENT);
        godkjentUtgattEtter.setRefusjonStatus(RefusjonStatus.UTGÅTT);
        avslattEtter.setStatus(TilskuddPeriodeStatus.AVSLÅTT);

        LocalDate nySluttDato = LocalDate.of(2026, 4, 15);
        tomHendelser(avtale);

        new InkluderingstilskuddAvtaleBeregningStrategy(avtale).forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactlyInAnyOrder(
            forNySlutt,
            godkjentKryssende,
            ubehandletKryssende,
            godkjentEtter,
            godkjentUtgattEtter,
            avslattEtter
        );
        assertThat(avtale.getTilskuddPeriode()).doesNotContain(ubehandletEtter);
        assertThat(forNySlutt.getSluttDato()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(godkjentKryssende.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(godkjentKryssende.getBeløp()).isEqualTo(10_000);
        assertThat(ubehandletKryssende.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(ubehandletKryssende.getBeløp()).isEqualTo(12_000);
        assertThat(godkjentEtter.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(godkjentUtgattEtter.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(avslattEtter.getStatus()).isEqualTo(TilskuddPeriodeStatus.AVSLÅTT);
        assertThat(hentHendelser(avtale)).containsExactlyInAnyOrder(
            new TilskuddsperiodeForkortet(avtale, godkjentKryssende),
            new TilskuddsperiodeAnnullert(avtale, godkjentEtter)
        );
    }

    private static void tomHendelser(Avtale avtale) {
        ReflectionTestUtils.invokeMethod(avtale, "clearDomainEvents");
    }

    private static Collection<Object> hentHendelser(Avtale avtale) {
        return ReflectionTestUtils.invokeMethod(avtale, "domainEvents");
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
