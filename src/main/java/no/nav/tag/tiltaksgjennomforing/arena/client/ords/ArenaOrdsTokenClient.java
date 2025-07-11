package no.nav.tag.tiltaksgjennomforing.arena.client.ords;

import no.nav.tag.tiltaksgjennomforing.arena.configuration.ArenaOrdsProperties;
import no.nav.tag.tiltaksgjennomforing.utils.Now;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class ArenaOrdsTokenClient {

    private static final long expireEarlySeconds = 30L;

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;
    private final String baseUrl;

    private CachedOrdsToken cachedToken;

    public ArenaOrdsTokenClient(
        ArenaOrdsProperties properties,
        RestTemplate noAuthRestTemplate
    ) {
        this.baseUrl = properties.getUrl();
        this.clientId = properties.getClientId();
        this.clientSecret = properties.getClientSecret();
        this.restTemplate = noAuthRestTemplate;
    }

    public String getToken() {
        if (isMissingOrExpired(cachedToken)) {
            cachedToken = new CachedOrdsToken(fetchToken());
        }

        return cachedToken.getToken().accessToken();
    }

    private CachedOrdsToken.OrdsToken fetchToken() {
        String request = "grant_type=client_credentials";
        String auth = clientId + ":" + clientSecret;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeBase64String(auth.getBytes()));

        return restTemplate.postForObject(
            baseUrl + "/arena/api/oauth/token",
            new HttpEntity<>(request, headers),
            CachedOrdsToken.OrdsToken.class
        );
    }

    private static boolean isMissingOrExpired(CachedOrdsToken token) {
        if (token == null) {
            return true;
        }

        Instant expiresAt = token.getCachedAt()
            .plusSeconds(token.getToken().expiresIn())
            .minusSeconds(expireEarlySeconds);

        return Now.instant().isAfter(expiresAt);
    }


}
