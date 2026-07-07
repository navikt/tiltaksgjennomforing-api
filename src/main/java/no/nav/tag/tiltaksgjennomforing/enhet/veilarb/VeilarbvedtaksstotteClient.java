package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
class VeilarbvedtaksstotteClient {
    private final RestTemplate restTemplate;
    private final VeilarbvedtaksstotteProperties properties;

    public VeilarbvedtaksstotteClient(
            RestTemplate azureRestTemplate,
            VeilarbvedtaksstotteProperties properties
    ) {
        this.restTemplate = azureRestTemplate;
        this.properties = properties;
    }

    @Retryable(backoff = @Backoff(delayExpression = "${tiltaksgjennomforing.retry.delay}", maxDelayExpression = "${tiltaksgjennomforing.retry.max-delay}", multiplier = 2))
    @Cacheable(CacheConfig.VEILARBVEDTAKSSTOTTE_CACHE)
    public Optional<Gjeldende14aVedtakRespons> hentGjeldende14aVedtak(Gjeldende14aVedtakRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        ResponseEntity<Gjeldende14aVedtakRespons> response = restTemplate.exchange(
            properties.getUrl() + "/veilarbvedtaksstotte/api/ekstern/hent-gjeldende-14a-vedtak",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            Gjeldende14aVedtakRespons.class
        );

        Optional<Gjeldende14aVedtakRespons> responsOpt = Optional.ofNullable(response.getBody());
        log.info("Respons fra 14a status={} body={}", response.getStatusCode().value(), responsOpt.map(Gjeldende14aVedtakRespons::toString).orElse("null"));

        return responsOpt;
    }
}
