package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import no.nav.tag.tiltaksgjennomforing.autorisasjon.TokenUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeatureToggleServiceTest {

    @Mock private Unleash unleash;
    @Mock private TokenUtils innloggingService;

    @InjectMocks
    private FeatureToggleService featureToggleService;

    @Test
    public void hentFeatureToggles__skal_returnere_true_hvis_feature_er_på() {
        when(unleash.isEnabled(eq(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST.getToggleNavn()), any(UnleashContext.class))).thenReturn(true);
        Map<FeatureToggle, Boolean> toggles = featureToggleService.hentFeatureToggles(List.of(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST));
        assertThat(toggles.get(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST)).isTrue();
    }

    @Test
    public void hentFeatureToggles__skal_returnere_false_hvis_feature_er_av() {
        when(unleash.isEnabled(eq(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST.getToggleNavn()), any(UnleashContext.class))).thenReturn(false);
        Map<FeatureToggle, Boolean> toggles = featureToggleService.hentFeatureToggles(List.of(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST));
        assertThat(toggles.get(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST)).isFalse();
    }

    @Test
    public void hentFeatureToggles__skal_default_returnere_false() {
        Map<FeatureToggle, Boolean> toggles = featureToggleService.hentFeatureToggles(List.of(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST));
        assertThat(toggles.get(FeatureToggle.SJEKK_OM_DELTAKER_KAN_MOTTA_POST)).isFalse();
    }

    @Test
    public void hentFeatureToggles__skal_kunne_returnere_flere_toggles() {
        List<FeatureToggle> features = Arrays.asList(FeatureToggle.KODE_6_SPERRE, FeatureToggle.ARENA_KAFKA, FeatureToggle.SMS_TIL_MOBILNUMMER);
        when(unleash.isEnabled(eq(FeatureToggle.KODE_6_SPERRE.getToggleNavn()), any(UnleashContext.class))).thenReturn(true);
        when(unleash.isEnabled(eq(FeatureToggle.ARENA_KAFKA.getToggleNavn()), any(UnleashContext.class))).thenReturn(false);
        when(unleash.isEnabled(eq(FeatureToggle.SMS_TIL_MOBILNUMMER.getToggleNavn()), any(UnleashContext.class))).thenReturn(false);

        Map<FeatureToggle, Boolean> toggles = featureToggleService.hentFeatureToggles(features);

        assertThat(toggles.get(FeatureToggle.KODE_6_SPERRE)).isTrue();
        assertThat(toggles.get(FeatureToggle.ARENA_KAFKA)).isFalse();
        assertThat(toggles.get(FeatureToggle.SMS_TIL_MOBILNUMMER)).isFalse();
        assertThat(toggles.size()).isEqualTo(3);
    }
}
