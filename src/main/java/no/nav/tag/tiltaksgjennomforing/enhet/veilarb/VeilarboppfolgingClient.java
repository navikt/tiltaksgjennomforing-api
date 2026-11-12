package no.nav.tag.tiltaksgjennomforing.enhet.veilarb;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import no.nav.tag.tiltaksgjennomforing.logging.TeamLogs;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
class VeilarboppfolgingClient {
    private static final TeamLogs teamLogs = TeamLogs.getLogger(log);

    private static final String OPPFOLGINGSENHET_QUERY = """
        query($fnr: String!) {
            oppfolgingsEnhet(fnr: $fnr) {
                enhet {
                    id
                    navn
                    kilde
                }
            }
        }
        """;

    private final RestTemplate restTemplate;
    private final VeilarboppfolgingProperties properties;

    public VeilarboppfolgingClient(
            RestTemplate azureRestTemplate,
            VeilarboppfolgingProperties properties
    ) {
        this.restTemplate = azureRestTemplate;
        this.properties = properties;
    }

    @Retryable(delayString = "${tiltaksgjennomforing.retry.delay}", maxDelayString = "${tiltaksgjennomforing.retry.max-delay}", multiplier = 2, maxRetries = 2)
    @Cacheable(CacheConfig.VEILARBOPPFOLGING_CACHE)
    public Optional<Oppfolgingsenhet> hentOppfolgingsenhet(HentOppfolgingsenhetRequest request) {
        log.info("Henter oppfølgingsenhet fra veilarboppfolging");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Nav-Consumer-Id", "tiltaksgjennomforing-api");

        GraphqlRequest body = new GraphqlRequest(OPPFOLGINGSENHET_QUERY, Map.of("fnr", request.fnr()));

        ResponseEntity<OppfolgingsenhetGraphqlRespons> response = restTemplate.exchange(
                properties.getUrl() + "/veilarboppfolging/api/graphql",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                OppfolgingsenhetGraphqlRespons.class
        );

        OppfolgingsenhetGraphqlRespons respons = response.getBody();
        if (respons == null) {
            return Optional.empty();
        }
        if (respons.harFeil()) {
            teamLogs.error("GraphQL-feil fra veilarboppfolging ved henting av oppfølgingsenhet: {}", respons.errors());
            throw new IllegalStateException(
                "GraphQL-feil fra veilarboppfolging ved henting av oppfølgingsenhet (" + respons.errors().size() + " feil)"
            );
        }
        return respons.enhet();
    }
}
