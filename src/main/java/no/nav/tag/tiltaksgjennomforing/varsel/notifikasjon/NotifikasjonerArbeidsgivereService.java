package no.nav.tag.tiltaksgjennomforing.varsel.notifikasjon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifikasjonerArbeidsgivereService {
    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private final NotifikasjonerProperties notifikasjonerProperties;
   // private final RestTemplate restTemplate;

    private HttpEntity<String> createRequestEntity(NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest) {
        String stsToken = stsClient.hentSTSToken().getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Tema", "GEN");
        headers.set("Nav-Consumer-Token", "Bearer " + stsToken);
        return new HttpEntity(notifikasjonerArbeidsgiverRequest, headers);
    }

    private NotifikasjonerRespons utførKallTilNotifikasjoner(NotifikasjonerArbeidsgiverRequest notifikasjonerArbeidsgiverRequest) {
        try {
            return restTemplate.postForObject(notifikasjonerProperties.getUri(), createRequestEntity(notifikasjonerArbeidsgiverRequest), NotifikasjonerRespons.class);
        } catch (RestClientException exception) {
            stsClient.evictToken();
            log.error("Feil fra Notifikasjoner med request-url: " + notifikasjonerProperties.getUri(), exception);
            throw exception;
        }
    }
}
