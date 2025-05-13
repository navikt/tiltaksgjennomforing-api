package no.nav.tag.tiltaksgjennomforing.arena.client.hendelse;

import no.nav.tag.tiltaksgjennomforing.arena.configuration.HendelseAktivitetsplanProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class HendelseAktivitetsplanClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;

    public HendelseAktivitetsplanClient(
        HendelseAktivitetsplanProperties properties,
        RestTemplate azureRestTemplate
    ) {
        this.baseUrl = properties.getUrl();
        this.restTemplate = azureRestTemplate;
    }

    public void putAktivitetsplanId(UUID avtaleId, UUID aktivitetsplanId) {
        putAktivitetsplanId(avtaleId, aktivitetsplanId, false);
    }

    public void putAktivitetsplanId(UUID avtaleId, UUID aktivitetsplanId, boolean resendSisteMelding) {
        HendelseAktivitetsplanIdRequest request = new HendelseAktivitetsplanIdRequest(
            aktivitetsplanId,
            resendSisteMelding
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.put(
                baseUrl + "/tiltak-hendelse-aktivitetsplan/api/avtale/{avtaleId}/aktivitetsplan-id",
                new HttpEntity<>(request, headers),
                Map.of("avtaleId", avtaleId.toString())
            );
        } catch (RestClientException e) {
            throw new RuntimeException(
                "Klarte ikke å oppdatere aktivitetsplan id i tiltak-hendelse-aktivitetsplan for avtale: " + avtaleId,
                e
            );
        }
    }

    public void postSendSisteMelding(UUID avtaleId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForLocation(
                baseUrl + "/tiltak-hendelse-aktivitetsplan/api/avtale/{avtaleId}/send-siste-melding",
                new HttpEntity<>(null, headers),
                Map.of("avtaleId", avtaleId.toString())
            );
        } catch (RestClientException e) {
            throw new RuntimeException(
                "Feil ved ny sending av aktivitetsplanmelding for avtale: " + avtaleId,
                e
            );
        }
    }
}
