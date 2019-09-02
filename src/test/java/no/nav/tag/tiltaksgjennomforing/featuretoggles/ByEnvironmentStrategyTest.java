package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.junit.Test;

import no.nav.tag.tiltaksgjennomforing.featuretoggles.ByEnvironmentStrategy;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ByEnvironmentStrategyTest {

    @Test
    public void featureIsEnabledWhenEnvironmentInList() {
        Map<String, String> parametre = new HashMap<>();

        parametre.put("miljø", "local,dev");
        assertThat(new ByEnvironmentStrategy("local").isEnabled(parametre)).isEqualTo(true);
    }

    @Test
    public void featureIsDisabledWhenEnvironmentNotInList() {
        Map<String, String> parametre = new HashMap<>();

        parametre.put("miljø", "prod");
        assertThat(new ByEnvironmentStrategy("dev").isEnabled(parametre)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisParametreErNull() {
        assertThat(new ByEnvironmentStrategy("dev").isEnabled(null)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisMiljøIkkeErSatt() {
        Map<String, String> parametre = new HashMap<>();
        assertThat(new ByEnvironmentStrategy("dev").isEnabled(parametre)).isEqualTo(false);
    }
}
