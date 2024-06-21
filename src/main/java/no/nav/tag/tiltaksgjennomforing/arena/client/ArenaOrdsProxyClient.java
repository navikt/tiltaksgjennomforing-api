package no.nav.tag.tiltaksgjennomforing.arena.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ArenaOrdsProxyClient {
    private String baseUrl;
    private final RestTemplate restTemplate;

    public ArenaOrdsProxyClient(
        @Value("${tiltaksgjennomforing.arena.ords-proxy.url}") String baseUrl,
        RestTemplate azureRestTemplate

    ) {
        this.restTemplate = azureRestTemplate;
        this.baseUrl = baseUrl;
    }

    public ArenaOrdsFnrResponse getFnr(int personId) {
        ResponseEntity<ArenaOrdsFnrResponse> response = restTemplate.getForEntity(
            baseUrl + "/api/ords/fnr?personId={personId}",
            ArenaOrdsFnrResponse.class,
            personId
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feil ved henting av fnr fra Arena ORDS");
        }

        return response.getBody();
    }

    public ArenaOrdsArbeidsgiverResponse getArbeidsgiver(int arbeidsgiverId) {
        ResponseEntity<ArenaOrdsArbeidsgiverResponse> response = restTemplate.getForEntity(
            baseUrl + "/api/ords/arbeidsgiver?arbeidsgiverId={arbeidsgiverId}",
            ArenaOrdsArbeidsgiverResponse.class,
            arbeidsgiverId
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feil ved henting av arbeidsgiver fra Arena ORDS");
        }

        return response.getBody();
    }
}
