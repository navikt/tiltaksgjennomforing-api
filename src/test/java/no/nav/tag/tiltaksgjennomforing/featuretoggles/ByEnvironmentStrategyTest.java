package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.util.HashMap;
import java.util.Map;

import static no.nav.tag.tiltaksgjennomforing.Miljø.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ByEnvironmentStrategyTest {
    public static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @After
    public void tearDown() throws Exception {
        environmentVariables.clear(NAIS_CLUSTER_NAME);
    }

    @Test
    public void featureIsEnabledWhenEnvironmentInList() {
        environmentMock(LOCAL);
        assertThat(new ByEnvironmentStrategy().isEnabled(Map.of("miljø", "local,dev-fss"))).isEqualTo(true);
    }

    @Test
    public void featureIsEnabledWhenLocalEnvironmentInList() {
        assertThat(new ByEnvironmentStrategy().isEnabled(Map.of("miljø", "local"))).isEqualTo(true);
    }

    @Test
    public void featureIsDisabledWhenEnvironmentNotInList() {
        environmentMock(PROD_FSS);
        assertThat(new ByEnvironmentStrategy().isEnabled(Map.of("miljø", "local"))).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisParametreErNull() {
        environmentMock(DEV_FSS);
        assertThat(new ByEnvironmentStrategy().isEnabled(null)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisMiljøIkkeErSatt() {
        environmentMock(DEV_FSS);
        assertThat(new ByEnvironmentStrategy().isEnabled(new HashMap<>())).isEqualTo(false);
    }

    private void environmentMock(String profile) {
        environmentVariables.set(NAIS_CLUSTER_NAME, profile);
    }
}
