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
class AoOppfolgingskontorClient {
    private static final TeamLogs teamLogs = TeamLogs.getLogger(log);

    private static final String KONTOR_TILHORIGHET_QUERY = """
        query($ident: String!) {
            kontorTilhorighet(ident: $ident) {
                kontorId
                kontorNavn
                kontorType
            }
        }
        """;

    private final RestTemplate restTemplate;
    private final AoOppfolgingskontorProperties properties;

    public AoOppfolgingskontorClient(
            RestTemplate azureRestTemplate,
            AoOppfolgingskontorProperties properties
    ) {
        this.restTemplate = azureRestTemplate;
        this.properties = properties;
    }

    @Retryable(delayString = "${tiltaksgjennomforing.retry.delay}", maxDelayString = "${tiltaksgjennomforing.retry.max-delay}", multiplier = 2, maxRetries = 2)
    @Cacheable(CacheConfig.AO_OPPFOLGINGSKONTOR_CACHE)
    public Optional<Oppfolgingskontor> hentOppfolgingskontor(HentOppfolgingskontorRequest request) {
        log.info("Henter oppfølgingskontor fra ao-oppfolgingskontor");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set("Nav-Consumer-Id", "tiltaksgjennomforing-api");

        GraphqlRequest body = new GraphqlRequest(KONTOR_TILHORIGHET_QUERY, Map.of("ident", request.fnr()));

        ResponseEntity<KontorTilhorighetGraphqlRespons> response = restTemplate.exchange(
                properties.getUrl() + "/graphql",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                KontorTilhorighetGraphqlRespons.class
        );

        KontorTilhorighetGraphqlRespons respons = response.getBody();
        if (respons == null) {
            return Optional.empty();
        }
        if (respons.harFeil()) {
            teamLogs.error("GraphQL-feil fra ao-oppfolgingskontor ved henting av kontortilhørighet: {}", respons.errors());
            throw new IllegalStateException(
                "GraphQL-feil fra ao-oppfolgingskontor ved henting av kontortilhørighet (" + respons.errors().size() + " feil)"
            );
        }
        return respons.kontor();
    }
}
