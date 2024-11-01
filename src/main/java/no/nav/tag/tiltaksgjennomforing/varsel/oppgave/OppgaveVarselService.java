package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.exceptions.GosysFeilException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.CorrelationIdSupplier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
class OppgaveVarselService {

    private static final String CORR_ID = "X-Correlation-ID";
    private final URI uri;
    private final RestTemplate azureRestTemplate;

    public OppgaveVarselService(
        OppgaveProperties props,
        RestTemplate azureRestTemplate
    ) {
        uri = UriComponentsBuilder.fromUri(props.getOppgaveUri()).build().toUri();
        this.azureRestTemplate = azureRestTemplate;
    }

    public void opprettOppgave(OppgaveRequest oppgaveRequest) {
        OppgaveResponse oppgaveResponse;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(CORR_ID, CorrelationIdSupplier.get());

        try {
            oppgaveResponse = azureRestTemplate.postForObject(
                uri,
                new HttpEntity<>(oppgaveRequest, headers),
                OppgaveResponse.class
            );
        } catch (Exception e2) {
            log.error("Klarte ikke opprette Oppgave i Gosys", e2);
            throw new GosysFeilException();
        }
        log.info("Opprettet Oppgave i Gosys. OppgaveId={}", oppgaveResponse.getId());
    }

    @Data
    static class OppgaveResponse {
        private String id;
    }
}
