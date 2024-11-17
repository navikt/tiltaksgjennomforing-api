package no.nav.tag.tiltaksgjennomforing.arena.client.ords;

import no.nav.tag.tiltaksgjennomforing.arena.configuration.ArenaOrdsProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class ArenaOrdsClient {
    private String baseUrl;
    private final RestTemplate restTemplate;
    private final ArenaOrdsTokenClient tokenClient;

    public ArenaOrdsClient(
        ArenaOrdsProperties properties,
        ArenaOrdsTokenClient arenaOrdsTokenClient,
        RestTemplate noAuthRestTemplate
    ) {
        this.baseUrl = properties.getUrl();
        this.restTemplate = noAuthRestTemplate;
        this.tokenClient = arenaOrdsTokenClient;
    }

    public Optional<ArenaOrdsFnrResponse> getFnr(int personId) {
        ArenaOrdsFnrRequest request = new ArenaOrdsFnrRequest(List.of(new ArenaOrdsFnrRequest.Person(personId)));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClient.getToken());

        ResponseEntity<ArenaOrdsFnrResponse> response = restTemplate.postForEntity(
            baseUrl + "/arena/api/v1/person/identListe",
            new HttpEntity<>(request, headers),
            ArenaOrdsFnrResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feil ved henting av fnr fra Arena ORDS. Status: " + response.getStatusCode());
        }

        if (HttpStatus.NO_CONTENT == response.getStatusCode()) {
            return Optional.empty();
        }

        ArenaOrdsFnrResponse body = response.getBody();
        if (body == null || body.personListe() == null || body.personListe().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(body);
    }

    public Optional<ArenaOrdsArbeidsgiverResponse> getArbeidsgiver(int arbeidsgiverId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClient.getToken());
        headers.set("arbgivId", String.valueOf(arbeidsgiverId));

        ResponseEntity<ArenaOrdsArbeidsgiverResponse> response = restTemplate.exchange(
            baseUrl + "/arena/api/v1/arbeidsgiver/ident",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ArenaOrdsArbeidsgiverResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feil ved henting av arbeidsgiver fra Arena ORDS. Status: " + response.getStatusCode());
        }

        if (HttpStatus.NO_CONTENT == response.getStatusCode()) {
            return Optional.empty();
        }

        ArenaOrdsArbeidsgiverResponse body = response.getBody();
        if (body == null) {
            return Optional.empty();
        }

        return Optional.of(body);
    }
}
