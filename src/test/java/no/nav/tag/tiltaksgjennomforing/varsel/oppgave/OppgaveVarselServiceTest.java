package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import no.nav.tag.tiltaksgjennomforing.avtale.Avtale;
import no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype;
import no.nav.tag.tiltaksgjennomforing.exceptions.GosysFeilException;
import no.nav.tag.tiltaksgjennomforing.persondata.aktorId.AktorId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.UUID;

import static java.lang.String.format;
import static no.nav.tag.tiltaksgjennomforing.avtale.Tiltakstype.VARIG_LONNSTILSKUDD;
import static no.nav.tag.tiltaksgjennomforing.varsel.oppgave.LagGosysVarselLytter.GOSYS_OPPRETTET_AVTALE_BESKRIVELSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OppgaveVarselServiceTest {

    private URI uri = URI.create("test");
    private OppgaveVarselService.OppgaveResponse oppgaveResponse = new OppgaveVarselService.OppgaveResponse();
    private OppgaveProperties oppgaveProperties = new OppgaveProperties();

    @Captor
    private ArgumentCaptor<HttpEntity<OppgaveRequest>> requestCaptor;

    @Mock
    private RestTemplate stsRestTemplate;

    private OppgaveVarselService oppgaveVarselService;

    @BeforeEach
    public void setUp() {
        oppgaveResponse.setId("oppgaveId");
        oppgaveProperties.setOppgaveUri(uri);
        oppgaveVarselService = new OppgaveVarselService(oppgaveProperties, stsRestTemplate);
    }

    @ParameterizedTest
    @EnumSource(Tiltakstype.class)
    public void oppretterOppgaveRequestForTiltak(Tiltakstype tiltakstype) {
        when(stsRestTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenReturn(oppgaveResponse);
        Avtale avtale = new Avtale();
        avtale.setTiltakstype(tiltakstype);
        avtale.setId(UUID.randomUUID());

        oppgaveVarselService.opprettOppgave(new OppgaveRequest(
            AktorId.av("aktørId"),
            GosysTema.TILTAK,
            GosysBehandlingstype.SOKNAD,
            avtale.getTiltakstype(),
            format(GOSYS_OPPRETTET_AVTALE_BESKRIVELSE, avtale.getTiltakstype().getBeskrivelse())
        ));
        verify(stsRestTemplate).postForObject(
            eq(uri),
            requestCaptor.capture(),
            eq(OppgaveVarselService.OppgaveResponse.class)
        );
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
        when(stsRestTemplate.postForObject(any(URI.class), any(), any(Class.class))).thenThrow(RuntimeException.class);
        Avtale avtale = new Avtale();
        avtale.setTiltakstype(VARIG_LONNSTILSKUDD);
        avtale.setId(UUID.randomUUID());

        assertThrows(
            GosysFeilException.class, () ->
                oppgaveVarselService.opprettOppgave(new OppgaveRequest(
                    AktorId.av("aktørId"),
                    GosysTema.TILTAK,
                    GosysBehandlingstype.INGEN,
                    Tiltakstype.INKLUDERINGSTILSKUDD,
                    ""
                ))
        );
    }
}
