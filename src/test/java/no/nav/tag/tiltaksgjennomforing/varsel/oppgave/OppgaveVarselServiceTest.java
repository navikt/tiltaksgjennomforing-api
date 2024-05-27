package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import no.nav.tag.tiltaksgjennomforing.Miljø;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.GosysFeilException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.VARIG_LONNSTILSKUDD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles({Miljø.LOCAL})
@SpringBootTest
public class OppgaveVarselServiceTest {

    private final URI uri = URI.create("test");
    private final OppgaveVarselService.OppgaveResponse oppgaveResponse = new OppgaveVarselService.OppgaveResponse();
    private final OppgaveProperties oppgaveProperties = new OppgaveProperties();

    @Captor
    private ArgumentCaptor<HttpEntity<OppgaveRequest>> requestCaptor;

    @MockBean(name = "azureRestTemplate")
    private RestTemplate azureRestTemplate;

    private OppgaveVarselService oppgaveVarselService;

    @BeforeEach
    public void setUp() {
        oppgaveResponse.setId("oppgaveId");
        oppgaveProperties.setOppgaveUri(uri);
        oppgaveVarselService = new OppgaveVarselService(oppgaveProperties, azureRestTemplate);
    }

    @ParameterizedTest
    @EnumSource(Tiltakstype.class)
    public void oppretterOppgaveRequestForTiltak(Tiltakstype tiltakstype) {
        when(azureRestTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenReturn(oppgaveResponse);

        oppgaveVarselService.opprettOppgave("aktørId", tiltakstype, UUID.randomUUID());
        verify(azureRestTemplate).postForObject(eq(uri), requestCaptor.capture(), eq(OppgaveVarselService.OppgaveResponse.class));
        OppgaveRequest request = requestCaptor.getValue().getBody();

        assertThat(request.getAktivDato()).isToday();
        assertThat(request.getAktoerId()).isEqualTo("aktørId");
        assertThat(request.getBehandlingstema()).isEqualTo(tiltakstype.getBehandlingstema());
        assertThat(request.getBeskrivelse()).contains(tiltakstype.getBeskrivelse());
        assertThat(request.getBeskrivelse()).contains("Avtale er opprettet av arbeidsgiver på tiltak ");
        assertThat(request.getBehandlingstype()).isEqualTo("ae0034");
        assertThat(request.getOppgavetype()).isEqualTo("VURD_HENV");
        assertThat(request.getPrioritet()).isEqualTo("NORM");
        assertThat(request.getTema()).isEqualTo("TIL");
    }

    @Test
    public void oppretterOppgaveRequestFeiler() {
        when(azureRestTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenThrow(RuntimeException.class);

        assertThrows(GosysFeilException.class, () ->
                oppgaveVarselService.opprettOppgave("aktørId", VARIG_LONNSTILSKUDD, UUID.randomUUID()));
    }
}
