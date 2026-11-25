package no.nav.tag.tiltaksgjennomforing.avtale.startOgSluttDatoStrategy;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Avtaleopphav;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.avtale.OpprettAvtale;
import no.nav.tag.tiltaksgjennomforing.avtale.TestData;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.INKLUDERINGSTILSKUDD;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
class InkluderingstilskuddStartOgSluttDatoStrategyTest {

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        Now.fixedDate(LocalDate.of(2027, 1, 1));
        featureToggleService = mock(FeatureToggleService.class);
        new FeatureToggleHolder(featureToggleService);
    }

    @AfterEach
    void tearDown() {
        FeatureToggleHolder.reset();
        Now.resetClock();
    }

    private InkluderingstilskuddStartOgSluttdatoStrategy opprettStrategy() {
        Avtale avtale = Avtale.opprett(
            new OpprettAvtale(TestData.etFodselsnummer(), TestData.etBedriftNr(), INKLUDERINGSTILSKUDD),
            Avtaleopphav.VEILEDER,
            TestData.enNavIdent()
        );
        return new InkluderingstilskuddStartOgSluttdatoStrategy(avtale);
    }

    @Test
    void deltaker_er_for_gammel_for_å_gå_på_Inkluderingstilskudd() {
        LocalDate avtaleStart = Now.localDate();
        LocalDate avtaleSlutt = avtaleStart.plusMonths(11).plusDays(1);
        Avtale avtale = Avtale.opprett(new OpprettAvtale(Fnr.generer(1952,8,12), TestData.etBedriftNr(), INKLUDERINGSTILSKUDD), Avtaleopphav.VEILEDER, TestData.enNavIdent());
        InkluderingstilskuddStartOgSluttdatoStrategy inkluderingstilskuddStartOgSluttDatoStrategy = new InkluderingstilskuddStartOgSluttdatoStrategy(avtale);
        assertFeilkode(Feilkode.DELTAKER_72_AAR, () -> inkluderingstilskuddStartOgSluttDatoStrategy.sjekkStartOgSluttdato(avtaleStart, avtaleSlutt));
    }

    @Test
    void ny_inkluderingstilskudd_tillater_avtale_innenfor_samme_kalenderaar() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        InkluderingstilskuddStartOgSluttdatoStrategy strategy = opprettStrategy();

        strategy.sjekkStartOgSluttdato(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31));
    }

    @Test
    void ny_inkluderingstilskudd_tillater_ikke_avtale_paa_tvers_av_kalenderaar() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        InkluderingstilskuddStartOgSluttdatoStrategy strategy = opprettStrategy();

        assertFeilkode(
            Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD_PAA_TVERS_AV_AAR,
            () -> strategy.sjekkStartOgSluttdato(LocalDate.of(2027, 1, 2), LocalDate.of(2028, 1, 1))
        );
    }

    @Test
    void ny_inkluderingstilskudd_tillater_ikke_kort_avtale_paa_tvers_av_kalenderaar() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        InkluderingstilskuddStartOgSluttdatoStrategy strategy = opprettStrategy();

        assertFeilkode(
            Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD_PAA_TVERS_AV_AAR,
            () -> strategy.sjekkStartOgSluttdato(LocalDate.of(2027, 12, 15), LocalDate.of(2028, 1, 15))
        );
    }

    @Test
    void ny_inkluderingstilskudd_tillater_ikke_varighet_over_12_maaneder() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        InkluderingstilskuddStartOgSluttdatoStrategy strategy = opprettStrategy();

        assertFeilkode(
            Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD_PAA_TVERS_AV_AAR,
            () -> strategy.sjekkStartOgSluttdato(LocalDate.of(2027, 1, 1), LocalDate.of(2028, 1, 2))
        );
    }

    @Test
    void gammel_inkluderingstilskudd_tillater_avtale_paa_tvers_av_kalenderaar_innenfor_12_maaneder() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(false);
        InkluderingstilskuddStartOgSluttdatoStrategy strategy = opprettStrategy();

        strategy.sjekkStartOgSluttdato(LocalDate.of(2027, 6, 1), LocalDate.of(2028, 5, 31));
    }

    @Test
    void gammel_inkluderingstilskudd_tillater_ikke_varighet_over_12_maaneder() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(false);
        InkluderingstilskuddStartOgSluttdatoStrategy strategy = opprettStrategy();

        assertFeilkode(
            Feilkode.VARIGHET_FOR_LANG_INKLUDERINGSTILSKUDD,
            () -> strategy.sjekkStartOgSluttdato(LocalDate.of(2027, 1, 1), LocalDate.of(2028, 1, 1))
        );
    }
}
