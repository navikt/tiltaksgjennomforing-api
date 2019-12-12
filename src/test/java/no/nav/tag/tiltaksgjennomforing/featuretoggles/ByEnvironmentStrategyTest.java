package no.nav.tag.tiltaksgjennomforing.featuretoggles;

import org.junit.Test;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ByEnvironmentStrategyTest {

    @Test
    public void featureIsEnabledWhenEnvironmentInList() {
        assertThat(new ByEnvironmentStrategy(environmentMock("local")).isEnabled(Map.of("miljø", "local,dev"))).isEqualTo(true);
    }

    @Test
    public void strategiSkalHandtereFlereProfiler() {
        assertThat(new ByEnvironmentStrategy(environmentMock("local", "test")).isEnabled(Map.of("miljø", "local,dev"))).isEqualTo(true);
    }

    @Test
    public void featureIsDisabledWhenEnvironmentNotInList() {
        assertThat(new ByEnvironmentStrategy(environmentMock("dev")).isEnabled(Map.of("miljø", "prod"))).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisParametreErNull() {
        assertThat(new ByEnvironmentStrategy(environmentMock("dev")).isEnabled(null)).isEqualTo(false);
    }

    @Test
    public void skalReturnereFalseHvisMiljøIkkeErSatt() {
        assertThat(new ByEnvironmentStrategy(environmentMock("dev")).isEnabled(new HashMap<>())).isEqualTo(false);
    }

    private static Environment environmentMock(String... profiles) {
        Environment mock = mock(Environment.class);
        when(mock.getActiveProfiles()).thenReturn(profiles);
        return mock;
    }
}
