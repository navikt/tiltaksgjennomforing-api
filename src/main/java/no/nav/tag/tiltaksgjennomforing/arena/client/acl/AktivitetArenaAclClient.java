package no.nav.tag.tiltaksgjennomforing.arena.client.acl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.tag.tiltaksgjennomforing.arena.configuration.AktivitetArenaAclProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class AktivitetArenaAclClient {
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AktivitetArenaAclClient(
        AktivitetArenaAclProperties properties,
        RestTemplate azureRestTemplate,
        ObjectMapper objectMapper
    ) {
        this.baseUrl = properties.getUrl();
        this.restTemplate = azureRestTemplate;
        this.objectMapper = objectMapper;
    }

    public UUID getAktivitetsId(int deltakerId) {
        AktivitetArenaAclTranslationRequest request = new AktivitetArenaAclTranslationRequest(
            deltakerId,
            AktivitetArenaAclTranslationAktivitetKategori.TILTAKSAKTIVITET
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
            baseUrl + "/api/translation/arenaid",
            new HttpEntity<>(request, headers),
            String.class
        );

        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new RuntimeException("AktivitetsId ble ikke funnet for deltakerId: " + deltakerId);
        }

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Feil ved henting av aktivitetsId fra aktivitetsplanen. Status: " + response.getStatusCode());
        }

        String body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Fikk ingen respons fra aktivitetsplanen");
        }

        try {
            return objectMapper.readValue(body, UUID.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Svar fra aktivitetsplanen er ikke en gyldig UUID: " + body);
        }
    }
}
