package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeAnnullert;
import no.nav.tag.tiltaksgjennomforing.avtale.events.TilskuddsperiodeForkortet;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreAvtaleDTO;
import no.nav.tag.tiltaksgjennomforing.enhet.Innsatsgruppe;
import no.nav.tag.tiltaksgjennomforing.exceptions.FeilkodeException;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.BeregningStrategy;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.EndreTilskuddsberegning;
import no.nav.tag.tiltaksgjennomforing.tilskuddsperiode.beregning.GenererTilskuddsperioder;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import no.nav.tag.tiltaksgjennomforing.utils.Periode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.tag.tiltaksgjennomforing.utils.DatoUtils.sisteDatoIMnd;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@SyntetiskFnr
public class BeregningStrategyTest {

    @BeforeEach
    public void setup() {
        new FeatureToggleHolder(mock(FeatureToggleService.class));
    }

    @AfterEach
    public void tearDown() {
        Now.resetClock();
        FeatureToggleHolder.reset();
    }

    @Test
    public void en_tilskuddsperiode() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate fra = LocalDate.of(2021, 1, 1);
        LocalDate til = LocalDate.of(2021, 3, 31);
        final int forventetBeløpForPeriode = 48960;
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(fra, til);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        assertThat(BeregningStrategy.create(avtale).getBeløpForPeriode(avtale.getGjeldendeInnhold(), Periode.av(fra, til))).isEqualTo(forventetBeløpForPeriode);
        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(3);
        assertThat(BeregningStrategy.create(avtale).getBeløpForPeriode(avtale.getGjeldendeInnhold(), Periode.av(fra, til))).isEqualTo(forventetBeløpForPeriode);
        assertThat(avtale.getTilskuddPeriode().first().getBeløp()).isEqualTo(avtale.getGjeldendeInnhold().getSumLonnstilskudd());
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ARBEIDSTRENING", "INKLUDERINGSTILSKUDD"})
    public void en_avtaletype_som_ikke_stotter_tilskuddsperioder_skal_ikke_generere_perioder_i_beregning_strategi(String tiltakstypeString) {
        final Tiltakstype tiltakstype = Tiltakstype.valueOf(tiltakstypeString);
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate fra = LocalDate.of(2021, 1, 1);
        LocalDate til = LocalDate.of(2021, 3, 31);
        final int forventetBeløpForPeriode = 0;
        Avtale avtale = TestData.enAvtale(tiltakstype);
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(fra, til);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);


        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(0);
        assertThat(BeregningStrategy.create(avtale).getBeløpForPeriode(avtale.getGjeldendeInnhold(), Periode.av(fra, til))).isEqualTo(forventetBeløpForPeriode);
        assertThat(avtale.getTilskuddPeriode()).isEmpty();
        Now.resetClock();
    }

    @Test
    public void beregn_mentor_tilskuddsbeløp_for_periode_samsvarer_med_sum_genererte_perioder() {
        Now.fixedDate(LocalDate.of(2025, 1, 5));
        Avtale avtale = TestData.enMentorAvtaleUsignert();
        LocalDate start = avtale.getGjeldendeInnhold().getStartDato();
        LocalDate slutt = avtale.getGjeldendeInnhold().getSluttDato();
        int direkte = BeregningStrategy.create(avtale).genererTilskuddsperioder(GenererTilskuddsperioder.fra(start, slutt))
                .stream().mapToInt(TilskuddPeriode::getBeløp).sum();
        int sum = avtale.getTilskuddPeriode().stream().mapToInt(TilskuddPeriode::getBeløp).sum();
        assertThat(direkte).isEqualTo(sum);
        Now.resetClock();
    }

    @Test
    public void en_VTAO_Avtale_Med_fast_beløp_for_tilskuddsperioder() {
        Now.fixedDate(LocalDate.of(2024, 7, 1));
        final int fastBeløpPerMånedForPeriode = 6808;
        LocalDate fra = LocalDate.of(2024, 7, 1);
        LocalDate til = LocalDate.of(2024, 9, 1);

        Avtale avtale = TestData.enVtaoAvtaleIKKEGodkjentAvArbeidsgiver();

        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(fra, til);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);


        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(3);
        assertThat(avtale.getTilskuddPeriode().first().getBeløp()).isEqualTo(fastBeløpPerMånedForPeriode);
        assertThat(avtale.getTilskuddPeriode().first().getLonnstilskuddProsent()).isEqualTo(null);
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void splitt_ved_nyttår() {
        Now.fixedDate(LocalDate.of(2020, 12, 1));
        LocalDate fra = LocalDate.of(2020, 12, 1);
        LocalDate til = LocalDate.of(2021, 1, 31);

        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(fra, til);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        var tilskuddPerioder = avtale.getTilskuddPeriode();

        assertThat(tilskuddPerioder.size()).isEqualTo(2);
        Iterator<TilskuddPeriode> iterator = tilskuddPerioder.iterator();
        TilskuddPeriode første = iterator.next();
        TilskuddPeriode andre = iterator.next();
        assertThat(første.getBeløp()).isEqualTo(andre.getBeløp());
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void reduksjon_etter_6_mnd__30_prosent_lonnstilskudd() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(40);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        TilskuddPeriode tilskuddpeirode6mndEtterStart = finnTilskuddsperiodeForDato(avtale.getGjeldendeInnhold().getStartDato().plusMonths(6), avtale);
        TilskuddPeriode tilskuddperiodeDagenFør6Mnd = finnTilskuddsperiodeForDato(avtale.getGjeldendeInnhold().getStartDato().plusMonths(6).minusDays(1), avtale);

        assertThat(tilskuddpeirode6mndEtterStart.getLonnstilskuddProsent()).isEqualTo(30);
        assertThat(tilskuddperiodeDagenFør6Mnd.getLonnstilskuddProsent()).isEqualTo(40);

        harRiktigeEgenskaper(avtale);
    }

    private TilskuddPeriode finnTilskuddsperiodeForDato(LocalDate dato, Avtale avtale) {
        for (TilskuddPeriode tilskuddsperiode : avtale.getTilskuddPeriode()) {
            if (tilskuddsperiode.getStartDato().isBefore(dato.plusDays(1)) && tilskuddsperiode.getSluttDato().isAfter(dato.minusDays(1))) {
                return tilskuddsperiode;
            }
        }
        return null;
    }

    @Test
    public void finnTilskuddsperiodeForDato() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 10, 1));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        TilskuddPeriode tilskuddPeriode1 = finnTilskuddsperiodeForDato(LocalDate.of(2021, 1, 1), avtale);
        TilskuddPeriode tilskuddPeriode2 = finnTilskuddsperiodeForDato(LocalDate.of(2021, 2, 1), avtale);

        assertThat(tilskuddPeriode1).isEqualTo(avtale.tilskuddsperiode(0));
        assertThat(tilskuddPeriode2).isEqualTo(avtale.tilskuddsperiode(1));
        Now.resetClock();
    }

    @Test
    public void sjekkAtEnhetsnrOgEnhetsnavnBlirSattPaEndreAvtale() {
        final String ENHETS_NR = "1001";
        final String ENHETS_NAVN = "NAV Ullensaker";

        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(
            avtale.getGjeldendeInnhold().getStartDato(),
            avtale.getGjeldendeInnhold().getSluttDato()
        );

        avtale.oppdatereKostnadsstedForTilskuddsperioder(ENHETS_NR, ENHETS_NAVN);
        assertThat(avtale.tilskuddsperiode(0).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(0).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);
        assertThat(avtale.tilskuddsperiode(1).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(2).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);

        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.tilskuddsperiode(0).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(0).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);
        assertThat(avtale.tilskuddsperiode(1).getEnhet()).isEqualTo(ENHETS_NR);
        assertThat(avtale.tilskuddsperiode(2).getEnhetsnavn()).isEqualTo(ENHETS_NAVN);
    }

    @Test
    public void reduksjon_etter_12_mnd_60_prosent_lonnstilskudd() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        avtale.setInnsatsgruppe(Innsatsgruppe.TRENGER_VEILEDNING_NEDSATT_ARBEIDSEVNE);
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter();
        endreAvtale.setLonnstilskuddProsent(60);
        endreAvtale.setSluttDato(endreAvtale.getStartDato().plusMonths(13));
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        TilskuddPeriode sisteTilskuddsperiode = avtale.tilskuddsperiode(avtale.getTilskuddPeriode().size() - 1);
        assertThat(sisteTilskuddsperiode.getLonnstilskuddProsent()).isEqualTo(50);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void splitt_etter_reduksjon_30_prosnt_lonnstilskudd() {
        Now.fixedDate(LocalDate.of(2020, 6, 28));
        LocalDate startDato = LocalDate.of(2020, 6, 30);
        LocalDate sluttDato = LocalDate.of(2021, 1, 2);
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();

        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(startDato, sluttDato);
        endreAvtale.setLonnstilskuddProsent(40);

        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        assertThat(avtale.getTilskuddPeriode().size()).isEqualTo(9);
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjett_at_reduksjon_skjer_tidlig_12_mnd_ved_68_prosent_eller_høyre() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate startDato = LocalDate.of(2021, 1, 1);
        LocalDate sluttDato = LocalDate.of(2022, 1, 10);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);

        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(startDato, sluttDato);
        endreAvtale.setLonnstilskuddProsent(68);

        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.tilskuddsperiode(0).getLonnstilskuddProsent()).isEqualTo(68);
        assertThat(avtale.tilskuddsperiode(avtale.getTilskuddPeriode().size() - 1).getLonnstilskuddProsent()).isEqualTo(67);
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjett_at_reduksjon_skjer_etter_12_mnd_ved_høyre_enn_68_prosent() {
        Now.fixedDate(LocalDate.of(2023, 1, 2));
        LocalDate startDato = LocalDate.of(2022, 1, 1);
        LocalDate sluttDato = LocalDate.of(2023, 12, 12);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.setGodkjentForEtterregistrering(true);
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(startDato, sluttDato);
        endreAvtale.setLonnstilskuddProsent(70);

        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.tilskuddsperiode(0).getLonnstilskuddProsent()).isEqualTo(70);
        assertThat(avtale.tilskuddsperiode(avtale.getTilskuddPeriode().size() - 1).getLonnstilskuddProsent()).isEqualTo(67);
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjett_at_reduksjon_ikke_skjer_etter_12_mnd_under_68_prosent() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate startDato = LocalDate.of(2021, 2, 1);
        LocalDate sluttDato = LocalDate.of(2023, 3, 10);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);

        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(startDato, sluttDato);
        endreAvtale.setLonnstilskuddProsent(40);

        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode())
            .allSatisfy(periode -> assertThat(periode.getLonnstilskuddProsent()).isEqualTo(40));
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjett_at_reduksjon_ikke_skjer_før_12_mnd_over_68_prosent_eller_høyre() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate startDato = LocalDate.of(2021, 2, 1);
        LocalDate sluttDato = LocalDate.of(2021, 3, 1);
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);

        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(startDato, sluttDato);
        endreAvtale.setLonnstilskuddProsent(69);

        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode())
            .allSatisfy(periode -> assertThat(periode.getLonnstilskuddProsent()).isEqualTo(69));
        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_avtalen_ikke_annulleres_om_den_har_en_utbetalt_tilskuddsperiode() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.UTBETALT);
        assertThrows(FeilkodeException.class, () -> avtale.annuller(TestData.enVeileder(avtale), ""));
    }

    @Test
    public void sjekk_at_avtalen_ikke_annulleres_om_den_har_en_godkjent_refusjon_på_tilskuddsperiode() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.SENDT_KRAV);
        assertThrows(FeilkodeException.class, () -> avtale.annuller(TestData.enVeileder(avtale), ""));
    }

    @Test
    public void sjekk_at_utbetalte_perioder_beholdes_ved_endring() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(
            LocalDate.of(2021, 1, 1),
            LocalDate.of(2021, 3, 31)
        );
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.UTBETALT);
        UUID idPåUtbetaltTilskuddsperiode = avtale.tilskuddsperiode(0).getId();
        Integer beløpPåUtbetaltTilskuddsperiode = avtale.tilskuddsperiode(0).getBeløp();
        Integer belopPaUbehandletForEndring = avtale.tilskuddsperiode(1).getBeløp();

        EndreTilskuddsberegning endreTilskuddsberegning = EndreTilskuddsberegning.builder()
            .manedslonn(99999)
            .arbeidsgiveravgift(avtale.getGjeldendeInnhold().getArbeidsgiveravgift())
            .feriepengesats(avtale.getGjeldendeInnhold().getFeriepengesats())
            .otpSats(avtale.getGjeldendeInnhold().getOtpSats())
            .build();
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getRefusjonStatus()).isEqualTo(RefusjonStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåUtbetaltTilskuddsperiode);
        assertThat(avtale.tilskuddsperiode(0).getBeløp()).isEqualTo(beløpPåUtbetaltTilskuddsperiode);
        assertThat(avtale.tilskuddsperiode(1).getBeløp()).isNotEqualTo(belopPaUbehandletForEndring);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void sjekk_at_nye_perioder_ved_forlengelse_starter_etter_utbetalte_perioder() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 4, 1));

        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(1).setRefusjonStatus(RefusjonStatus.UTBETALT);

        avtale.forlengAvtale(avtale.getGjeldendeInnhold().getSluttDato().plusMonths(3), TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getRefusjonStatus()).isEqualTo(RefusjonStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(1).getRefusjonStatus()).isEqualTo(RefusjonStatus.UTBETALT);

        assertThat(avtale.tilskuddsperiode(1).getStartDato()).isEqualTo(avtale.tilskuddsperiode(0).getSluttDato().plusDays(1));
        assertThat(avtale.tilskuddsperiode(2).getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_godkjent_perioder_beholdes_ved_endring_som_påvirker_økonomi() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleGodkjentAvVeileder();
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        Integer beløpFørEndring = avtale.tilskuddsperiode(0).getBeløp();

        EndreTilskuddsberegning endreTilskuddsberegning = EndreTilskuddsberegning.builder()
                .manedslonn(99999)
                .arbeidsgiveravgift(avtale.getGjeldendeInnhold().getArbeidsgiveravgift())
                .feriepengesats(avtale.getGjeldendeInnhold().getFeriepengesats())
                .otpSats(avtale.getGjeldendeInnhold().getOtpSats())
                .build();
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(0).getBeløp()).isEqualTo(beløpFørEndring);
        harRiktigeEgenskaper(avtale);
    }

    @Test
    public void sjekk_at_godkjent_periode_annulleres() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.tilskuddsperiode(0).getId();

        avtale.annuller(TestData.enVeileder(avtale), "");

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
    }

    @Test
    public void sjekk_at_ubehandlet_periode_slettes() {
        Avtale avtale = TestData.enLonnstilskuddAvtaleMedAltUtfylt(Tiltakstype.VARIG_LONNSTILSKUDD);
        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.UBEHANDLET);

        avtale.annuller(TestData.enVeileder(avtale), "");

        assertThat(avtale.getTilskuddPeriode()).isEmpty();
    }

    @Test
    public void sjekk_at_godkjent_periode_annulleres_ved_forlengelse() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate avtaleFørsteDag = LocalDate.of(2021, 1, 1);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleFørsteDag, avtaleFørsteDag);

        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.tilskuddsperiode(0).getId();

        avtale.forlengAvtale(avtaleFørsteDag.plusDays(1), TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
        assertThat(avtale.tilskuddsperiode(0).getStartDato()).isEqualTo(avtaleFørsteDag);
        assertThat(avtale.tilskuddsperiode(0).getSluttDato()).isEqualTo(avtaleFørsteDag);

        assertThat(avtale.tilskuddsperiode(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
        assertThat(avtale.tilskuddsperiode(1).getStartDato()).isEqualTo(avtaleFørsteDag);
        assertThat(avtale.tilskuddsperiode(1).getSluttDato()).isEqualTo(avtaleFørsteDag.plusDays(1));

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_godkjent_periode_ikke_annulleres_ved_forlengelse_om_den_dekker_hele_måneden() {
        Now.fixedDate(LocalDate.of(2023, 1, 1));
        LocalDate avtaleFørsteDag = LocalDate.of(2023, 1, 1);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleFørsteDag, sisteDatoIMnd(avtaleFørsteDag));

        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        UUID idPåGodkjentTilskuddsperiode = avtale.tilskuddsperiode(0).getId();

        avtale.forlengAvtale(sisteDatoIMnd(avtaleFørsteDag).plusDays(2), TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(0).getId()).isEqualTo(idPåGodkjentTilskuddsperiode);
        assertThat(avtale.tilskuddsperiode(0).getStartDato()).isEqualTo(avtaleFørsteDag);
        assertThat(avtale.tilskuddsperiode(0).getSluttDato()).isEqualTo(LocalDate.of(2023, 1, 31));

        assertThat(avtale.tilskuddsperiode(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
        assertThat(avtale.tilskuddsperiode(1).getStartDato()).isEqualTo(LocalDate.of(2023, 2, 1));
        assertThat(avtale.tilskuddsperiode(1).getSluttDato()).isEqualTo(LocalDate.of(2023, 2, 2));

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void sjekk_at_godkjent_periode_ikke_annulleres_ved_økonomiendring_i_et_hull() {
        Now.fixedDate(LocalDate.of(2020, 6, 28));
        LocalDate avtaleStart = LocalDate.of(2021, 1, 1);
        LocalDate avtaleSlutt = LocalDate.of(2021, 8, 1);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);


        avtale.tilskuddsperiode(0).setStatus(TilskuddPeriodeStatus.GODKJENT);
        avtale.tilskuddsperiode(0).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(1).setStatus(TilskuddPeriodeStatus.GODKJENT);
        avtale.tilskuddsperiode(2).setRefusjonStatus(RefusjonStatus.UTBETALT);
        avtale.tilskuddsperiode(2).setStatus(TilskuddPeriodeStatus.GODKJENT);

        EndreTilskuddsberegning endreTilskuddsberegning = EndreTilskuddsberegning.builder()
                .lonnstilskuddProsent(avtale.getGjeldendeInnhold().getLonnstilskuddProsent())
                .manedslonn(77777)
                .arbeidsgiveravgift(avtale.getGjeldendeInnhold().getArbeidsgiveravgift())
                .feriepengesats(avtale.getGjeldendeInnhold().getFeriepengesats())
                .otpSats(avtale.getGjeldendeInnhold().getOtpSats())
                .build();
        avtale.endreTilskuddsberegning(endreTilskuddsberegning, TestData.enNavIdent());

        assertThat(avtale.tilskuddsperiode(0).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(0).getRefusjonStatus()).isEqualTo(RefusjonStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(1).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avtale.tilskuddsperiode(2).getRefusjonStatus()).isEqualTo(RefusjonStatus.UTBETALT);
        assertThat(avtale.tilskuddsperiode(2).getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);

        harRiktigeEgenskaper(avtale);
        Now.resetClock();
    }

    @Test
    public void genererMaks1MndTilskuddsperiode() {
        Now.fixedDate(LocalDate.of(2021, 1, 01));
        LocalDate avtaleStart = LocalDate.of(2021, 1, 1);
        LocalDate avtaleSlutt = LocalDate.of(2021, 6, 2);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);

        avtale.getTilskuddPeriode().forEach(tilskuddPeriode -> {
            LocalDate fra = tilskuddPeriode.getStartDato();
            LocalDate til = tilskuddPeriode.getSluttDato();
            // fra og til dato er i samme måned
            assertThat(fra.getMonth()).isEqualTo(til.getMonth());
            // Det er maks 1 måneds lengde på tilskuddsperiodene
            int dagerITilskuddsperiode = fra.until(til).getDays() + 1;
            int dagerIMåneden = fra.lengthOfMonth();
            assertThat(dagerITilskuddsperiode).isLessThanOrEqualTo(dagerIMåneden);
        });
        Now.resetClock();
    }

    @Test
    public void splittVedMånedsskifte() {
        Now.fixedDate(LocalDate.of(2021, 1, 1));
        LocalDate avtaleStart = LocalDate.of(2021, 1, 20);
        LocalDate avtaleSlutt = LocalDate.of(2022, 3, 2);
        Avtale avtale = TestData.enMidlertidigLønnstilskuddsAvtaleMedStartOgSluttGodkjentAvAlleParter(avtaleStart, avtaleSlutt);

        avtale.getTilskuddPeriode().forEach(tilskuddPeriode -> {
            // start- og sluttdato er alltid i samme måned
            assertThat(tilskuddPeriode.getStartDato().getMonth()).isEqualTo(tilskuddPeriode.getSluttDato().getMonth());
        });


        Now.resetClock();
    }

    /* ------------ Tester av BeregningStrategy.forkort ------------ */

    @ParameterizedTest
    @EnumSource(value = TilskuddPeriodeStatus.class, names = {"UBEHANDLET", "GODKJENT"})
    public void forkort_beholder_perioder_som_slutter_for_eller_pa_ny_sluttdato(TilskuddPeriodeStatus status) {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31));
        TilskuddPeriode forNySlutt = avtale.tilskuddsperiode(0);
        TilskuddPeriode paNySlutt = avtale.tilskuddsperiode(1);
        forNySlutt.setStatus(status);
        paNySlutt.setStatus(status);
        Integer belopFor = forNySlutt.getBeløp();
        Integer belopPa = paNySlutt.getBeløp();
        LocalDate nySluttDato = LocalDate.of(2021, 2, 28);
        tomHendelser(avtale);

        BeregningStrategy.create(avtale).forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactly(forNySlutt, paNySlutt);
        assertThat(forNySlutt.getSluttDato()).isEqualTo(LocalDate.of(2021, 1, 31));
        assertThat(forNySlutt.getBeløp()).isEqualTo(belopFor);
        assertThat(forNySlutt.getStatus()).isEqualTo(status);
        assertThat(paNySlutt.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(paNySlutt.getBeløp()).isEqualTo(belopPa);
        assertThat(paNySlutt.getStatus()).isEqualTo(status);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    public void forkort_forkorter_ubehandlet_periode_som_krysser_ny_sluttdato_uten_hendelse() {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 28));
        TilskuddPeriode periodeSomForkortes = avtale.tilskuddsperiode(1);
        Integer opprinneligBelop = periodeSomForkortes.getBeløp();
        LocalDate nySluttDato = LocalDate.of(2021, 2, 15);
        BeregningStrategy strategy = BeregningStrategy.create(avtale);
        Integer forventetBelop = strategy.getBeløpForPeriode(
            avtale.getGjeldendeInnhold(),
            Periode.av(periodeSomForkortes.getStartDato(), nySluttDato)
        );
        tomHendelser(avtale);

        strategy.forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).contains(periodeSomForkortes);
        assertThat(periodeSomForkortes.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(periodeSomForkortes.getBeløp()).isEqualTo(forventetBelop).isLessThan(opprinneligBelop);
        assertThat(periodeSomForkortes.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    public void forkort_forkorter_godkjent_periode_som_krysser_ny_sluttdato_med_hendelse() {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 28));
        TilskuddPeriode periodeSomForkortes = avtale.tilskuddsperiode(1);
        periodeSomForkortes.setStatus(TilskuddPeriodeStatus.GODKJENT);
        Integer opprinneligBelop = periodeSomForkortes.getBeløp();
        LocalDate nySluttDato = LocalDate.of(2021, 2, 15);
        BeregningStrategy strategy = BeregningStrategy.create(avtale);
        Integer forventetBelop = strategy.getBeløpForPeriode(
            avtale.getGjeldendeInnhold(),
            Periode.av(periodeSomForkortes.getStartDato(), nySluttDato)
        );
        tomHendelser(avtale);

        strategy.forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).contains(periodeSomForkortes);
        assertThat(periodeSomForkortes.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(periodeSomForkortes.getBeløp()).isEqualTo(forventetBelop).isLessThan(opprinneligBelop);
        assertThat(periodeSomForkortes.getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeForkortet(avtale, periodeSomForkortes));
    }

    @ParameterizedTest
    @EnumSource(value = TilskuddPeriodeStatus.class, names = {"UBEHANDLET", "GODKJENT"})
    public void forkort_til_startdato_av_periode_gir_endagsperiode(TilskuddPeriodeStatus status) {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 28));
        TilskuddPeriode periodeSomForkortes = avtale.tilskuddsperiode(1);
        periodeSomForkortes.setStatus(status);
        LocalDate nySluttDato = periodeSomForkortes.getStartDato();
        BeregningStrategy strategy = BeregningStrategy.create(avtale);
        Integer forventetBelop = strategy.getBeløpForPeriode(
            avtale.getGjeldendeInnhold(),
            Periode.av(nySluttDato, nySluttDato)
        );
        tomHendelser(avtale);

        strategy.forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).contains(periodeSomForkortes);
        assertThat(periodeSomForkortes.getStartDato()).isEqualTo(nySluttDato);
        assertThat(periodeSomForkortes.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(periodeSomForkortes.getBeløp()).isEqualTo(forventetBelop);
        assertThat(periodeSomForkortes.getStatus()).isEqualTo(status);
        if (status == TilskuddPeriodeStatus.GODKJENT) {
            assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeForkortet(avtale, periodeSomForkortes));
        } else {
            assertThat(hentHendelser(avtale)).isEmpty();
        }
    }

    @Test
    public void forkort_fjerner_ubehandlet_periode_etter_ny_sluttdato_uten_hendelse() {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31));
        TilskuddPeriode beholdes = avtale.tilskuddsperiode(0);
        TilskuddPeriode fjernes1 = avtale.tilskuddsperiode(1);
        TilskuddPeriode fjernes2 = avtale.tilskuddsperiode(2);
        tomHendelser(avtale);

        BeregningStrategy.create(avtale).forkort(LocalDate.of(2021, 1, 31));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(beholdes);
        assertThat(avtale.getTilskuddPeriode()).doesNotContain(fjernes1, fjernes2);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    public void forkort_annullerer_godkjent_periode_etter_ny_sluttdato_med_hendelse() {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 28));
        TilskuddPeriode beholdes = avtale.tilskuddsperiode(0);
        TilskuddPeriode annulleres = avtale.tilskuddsperiode(1);
        annulleres.setStatus(TilskuddPeriodeStatus.GODKJENT);
        LocalDate opprinneligSluttDato = annulleres.getSluttDato();
        Integer opprinneligBelop = annulleres.getBeløp();
        tomHendelser(avtale);

        BeregningStrategy.create(avtale).forkort(LocalDate.of(2021, 1, 31));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(beholdes, annulleres);
        assertThat(annulleres.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(annulleres.getSluttDato()).isEqualTo(opprinneligSluttDato);
        assertThat(annulleres.getBeløp()).isEqualTo(opprinneligBelop);
        assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeAnnullert(avtale, annulleres));
    }

    @Test
    public void forkort_annullerer_godkjent_periode_med_utgatt_refusjon_etter_ny_sluttdato_uten_hendelse() {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 2, 28));
        TilskuddPeriode beholdes = avtale.tilskuddsperiode(0);
        TilskuddPeriode annulleresMedUtgattRefusjon = avtale.tilskuddsperiode(1);
        annulleresMedUtgattRefusjon.setStatus(TilskuddPeriodeStatus.GODKJENT);
        annulleresMedUtgattRefusjon.setRefusjonStatus(RefusjonStatus.UTGÅTT);
        tomHendelser(avtale);

        BeregningStrategy.create(avtale).forkort(LocalDate.of(2021, 1, 31));

        assertThat(avtale.getTilskuddPeriode()).containsExactly(beholdes, annulleresMedUtgattRefusjon);
        assertThat(annulleresMedUtgattRefusjon.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TilskuddPeriodeStatus.class, names = {"AVSLÅTT", "ANNULLERT", "BEHANDLET_I_ARENA"})
    public void forkort_lar_perioder_med_andre_statuser_vaere_urort(TilskuddPeriodeStatus status) {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31));
        TilskuddPeriode kryssende = avtale.tilskuddsperiode(1);
        TilskuddPeriode etter = avtale.tilskuddsperiode(2);
        kryssende.setStatus(status);
        etter.setStatus(status);
        LocalDate kryssendeSluttFor = kryssende.getSluttDato();
        Integer kryssendeBelopFor = kryssende.getBeløp();
        LocalDate etterSluttFor = etter.getSluttDato();
        Integer etterBelopFor = etter.getBeløp();
        tomHendelser(avtale);

        BeregningStrategy.create(avtale).forkort(LocalDate.of(2021, 2, 15));

        assertThat(avtale.getTilskuddPeriode()).contains(kryssende, etter);
        assertThat(kryssende.getStatus()).isEqualTo(status);
        assertThat(kryssende.getSluttDato()).isEqualTo(kryssendeSluttFor);
        assertThat(kryssende.getBeløp()).isEqualTo(kryssendeBelopFor);
        assertThat(etter.getStatus()).isEqualTo(status);
        assertThat(etter.getSluttDato()).isEqualTo(etterSluttFor);
        assertThat(etter.getBeløp()).isEqualTo(etterBelopFor);
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    public void forkort_med_tom_tilskuddsperiodeliste_gjor_ingenting() {
        Avtale avtale = TestData.enMidlertidigLonnstilskuddsjobbAvtale();
        tomHendelser(avtale);

        BeregningStrategy.create(avtale).forkort(LocalDate.of(2021, 2, 15));

        assertThat(avtale.getTilskuddPeriode()).isEmpty();
        assertThat(hentHendelser(avtale)).isEmpty();
    }

    @Test
    public void forkort_handterer_kombinasjon_av_tilstander_inkludert_deaktivert_avslatt_med_samme_lopenummer() {
        Avtale avtale = opprettLonnstilskuddsavtale(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 6, 30));
        TilskuddPeriode forNySlutt = avtale.tilskuddsperiode(0);
        TilskuddPeriode avslattKryssende = avtale.tilskuddsperiode(1);
        TilskuddPeriode godkjentEtter = avtale.tilskuddsperiode(2);
        TilskuddPeriode godkjentUtgattEtter = avtale.tilskuddsperiode(3);
        TilskuddPeriode ubehandletEtter = avtale.tilskuddsperiode(4);
        TilskuddPeriode arenaEtter = avtale.tilskuddsperiode(5);

        forNySlutt.setStatus(TilskuddPeriodeStatus.GODKJENT);
        avslattKryssende.setStatus(TilskuddPeriodeStatus.AVSLÅTT);
        TilskuddPeriode reaktivertKryssende = avslattKryssende.deaktiverOgLagNyUbehandlet();
        avtale.getTilskuddPeriode().add(reaktivertKryssende);
        godkjentEtter.setStatus(TilskuddPeriodeStatus.GODKJENT);
        godkjentUtgattEtter.setStatus(TilskuddPeriodeStatus.GODKJENT);
        godkjentUtgattEtter.setRefusjonStatus(RefusjonStatus.UTGÅTT);
        arenaEtter.setStatus(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);

        LocalDate nySluttDato = LocalDate.of(2021, 2, 15);
        BeregningStrategy strategy = BeregningStrategy.create(avtale);
        Integer forventetForkortetBelop = strategy.getBeløpForPeriode(
            avtale.getGjeldendeInnhold(),
            Periode.av(reaktivertKryssende.getStartDato(), nySluttDato)
        );
        tomHendelser(avtale);

        strategy.forkort(nySluttDato);

        assertThat(avtale.getTilskuddPeriode()).containsExactlyInAnyOrder(
            forNySlutt,
            avslattKryssende,
            reaktivertKryssende,
            godkjentEtter,
            godkjentUtgattEtter,
            arenaEtter
        );
        assertThat(avtale.getTilskuddPeriode()).doesNotContain(ubehandletEtter);
        assertThat(forNySlutt.getStatus()).isEqualTo(TilskuddPeriodeStatus.GODKJENT);
        assertThat(avslattKryssende.getStatus()).isEqualTo(TilskuddPeriodeStatus.AVSLÅTT);
        assertThat(avslattKryssende.getSluttDato()).isEqualTo(LocalDate.of(2021, 2, 28));
        assertThat(reaktivertKryssende.getStatus()).isEqualTo(TilskuddPeriodeStatus.UBEHANDLET);
        assertThat(reaktivertKryssende.getSluttDato()).isEqualTo(nySluttDato);
        assertThat(reaktivertKryssende.getBeløp()).isEqualTo(forventetForkortetBelop);
        assertThat(godkjentEtter.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(godkjentUtgattEtter.getStatus()).isEqualTo(TilskuddPeriodeStatus.ANNULLERT);
        assertThat(arenaEtter.getStatus()).isEqualTo(TilskuddPeriodeStatus.BEHANDLET_I_ARENA);
        assertThat(hentHendelser(avtale)).containsExactly(new TilskuddsperiodeAnnullert(avtale, godkjentEtter));
    }

    private Avtale opprettLonnstilskuddsavtale(LocalDate startDato, LocalDate sluttDato) {
        Now.fixedDate(startDato);
        Avtale avtale = TestData.enMidlertidigLonnstilskuddAvtaleMedAltUtfylt();
        EndreAvtaleDTO endreAvtale = TestData.endringPåAlleLønnstilskuddFelter(startDato, sluttDato);
        avtale.endreAvtale(endreAvtale, Avtalerolle.VEILEDER);
        return avtale;
    }

    private void tomHendelser(Avtale avtale) {
        ReflectionTestUtils.invokeMethod(avtale, "clearDomainEvents");
    }

    private Collection<Object> hentHendelser(Avtale avtale) {
        return ReflectionTestUtils.invokeMethod(avtale, "domainEvents");
    }

    /* ------------ Metoder som kun brukes innad i denne test-klassen ------------ */
    private void harRiktigeEgenskaper(Avtale avtale) {
        harOverlappendeDatoer(avtale.getTilskuddPeriode());
        harAlleDageneIAvtalenperioden(avtale.getTilskuddPeriode(), avtale.getGjeldendeInnhold().getStartDato(), avtale.getGjeldendeInnhold().getSluttDato());
        harRiktigeLøpenumre(avtale.getTilskuddPeriode());
    }

    private void harAlleDageneIAvtalenperioden(Collection<TilskuddPeriode> tilskuddPerioder, LocalDate avtaleStart, LocalDate avtaleSlutt) {
        long antallDager = avtaleStart.until(avtaleSlutt.plusDays(1), ChronoUnit.DAYS);
        Set<LocalDate> dateSet = new HashSet<>();
        List<TilskuddPeriode> tilskuddsperioderUtenAnnullerte = tilskuddPerioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList());
        for (TilskuddPeriode periode : tilskuddsperioderUtenAnnullerte) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());
            dateSet.addAll(localDates);
        }
        if (dateSet.size() != antallDager) {
            fail("Ulikt antall dager i avtalen og tilskuddsperiodene");
        }

    }

    private void harOverlappendeDatoer(Collection<TilskuddPeriode> tilskuddPerioder) {
        Set<LocalDate> dateSet = new HashSet<>();
        List<TilskuddPeriode> tilskuddsperioderUtenAnnullerte = tilskuddPerioder.stream().filter(tilskuddPeriode -> tilskuddPeriode.getStatus() != TilskuddPeriodeStatus.ANNULLERT).collect(Collectors.toList());
        for (TilskuddPeriode periode : tilskuddsperioderUtenAnnullerte) {
            Set<LocalDate> localDates = periode.getStartDato().datesUntil(periode.getSluttDato().plusDays(1)).collect(Collectors.toSet());

            if (!Collections.disjoint(dateSet, localDates)) {
                fail("Det finnes overlappende datoer");
            } else {
                dateSet.addAll(localDates);
            }
        }
    }

    private void harRiktigeLøpenumre(Collection<TilskuddPeriode> tilskuddPerioder) {
        int løpenummer = 1;
        for (TilskuddPeriode tilskuddPeriode : tilskuddPerioder) {
            assertThat(tilskuddPeriode.getLøpenummer()).isEqualTo(løpenummer++);
        }
    }


    /* ------------ Tester av metoder som kun brukes innad i denne test-klassen ------------ */

    @Test
    public void sjekk_at_ikkeoverlappende_periode_ikke_har_overlappende_datoer() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 1), 60);
        harOverlappendeDatoer(new TreeSet<>(Set.of(tilskuddPeriode1, tilskuddPeriode2)));
    }

    @Test
    public void sjekk_at_har_overlappende_datoer() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 3, 31), LocalDate.of(2021, 6, 1), 60);
        assertThatThrownBy(() -> harOverlappendeDatoer(List.of(tilskuddPeriode1, tilskuddPeriode2))).isInstanceOf(AssertionError.class);
    }

    @Test
    public void sjekk_at_alle_dagene_i_avtalen_er_i_tilskuddsperiodene() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 1), 60);
        harAlleDageneIAvtalenperioden(List.of(tilskuddPeriode1, tilskuddPeriode2), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 6, 1));
    }

    @Test
    public void sjekk_at_det_feiler_nar_ikke_alle_dagene_i_avtalen_er_i_tilskuddsperiodene() {
        TilskuddPeriode tilskuddPeriode1 = new TilskuddPeriode(1000, LocalDate.of(2021, 1, 1), LocalDate.of(2021, 3, 31), 60);
        TilskuddPeriode tilskuddPeriode2 = new TilskuddPeriode(1000, LocalDate.of(2021, 4, 1), LocalDate.of(2021, 5, 25), 60);
        assertThatThrownBy(() -> harAlleDageneIAvtalenperioden(List.of(tilskuddPeriode1, tilskuddPeriode2), LocalDate.of(2021, 1, 1), LocalDate.of(2021, 5, 26))).isInstanceOf(AssertionError.class);
    }

}
