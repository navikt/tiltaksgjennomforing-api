package no.nav.tag.tiltaksgjennomforing.autorisasjon.altinntilgangsstyring;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import no.nav.tag.tiltaksgjennomforing.exceptions.AltinnFeilException;
import no.nav.tag.tiltaksgjennomforing.infrastruktur.cache.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@Slf4j
public class AltinnTilgangsstyringKlient {
    private final URI arbeidsgiverAltinnTilgangerUri;
    private final RestTemplate azureRestTemplate;

    public AltinnTilgangsstyringKlient(
            AltinnTilgangsstyringProperties altinnTilgangsstyringProperties,
            RestTemplate azureRestTemplate
    ) {
        this.arbeidsgiverAltinnTilgangerUri = altinnTilgangsstyringProperties.getArbeidsgiverAltinnTilgangerUri();
        this.azureRestTemplate = azureRestTemplate;
    }

    @Cacheable(CacheConfig.ALTINN_CACHE)
    @Retryable(backoff = @Backoff(delayExpression = "${tiltaksgjennomforing.retry.delay}", maxDelayExpression = "${tiltaksgjennomforing.retry.max-delay}", multiplier = 2))
    public AltinnTilgangerResponse kallAltinn3(Fnr fnrSomCacheParameter) {
        AltinnTilgangerResponse response;
        try {
            response = azureRestTemplate.postForObject(
                arbeidsgiverAltinnTilgangerUri,
                null,
                AltinnTilgangerResponse.class
            );
        } catch (RestClientResponseException e) {
            log.error("HTTP-feil fra arbeidsgiver-altinn-tilganger: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new AltinnFeilException();
        } catch (ResourceAccessException e) {
            log.error("Nettverksfeil ved kall til arbeidsgiver-altinn-tilganger", e);
            throw new AltinnFeilException();
        }

        if (response == null || response.isError() || response.orgNrTilTilganger() == null) {
            log.warn("Ugyldig respons fra arbeidsgiver-altinn-tilganger, isError: {}", response != null ? response.isError() : "null");
            throw new AltinnFeilException();
        }

        return response;
    }
}
