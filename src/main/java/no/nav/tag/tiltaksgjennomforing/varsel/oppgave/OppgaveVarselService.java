package no.nav.tag.tiltaksgjennomforing.varsel.oppgave;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;

@Slf4j
@Service
public class OppgaveVarselService {

    private static final String CORR_ID = "X-Correlation-ID";
    private final URI uri;
    private final RestTemplate restTemplate;
    private final STSClient stsClient;

    public OppgaveVarselService(OppgaveProperties props, RestTemplate restTemplate, STSClient stsClient) {
        uri = UriComponentsBuilder.fromUri(props.getOppgaveUri()).build().toUri();
        this.restTemplate = restTemplate;
        this.stsClient = stsClient;
    }

    public void opprettOppgave(final String fnr, String aktørId) {
        OppgaveRequest oppgaveRequest = new OppgaveRequest(fnr, aktørId);
        OppgaveResponse oppgaveResponse;
        String corrId = "corr-id";

            try {
                oppgaveResponse = restTemplate.postForObject(uri, entityMedStsToken(oppgaveRequest, corrId), OppgaveResponse.class);
            } catch (Exception e2) {
                log.error("Kall til Oppgave feilet: {}", e2.getMessage());
                throw e2;
            }

        log.info("Opprettet oppgave {} for manuell søknad {} med sakid {}", oppgaveResponse.getId(), corrId);
    }

    private HttpEntity<OppgaveRequest> entityMedStsToken(final OppgaveRequest oppgaveRequest, String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        headers.set(CORR_ID, correlationId);
        HttpEntity<OppgaveRequest> entity = new HttpEntity<>(oppgaveRequest, headers);
        return entity;
    }
}
