package no.nav.tag.tiltaksgjennomforing.domene;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.tag.tiltaksgjennomforing.domene.events.AvtaleOpprettet;
import no.nav.tag.tiltaksgjennomforing.domene.varsel.VarslbarHendelseRepository;
import no.nav.tag.tiltaksgjennomforing.integrasjon.configurationProperties.PilotProperties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class MetrikkRegistreringTest {

    @Rule
    public OutputCapture capture = new OutputCapture();
    private PilotProperties pilotProperties;
    private MetrikkRegistrering metrikkRegistrering;

    @Before
    public void setUp() throws Exception {
        pilotProperties = new PilotProperties();
        pilotProperties.setIdenter(List.of(new NavIdent("X123456")));
        metrikkRegistrering = new MetrikkRegistrering(mock(MeterRegistry.class, RETURNS_DEEP_STUBS), pilotProperties, mock(VarslbarHendelseRepository.class));
    }

    @Test
    public void opprettAvtale__skal_logge_pilotfylke() {
        metrikkRegistrering.avtaleOpprettet(new AvtaleOpprettet(TestData.enAvtale(), new NavIdent("X123459")));
        assertThat(capture.toString()).contains("PilotFylke=true");
    }

    @Test
    public void opprettAvtale__skal_ikke_logge_pilotfylke() {
        metrikkRegistrering.avtaleOpprettet(new AvtaleOpprettet(TestData.enAvtale(), new NavIdent("X123456")));
        assertThat(capture.toString()).contains("PilotFylke=false");
    }
}