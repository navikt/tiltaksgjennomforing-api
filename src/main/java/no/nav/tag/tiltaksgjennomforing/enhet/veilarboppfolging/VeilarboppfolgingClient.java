package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
class VeilarboppfolgingClient {
    private static final HttpStatusCode NOT_FOUND = HttpStatusCode.valueOf(404);

    private final RestTemplate restTemplate;
    private final VeilarboppfolgingProperties properties;

    public VeilarboppfolgingClient(
        RestTemplate azureRestTemplate,
        VeilarboppfolgingProperties properties
    ) {
        this.restTemplate = azureRestTemplate;
        this.properties = properties;
    }

    @Cacheable(CacheConfig.VEILARBOPPFOLGING_CACHE)
    public Optional<HentOppfolgingsstatusRespons> hentOppfolgingsstatus(HentOppfolgingsstatusRequest request) {
        log.info("Henter oppfølgingenhet fra veilarboppfolging");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Nav-Consumer-Id", "tiltaksgjennomforing-api");

        ResponseEntity<HentOppfolgingsstatusRespons> response = restTemplate.exchange(
            properties.getUrl() + "/veilarboppfolging/api/v2/person/hent-oppfolgingsstatus",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            HentOppfolgingsstatusRespons.class
        );

        if (NOT_FOUND.isSameCodeAs(response.getStatusCode()) || response.getBody() == null) {
            return Optional.empty();
        }

        return Optional.of(response.getBody());
    }
}
