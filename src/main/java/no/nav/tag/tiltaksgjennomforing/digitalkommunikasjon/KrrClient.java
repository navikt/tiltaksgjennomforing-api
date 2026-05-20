package no.nav.tag.tiltaksgjennomforing.digitalkommunikasjon;

import lombok.extern.slf4j.Slf4j;
import no.nav.tag.tiltaksgjennomforing.avtale.Fnr;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Digdir sitt Kontakt- og reservasjonsregister (KRR) inneholder informasjon
 * om hvordan innbyggere ønsker å bli kontaktet av det offentlige, og
 * om de har reservert seg mot digital kommunikasjon.
 *
 * Klient for Digdir KRR-proxy.
 *
 * @see <a href="https://digdir-krr-proxy.intern.dev.nav.no/swagger-ui/index.html">Swagger</a>
 * @see <a href="https://github.com/navikt/digdir-krr">digdir-krr</a>
 */
@Slf4j
@Component
public class KrrClient {

    private static final String KRR_PERSON_ENDEPUNKT = "/rest/v1/personer";

    private final RestTemplate azureRestTemplate;
    private final URI baseUri;

    public KrrClient(
        @Qualifier("azureRestTemplate") RestTemplate azureRestTemplate,
        KrrProperties krrProperties
    ) {
        this.azureRestTemplate = azureRestTemplate;
        this.baseUri = Objects.requireNonNull(krrProperties.getUri(), "KRR URI må være konfigurert");
    }

    public Optional<Boolean> hentPersonReservertForDigitalKontakt(Fnr fnr) {
        String personident = Objects.requireNonNull(fnr, "fnr kan ikke være null").asString();

        try {
            ResponseEntity<KrrPersonKontaktinformasjonRespons> respons = azureRestTemplate.exchange(
                baseUri + KRR_PERSON_ENDEPUNKT,
                HttpMethod.POST,
                lagRequest(personident),
                KrrPersonKontaktinformasjonRespons.class
            );

            if (!respons.getStatusCode().is2xxSuccessful()) {
                log.warn("KRR svarte med uventet status {}", respons.getStatusCode());
                return Optional.empty();
            }

            return hentKontaktinfo(respons.getBody(), personident)
                .map(Kontaktinfo::getReservert);
        } catch (RestClientException e) {
            log.warn("Feil ved henting av digital kontaktreservasjon fra KRR", e);
            throw e;
        }
    }

    private HttpEntity<KrrPersonKontaktinformasjonRequest> lagRequest(String personident) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(new KrrPersonKontaktinformasjonRequest(List.of(personident)), headers);
    }

    private Optional<Kontaktinfo> hentKontaktinfo(KrrPersonKontaktinformasjonRespons respons, String personident) {
        return Optional.ofNullable(respons)
            .map(KrrPersonKontaktinformasjonRespons::getPersoner)
            .map(personer -> personer.get(personident));
    }
}
