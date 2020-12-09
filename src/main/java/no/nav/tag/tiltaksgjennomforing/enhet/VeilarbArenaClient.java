package no.nav.tag.tiltaksgjennomforing.enhet;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.sts.STSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  @Value("${tiltaksgjennomforing.veilarbarena.url}")
  String veilarbarenaUrl;

  public Oppfølgingsbruker hentOppfølgingsbruker(String fnr, String aktørId) {
    log.info("Henter oppfølgingsbruker for aktørId {}", aktørId);

    String uri = UriComponentsBuilder.fromHttpUrl(veilarbarenaUrl)
        .path("/oppfolgingsbruker/" + fnr)
        .toUriString();

    try {
      ResponseEntity<Oppfølgingsbruker> respons = restTemplate.exchange(
          uri,
          HttpMethod.GET,
          httpHeadere(),
          Oppfølgingsbruker.class
      );

      if (respons.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
        log.warn("Kandidat ikke registrert i veilarbarena, aktørId: {}", aktørId);
        return Oppfølgingsbruker.builder().fnr(fnr).navKontor(null).build();
      } else {
        return respons.getBody();
      }

    } catch (RestClientResponseException exception) {
      log.error("Kunne ikke hente oppfølgingsbruker fra veilarbarena, aktørId: {}", aktørId, exception);
      throw new FinnKandidatException("Kunne ikke hente oppfølgingsbruker fra veilarbarena");
    }
  }

  private HttpEntity httpHeadere() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(stsClient.hentSTSToken().getAccessToken());
    return new HttpEntity<>(headers);
  }
}
