package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import no.nav.tag.tiltaksgjennomforing.exceptions.GosysFeilException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OppgaveVarselServiceTest {

    private URI uri = URI.create("test");
    private OppgaveResponse response = new OppgaveResponse();
    private OppgaveProperties oppgaveProperties = new OppgaveProperties();

    @Captor
    private ArgumentCaptor<HttpEntity<OppgaveRequest>> requestCaptor;

    @Mock
    private STSClient stsClient;

    @Mock
    private RestTemplate restTemplate;

    private OppgaveVarselService oppgaveVarselService;

    @BeforeEach
    public void setUp() {
        response.setId("oppgaveId");
        oppgaveProperties.setOppgaveUri(uri);
        oppgaveVarselService = new OppgaveVarselService(oppgaveProperties, restTemplate, stsClient);
    }

    @Test
    public void oppretterOppgaveRequestForArbeidstrening(){

        when(stsClient.hentSTSToken()).thenReturn(new STSToken());
        when(restTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenReturn(response);

        oppgaveVarselService.opprettOppgave("aktørId", ARBEIDSTRENING, UUID.randomUUID());
        verify(restTemplate).postForObject(eq(uri), requestCaptor.capture(), eq(OppgaveResponse.class));
        OppgaveRequest request = requestCaptor.getValue().getBody();

        assertThat(request.getAktivDato()).isToday();
        assertThat(request.getAktoerId()).isEqualTo("aktørId");
        assertThat(request.getBehandlingstema()).isEqualTo(ARBEIDSTRENING.getBehandlingstema());
        assertThat(request.getBeskrivelse()).isEqualTo("Avtale er opprettet av arbeidsgiver på tiltak ARBEIDSTRENING");
        assertThat(request.getBehandlingstype()).isEqualTo("ae0034");
        assertThat(request.getOppgavetype()).isEqualTo("VURD_HENV");
        assertThat(request.getPrioritet()).isEqualTo("HOY");
        assertThat(request.getTema()).isEqualTo("TIL");
    }

    @Test
    public void oppretterOppgaveRequestForMidlertidigLønnstilskudd(){

        when(stsClient.hentSTSToken()).thenReturn(new STSToken());
        when(restTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenReturn(response);

        oppgaveVarselService.opprettOppgave("aktørId", MIDLERTIDIG_LONNSTILSKUDD, UUID.randomUUID());
        verify(restTemplate).postForObject(eq(uri), requestCaptor.capture(), eq(OppgaveResponse.class));
        OppgaveRequest request = requestCaptor.getValue().getBody();

        assertThat(request.getAktivDato()).isToday();
        assertThat(request.getAktoerId()).isEqualTo("aktørId");
        assertThat(request.getBehandlingstema()).isEqualTo(MIDLERTIDIG_LONNSTILSKUDD.getBehandlingstema());
        assertThat(request.getBeskrivelse()).isEqualTo("Avtale er opprettet av arbeidsgiver på tiltak MIDLERTIDIG_LONNSTILSKUDD");
        assertThat(request.getBehandlingstype()).isEqualTo("ae0034");
        assertThat(request.getOppgavetype()).isEqualTo("VURD_HENV");
        assertThat(request.getPrioritet()).isEqualTo("HOY");
        assertThat(request.getTema()).isEqualTo("TIL");
    }

    @Test
    public void oppretterOppgaveRequestForVarigLønnstilskudd(){

        when(stsClient.hentSTSToken()).thenReturn(new STSToken());
        when(restTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenReturn(response);

        oppgaveVarselService.opprettOppgave("aktørId", VARIG_LONNSTILSKUDD, UUID.randomUUID());
        verify(restTemplate).postForObject(eq(uri), requestCaptor.capture(), eq(OppgaveResponse.class));
        OppgaveRequest request = requestCaptor.getValue().getBody();

        assertThat(request.getAktivDato()).isToday();
        assertThat(request.getAktoerId()).isEqualTo("aktørId");
        assertThat(request.getBehandlingstema()).isEqualTo(VARIG_LONNSTILSKUDD.getBehandlingstema());
        assertThat(request.getBeskrivelse()).isEqualTo("Avtale er opprettet av arbeidsgiver på tiltak VARIG_LONNSTILSKUDD");
        assertThat(request.getBehandlingstype()).isEqualTo("ae0034");
        assertThat(request.getOppgavetype()).isEqualTo("VURD_HENV");
        assertThat(request.getPrioritet()).isEqualTo("HOY");
        assertThat(request.getTema()).isEqualTo("TIL");
    }

    @Test
    public void oppretterOppgaveRequestFeiler() {

        when(stsClient.hentSTSToken()).thenReturn(new STSToken());
        when(restTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenThrow(RuntimeException.class);

        assertThrows(GosysFeilException.class, () -> {
            oppgaveVarselService.opprettOppgave("aktørId", VARIG_LONNSTILSKUDD, UUID.randomUUID());
        });
    }
}
