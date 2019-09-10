package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.junit.Test;

import no.nav.tag.tiltaksgjennomforing.featuretoggles.ByEnvironmentStrategy;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ByEnvironmentStrategyTest {

    @Test
    public void featureIsEnabledWhenEnvironmentInList() {
        assertThat(new ByEnvironmentStrategy("local").isEnabled(Map.of("miljø", "local,dev"))).isEqualTo(true);
    }

    @Test
    public void strategiSkalHandtereFlereProfiler() {
        assertThat(new ByEnvironmentStrategy("local,test").isEnabled(Map.of("miljø", "local,dev"))).isEqualTo(true);
    }

    @Test
    public void featureIsDisabledWhenEnvironmentNotInList() {
        assertThat(new ByEnvironmentStrategy("dev").isEnabled(Map.of("miljø", "prod"))).isEqualTo(false);
    }
    
    @Test
    public void skalReturnereFalseHvisParametreErNull() {
        assertThat(new ByEnvironmentStrategy("dev").isEnabled(null)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisMiljøIkkeErSatt() {
        assertThat(new ByEnvironmentStrategy("dev").isEnabled(new HashMap<>())).isEqualTo(false);
    }
}
