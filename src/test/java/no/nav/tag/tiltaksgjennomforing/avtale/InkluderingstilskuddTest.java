package no.nav.tag.tiltaksgjennomforing.avtale;

import no.nav.tag.tiltaksgjennomforing.SyntetiskFnr;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggle;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleHolder;
import no.nav.tag.tiltaksgjennomforing.featuretoggles.FeatureToggleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
    }

    @Test
    public void endreInkluderingstilskudd_verifisere_endring_etter_godkjenning() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver().getIdentifikator());
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        List<Inkluderingstilskuddsutgift> eksisterendeUtgifter = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift();

        // Jukse litt ved å sette id på inkluderingstilskudd-entitetene, ettersom denne testen ikke lagrer til database,
        // og endreInkluderingstilskudd-metoden forventer at id er satt for eksisterende utgifter.
        eksisterendeUtgifter.forEach(x -> x.setId(UUID.randomUUID()));

        EndreInkluderingstilskudd endreInkluderingstilskudd = TestData.endringMedNyeInkluderingstilskudd(eksisterendeUtgifter);
        avtale.endreInkluderingstilskudd(endreInkluderingstilskudd, TestData.enNavIdent());
    }

    @Test
    public void endre_inkluderingstilskudd_beholder_lopenummer_paa_eksisterende_tilskuddsperioder() {
        Avtale avtale = TestData.enInkluderingstilskuddAvtale();
        avtale.godkjennForArbeidsgiver(TestData.enArbeidsgiver().getIdentifikator());
        avtale.godkjennForVeilederOgDeltaker(TestData.enNavIdent(), TestData.enGodkjentPaVegneGrunn());
        List<Inkluderingstilskuddsutgift> eksisterendeUtgifter = avtale.getGjeldendeInnhold().getInkluderingstilskuddsutgift();
        eksisterendeUtgifter.forEach(x -> x.setId(UUID.randomUUID()));
        Map<UUID, Integer> lopenummerFoer = avtale.getTilskuddPeriode().stream()
            .collect(Collectors.toMap(TilskuddPeriode::getId, TilskuddPeriode::getLøpenummer));

        avtale.endreInkluderingstilskudd(
            TestData.endringMedNyeInkluderingstilskudd(eksisterendeUtgifter),
            TestData.enNavIdent()
        );

        assertThat(lopenummerFoer).isNotEmpty();
        assertThat(avtale.getTilskuddPeriode()).hasSizeGreaterThan(lopenummerFoer.size());
        avtale.getTilskuddPeriode().stream()
            .filter(periode -> lopenummerFoer.containsKey(periode.getId()))
            .forEach(periode -> assertThat(periode.getLøpenummer()).isEqualTo(lopenummerFoer.get(periode.getId())));
        assertThat(avtale.getTilskuddPeriode()).extracting(TilskuddPeriode::getLøpenummer).doesNotHaveDuplicates();
    }
}
