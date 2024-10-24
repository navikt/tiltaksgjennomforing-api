package no.nav.tag.tiltaksgjennomforing.enhet.veilarboppfolging;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
class VeilarboppfolgingClient {

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
    public HentOppfolgingsstatusRespons hentOppfolgingsstatus(HentOppfolgingsstatusRequest request) {
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

        return response.getBody();
    }
}
