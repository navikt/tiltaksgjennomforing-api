package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreInkluderingstilskuddDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.InkluderingstilskuddsutgiftDTO;
import no.nav.tag.tiltaksgjennomforing.exceptions.Feilkode;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.AssertFeilkode.assertFeilkode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
public class InkluderingstilskuddTest {
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setup() {
        Now.fixedDate(LocalDate.of(2027, 1, 1));
        featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        new FeatureToggleHolder(featureToggleService);
    }

    @AfterEach
    public void tearDown() {
        FeatureToggleHolder.reset();
        Now.resetClock();
    }

    @Test
    public void endreInkluderingstilskudd_verifisere_enkel_endring() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.endreAvtale(TestData.endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER);

        assertThat(avtale.getTilskuddPeriode())
            .extracting(TilskuddPeriode::getLøpenummer, TilskuddPeriode::getBeløp)
            .containsExactlyInAnyOrder(tuple(1, 13337), tuple(2, 25697), tuple(3, 7195));
    }

    @Test
    public void endreAvtale_med_uendrede_utgifter_skal_koble_utgiftene_til_avtalens_tilskuddsperioder() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.endreAvtale(TestData.endringPåAlleInkluderingstilskuddFelter(), Avtalerolle.VEILEDER);

        assertThat(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift())
            .allSatisfy(utgift -> assertThat(avtale.getTilskuddPeriode()).contains(utgift.getTilskuddperiode()));
    }

    @Test
    public void endreInkluderingstilskudd_verifisere_endring_etter_godkjenning() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver().getIdentifikator());
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        // Testen lagrer ikke til database, så id må settes manuelt på eksisterende utgifter
        avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().forEach(utgift -> utgift.setId(UUID.randomUUID()));
        List<InkluderingstilskuddsutgiftDTO> eksisterendeUtgifter = InkluderingstilskuddsutgiftDTO.map(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift());

        EndreInkluderingstilskuddDTO endreInkluderingstilskudd = TestData.endringMedNyeInkluderingstilskudd(eksisterendeUtgifter);
        avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, TestData.enNavIdent());

        assertThat(avtale.getTilskuddPeriode())
            .extracting(TilskuddPeriode::getLøpenummer)
            .containsExactlyInAnyOrder(1, 2, 3, 4);
        assertThat(avtale.getTilskuddPeriode())
            .filteredOn(periode -> periode.getLøpenummer() == 4)
            .singleElement()
            .extracting(TilskuddPeriode::getBeløp)
            .isEqualTo(13337);
        assertThat(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift()).hasSize(4);
    }

    @Test
    public void endreInkluderingstilskudd_avvises_for_avtale_godkjent_før_ny_inkluderingstilskudd() {
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(false);
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver().getIdentifikator());
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift().forEach(utgift -> utgift.setId(UUID.randomUUID()));
        List<InkluderingstilskuddsutgiftDTO> eksisterendeUtgifter = InkluderingstilskuddsutgiftDTO.map(avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift());

        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        EndreInkluderingstilskuddDTO endreInkluderingstilskudd = TestData.endringMedNyeInkluderingstilskudd(eksisterendeUtgifter);

        assertFeilkode(
            Feilkode.KAN_IKKE_ENDRE_INKLUDERINGSTILSKUDD_IKKE_BELOP_ELLER_TYPE,
            () -> avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, TestData.enNavIdent())
        );
    }
}
