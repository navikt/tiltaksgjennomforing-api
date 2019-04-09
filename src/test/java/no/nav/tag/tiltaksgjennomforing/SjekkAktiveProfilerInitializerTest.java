package no.nav.tag.tiltaksgjennomforing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SjekkAktiveProfilerInitializerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurableApplicationContext applicationContext;

    private void aktiveProfiler(String... profiler) {
        when(applicationContext.getEnvironment().getActiveProfiles()).thenReturn(profiler);
    }

    @Test(expected = Exception.class)
    public void initialize__ingen_profil() {
        aktiveProfiler();
        new SjekkAktiveProfilerInitializer().initialize(applicationContext);
    }

    @Test(expected = Exception.class)
    public void initialize__feilaktig_profil() {
        aktiveProfiler("foo");
        new SjekkAktiveProfilerInitializer().initialize(applicationContext);
    }

    @Test(expected = Exception.class)
    public void initialize__for_mange_profiler() {
        aktiveProfiler("dev", "prod");
        new SjekkAktiveProfilerInitializer().initialize(applicationContext);
    }

    @Test
    public void initialize__riktig() {
        aktiveProfiler("prod", "foo");
        new SjekkAktiveProfilerInitializer().initialize(applicationContext);
    }
}