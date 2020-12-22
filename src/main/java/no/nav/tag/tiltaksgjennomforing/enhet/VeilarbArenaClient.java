package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@AllArgsConstructor
public class VeilarbArenaClient {

    private final RestTemplate restTemplate;
    private final STSClient stsClient;
    private VeilarbArenaProperties veilarbArenaProperties;

    public String hentOppfølgingsEnhet(String fnr) {

        String uri = UriComponentsBuilder.fromHttpUrl(veilarbArenaProperties.getUrl().toString())
                .pathSegment(fnr)
                .toUriString();

        try {
            ResponseEntity<Oppfølgingsstatus> respons = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    httpHeadere(),
                    Oppfølgingsstatus.class
            );

            if (respons.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
                log.warn("Kandidat ikke registrert i veilarbarena");
                return null;
            } else {
                return respons.getBody().getOppfolgingsenhet();
            }

        } catch (RestClientResponseException exception) {
            log.error("Kunne ikke hente Oppfølgingsstatus fra veilarbarena", exception);
            throw new VeilarbArenaException("Kunne ikke hente Oppfølgingsstatus fra veilarbarena");
        }
    }

    private HttpEntity httpHeadere() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
        return new HttpEntity<>(headers);
    }
}
