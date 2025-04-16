package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import io.getunleash.UnleashContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static no.nav.tag.tiltaksgjennomforing.Miljø.DEV_FSS;
import static no.nav.tag.tiltaksgjennomforing.Miljø.LOCAL;
import static no.nav.tag.tiltaksgjennomforing.Miljø.PROD_FSS;
import static org.assertj.core.api.Assertions.assertThat;

public class ByEnvironmentStrategyTest {

    private static final UnleashContext TOM_KONTEKST = UnleashContext.builder().build();

    @Test
    public void featureIsEnabledWhenEnvironmentInList() {
        assertThat(new ByEnvironmentStrategy(LOCAL).isEnabled(Map.of("miljø", "local,dev-fss"), TOM_KONTEKST)).isEqualTo(true);
    }

    @Test
    public void featureIsEnabledWhenLocalEnvironmentInList() {
        assertThat(new ByEnvironmentStrategy(LOCAL).isEnabled(Map.of("miljø", "local"), TOM_KONTEKST)).isEqualTo(true);
    }

    @Test
    public void featureIsDisabledWhenEnvironmentNotInList() {
        assertThat(new ByEnvironmentStrategy(PROD_FSS).isEnabled(Map.of("miljø", "local"), TOM_KONTEKST)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisParametreErNull() {
        assertThat(new ByEnvironmentStrategy(DEV_FSS).isEnabled(null, TOM_KONTEKST)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisMiljøIkkeErSatt() {
        assertThat(new ByEnvironmentStrategy(DEV_FSS).isEnabled(new HashMap<>(), TOM_KONTEKST)).isEqualTo(false);
    }
}
