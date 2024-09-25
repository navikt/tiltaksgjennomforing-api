package no.nav.tag.tiltaksgjennomforing.arena.client.hendelse;

import no.nav.tag.tiltaksgjennomforing.arena.configuration.HendelseAktivitetsplanProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    public void putAktivietsplanId(UUID avtaleId, UUID aktivitetsplanId) {
        HendelseAktivietsplanIdRequest request = new HendelseAktivietsplanIdRequest(
            avtaleId,
            aktivitetsplanId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.put(
                baseUrl + "/tiltak-hendelse-aktivitetsplan/api/aktivitetsplan-id",
                new HttpEntity<>(request, headers)
            );
        } catch(RestClientException e) {
            throw new RuntimeException(
                "Klarte ikke å oppdatere aktivitetsplan id i tiltak-hendelse-aktivitetsplan for avtale: " + avtaleId,
                e
            );
        }
    }
}
