package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.EndreInkluderingstilskuddDTO;
import no.nav.tag.tiltaksgjennomforing.avtale.transportlag.InkluderingstilskuddsutgiftDTO;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SyntetiskFnr
public class InkluderingstilskuddTest {

    @BeforeEach
    public void setup() {
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
        when(featureToggleService.isEnabled(FeatureToggle.NY_INKLUDERINGSTILSKUDD)).thenReturn(true);
        new FeatureToggleHolder(featureToggleService);
    }

    @AfterEach
    public void tearDown() {
        FeatureToggleHolder.reset();
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
}
